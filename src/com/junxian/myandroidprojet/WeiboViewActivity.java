package com.junxian.myandroidprojet;

import org.json.JSONException;
import org.json.JSONObject;

import com.junxian.myWeibo.AsyncImageLoader;
import com.junxian.myWeibo.WeiboConstParam;
import com.junxian.myWeibo.WeiboContenu;
import com.junxian.myWeibo.AsyncImageLoader.ImageCallback;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboViewActivity extends Activity {

	protected static final int DO_CHARGE = 0;
	protected String key;
	protected String weibo_user_name;
	protected String weibo_text;
	protected String weibo_icon;
	protected boolean weibo_hasPic;
	protected String weibo_comments;
	protected String weibo_reposts;
	protected String weibo_attitudes;
	
	private WeiboContenu weiboContext;
	private LinearLayout loadingLayout;
	public AsyncImageLoader asyncImageLoader;

	public ImageButton btnReturn;
	public ImageButton btnHome;
	
	public Button favorite;
	public Button zhuanfa;
	public Button comment;
	public Button refresh;

	
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_view);
		// 获取上一个页面传递过来的key，key为某一条微博的id
		Intent i = this.getIntent();
		if (!i.equals(null)) {
			Bundle b = i.getExtras();
			if (b != null) {
					if (weiboContext == null) weiboContext = new WeiboContenu();
					key = b.getString("key");
					weibo_icon = b.getString("weibo_icon");
					weibo_user_name = b.getString("weibo_user_name");
					weibo_text = b.getString("weibo_text");
					weibo_hasPic = b.getBoolean("weibo_hasPic", false);
						if (weibo_hasPic) {
							weiboContext.setBmiddle_pic(b.getString("middle_pic"));
							weiboContext.setOriginal_pic(b.getString("original_pic"));
							weiboContext.setThumbnail_pic(b.getString("thum_pic"));
						}
						else weibo_hasPic =false;
					weibo_comments = b.getString("comments");
					weibo_reposts = b.getString("reposts");
					weibo_attitudes = b.getString("favorite");
					
					weiboContext.setComments(weibo_attitudes);
					weiboContext.setHasPic(weibo_hasPic);
					weiboContext.setReposts(weibo_reposts);
					weiboContext.setText(weibo_text);
					weiboContext.setUsericon(weibo_icon);
					weiboContext.setUsername(weibo_user_name);
					Log.i("weiboContext    ", weiboContext.toString());
					charge();
			}
		}
		
		
		
		
	}
	
	 @SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
			public void handleMessage(Message msg) {
	             switch (msg.what) {
	                  case DO_CHARGE:{
	                      charge();
	                        break;
	                  }
	             }
	        }
	  };
	
	  
	public void charge(){
		Log.i("charge ", "starting to charge the content");
		TextView utv = (TextView) findViewById(R.id.user_name);
		utv.setText(weiboContext.getUsername());
		TextView ttv = (TextView) findViewById(R.id.text);
		ttv.setText(weiboContext.getText());
		ImageView iv = (ImageView) findViewById(R.id.user_icon);
		if (asyncImageLoader == null)  asyncImageLoader= new AsyncImageLoader();
		Drawable cachedImage = asyncImageLoader.loadDrawable(weiboContext.getUsericon(), iv, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
						if (imageDrawable != null)
						imageView.setImageDrawable(imageDrawable);
					}
				});
		if (cachedImage == null) {
			iv.setImageResource(R.drawable.user_icon);
		} else {
			iv.setImageDrawable(cachedImage);
		}
		
		if (weiboContext.isHasPic()) {
			
			if (loadingLayout == null) loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
			loadingLayout.setVisibility(View.VISIBLE);
			
			ImageView pic = (ImageView) findViewById(R.id.pic);
			pic.setTag(weiboContext.getOriginal_pic());
			pic.setOnClickListener(new OnClickListener() {
				private String weiboID = key;
				@Override
				public void onClick(View v) {
					Object obj = v.getTag();
					Intent intent = new Intent(WeiboViewActivity.this,WeiboImageActivity.class);
					Bundle b = new Bundle();
					b.putString("url", obj.toString());
					b.putString("weiboID", weiboID);
					intent.putExtras(b);
					startActivity(intent);
				}
			});
			
			Drawable cachedImage2 = asyncImageLoader.loadDrawable(
					weiboContext.getBmiddle_pic(), pic, new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
								showImg(imageView, imageDrawable);
								loadingLayout.setVisibility(View.GONE);
							}
					});
			if (cachedImage2 == null) {
				pic.setImageResource(R.drawable.pic_charge);
			} 
			else {
				loadingLayout.setVisibility(View.GONE);
				showImg(pic, cachedImage2);
			}

		}
		
		zhuanfa = (Button) findViewById(R.id.btn_gz);
		zhuanfa.setText("转发(" + weiboContext.getReposts() + ")");
		
		comment = (Button) findViewById(R.id.btn_pl);
		comment.setText("评论(" + weiboContext.getComments() + ")");	
		
		btnReturn =(ImageButton) findViewById(R.id.returnBtn);
		btnReturn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		
		favorite = (Button) findViewById(R.id.favorite);
		favorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String url = "https://api.weibo.com/2/favorites/create.json";
				final WeiboParameters bundle = new WeiboParameters();
				bundle.add("source", WeiboConstParam.CONSUMER_KEY);
				bundle.add("access_token", Weibo.getInstance().getAccessToken().getToken());
				bundle.add("id", key);
				//这个收藏很快，不必再开一个线程进行操作
				try {
					String response = Weibo.getInstance().request(WeiboViewActivity.this, url,bundle, Utility.HTTPMETHOD_POST,Weibo.getInstance().getAccessToken());
					JSONObject json = new JSONObject(response);
					String favoriteTime = json.getString("favorited_time");
					if (favoriteTime != null) Toast.makeText(WeiboViewActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (e.getMessage().startsWith("java.net.UnknownHostException: Unable to resolve host")) {
								Toast.makeText(WeiboViewActivity.this, "网络未连接，请链接网络",Toast.LENGTH_SHORT).show();
								onBackPressed();
								}
							else {
								Toast.makeText(WeiboViewActivity.this, "收藏失败",Toast.LENGTH_SHORT).show();
								}
						}
					});
				} 
			}
		});
		
		zhuanfa.setOnClickListener(new OnClickListener() {
			private Context context= WeiboViewActivity.this;
			private String weiboContenu = weiboContext.getText();
			private String weiboIcon = weiboContext.getUsericon();
			private Dialog dialog;
			private EditText zhuanfa_comment;
			private Button zhuanfa;
			private TextView contenu;
			private ImageView iv;
			private CheckBox addoriginal;
			private CheckBox addthis;
			private ImageView delete;
			@Override
			public void onClick(View v) {
				// show the dialog to edit the comment
				View diaView=View.inflate(context, R.layout.weibo_dialog, null);
				contenu = (TextView) diaView.findViewById(R.id.zhuanfa_weibo_contenu);
				contenu.setText(weiboContenu);
				iv = (ImageView) diaView.findViewById(R.id.zhuanfa_user_icon);
				Drawable cachedImage = asyncImageLoader.loadDrawable(weiboIcon, iv, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
						if(imageDrawable!=null)
						imageView.setImageDrawable(imageDrawable);
					}
				});
				if (cachedImage == null) {
					iv.setImageResource(R.drawable.user_icon);
				} else {
					iv.setImageDrawable(cachedImage);
				}
				addoriginal = (CheckBox) diaView.findViewById(R.id.box_addoriginal);
				addthis = (CheckBox) diaView.findViewById(R.id.box_addthis);
				delete = (ImageView) diaView.findViewById(R.id.dialog_delet);
				delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
				zhuanfa_comment = (EditText) diaView.findViewById(R.id.zhuanfa_weibo_comment);
				zhuanfa = (Button) diaView.findViewById(R.id.btn_dialog_zhuanfa);
				zhuanfa.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final String url = "https://api.weibo.com/2/statuses/repost.json";
						final WeiboParameters bundle = new WeiboParameters();
						bundle.add("source", WeiboConstParam.CONSUMER_KEY);
						bundle.add("access_token", Weibo.getInstance().getAccessToken().getToken());
						bundle.add("id", key);
						bundle.add("status", zhuanfa_comment.getText().toString());
						//1：评论给当前微博、2：评论给原微博、3：都评论,0 都不评论
						String addcomment;
						if (addoriginal.isChecked() & addthis.isChecked()) addcomment ="3";
						else if (addoriginal.isChecked() & !addthis.isChecked()) addcomment ="2";
						else if (!addoriginal.isChecked() & addthis.isChecked()) addcomment ="1";
						else addcomment="0";
						System.out.println("comment stateus :" + addcomment);
						bundle.add("is_comment", addcomment);
						//这个转发很快，不必再开一个线程进行操作
						try {
							String response = Weibo.getInstance().request(WeiboViewActivity.this, url,bundle, Utility.HTTPMETHOD_POST,Weibo.getInstance().getAccessToken());
							JSONObject json = new JSONObject(response);
							String favoriteTime = json.getString("created_at");
							if (favoriteTime !=null) Toast.makeText(WeiboViewActivity.this, "转发成功", Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						} catch (final Exception e) {
							Toast.makeText(WeiboViewActivity.this, "转发失败,error: " + e.getMessage() , Toast.LENGTH_SHORT).show();
							dialog.dismiss();
							e.printStackTrace();
						} 
					}
				});
				dialog= new Dialog(context,R.style.dialog);
				dialog.setContentView(diaView);
				dialog.show();
			
			}
		});
		
		comment.setOnClickListener(new OnClickListener() {
			private Context context= WeiboViewActivity.this;
			private String weiboContenu = weiboContext.getText();
			private String weiboIcon = weiboContext.getUsericon();
			private Dialog dialog;
			private EditText zhuanfa_comment;
			private Button zhuanfa;
			private TextView contenu;
			private ImageView iv;
			private CheckBox addoriginal;
			private CheckBox addthis;
			private ImageView delete;
			@Override
			public void onClick(View v) {
				// show the dialog to edit the comment
				View diaView=View.inflate(context, R.layout.weibo_dialog, null);
				contenu = (TextView) diaView.findViewById(R.id.zhuanfa_weibo_contenu);
				contenu.setText(weiboContenu);
				iv = (ImageView) diaView.findViewById(R.id.zhuanfa_user_icon);
				Drawable cachedImage = asyncImageLoader.loadDrawable(weiboIcon, iv, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl) {
						if (imageDrawable != null)
						imageView.setImageDrawable(imageDrawable);
					}
				});
				if (cachedImage == null) {
					iv.setImageResource(R.drawable.user_icon);
				} else {
					iv.setImageDrawable(cachedImage);
				}
				addoriginal = (CheckBox) diaView.findViewById(R.id.box_addoriginal);
				addthis = (CheckBox) diaView.findViewById(R.id.box_addthis);
				addthis.setVisibility(View.INVISIBLE);
				delete = (ImageView) diaView.findViewById(R.id.dialog_delet);
				delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				zhuanfa_comment = (EditText) diaView.findViewById(R.id.zhuanfa_weibo_comment);
				zhuanfa = (Button) diaView.findViewById(R.id.btn_dialog_zhuanfa);
				zhuanfa.setText("评论");
				zhuanfa.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final String url = "https://api.weibo.com/2/comments/create.json";
						final WeiboParameters bundle = new WeiboParameters();
						bundle.add("source", WeiboConstParam.CONSUMER_KEY);
						bundle.add("access_token", Weibo.getInstance().getAccessToken().getToken());
						bundle.add("id", key);
						// should show a dialog to edit the text and comment? to be done
						//comment	true	string	评论内容，必须做URLencode，内容不超过140个汉字。
						//comment_ori	false	int	当评论微博时，是否评论给原微博，0：否、1：是，默认为0。。
						bundle.add("comment", zhuanfa_comment.getText().toString());
						String addcomment;
						if (addoriginal.isChecked()) addcomment ="1";
						else addcomment ="0";
						bundle.add("comment_ori", addcomment);
						//这个转发很快，不必再开一个线程进行操作
						try {
							String response = Weibo.getInstance().request(WeiboViewActivity.this, url,bundle, Utility.HTTPMETHOD_POST,Weibo.getInstance().getAccessToken());
							JSONObject json = new JSONObject(response);
							String favoriteTime = json.getString("created_at");
							if (favoriteTime !=null) Toast.makeText(WeiboViewActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						} catch (final Exception e) {
							Toast.makeText(WeiboViewActivity.this, "评论失败,error: " + e.getMessage() , Toast.LENGTH_SHORT).show();
							dialog.dismiss();
							e.printStackTrace();
						} 
					}
				});
				dialog= new Dialog(context,R.style.dialog);
				dialog.setContentView(diaView);
				dialog.show();
			}
		});
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new view(key)).start();
				}
			});
		
		
	};
	  
	class view implements Runnable{
		private String weiboId;
		public view(String id){
			this.weiboId = id;
		}
		@Override
		public void run() {
			Message message = new Message();
			final String url = "https://api.weibo.com/2/statuses/show.json";
			final WeiboParameters bundle = new WeiboParameters();
			bundle.add("source", WeiboConstParam.CONSUMER_KEY);
			bundle.add("access_token", Weibo.getInstance().getAccessToken().getToken());
			bundle.add("id", weiboId);
			

			try {
				String response = Weibo.getInstance().request(WeiboViewActivity.this, url,bundle, Utility.HTTPMETHOD_GET,Weibo.getInstance().getAccessToken());
				JSONObject json = new JSONObject(response);
				Log.i("weibo", response);
				if (json != null) {
					JSONObject u = json.getJSONObject("user");
					String userName = u.getString("screen_name");
					String userIcon = u.getString("profile_image_url");
					String time = json.getString("created_at");
					String text = json.getString("text");
					String reposts= json.getString("reposts_count");
					String comments= json.getString("comments_count");
					if (weiboContext == null) {
						weiboContext =new WeiboContenu();
					}
					if (json.has("retweeted_status")) {
						JSONObject r = json.getJSONObject("retweeted_status");
						text = text + "//"+ r.getJSONObject("user").getString("screen_name") + ": " + r.getString("text");
						
						if (r.has("thumbnail_pic")) {
							String thumbnail_pic = r.getString("thumbnail_pic");
							String bmiddle_pic = r.getString("bmiddle_pic");
							String original_pic = r.getString("original_pic");
							weiboContext.setHasPic(true);
							weiboContext.setThumbnail_pic(thumbnail_pic);
							weiboContext.setBmiddle_pic(bmiddle_pic);
							weiboContext.setOriginal_pic(original_pic);
							}
					}
					weiboContext.setUsername(userName);
					weiboContext.setUsericon(userIcon);
					weiboContext.setText(text);
					weiboContext.setReposts(reposts);
					weiboContext.setComments(comments);
					
					if (json.has("thumbnail_pic")) {
						String thumbnail_pic = json.getString("thumbnail_pic");
						String bmiddle_pic = json.getString("bmiddle_pic");
						String original_pic = json.getString("original_pic");
						weiboContext.setHasPic(true);
						weiboContext.setThumbnail_pic(thumbnail_pic);
						weiboContext.setBmiddle_pic(bmiddle_pic);
						weiboContext.setOriginal_pic(original_pic);
					}
				}			
				message.what = DO_CHARGE;
                myHandler.sendMessage(message);
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (e.getMessage().startsWith("java.net.UnknownHostException: Unable to resolve host")) {
							Toast.makeText(WeiboViewActivity.this, "网络未连接，请链接网络",Toast.LENGTH_SHORT).show();
							onBackPressed();
							}
						else {
							Toast.makeText(WeiboViewActivity.this, "用户token已过期，请重新登录验证",Toast.LENGTH_SHORT).show();
							onBackPressed();
							}
					}
				});
			}
		}
	}

	private void showImg(ImageView view,Drawable img){
		if (img ==null) {
				Toast.makeText(WeiboViewActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
			return;
				}
        int w=img.getIntrinsicWidth();
        int h=img.getIntrinsicHeight();
        Log.i("windows width and heigt", w+"/"+h);
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
}
