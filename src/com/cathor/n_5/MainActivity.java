package com.cathor.n_5;

import java.io.File;
import java.io.IOException;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	String filePath;
	public static String data;
	private static Activity activity;
	private static Bitmap bitmap = null;
	private static Bitmap nB = null;
	private static int width;
	private static int height;
	private static ImageView background;
	private static int counter = 1;
	public static NotificationManager nm;
	private final static int FILE_SELECT = 101;
	public static float scale;
	private static Intent service;
	private static ValueAnimator va;
	private static RemoteViews remote;
	private static FragmentManager fm;
	private static MyFragment fragment;
	private static Controller control;
	private static Toolbar toolbar;
	private static Notification notifi;
	public static Activity getInstance(){
		return activity;
	}
	/**
	 * 推送Notification
	 * （尚未完成）
	 * @author 瑞凯
	 * @param flag 播放或否
	 * 
	 * */
	public static void updateNoti(int flag){
		if(MyService.getNowPlay() != -1){
			remote.setTextViewText(R.id.ntitle,MyService.getItemAt(MyService.getNowPlay()).title);
			if(flag == 1){
				remote.setImageViewResource(R.id.npause,R.drawable.pause);
				nm.notify(111, notifi);
			}
			else if(flag == 2){
				remote.setImageViewResource(R.id.npause, R.drawable.play);
				nm.cancel(111);
			}
		}
		else{
			remote.setImageViewResource(R.id.npause, R.drawable.play);
			nm.cancel(111);
		}
	}
	
	/***
	 * 耳机拔出状态监听
	 * 
	 * */
	
	private void registerHeadsetPlugReceiver() {    
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);    
        registerReceiver(headsetPlugReceiver, intentFilter);   
    }  
      
    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {  
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {  
                MyFragment.pauseMusic();  
            }  
        }  
    };  
	
    /**
     * Handler处理
     * 
     * */
    
	static Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case 101:
				FragmentTransaction ft = fm.beginTransaction();
				android.app.Fragment f = fm.findFragmentByTag("frag");
				if(f != null){
					ft.remove(f);
				}
				ft.commit();
				try{
					MyService.stop();
				}
				catch(IllegalStateException e){
					e.printStackTrace();
				}
				MyService.setNowPlay(-1);
				Controller.init();
				updateNoti(11);
				MyFragment.dropData();
				handler.sendEmptyMessage(102);
				break;
			case 102:
				fragment = new MyFragment();
				FragmentTransaction ft1 = fm.beginTransaction();
				android.app.Fragment f1 = fm.findFragmentByTag("frag");
				if(f1 != null){
					ft1.remove(f1);
				}
				ft1.replace(R.id.frag, fragment, "frag");
				ft1.commit();
				break;
			case 104:
				data = msg.getData().getString("backImg");
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						BitmapRegionDecoder bd;
						try {
							bd = BitmapRegionDecoder.newInstance(data, true);
							int left = 0;
							int right = bd.getWidth();
							int top = 0;
							int bottom = bd.getHeight();
							if(bd.getWidth() > 4096){
								left = bd.getWidth() / 2 - 2048;
								right = bd.getHeight() / 2 + 2048;
							}
							if(bd.getHeight() > 4096){
								top = bd.getHeight() / 2 - 2048;
								bottom = bd.getHeight() / 2 + 2048;
							}
							bitmap = bd.decodeRegion(new Rect(left, top, right, bottom), null);
							//nB = zoomBitmap(bitmap);
							handler.sendEmptyMessage(202);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				thread.start();
				break;
			case 201:
				toolbar.getMenu().findItem(R.id.refresh).setVisible(true); //当音乐列表界面出现后方可刷新
				toolbar.getMenu().findItem(R.id.change).setVisible(false);
				break;
			case 202:
				//remote.setImageViewBitmap(R.id.album, nB);
				updateNoti(1);
				background.setImageBitmap(bitmap);
				break;
			}
		}
		
	};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//getActionBar().hide();
    	scale = MainActivity.this.getResources().getDisplayMetrics().density;
    	service = new Intent(this, MyService.class); //音乐服务
    	startService(service);
    	activity = this;
    	fm = getFragmentManager();
    	if(fragment == null){
    		fragment = new MyFragment(); //手动单例。。。。。。
    	}
    	WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    	Point size = new Point();
    	wm.getDefaultDisplay().getSize(size);
    	width = size.x;
    	nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); //通知栏，，，，做的还有问题
    	va = ValueAnimator.ofFloat(0f, 1f); //某个蛋疼功能
        va.setDuration(1000);
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        final RelativeLayout main = (RelativeLayout)inflater.inflate(R.layout.activity_main, null);
        background = (ImageView)main.findViewById(R.id.backImg);
        height = background.getHeight();
        toolbar = (Toolbar)main.findViewById(R.id.tool); //Toolbar android.support.v7.Toolbar 使用
        //toolbar.getMenu().getItem(3).setVisible(false);
        toolbar.setTitle("N_5");
        toolbar.setSubtitle("music player");
        toolbar.setBackgroundColor(0xff66ccff);
        toolbar.setTitleTextColor(0xffffffff);
        toolbar.setSubtitleTextColor(0xffffffff);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        notifi = new Notification(R.drawable.ic_launcher, "开始音乐", System.currentTimeMillis());
    	notifi.flags = Notification.FLAG_NO_CLEAR;
    	RelativeLayout notiV = (RelativeLayout)inflater.inflate(R.layout.notification, null);
    	//registerHeadsetPlugReceiver();
        remote =  new RemoteViews(getPackageName(), R.layout.notification);
    	notifi.contentView = remote;
    	remote.setTextViewText(R.id.ntitle, "No Media");
    	remote.setImageViewResource(R.id.npause, R.drawable.play);
    	nm.notify(111, notifi);
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				int id = item.getItemId();
		        switch(id){
		        case R.id.click: 
		        	va.start();
		        	return false;
		        case R.id.select:
		        	Intent tIntent = new Intent(Intent.ACTION_GET_CONTENT);
					tIntent.setType("image/*");
					tIntent.addCategory(Intent.CATEGORY_OPENABLE);
					try{
						startActivityForResult(Intent.createChooser(tIntent, "选择一个应用"), FILE_SELECT);
					}catch(android.content.ActivityNotFoundException ex){
						Toast.makeText(MainActivity.this, "请安装一个选择器", Toast.LENGTH_SHORT).show();
					}
					return false;
		        case R.id.change:
		        	
		        	
		        	new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							FragmentTransaction ft = fm.beginTransaction();
							android.app.Fragment f = fm.findFragmentByTag("frag"); //加载MyFragment到中间的FrameLayout中
							if(f != null){
								ft.remove(f);
							}
							if(fragment == null){
								fragment = new MyFragment();
							}
							ft.replace(R.id.frag, fragment, "frag");
							ft.commit();
							handler.sendEmptyMessage(201);
						}
					}).start();
		        	return false;
		        case R.id.refresh:
		        	handler.sendEmptyMessage(101);
		        }
				return false;
			}
		});
        if(control == null){ //手动单例。。。。。。
        	control = new Controller();
        }
        FragmentTransaction ftt = fm.beginTransaction();
    	android.app.Fragment f = fm.findFragmentByTag("cont");
    	if(f != null){
    		ftt.remove(f);
    	}
        ftt.replace(R.id.controller, control, "cont");
        ftt.commit();
        AnimatorUpdateListener listener = new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float alpha = (Float) animation.getAnimatedValue();
				main.setAlpha(alpha);
			}
		};
        va.addUpdateListener(listener);
        setContentView(main);
    }


    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	switch(requestCode){
		case FILE_SELECT:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
				filePath = FileUtils.getPath(this, uri);
				setBackGround(filePath);
			}
		}
	}
    /**
     * 设置背景图片
     * @param path 图片路径
     * 
     * */
    
    public static void setBackGround(String path){
    	Message msg = new Message();
    	Bundle data = new Bundle();
    	data.putString("backImg", path);
    	msg.setData(data);
    	msg.what = 104;
    	handler.sendMessage(msg);
    }

    public static void initBackGround(){
    	background.setImageResource(R.drawable.bg);
    	//remote.setImageViewResource(R.id.album, R.drawable.bg);
    	updateNoti(1);
    	nB = null;
    	bitmap = null;
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		//fm.beginTransaction().replace(R.id.controller, new Controller(counter++ + ""));
		super.onRestart();
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
    
    private static int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * scale + 0.5f);
	}
    
    private static Bitmap zoomBitmap(Bitmap bitmap){
    	Matrix matrix = new Matrix();
    	float width = bitmap.getWidth();
    	float height = bitmap.getHeight();
    	float scaleX = getPx(70) / width;
    	float scaleY = getPx(70) / height;
    	Bitmap newB = Bitmap.createBitmap(bitmap, 0, 0, (int)width, (int)height, matrix, true);
    	return newB;
    }
    
    /**
     * 解析Uri的类
     * 通过调用其静态方法实现
     * 
     * */
    public static class FileUtils {
    	/***
    	 * 解析Uri
    	 * @author 瑞凯
    	 * @param context 调用的Context
    	 * @param uri 需要解析的Uri (仅允许Image)
    	 * @return 解析后的Path
    	 * 
    	 * */
    	
	    public static String getPath(Context context, Uri uri) {
	    	File fi = new File(uri.getPath());
	    	
	    	if ( null == uri ) return null;
	        final String scheme = uri.getScheme();
	        System.out.println();
	        String data = null;
	        if ( scheme == null ){
	        	System.out.println("sch1");
	            data = uri.getPath();
	        }
	        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
	            data = uri.getPath();
	        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
	        	ContentResolver cr = context.getContentResolver();
	        
	        	//CursorLoader loader = new CursorLoader(context,uri, new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
	        	Cursor cursor = cr.query(uri, new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID }, null, null, null);
	            if ( null != cursor ) {
	            	System.out.println("sch2");
	                if ( cursor.moveToFirst() ) {
	                	System.out.println(cursor.getColumnCount());
	                    int index = cursor.getColumnIndex( MediaStore.Images.Media.DATA );
	                    String tem = cursor.getString(index);
	                    System.out.println("id: " + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
	                    if ( index > -1 ) {
	                        data = tem;
	                        System.out.println("sch3: " + data);
	                    }
	                }
	                cursor.close();
	            }
	        }
	        return data;
	    }
	}
}
