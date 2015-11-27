package com.junxian.myandroidprojet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.junxian.myWeibo.AsyncImageLoader;
import com.junxian.myWeibo.WeiboConstParam;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.SimpleZoomListener;
import com.sonyericsson.zoom.ZoomState;

public class WeiboImageActivity extends Activity {

	private Bitmap image;
	private ImageZoomView mZoomView;
	private ZoomState mZoomState;
	private SimpleZoomListener mZoomListener;
	private ImageButton back;
	private ImageButton download;
	private String url;
	private String weiboID;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weibo_image);
        Intent i=this.getIntent();
        if(i!=null){
            Bundle b=i.getExtras();
            if(b!=null){
                if(b.containsKey("url")){
                    url = b.getString("url");
                    weiboID = b.getString("weiboID");
                    mZoomView=(ImageZoomView)findViewById(R.id.pic);
                    Drawable img= AsyncImageLoader.loadImageFromUrl(url);
                    
                    
                    back = (ImageButton) findViewById(R.id.returnBtn);
                    back.setOnClickListener(new  OnClickListener() {
						@Override
						public void onClick(View v) {
							onBackPressed();
						}
					});
                    
                    download = (ImageButton) findViewById(R.id.downBtn);
                    download.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							saveMyBitmap(image);
						}
					});
                    
                    
                    if (img ==null) return;
                    image=drawableToBitmap(img);
                    mZoomView.setImage(image);
                    mZoomState = new ZoomState();
                    mZoomView.setZoomState(mZoomState);
                    mZoomListener = new SimpleZoomListener();
                    mZoomListener.setZoomState(mZoomState);
                    mZoomView.setOnTouchListener(mZoomListener);
                    resetZoomState();
                    ZoomControls zoomCtrl = (ZoomControls) findViewById(R.id.zoomCtrl);
                    zoomCtrl.setOnZoomInClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            float z= mZoomState.getZoom()+0.25f;
                            mZoomState.setZoom(z);
                            mZoomState.notifyObservers();
                        }
                    });
                    zoomCtrl.setOnZoomOutClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            float z= mZoomState.getZoom()-0.25f;
                            mZoomState.setZoom(z);
                            mZoomState.notifyObservers();
                        }
                    }); 
                }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (image == null) return;
        image.recycle();
        mZoomView.setOnTouchListener(null);
        mZoomState.deleteObservers();
    }

    
    
    private void resetZoomState() {
        mZoomState.setPanX(0.5f);
        mZoomState.setPanY(0.5f);
        final int mWidth = image.getWidth();
        final int vWidth= mZoomView.getWidth();
        Log.i("iw:",vWidth+"");
        mZoomState.setZoom(1f);
        mZoomState.notifyObservers();
    }
    
    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap(); 
        return bitmap;
    }
    
	public void saveMyBitmap(Bitmap mBitmap) {
		if (image != null) {
			try {
				if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					Toast.makeText(WeiboImageActivity.this, "请插入SD卡后进行保存 !",Toast.LENGTH_SHORT).show(); 
		    		return;
				}
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),WeiboConstParam.APPNAME);
				// Create the storage directory if it does not exist
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Toast.makeText(WeiboImageActivity.this, "无法创建存储文件目录 ",Toast.LENGTH_SHORT).show(); 
						return;
					}
				}
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String bitName = format.format(date);
				String filePath = mediaStorageDir.getAbsolutePath() + "/IMG_"+ bitName+ ".JPEG";
				Log.i("filePath","filePath");
				File f = new File(filePath);
				f.createNewFile();
				FileOutputStream fOut = null;
				fOut = new FileOutputStream(f);
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				fOut.flush();
				fOut.close();
				Toast.makeText(WeiboImageActivity.this, "图片保存成功!",Toast.LENGTH_SHORT).show();
				return;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(WeiboImageActivity.this, "图片保存失败!",Toast.LENGTH_SHORT).show(); 				
					}
			});
		}
		return;
	}
	

}


