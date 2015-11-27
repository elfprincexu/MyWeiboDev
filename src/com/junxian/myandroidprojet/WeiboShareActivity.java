/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.junxian.myandroidprojet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.junxian.myWeibo.WeiboConstParam;
import com.junxian.myandroidprojet.WeiboViewActivity.view;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

/**
 * A dialog activity for sharing any text or image message to weibo. Three
 * parameters , accessToken, tokenSecret, consumer_key, are needed, otherwise a
 * WeiboException will be throwed.
 * 
 * ShareActivity should implement an interface, RequestListener which will
 * return the request result.
 * 
 * @author ZhangJie (zhangjie2@staff.sina.com.cn)
 */

public class WeiboShareActivity extends Activity implements OnClickListener, RequestListener {
    
	private TextView mTextNum;
    private ImageButton mSend;
    private ImageButton mGoback;
    private EditText mEdit;
    private FrameLayout mPiclayout;
    private View popView;
    private Dialog popDialog;

    private Uri fileUri;
    
    private String userName="";
    private String mAccessToken = "";
    private String mPicPath = "";
    private String mContent = "";
    private String lon="";
    private String lat="";
    
    public static final String IMAGE_UNSPECIFIED = "image/*";
    public static final int NONE = 0;
    public static final int CAMERATAKE = 1;
    public static final int PHOTOSTACK = 2; // 锟斤拷锟斤拷
	public static final int PHOTORESULT = 3;// 锟斤拷锟�

    public static final int WEIBO_MAX_LENGTH = 140;

    public void onCreate(Bundle savedInstanceState) {
        
    	Log.i("", "shareActivity has been created");
    	
    	super.onCreate(savedInstanceState);
    	Bundle values= this.getIntent().getExtras();
    	this.userName=values.getString("nickname");
    	
        this.setContentView(R.layout.share_mblog_view);

        //go back button 
        mGoback  = (ImageButton) this.findViewById(R.id.btnGoBack);
        mGoback.setOnClickListener(this);
        //userName show
        TextView userName = (TextView) this.findViewById(R.id.showUserName);
        userName.setText(this.userName);
        //send button
        mSend = (ImageButton) this.findViewById(R.id.btnSend);
        mSend.setOnClickListener(this);
        //clear the content of weibo linearlayout
        LinearLayout total = (LinearLayout) this.findViewById(R.id.ll_text_limit_unit);
        total.setOnClickListener(this);
        //add picture 
        LinearLayout addPic = (LinearLayout) this.findViewById(R.id.add_pic);
        addPic.setOnClickListener(this);
        //add location
        LinearLayout addLoc = (LinearLayout) this.findViewById(R.id.add_location);
        addLoc.setOnClickListener(this);
        // show the left number of content
        mTextNum = (TextView) this.findViewById(R.id.tv_text_limit);
        // delet the image inputed 
        ImageView picture = (ImageView) this.findViewById(R.id.ivDelPic);
        picture.setOnClickListener(this);
        // input the content of weibo, dynamically show the number left in the mTextNum
        mEdit = (EditText) this.findViewById(R.id.etEdit);
		mEdit.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {	}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				String mText = mEdit.getText().toString();
				int len = mText.length();
				if (len <= WEIBO_MAX_LENGTH) {
					len = WEIBO_MAX_LENGTH - len;
					// mTextNum.setTextColor(R.color.blue);
					if (!mSend.isEnabled())
						mSend.setEnabled(true);
				} else {
					// more than the limit, show the overpass number
					len = len - WEIBO_MAX_LENGTH;
					mTextNum.setTextColor(Color.RED);
					if (mSend.isEnabled())
						mSend.setEnabled(false);
				}
				mTextNum.setText(String.valueOf(len));
			}
		});
		
		// add the picture 
		mPiclayout = (FrameLayout) WeiboShareActivity.this.findViewById(R.id.flPic);
		if (TextUtils.isEmpty(this.mPicPath)) {
			mPiclayout.setVisibility(View.VISIBLE);
		} else {
			mPiclayout.setVisibility(View.VISIBLE);
			File file = new File(mPicPath);
			if (file.exists()) {
				Bitmap pic = BitmapFactory.decodeFile(this.mPicPath);
				ImageView image = (ImageView) this.findViewById(R.id.ivImage);
				image.setImageBitmap(pic);
			} else {
				mPiclayout.setVisibility(View.VISIBLE);
			}
		}
    }

    
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		if (viewId == R.id.btnGoBack) {		
           onBackPressed();
		} 
		else if (viewId == R.id.btnSend) {
			Weibo weibo = Weibo.getInstance();
			if (!TextUtils.isEmpty((String) (weibo.getAccessToken().getToken()))) {
				this.mContent = mEdit.getText().toString();
				if (TextUtils.isEmpty(mContent)) { 
					Toast.makeText(WeiboShareActivity.this, "微博内容不能为空，请输入后再次发送", Toast.LENGTH_SHORT).show(); 
					return ;
				}
				if (!TextUtils.isEmpty(mPicPath)) {
					onBackPressed();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								upload(Weibo.getInstance(), Weibo.getAppKey(),mPicPath, mContent,lon, lat);
							} catch (WeiboException e) {									
								if (e.getMessage().startsWith("java.net.UnknownHostException: Unable to resolve host")) {
									Toast.makeText(WeiboShareActivity.this, "网络未连接，请链接网络",Toast.LENGTH_LONG).show();
								}
							}
						}
					});
				} else {
					onBackPressed();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								update(Weibo.getInstance(), Weibo.getAppKey(), mContent, lon,lat);
							} catch (Exception e) {									
								if (e.getMessage().startsWith("java.net.UnknownHostException: Unable to resolve host")) {
									Toast.makeText(WeiboShareActivity.this, "网络未连接，请链接网络",Toast.LENGTH_LONG).show();
								}
							} 
						}
					});
				}
			} 
			else {
				Toast.makeText(this, this.getString(R.string.please_login),Toast.LENGTH_LONG);
			}
		} 
		
		else if (viewId == R.id.ll_text_limit_unit) {
			Dialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.attention)
					.setMessage(R.string.delete_all)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mEdit.setText("");
								}
							}).setNegativeButton(R.string.cancel, null)
					.create();
			dialog.show();
		} 
		
		else if (viewId == R.id.add_pic){
			if (popView ==null) popView = View.inflate(WeiboShareActivity.this,R.layout.weibo_share_pic, null);
			if (popDialog ==null) popDialog = new Dialog(WeiboShareActivity.this, R.style.pop_dialog);
			popDialog.setContentView(popView);
			popDialog.show();
			
			LinearLayout photostack = (LinearLayout) popView.findViewById(R.id.weibo_photo_stack);
			photostack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK, null);
					intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
		            startActivityForResult(intent, PHOTOSTACK);
					Log.i("looking for picture location",MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath() );
					popDialog.dismiss();
				}
			});
			
			LinearLayout cameratake = (LinearLayout) popView.findViewById(R.id.weibo_photo_camera);
			cameratake.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
						//create a file to save the image
						 File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), WeiboConstParam.APPNAME);
						// Create the storage directory if it does not exist
						 if (!mediaStorageDir.exists()) {
							if (!mediaStorageDir.mkdirs()) {
								Toast.makeText(WeiboShareActivity.this, "无法创建存储文件目录 ",Toast.LENGTH_SHORT).show(); 
								return;
							}
						 }
						// Create a media file name
						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
						String bitName = format.format(date);
						String filePath = mediaStorageDir.getAbsolutePath() + "/IMG_"+ bitName+ ".JPEG";
						Log.i("filePath","filePath");
						File f = new File(filePath);
						f.createNewFile();
						fileUri = Uri.fromFile(f);
						// set the image file name
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
						// start the image capture Intent
			            startActivityForResult(cameraIntent, CAMERATAKE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				      popDialog.dismiss();
				}
			});
			
		}
		
		else if (viewId == R.id.add_location){
			// here we give the lon lat the value of position
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // ���þ���
			criteria.setAltitudeRequired(false); // �����Ƿ���Ҫ�ṩ������Ϣ
			criteria.setBearingRequired(false); // �Ƿ���Ҫ������Ϣ
			criteria.setCostAllowed(false); // �����ҵ���Provider�Ƿ�����������
			criteria.setPowerRequirement(Criteria.POWER_LOW); // ���úĵ�
			try {
				LocationManager locationManager = (LocationManager) WeiboShareActivity.this.getSystemService(Context.LOCATION_SERVICE);
				String provider = locationManager.getBestProvider(criteria, true);
				Log.i("location manager", provider);
				Location ret = locationManager.getLastKnownLocation(provider);
				this.lon = Double.toString(ret.getLongitude());
				this.lat = Double.toString(ret.getLatitude());
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(WeiboShareActivity.this, "经度["+ lon + "]纬度[" + lat +"]", Toast.LENGTH_SHORT).show();
					}
				});
				Log.i("position lat et lon", this.lat + this.lon);
				// to finish the location service
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(WeiboShareActivity.this, "GPS位置服务暂时无法提供：" + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
				e.printStackTrace();
			}
		}
		
		else if (viewId == R.id.ivDelPic) {
			Dialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.attention)
					.setMessage(R.string.del_pic)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mPiclayout.setVisibility(View.INVISIBLE);
									// delete the pic
									mPicPath = "";
								}
							}).setNegativeButton(R.string.cancel, null)
					.create();
			dialog.show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == NONE)
			return;
		// 閫夋嫨鍥剧墖鐨刟ctivity杩斿洖
		else if (requestCode == PHOTOSTACK) {
			//鏍规嵁杩斿洖鐨剈ri = data锛実etData鎴戜滑瀵硅鍥剧墖url鎿嶄綔杩涜
			addPicture(data.getData());
		}
		else if (requestCode == CAMERATAKE) {
			   Bitmap bmp = null;
			   super.onActivityResult(requestCode, resultCode, data);
			   // because we have defined the fileuri in the intent, data == null
			   if (data != null) {
			      Bundle extras = data.getExtras();
			      bmp = compressImage ( (Bitmap) extras.get("data") );
			   }
			   // we will operate the pic here
			   else {
				   try {
						if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
							Toast.makeText(WeiboShareActivity.this, "请插入SD卡后进行操作 !",Toast.LENGTH_SHORT).show(); 
							return;
						}
						File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),WeiboConstParam.APPNAME);
						// Create the storage directory if it does not exist
						if (!mediaStorageDir.exists()) {
							if (!mediaStorageDir.mkdirs()) {
								Toast.makeText(WeiboShareActivity.this, "无法创建存储文件目录 ",Toast.LENGTH_SHORT).show(); 
								return;
							}
						}
						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
						String bitName = format.format(date);
						String filePath = mediaStorageDir.getAbsolutePath() + "/IMG_"+ bitName+ ".JPEG";
						Log.i("add file path ",filePath);
						File f = new File(filePath);
						f.createNewFile();
						FileOutputStream fOut = null;
						fOut = new FileOutputStream(f);
						// 压缩后的图片 《 100 k
						Bitmap pic_compressed = getimage(fileUri.getPath());
						pic_compressed.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
						fOut.flush();
						fOut.close();
						if (f.exists()) {
							// reload the compressed file to the mpicPath
							mPiclayout.setVisibility(View.VISIBLE);
							this.mPicPath = filePath;	
							ImageView image = (ImageView) this.findViewById(R.id.ivImage);
							image.setImageBitmap(pic_compressed);
						} 
						else {
							mPiclayout.setVisibility(View.GONE);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			   }
			  
		}
	}

	public void addPicture(Uri uri) {
		 String[] proj = {MediaStore.Images.Media.DATA};          
         Cursor cursor = managedQuery(uri, proj, null, null, null);   
         //鎸夋垜涓汉鐞嗚В 杩欎釜鏄幏寰楃敤鎴烽�鎷╃殑鍥剧墖鐨勭储寮曞�  
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);             
         cursor.moveToFirst();  
         //鏈�悗鏍规嵁绱㈠紩鍊艰幏鍙栧浘鐗囪矾寰� 
        this.mPicPath = cursor.getString(column_index);  
        if (TextUtils.isEmpty(this.mPicPath)) {
			mPiclayout.setVisibility(View.GONE);
		} else {
			try {
				if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					Toast.makeText(WeiboShareActivity.this, "请插入SD卡后进行操作 !",Toast.LENGTH_SHORT).show(); 
					return;
				}
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),WeiboConstParam.APPNAME);
				// Create the storage directory if it does not exist
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Toast.makeText(WeiboShareActivity.this, "无法创建存储文件目录 ",Toast.LENGTH_SHORT).show(); 
						return;
					}
				}
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String bitName = format.format(date);
				String filePath = mediaStorageDir.getAbsolutePath() + "/IMG_"+ bitName+ ".JPEG";
				Log.i("add file path ",filePath);
				File f = new File(filePath);
				f.createNewFile();
				FileOutputStream fOut = null;
				fOut = new FileOutputStream(f);
				
				mPiclayout.setVisibility(View.VISIBLE);
				// 压缩后的图片 《 100 k
				Bitmap pic_compressed = getimage(this.mPicPath);
				pic_compressed.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				
				fOut.flush();
				fOut.close();
				if (f.exists()) {
					// reload the compressed file to the mpicPath
					this.mPicPath = filePath;	
					ImageView image = (ImageView) this.findViewById(R.id.ivImage);
					image.setImageBitmap(pic_compressed);
				} 
				else {
					mPiclayout.setVisibility(View.GONE);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//send a message with the picture, if the picture file is too large, it will cause a problem!!!!!!!!!!!!!!! compress the picture
    private String upload(Weibo weibo, String source, String file, String status, String lon,String lat) throws WeiboException {
    	
    	
    	Log.i("", "update a statue with a pic.............");
    	WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("pic", file);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("lon", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        System.out.println("upload weibo  "+ status + " "  + lon + " " + lat + " " + file);
        String rlt = "";
        String url = Weibo.SERVER + "statuses/upload.json";
        String resp = weibo.request(WeiboShareActivity.this, url, bundle, Utility.HTTPMETHOD_POST, weibo.getAccessToken());
        this.onComplete(resp);
        return rlt;
    }

     //send a message  statue without the picture
    private String update(Weibo weibo, String source, String status, String lon, String lat)throws MalformedURLException, IOException, WeiboException {
    	Log.i("   ", "update a statue without pic.............");
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("long", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        String rlt = "";
        String url = Weibo.SERVER + "statuses/update.json";
        String resp = weibo.request(WeiboShareActivity.this, url, bundle, Utility.HTTPMETHOD_POST, weibo.getAccessToken());
        this.onComplete(resp);
        return rlt;
    }
    
   
    @Override
    public void onComplete(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeiboShareActivity.this, R.string.send_sucess, Toast.LENGTH_LONG).show();
            }
        });
//        goHome();
    }

    @Override
    public void onIOException(final IOException e) {
    	runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeiboShareActivity.this, e.getMessage()+ " : 请稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(final WeiboException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeiboShareActivity.this, String.format(WeiboShareActivity.this.getString(R.string.send_failed) + ":%s", e.getMessage()+" : 请稍后再试"), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public void goHome(){
    	//go back to the front 
		Intent intent = new Intent();
		intent.setClass(WeiboShareActivity.this, WeiboHomeActivity.class);
		intent.putExtra("nickname", this.userName);
		startActivity(intent);
		this.finish();
    }
    
    // compress image to some extent
    private Bitmap getimage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
		
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是320*240分辨率，所以高和宽我们设置为
		float hh = 320f;//这里设置高度为800f
		float ww = 240f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}

    private Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 90;
		while ( baos.toByteArray().length / 1024>100) {	//循环判断如果压缩后图片是否大于100kb,大于继续		
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
	}

}
