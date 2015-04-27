package com.cathor.n_5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;

public class MyService extends Service{
	
	public final static String SET_NOW = "setnow"; 
	public final static String STOP = "stop";
	public final static String PAUSE = "pause";
	public final static String PLAY = "play"; //���ŵı�ʶ
	public final static String FLAG = "flag";
	public final static String MOVE_TO_NEXT = "moveToNext";
	private static MediaPlayer player= new MediaPlayer();
	private static int flag = 0; //����ģʽ
	public final static String PLAY_CHANGE_RESOURCE = "chan"; //�ı���Resource�Ĳ���tFlag
	public final static String PLAY_NO_CHANGE = "noc"; //δ�ı�Resource�Ĳ���tFlag
	private static ArrayList<MyFragment.Music> array; //�����б�
	private static int nowPlay = -1; //���ڲ�����Ŀ(��ʼΪ0)
	private static int length = 0; //array����
	/**
	 * ��ȡ���ڲ�����ĿId
	 * */	
	public static int getNowPlay(){
		return nowPlay;
	}
	/***
	 * �������ڲ�����Ŀid
	 * */
	public static void setNowPlay(int newNow){
		nowPlay = newNow;
		System.out.println("nowPlay------>" + nowPlay);
	}
	/**
	 * ����array��������length
	 * 
	 * */
	public static void setArray(ArrayList<MyFragment.Music> tArray){
		array = tArray;
		length = array.size();
	}
	
	/**
	 * ��ȡ��positionλ�õ�Music����
	 * */
	public static MyFragment.Music getItemAt(int position){
		return array.get(position);
	}
	/***
	 * ��ȡ���鳤��
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
	 * ��ȡ��ǰ����״̬
	 * */
	public static boolean getPlayStatewioutThrow(){
		return player.isPlaying();
	}
	
	public static void stop(){
		player.stop();
	}
	
	public static void pause(){
		player.pause();
		Controller.update();
	}
	/***
	 * ����
	 * @param tflag ���ŵ�ģʽ
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
		Controller.update();
		return 1;
	}
	/**
	 * ������ɺ����
	 * */
	public static void playover(){
		switch(flag){
		case 0:
			MyFragment.previous.setImageResource(R.drawable.play);
			MainActivity.updateNoti(2);
			break;
		case 1:
			moveToNext();
			break;
		case 2:
			play(MyService.PLAY_NO_CHANGE);
			break;
		case 3:
			MyFragment.previous.setImageResource(R.drawable.play);
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
	 * ���ò���ģʽ
	 * */
	public static void setFlag(int tflag){
		flag = tflag;
	}
	/**
	 * ��ȡ����ģʽ
	 * */
	public static int getFlag(){
		return flag;
	}
	/***
	 * ��һ��
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
		MainActivity.updateNoti(1);
	}
	
	/**
	 * ��һ��
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
		MainActivity.updateNoti(1);
	}
	/**
	 * ����intent���ݲ�ִ�в���
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
					case SET_NOW://δʹ��
						setNowPlay(Integer.parseInt(value));
						System.out.println("execute setNow");
						break;
					case STOP://δʹ��
						stop();
						System.out.println("execute stop");
						break;
					case PAUSE://δʹ��
						pause();
						System.out.println("execute pause");
						break;
					case PLAY:
						play(value);
						System.out.println("execute play");
						break;
					case MOVE_TO_NEXT: //δʹ��
						moveToNext();
						System.out.println("execute moveToNext");
						break;
					case FLAG: //δʹ��
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
