package com.junxian.myandroidprojet;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.junxian.myWeibo.DBOperate;
import com.junxian.myWeibo.ImageUtil;
import com.junxian.myWeibo.UserInfo;
import com.junxian.myWeibo.WeiboConstParam;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class WeiboUserHomeActivity extends Activity {
    /** Called when the activity is first created. */
   

	public DBOperate dboperate;
	public Dialog dialog_select_user;
	public Dialog user_login;
	public View diaView;
	public List<UserInfo> userList;
	public UserAdapater adapater;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weibo_user_home);
//        if (!internet()) return;     // un bug here
        
        //初始化数据库
    	dboperate  = new DBOperate(WeiboUserHomeActivity.this);
    	dboperate.initDB();
        if (userList == null) userList = new ArrayList<UserInfo>();
    	userList = dboperate.findAllUser();
    	
    	if (diaView ==null) diaView = View.inflate(WeiboUserHomeActivity.this,R.layout.dialog2, null);
		if (dialog_select_user == null) dialog_select_user = new Dialog(WeiboUserHomeActivity.this, R.style.pop_dialog);
		dialog_select_user.setContentView(diaView);
		dialog_select_user.show();
		
		if (adapater ==null) adapater = new UserAdapater();
		ListView listview = (ListView) diaView.findViewById(R.id.loginlist);
		listview.setVerticalScrollBarEnabled(false);// ListView去掉下拉条
		listview.setAdapter(adapater);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {
				Object obj = view.getTag();
				if ("adduser".equals(obj)) {
					authroized();
					return;
				}
				else // another dialog pop up
				{
					dialog_select_user.dismiss();
					final TextView tv = (TextView) view.findViewById(R.id.userName);
					ImageView image = (ImageView) view.findViewById(R.id.iconImg);
					
					// another dialog poping 
					View login_view =  View.inflate(WeiboUserHomeActivity.this,R.layout.dialog3, null);
					if (user_login == null) user_login = new Dialog(WeiboUserHomeActivity.this, R.style.pop_dialog);
					user_login.setContentView(login_view);
					user_login.show();
					
					ImageView user_icon = (ImageView) login_view.findViewById(R.id.user_selected_icon);
					user_icon.setImageDrawable(image.getDrawable());
					TextView user_name = (TextView) login_view.findViewById(R.id.user_select_name);
					user_name.setText(tv.getText().toString());
					
					ImageButton user_goback_home = (ImageButton) login_view.findViewById(R.id.user_goBack);
					user_goback_home.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							user_login.dismiss();
							onCreate(null);
						}
					});
					
					ImageButton user_delete = (ImageButton) login_view.findViewById(R.id.user_delete);
					user_delete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							 dboperate.deleteUser(tv.getText().toString());
							 user_login.dismiss();
							 onCreate(null);
						}
					});
					ImageButton user_gohome = (ImageButton) login_view.findViewById(R.id.user_goHome);
					user_gohome.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							user_login.dismiss();
							goHome(tv.getText().toString());
						}
					});
				}
			}
		});
    		
    
    }
    
	
    public boolean internet() {
    	ConnectivityManager cm = (ConnectivityManager) WeiboUserHomeActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo info = cm.getActiveNetworkInfo();
		NetworkInfo.State state = info.getState();
		if (state != NetworkInfo.State.CONNECTED) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(WeiboUserHomeActivity.this, "当前未连接到网络，请链接到网络在尝试连接",
							Toast.LENGTH_LONG).show();
				}
			});
			return false;
		} 
		Log.i("internet connection", "connected");
		return true;
	}
    
    
	public void authroized()
	{
		// Oauth2.0
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(WeiboConstParam.CONSUMER_KEY, WeiboConstParam.CONSUMER_SECRET);	
		weibo.setRedirectUrl(WeiboConstParam.REDIRECT_URL);
		// should clear the access token first to avoid using the precedent token to enter directly， it doesn't work， here we must clear the session cookies 
		//             mWebView.clearCache(true);  
        // 			   mWebView.clearHistory();
		//             CookieSyncManager.createInstance(activity);
		 // ensure any cookies set by the dialog are saved
		//        	CookieSyncManager.getInstance().startSync();
		//        	CookieManager.getInstance().removeSessionCookie();
		weibo.setAccessToken(null);
		weibo.authorize(WeiboUserHomeActivity.this, new WeiboDialogListener() {
			@Override
			public void onWeiboException(WeiboException e) {}
			@Override
			public void onError(DialogError e) {}
			@Override
			public void onComplete(Bundle values) {
				String token = values.getString("access_token");	
			    String expires_in = values.getString("expires_in");
			    String remind_in = values.getString("remind_in");
			    String uid = values.getString("uid");			    		   
			    Log.i("authorize: ","token: "+ token + " expires_in: "+ expires_in+ " remind_in: "+ remind_in + " uid: "+ uid);
			    //get the userinfo and try to add it into the database, 及时保存得到的用户数据，方便下面调用api借口
				updateUsers(token ,expires_in, uid);
				
			}
			@Override
			public void onCancel() {}});
	}
	
	//when the user click the login, it has defines the accesstoken, for the next apply
	public void goHome(String nickname)
	{
		Log.i("weiboUserHome", "enter the homepage with the nickname");
		// we must add this ligne 
		Utility.setAuthorization(new Oauth2AccessTokenHeader());
		UserInfo user = dboperate.findUserByNickname(nickname);
		if (user!=null) // we find the user 
		{
			AccessToken accessToken = new AccessToken(user.getAccess_token(), WeiboConstParam.CONSUMER_SECRET);	
		    accessToken.setExpiresIn(user.getExpires_in());
			Weibo weibo = Weibo.getInstance();	
		    weibo.setAccessToken(accessToken);
			Intent intent = new Intent();
			intent.setClass(WeiboUserHomeActivity.this, WeiboHomeActivity.class);
			intent.putExtra("nickname", nickname);
			startActivity(intent);
		}
		else 
		{
			System.out.println("the user has not existed in the database");
			Toast.makeText(this, "没有该用户信息", Toast.LENGTH_LONG).show();
		}
		
	}
	
	// 得到该用户的信息数据然后及时存储相关重要信息，access token,token过期时间， 用户昵称，唯一的用户id，用户头像信息，用户地点, 到数据库中存储
	 private void updateUsers(String Token, String expiresIn, String uid) {
		 	AccessToken accessToken = new AccessToken(Token, WeiboConstParam.CONSUMER_SECRET);	
			//set the expire time
		    String expiretime = expiresIn;
		    accessToken.setExpiresIn(expiretime);
			Weibo weibo = Weibo.getInstance();	
		    weibo.setAccessToken(accessToken);
		 
		 	WeiboParameters bundle = new WeiboParameters();
	        bundle.add("access_token", Token);
	        bundle.add("uid", uid);
	        
	        String url = Weibo.SERVER + "users/show.json";
				String response;
				try {
					response = Weibo.getInstance().request(WeiboUserHomeActivity.this, url, bundle, Utility.HTTPMETHOD_GET, Weibo.getInstance().getAccessToken());
					Log.i("user info","start to update the user info");
					JSONObject user = new JSONObject(response);
					String user_id = uid;
					String name = user.getString("name");
					String screen_name = user.getString("screen_name");
					String token =Token;
					String expiresin =expiresIn;
					String location = user.getString("location");
					String headUrl = user.getString("profile_image_url");

					ContentValues values = new ContentValues();
					values.put("userid", user_id);
					values.put("name", name);
					values.put("type", "sina");
					values.put("nickname", screen_name);
					values.put("access_token", token);
					values.put("expires_in", expiresin);
					values.put("location", location);
					values.put("icon", ImageUtil.getBytesFromUrl(headUrl));
					

					if (!dboperate.isExist(values)) // no user in the db, insert it 
					{
						long insertrow =dboperate.insertUser(values);
						Log.i("insert user: ", " inset row :" + insertrow);
					}
					
					else //the user exist in db, update it
					{
						Log.i("update user: ", " +++++++++++++++++++++++++++++++++++");
						// 3g 下，用户图像来不及存储进去。
						long updaterow = dboperate.updateUser(values, user_id);
						Log.i("update row: ", " update row :" + updaterow);

					}
					// restart from the first
					onCreate(null);
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			                Toast.makeText(WeiboUserHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			            }
			        });
				}
	    }
 
	 
	class UserAdapater extends BaseAdapter{
        @Override
        public int getCount() {
        	// add one more ligne to add the new user
            return userList.size() + 1 ;
        }
        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	Log.i("user list  ", " " +getCount());
        	
        	if ((position+1) == getCount())  // the row is the adding user !
        	{
        		convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_user, null);
        		convertView.setTag("adduser");
				return convertView;
        	}
        	
        	else {
        		convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_user, null);
        		ImageView iv = (ImageView) convertView.findViewById(R.id.iconImg);
        		TextView tv = (TextView) convertView.findViewById(R.id.userName);
            
        		UserInfo user = userList.get(position);
        		Log.i("user " + position, user.toString());
        		try {
                //设置图片显示
        			iv.setImageDrawable(user.getIcon());
                //设置信息
        			tv.setText(user.getNickname());
        		} catch (Exception e) {
            		e.printStackTrace();
        		}
        		return convertView;
        	}
        }
	}
	
}