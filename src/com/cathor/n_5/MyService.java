package com.cathor.n_5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;

public class MyService extends Service{
	
	public final static String SET_NOW = "setnow"; 
	public final static String STOP = "stop";
	public final static String PAUSE = "pause";
	public final static String PLAY = "play"; //播放的标识
	public final static String FLAG = "flag";
	public final static String MOVE_TO_NEXT = "moveToNext";
	private static MediaPlayer player= new MediaPlayer();
	private static int flag = 0; //播放模式
	public final static String PLAY_CHANGE_RESOURCE = "chan"; //改变了Resource的播放tFlag
	public final static String PLAY_NO_CHANGE = "noc"; //未改变Resource的播放tFlag
	private static ArrayList<MyFragment.Music> array; //播放列表
	private static int nowPlay = -1; //正在播放曲目(初始为0)
	private static int length = 0; //array长度
	/**
	 * 获取正在播放曲目Id
	 * */	
	public static int getNowPlay(){
		return nowPlay;
	}
	
	private static void setNewBackGround(){
		if(MainActivity.getInstance()!= null){
			String mUriAlbums = "content://media/external/audio/albums";  
			String[] projection = new String[] { "album_art" };  
			Cursor cur = MainActivity.getInstance().getContentResolver().query(Uri.parse(mUriAlbums + "/" + array.get(nowPlay).album_id), projection, null, null, null);  
			String album_art = null;  
			if (cur.getCount() > 0 && cur.getColumnCount() > 0) {  
				cur.moveToNext();
				album_art = cur.getString(0);  
			}  
			cur.close();  
			cur = null;
			System.out.println("Image path------->" + album_art);
			if(album_art == null || album_art.equals("")){
				MainActivity.initBackGround();
			}
			else{
				MainActivity.setBackGround(album_art);
			}
		}
	}
	
	/***
	 * 设置正在播放曲目id
	 * */
	public static void setNowPlay(int newNow){
		nowPlay = newNow;
		System.out.println("nowPlay------>" + nowPlay);
	}
	/**
	 * 设置array，并更新length
	 * 
	 * */
	public static void setArray(ArrayList<MyFragment.Music> tArray){
		array = tArray;
		length = array.size();
	}
	
	/**
	 * 获取在position位置的Music类型
	 * */
	public static MyFragment.Music getItemAt(int position){
		return array.get(position);
	}
	/***
	 * 获取数组长度
	 * */
	public static int getLength(){
		return length;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		player.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				playover();
			}
		});
		System.out.println("Create the Srevice");
	}
	/**
	 * 获取当前播放状态
	 * */
	public static boolean getPlayStatewioutThrow(){
		return player.isPlaying();
	}
	
	public static void stop(){
		player.stop();
	}
	
	public static void totalStop(){
		setFlag(0);
		player.stop();
	}
	
	public static void pause(){
		player.pause();
		Controller.update();
	}
	/***
	 * 播放
	 * @param tflag 播放的模式
	 * */
	private static int play(String tflag){
		if(tflag.equals(PLAY_CHANGE_RESOURCE)){
			player.reset();
			System.out.println(getItemAt(nowPlay).path);
			try {
				player.setDataSource(getItemAt(nowPlay).path);
				player.prepare();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return 0;
			}
		}
		player.start();
		setNewBackGround();
		Controller.update();
		return 1;
	}
	/**
	 * 播放完成后调用
	 * */
	public static void playover(){
		switch(flag){
		case 0:
			if(MyFragment.previous != null){
				MyFragment.previous.setImageResource(R.drawable.play);
			}
			MainActivity.updateNoti(2);
			break;
		case 1:
			moveToNext();
			break;
		case 2:
			play(MyService.PLAY_NO_CHANGE);
			break;
		case 3:
			if(MyFragment.previous != null){
				MyFragment.previous.setImageResource(R.drawable.play);
			}
			setNowPlay(new Random().nextInt(length));
			MyService.play(MyService.PLAY_CHANGE_RESOURCE);
			MyFragment.previous = (ImageView)MyFragment.list.getChildAt(MyService.getNowPlay()).findViewById(MyService.getNowPlay() + 100);
			MyFragment.previous.setImageResource(R.drawable.pause);
			MyFragment.change = 1;
			MainActivity.updateNoti(1);
			break;
		}
		Controller.update();
	}
	/**
	 * 设置播放模式
	 * */
	public static void setFlag(int tflag){
		flag = tflag;
	}
	/**
	 * 获取播放模式
	 * */
	public static int getFlag(){
		return flag;
	}
	/***
	 * 下一曲
	 * */
	public static void moveToNext(){
		MyFragment.previous.setImageResource(R.drawable.play);
		System.out.println("nowPlay -p ------->" + MyService.getNowPlay());
		MyService.setNowPlay((MyService.getNowPlay() + 1) % length);
		System.out.println("nowPlay -l ------->" + MyService.getNowPlay());
		MyService.play(MyService.PLAY_CHANGE_RESOURCE);
		MyFragment.previous = (ImageView)MyFragment.list.getChildAt(MyService.getNowPlay()).findViewById(MyService.getNowPlay() + 100);
		MyFragment.previous.setImageResource(R.drawable.pause);
		MyFragment.change = 1;
		setNewBackGround();
		MainActivity.updateNoti(1);
	}
	
	/**
	 * 上一曲
	 * */
	public static void moveToLast(){
		MyFragment.previous.setImageResource(R.drawable.play);
		System.out.println("nowPlay -p ------->" + MyService.getNowPlay());
		MyService.setNowPlay((MyService.getNowPlay() - 1 + length) % length);
		System.out.println("nowPlay -l ------->" + MyService.getNowPlay());
		MyService.play(MyService.PLAY_CHANGE_RESOURCE);
		MyFragment.previous = (ImageView)MyFragment.list.getChildAt(MyService.getNowPlay()).findViewById(MyService.getNowPlay() + 100);
		MyFragment.previous.setImageResource(R.drawable.pause);
		MyFragment.change = 2;
		setNewBackGround();
		MainActivity.updateNoti(1);
	}
	/**
	 * 解析intent数据并执行操作
	 * */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				for(String key :bundle.keySet()){
					String value = bundle.getString(key);
					System.out.println("key------->"+key);
					System.out.println("value----->"+value);
					switch(key){
					case SET_NOW://未使用
						setNowPlay(Integer.parseInt(value));
						System.out.println("execute setNow");
						break;
					case STOP://未使用
						stop();
						System.out.println("execute stop");
						break;
					case PAUSE://未使用
						pause();
						System.out.println("execute pause");
						break;
					case PLAY:
						play(value);
						System.out.println("execute play");
						break;
					case MOVE_TO_NEXT: //未使用
						moveToNext();
						System.out.println("execute moveToNext");
						break;
					case FLAG: //未使用
						setFlag(Integer.parseInt(value));
						System.out.println("execute setFlag");
						break;
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("Destroy the Service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
