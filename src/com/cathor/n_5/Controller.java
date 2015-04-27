package com.cathor.n_5;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Controller extends Fragment {
	private static MusicInfo from;
	private static MusicInfo to;
	private static ImageView repeat;
	private static ImageView play;
	private static LayoutInflater inflater;
	private static RelativeLayout.LayoutParams initParams;
	private static ValueAnimator vaGo = ValueAnimator.ofFloat(1f, 0f);
	private static ValueAnimator vaCome = ValueAnimator.ofFloat(0f, 1f);
	private static class MusicInfo{
		RelativeLayout relative;
		TextView title;
		TextView author;
		public MusicInfo(RelativeLayout relative, TextView title, TextView author) {
			// TODO Auto-generated constructor stub
			this.relative = relative;
			this.title = title;
			this.author = author;
		}
		private void init(){
			this.relative.setLayoutParams(initParams);
			this.relative.setVisibility(View.INVISIBLE);
			this.relative.setAlpha(0);
		}
		public static void go(String titleV, String authorV){
			to.title.setText(titleV);
			to.author.setText(authorV);
			vaGo.start();
		}
		public static void come(String titleV, String authorV){
			to.title.setText(titleV);
			to.author.setText(authorV);
			vaCome.start();
		}
	}
	
	public static void init(){
		from.title.setText("列表中选择播放");
		from.author.setText("");
		play.setImageResource(R.drawable.play_w);
	}
	
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.scale + 0.5f);
	}
	
	public static void update(){
		if(MyService.getNowPlay() != -1){
			if(MyFragment.change != 0){
				String titleV = "";
				titleV = MyService.getItemAt(MyService.getNowPlay()).title;
				String authorV = MyService.getItemAt(MyService.getNowPlay()).author;
				if(MyFragment.change == 2){
					MusicInfo.go(titleV, authorV);
				}
				else if(MyFragment.change == 1){
					MusicInfo.come(titleV, authorV);
				}
				MyFragment.change = 0;
			}
			try{
				if(MyService.getPlayStatewioutThrow()){
					System.out.println("Pause");
					play.setImageResource(R.drawable.pause_w);
				}
				else{
					System.out.println("Play");
					play.setImageResource(R.drawable.play_w);
				}
			} catch(IllegalStateException e){
				e.printStackTrace();
				System.out.println("Error Play");
				play.setImageResource(R.drawable.play_w);
			}
			switch(MyService.getFlag()){
			case 0:
				repeat.setImageResource(R.drawable.single);
				break;
			case 1:
				repeat.setImageResource(R.drawable.on);
				break;
			case 2:
				repeat.setImageResource(R.drawable.repeat1);
				break;
			case 3:
				repeat.setImageResource(R.drawable.random);
				break;
			}
		}
	}
	
	private void toastInfo(){
		switch(MyService.getFlag()){
		case 0:
			Toast.makeText(inflater.getContext(), "单曲播放", Toast.LENGTH_SHORT).show();
			break;
		case 1:
			Toast.makeText(inflater.getContext(), "全部循环", Toast.LENGTH_SHORT).show();
			break;
		case 2:
			Toast.makeText(inflater.getContext(), "单曲循环", Toast.LENGTH_SHORT).show();
			break;
		case 3:
			Toast.makeText(inflater.getContext(), "随机播放", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		vaGo.setDuration(500);
		vaGo.removeAllListeners();
		
		vaGo.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				from.relative.setAlpha(value);
				from.relative.setScaleX(value);
				from.relative.setScaleY(value);
				to.relative.setAlpha(1 - value);
				to.relative.setScaleX(1 + value);
				to.relative.setScaleY(1 + value);
			}
		});
		
		vaGo.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				to.relative.setScaleX(2);
				to.relative.setScaleY(2);
				to.relative.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				from.relative.setVisibility(View.INVISIBLE);
				MusicInfo temp = from;
				System.out.println("From--------->" + from.title.getText());
				System.out.println("To  --------->" + to.title.getText());
				to.relative.setVisibility(View.VISIBLE);
				from = to;
				to = temp;
				System.out.println("From--------->" + from.title.getText());
				System.out.println("To  --------->" + to.title.getText());
				to.init();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		vaCome.setDuration(500);
		vaCome.removeAllListeners();
		vaCome.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				from.relative.setAlpha(1 - value);
				from.relative.setScaleX(1 + value);
				from.relative.setScaleY(1 + value);
				to.relative.setAlpha(value);
				to.relative.setScaleX(value);
				to.relative.setScaleY(value);
			}
		});
		
		vaCome.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				to.relative.setScaleX(0);
				to.relative.setScaleY(0);
				to.relative.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				from.relative.setVisibility(View.INVISIBLE);
				MusicInfo temp = from;
				System.out.println("From--------->" + from.title.getText());
				System.out.println("To  --------->" + to.title.getText());
				to.relative.setVisibility(View.VISIBLE);
				from = to;
				to = temp;
				System.out.println("From--------->" + from.title.getText());
				System.out.println("To  --------->" + to.title.getText());
				to.init();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		inflater = inflate;
		RelativeLayout relative = (RelativeLayout)inflater.inflate(R.layout.buttombar, null);
		RelativeLayout musicInfo = (RelativeLayout)relative.findViewById(R.id.name);
		OnTouchListener listener = new OnTouchListener() {
			float pY;
			float pX;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(MyService.getNowPlay() != -1){
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						pY = event.getY();
						pX = event.getX();
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						if(event.getY() >= pY + getPx(20) && (event.getX() - pX <= event.getY() - pY && pX - event.getX() <= event.getY() - pY)){
							MyService.moveToLast();
						}
						if(event.getY() <= pY - getPx(20) && (event.getX() - pX <= pY - event.getY() && pX - event.getX() <= pY - event.getY())){
							MyService.moveToNext();
						}
						update();
					}
				}
				return false;
			}
		};
		musicInfo.setOnTouchListener(listener);
		TextView title = (TextView)musicInfo.findViewById(R.id.title);
		TextView author = (TextView)musicInfo.findViewById(R.id.author);
		from = new MusicInfo(musicInfo, title, author);
		RelativeLayout musicInfo1 = (RelativeLayout)relative.findViewById(R.id.name1);
		musicInfo1.setOnTouchListener(listener);
		initParams = (RelativeLayout.LayoutParams)musicInfo1.getLayoutParams();
		TextView title1 = (TextView)musicInfo1.findViewById(R.id.title1);
		TextView author1 = (TextView)musicInfo1.findViewById(R.id.author1);
		to = new MusicInfo(musicInfo1, title1, author1);
		repeat = (ImageView)relative.findViewById(R.id.repeat);
		play = (ImageView)relative.findViewById(R.id.control);
		update();
		repeat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MyService.setFlag((MyService.getFlag() + 1) % 4);
				update();
				toastInfo();
			}
		});
		play.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int t = MyFragment.clickMenu();
				if(t == 0){
					Toast.makeText(inflater.getContext(), "尚未选择歌曲", Toast.LENGTH_SHORT).show();
				}
			}
		});
		return relative;
	}
}