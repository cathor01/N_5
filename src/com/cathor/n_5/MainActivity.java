package com.cathor.n_5;

import java.io.File;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends Activity {
	String filePath;
	private final static int FILE_SELECT = 101;
	public static MediaPlayer media;
	public static float scale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//getActionBar().hide();
    	scale = MainActivity.this.getResources().getDisplayMetrics().density;
    	media = new MediaPlayer();
    	media.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				MyFragment.playover();
			}
		});
    	final FragmentManager fm = getFragmentManager();
    	final MyFragment fragment = new MyFragment();
    	final ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(1000);
    	getWindow().setBackgroundDrawableResource(R.drawable.bg);
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        final RelativeLayout main = (RelativeLayout)inflater.inflate(R.layout.activity_main, null);
        final Button button = (Button)main.findViewById(R.id.button);
        final Button button2 = (Button)main.findViewById(R.id.select);
        button2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent tIntent = new Intent(Intent.ACTION_GET_CONTENT);
				tIntent.setType("image/*");
				tIntent.addCategory(Intent.CATEGORY_OPENABLE);
				try{
					startActivityForResult(Intent.createChooser(tIntent, "选择一个应用"), FILE_SELECT);
				}catch(android.content.ActivityNotFoundException ex){
					Toast.makeText(MainActivity.this, "请安装一个选择器", Toast.LENGTH_SHORT).show();
				}
			}
		});
        button.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				FragmentTransaction ft = fm.beginTransaction();
				ft.replace(android.R.id.content, fragment, "frag");
				ft.commit();
				va.start();
				return false;
			}
		});
        button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				va.start();
			}
		});
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
				Drawable draw = Drawable.createFromPath(filePath);
				getWindow().setBackgroundDrawable(draw);
			}
		}
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class FileUtils {
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
