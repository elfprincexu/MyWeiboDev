package com.junxian.myandroidprojet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.junxian.myWeibo.AsyncImageLoader;
import com.junxian.myWeibo.WeiBoHolder;
import com.junxian.myWeibo.WeiBoInfo;
import com.junxian.myWeibo.WeiboConstParam;
import com.junxian.myWeibo.AsyncImageLoader.ImageCallback;
import com.junxian.myandroidprojet.WeiboViewActivity.view;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboParameters;

public class WeiboHomeActivity extends Activity {
	
	private List<WeiBoInfo> wbList;
	private int iterator;
	private TextView showName;
	private ImageButton writeWeibo;
	private ImageButton refreshWeibo;
	private ListView Msglist;
	private WeiBoAdapater adapter;
	private LinearLayout loadingLayout;
	
	private RadioButton weiboHome;
	private RadioButton myMessage;
	private RadioButton myInfo;
	private RadioButton weiboSearch;
	private RadioButton weiboMore;

	public boolean moreweibo =false;
	public boolean refresh =false;
	public final int DO_CHARGE = 1;
	public final String TAG = "HOME ACTIVITY";
	public String userNickname;
	public String weibo_source;
	public String last_cache ="";
	public String last_time = "";
	public boolean read = true;
	
	@Override
	protected void onPause() {
		super.onPause();
		save();
	}

	@Override
	protected void onResume() {
		super.onResume();
		read();
	}
	
	private void save(){
		SharedPreferences setting=this.getPreferences(Activity.MODE_PRIVATE);
		boolean saved = setting.edit().putString("cache", last_cache.toString())
				.commit();
		Log.i("Save ? ", "save : " +saved);
	}
	
	private void read() {		
		SharedPreferences setting = this.getPreferences(Activity.MODE_PRIVATE);
		last_cache = setting.getString("cache", "");
		
		if (last_cache.equals("")) {
			new Thread(new loadWeiboList()).start();
		} else {
			try {
				JSONObject json = new JSONObject(last_cache);
				JSONArray weibos = json.getJSONArray("statuses");
				for (int i = 0; i < weibos.length(); i++) {
					JSONObject d = weibos.getJSONObject(i);
					if (d != null) {
						JSONObject user = d.getJSONObject("user");
						WeiBoInfo w = new WeiBoInfo();
						String weiboid = d.getString("id");
						String userId = user.getString("id");
						String userName = user.getString("screen_name");
						String userIcon = user.getString("profile_image_url");
						String created_time = d.getString("created_at");
						String reposts_count = d.getString("reposts_count");
						String comments_count = d.getString("comments_count");
						String attitudes_count = d.getString("attitudes_count");
						String text = d.getString("text");
						String weiboSource_first = d.getString("source");
						String weiboSource = "來自 "+ weiboSource_first.substring(weiboSource_first.indexOf(">") + 1,weiboSource_first.length() - 4);
						
						if (read == true) { 
							last_time  = created_time; 
							read = false;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Date now =new Date();
									Toast.makeText(WeiboHomeActivity.this,"上次登陆时间:  " + converteTime(last_time, now),Toast.LENGTH_LONG).show();
								}
							});
						}
						
						Boolean haveImg = false;
						if (d.has("thumbnail_pic")) {
							haveImg = true;
							w.setThu_pic(d.getString("thumbnail_pic"));
							w.setMiddle_pic(d.getString("bmiddle_pic"));
							w.setOriginal_pic(d.getString("original_pic"));
						}
						
						boolean is_retweedted = false;
						if (d.has("retweeted_status")) {
							JSONObject r = d.getJSONObject("retweeted_status");
							is_retweedted = true;
							text = text+ "//"+ r.getJSONObject("user").getString("screen_name") + ": "+ r.getString("text");
							if (r.has("thumbnail_pic")) 
							{haveImg = true;
							w.setThu_pic(r.getString("thumbnail_pic"));
							w.setMiddle_pic(r.getString("bmiddle_pic"));
							w.setOriginal_pic(r.getString("original_pic"));
							}
						}
						// 微博id
						w.setId(weiboid);
						w.setUserId(userId);
						w.setUserName(userName);
						w.setTime(created_time);
						w.setText(text);
						w.setHaveImage(haveImg);
						w.setIsretweedted(is_retweedted);
						w.setUserIcon(userIcon);
						w.setAttitudes_count(attitudes_count);
						w.setComments_count(comments_count);
						w.setReposts_count(reposts_count);
						w.setWeiboSource(weiboSource);
						// 加载更多时，会重复最后一条微博，bug!
						wbList.add(w);
					}
				}
				Message message = new Message();
				message.what = DO_CHARGE;
				myHandler.sendMessage(message);
			} catch (final JSONException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("read last cache", e.getMessage());
						Toast.makeText(WeiboHomeActivity.this,
								"error code:" + e.getMessage() + " 读取上次缓存失败",Toast.LENGTH_LONG).show();
					}
				});
				e.printStackTrace();
			}
		}

	}

	// to create a data base to stock these weibos
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.i(TAG, "on create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_home);
		
		// DO SOMETHING, LIKE LOADING THE WEIBOS
		Bundle values = this.getIntent().getExtras();
		userNickname = values.getString("nickname");
		
		if (wbList == null) {
			wbList = new ArrayList<WeiBoInfo>();
		}
		
		Msglist = (ListView) findViewById(R.id.Msglist);		
		Msglist.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3) {
					Object obj = view.getTag();
					// item to show more weibos
					if ("more".equals(obj)) {
						loadingLayout.setVisibility(View.VISIBLE);
						moreweibo=true;
						Msglist.setSelection(wbList.size());
						new Thread(new loadWeiboList()).start();
					}
					//other weibo details showing
					else if (obj != null) {
						
						WeiBoInfo wb = wbList.get(position);
						
						Intent intent = new Intent(WeiboHomeActivity.this,WeiboViewActivity.class);
						Bundle b = new Bundle();
						
						b.putString("key", wb.getId());
						b.putString("weibo_icon",wb.getUserIcon());
						b.putString("weibo_user_name", wb.getUserName());
						b.putString("weibo_text",wb.getText());
						if (wb.getHaveImage())
						 {
							b.putBoolean("weibo_hasPic", true); 
							b.putString("thum_pic", wb.getThu_pic());
							b.putString("middle_pic", wb.getMiddle_pic());
							b.putString("original_pic", wb.getOriginal_pic());
						 }
						else b.putBoolean("weibo_hasPic", false);
						b.putString("comments", wb.getComments_count());
						b.putString("favorite", wb.getAttitudes_count());
						b.putString("reposts", wb.getReposts_count());
						
						intent.putExtras(b);
						startActivity(intent);
					}
				}

			});
		adapter = new WeiBoAdapater();
		Msglist.setAdapter(adapter);
		
		
		//show the user nickname in the home page of user
		showName = (TextView) this.findViewById(R.id.showName);
		showName.setText(userNickname);
		
		// write button of home page, go to the share activity 
		writeWeibo = (ImageButton) this.findViewById(R.id.writeBtn);
		writeWeibo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// enter the share Activity
				Intent intent = new Intent();
				intent.setClass(WeiboHomeActivity.this, WeiboShareActivity.class);
				intent.putExtra("nickname", userNickname);
				startActivity(intent);
				
			}
		});

		refreshWeibo  = (ImageButton) findViewById(R.id.refreshBtn);
		refreshWeibo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// enter the share Activity
				refresh =true;
				Msglist.setSelection(0);
				loadingLayout.setVisibility(View.VISIBLE);
				new Thread(new loadWeiboList()).start();
			}
		});
		
		loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		loadingLayout.setVisibility(View.VISIBLE);
		
		weiboHome = (RadioButton) this.findViewById(R.id.weibo_index);
		weiboHome.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// enter the share Activity
				Intent intent = new Intent();
				intent.setClass(WeiboHomeActivity.this, WeiboUserHomeActivity.class);
				startActivity(intent);
			}
		});
		
		
	}

	  Handler myHandler = new Handler() {
	        public void handleMessage(Message msg) {
	             switch (msg.what) {
	                  case DO_CHARGE:{
	                      chargeWeiboList();
	                        break;
	                  }
	             }
	        }
	  };
	        
	  
	class loadWeiboList implements Runnable {
		@Override
		public void run() {
			Message message = new Message();
			final String url = Weibo.SERVER + "statuses/friends_timeline.json";
			final WeiboParameters bundle = new WeiboParameters();
			bundle.add("source", WeiboConstParam.CONSUMER_KEY);
			// to refresh the latest weibos
			
			if (refresh ==true)   {  iterator =0;	}
			
			//add more weibos according to the max_id 
			if (moreweibo)  { bundle.add("max_id", wbList.get(wbList.size()-1).getId()); moreweibo=false;}
			
			try {
				String response = Weibo.getInstance().request(WeiboHomeActivity.this, url, bundle, Utility.HTTPMETHOD_GET,Weibo.getInstance().getAccessToken());
				last_cache = response;
				final int wb_number_first = wbList.size();
				
				JSONObject json = new JSONObject(response);
				JSONArray weibos = json.getJSONArray("statuses");
				for (int i = 0; i < weibos.length(); i++) {
					boolean hasThisWeibo = false;
					WeiBoInfo w = new WeiBoInfo();
					JSONObject d = weibos.getJSONObject(i);
					if (d != null) {
						JSONObject user = d.getJSONObject("user");
						String weiboid = d.getString("id");
						String userId = user.getString("id");
						String userName = user.getString("screen_name");
						String userIcon = user.getString("profile_image_url");
						String created_time = d.getString("created_at");
						String reposts_count = d.getString("reposts_count");
						String comments_count = d.getString("comments_count");
						String attitudes_count = d.getString("attitudes_count");
						String text = d.getString("text");
						String weiboSource_first =  d.getString("source");
						String weiboSource = "来自 "+weiboSource_first.substring(weiboSource_first.indexOf(">")+1, weiboSource_first.length()-4);
						Boolean haveImg = false;
						if (d.has("thumbnail_pic")) {
							haveImg = true;
							w.setThu_pic(d.getString("thumbnail_pic"));
							w.setMiddle_pic(d.getString("bmiddle_pic"));
							w.setOriginal_pic(d.getString("original_pic"));
						}
						
						// have a retweeted status,should show the contenu of the weibo
						boolean is_retweedted = false;
						if (d.has("retweeted_status")) {
							JSONObject r = d.getJSONObject("retweeted_status");
							is_retweedted = true;
							text = text + "//"+ r.getJSONObject("user").getString("screen_name") + ": " + r.getString("text");
							if (r.has("thumbnail_pic"))  
							{
								haveImg = true; 	
								w.setThu_pic(r.getString("thumbnail_pic"));
								w.setMiddle_pic(r.getString("bmiddle_pic"));
								w.setOriginal_pic(r.getString("original_pic"));
							}
						}
						
						// 微博id
						w.setId(weiboid);
						w.setUserId(userId);
						w.setUserName(userName);
						w.setTime(created_time);
						w.setText(text);
						w.setHaveImage(haveImg);
						w.setIsretweedted(is_retweedted);
						w.setUserIcon(userIcon);
						w.setAttitudes_count(attitudes_count);
						w.setComments_count(comments_count);
						w.setReposts_count(reposts_count);
						w.setWeiboSource(weiboSource);
						
						//不能重复
						for (int m=0; m<wbList.size();m++) {
							if (wbList.get(m).getId().equals(w.getId()) ) hasThisWeibo =true; 
							}
					}
					if (hasThisWeibo == false) { 
						if (refresh ==true) {
							wbList.add(iterator, w); 
							iterator+=1;
							}
						else wbList.add(w); 
					}
				}
				if (refresh ==true ) {refresh = false; iterator = 0;}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(WeiboHomeActivity.this, "更新得到"+ (wbList.size()-wb_number_first)+" 条微博",Toast.LENGTH_SHORT).show();
					}
				});
				
				message.what = DO_CHARGE;
                myHandler.sendMessage(message);
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e(TAG, e.getMessage());
						if (e.getMessage().startsWith("java.net.UnknownHostException: Unable to resolve host")) {
							Toast.makeText(WeiboHomeActivity.this, "网络未连接，请链接网络",Toast.LENGTH_LONG).show();
							onBackPressed();
						}
						else {
							Toast.makeText(WeiboHomeActivity.this, "用户token已过期，请重新登录验证",Toast.LENGTH_LONG).show();
							onBackPressed();
							}
					}
				});
			}
		}
	}

	private void chargeWeiboList() {
		Log.i("chargeWeiboList","start to charge the list");
		adapter.notifyDataSetChanged();
		loadingLayout.setVisibility(View.GONE);
	}
	
	@Override
	public void onBackPressed() {
		//go back to the front 
		Intent intent = new Intent();
		intent.setClass(WeiboHomeActivity.this, WeiboUserHomeActivity.class);
		startActivity(intent);
		this.finish();
		return;
	}
	
	
	// it takes a little more time, should in the async process
	public static String converteTime(String gmtDatetime, Date now){
	       SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzzz yyyy", Locale.ENGLISH);                  
	       try {
	                Date date = df.parse(gmtDatetime);
	                long between=(now.getTime()-date.getTime())/1000;//除以1000是为了转换成秒
	                if (between >=76800) return "一天前";
	                if (between >= 9600) return "三小时前";
	                if (between >= 7200) return "二小时前";
	    	        if (between >= 3600) return "一小时前";
	    	        if (between >= 1800) return "三十分钟前";
	    	        if (between >= 1200) return "二十分钟前";
	    	        if (between >= 900) return "十五分钟前";
	    	        if (between >= 600) return "十分钟前";
	    	        if (between >= 300) return "五分钟前";
	    	        if (between >= 240) return "四分钟前";
	    	        if (between >= 180) return "三分钟前";
	                if (between >= 120) return "二分钟前";
	                if (between >= 60) return "一分钟前";
	                if (between  > 30) return "30秒前";
	    	        return "刚刚发布";
	       } catch (ParseException e) {
	               e.printStackTrace();
	               return gmtDatetime;
	       }
		}


	private void showImg(ImageView view,Drawable img){
		if (img ==null) {
			Toast.makeText(WeiboHomeActivity.this, "获取图片失败", Toast.LENGTH_LONG).show();
			return;
				}
        int w=img.getIntrinsicWidth();
        int h=img.getIntrinsicHeight();
        if(w>300)
        {
            int hh=300*h/w;
            Log.i("hh", hh+"");
            LayoutParams para=view.getLayoutParams();
            para.width=300;
            para.height=hh;
            view.setLayoutParams(para);
        }
        view.setImageDrawable(img);
	}
	public class WeiBoAdapater extends BaseAdapter {
		private AsyncImageLoader asyncImageLoader;
		@Override
		public int getCount() {
			// add one more ligne to show the more items
			return wbList.size() +1 ;
		}
		@Override
		public Object getItem(int position) {
			return wbList.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Log.i("adapter position", Integer.toString(position));
			Log.i("weiboList size", Integer.toString(wbList.size()));

			// position starts 0, it means 20 pointing to the 21 item
			if ( (position +1 ) == getCount()) {
				convertView =  LayoutInflater.from(getApplicationContext()).inflate(R.layout.moreweibo, null);
				convertView.setTag("more");
				return convertView;
			}
			
			asyncImageLoader = new AsyncImageLoader();
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.weibo, null);
			WeiBoHolder wh = new WeiBoHolder();
			wh.wbicon = (ImageView) convertView.findViewById(R.id.wbicon);
			wh.wbtext = (TextView) convertView.findViewById(R.id.wbtext);
			wh.wbtime = (TextView) convertView.findViewById(R.id.wbtime);
			wh.wbuser = (TextView) convertView.findViewById(R.id.wbuser);
			wh.wbimage = (ImageView) convertView.findViewById(R.id.wbimage);
			wh.wbattitude =(ImageView) convertView.findViewById(R.id.wbfavorite);
			wh.wbattitude_count = (TextView) convertView.findViewById(R.id.wbfavoriteshu);
			wh.wbcomment =(ImageView) convertView.findViewById(R.id.wbcomment);
			wh.wbcomment_count = (TextView) convertView.findViewById(R.id.wbcommentshu);
			wh.wbrepost =(ImageView) convertView.findViewById(R.id.wbzhuanfa);
			wh.wbrepost_count = (TextView) convertView.findViewById(R.id.wbzhuanfashu);
			wh.wbSource = (TextView) convertView.findViewById(R.id.wbSource);
			wh.wbthum_pic = (ImageView) convertView.findViewById(R.id.wbthumimage);
			
			WeiBoInfo wb = wbList.get(position);
			Date now =new Date();
			if (wb != null) {
				convertView.setTag(wb.getId());
				wh.wbuser.setText(wb.getUserName());
				wh.wbtime.setText(converteTime(wb.getTime(),now));
				wh.wbtext.setText(wb.getText(), TextView.BufferType.SPANNABLE);
				wh.wbattitude.setImageResource(R.drawable.weibo_favorite);
				wh.wbattitude_count.setText(wb.getAttitudes_count());
				wh.wbcomment.setImageResource(R.drawable.weibo_comments);
				wh.wbcomment_count.setText(wb.getComments_count());
				wh.wbrepost.setImageResource(R.drawable.weibo_homeview_zhuanfa);
				wh.wbrepost_count.setText(wb.getReposts_count());
				wh.wbSource.setText(wb.getWeibo_source());
				
				if (wb.getHaveImage()) {
					wh.wbthum_pic.setVisibility(View.VISIBLE);
					wh.wbimage.setImageResource(R.drawable.weibo_haveimage);
					
					Drawable thum_pic = null;
					thum_pic = asyncImageLoader.loadDrawable(wb.getThu_pic(),wh.wbthum_pic, new ImageCallback() {
								@Override
								public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
									if (imageDrawable != null)
										imageView.setImageDrawable(imageDrawable);
								}
							});

					if (thum_pic == null) {
						wh.wbthum_pic.setImageResource(R.drawable.thum_pic);
					} else
					{							
						showImg(wh.wbthum_pic, thum_pic);
					}
				}
				
				Drawable cachedImage =null;
				// set the tag of icon url
				wh.wbicon.setTag(wb.getUserIcon());
			    cachedImage = asyncImageLoader.loadDrawable(wb.getUserIcon(), wh.wbicon, new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
								if (imageDrawable != null)
									imageView.setImageDrawable(imageDrawable);
							}
						});
			    
				if (cachedImage == null) {
					wh.wbicon.setBackgroundResource(R.drawable.user_icon);
					} 
				else {
					wh.wbicon.setImageDrawable(cachedImage);
				}
			}
			return convertView;
		}

	}
}
