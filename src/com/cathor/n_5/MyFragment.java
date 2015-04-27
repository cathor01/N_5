package com.cathor.n_5;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore.Audio.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MyFragment extends Fragment{
	public static int height = 55;
	public static ImageView previous;
	private static SQLiteDatabase db;
	private static LayoutInflater inflater;
	private static BaseAdapter adapter;
	public static ListView list;
	public static int change = 0;
	private static Intent intent;
	/**
	 * 内部类，包含title，author，path
	 * */
	static class Music{
		String title;
		String author;
		String path;
		int album_id;
		Music(String title, String author, String path){
			this.title = title;
			this.author = author;
			this.path = path;
		}
		Music(String title, String author, String path, int album){
			this.title = title;
			this.author = author;
			this.path = path;
			this.album_id = album;
		}
	}
	
	/***
	 * 向Service发送具体的Message，现状态只可发送播放请求
	 * @param name 请求的名字
	 * @param value 具体值
	 * 
	 * */
	
	private static void handleMeg(String name, String value){
		intent = new Intent(inflater.getContext(), MyService.class);
		intent.putExtra(name, value);
		inflater.getContext().startService(intent);
	}
	
	/**
	 * 将dp转化为px
	 * 
	 * */
	
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.scale + 0.5f);
	}
	
	private ArrayList<MyFragment.Music> loadFromDatabase(SQLiteDatabase db){
		ArrayList<MyFragment.Music> array = new ArrayList<MyFragment.Music>();
		Cursor cursor = db.query("music_info", new String[]{"title", "author", "path", "album_id"}, null, null, null, null, null);
		cursor.moveToFirst();
		Music music = new Music(cursor.getString(cursor.getColumnIndex("title")), cursor.getString(cursor.getColumnIndex("author")), cursor.getString(cursor.getColumnIndex("path")), cursor.getInt(cursor.getColumnIndex("album_id")));
		array.add(music);
		while(cursor.moveToNext()){
			music = new Music(cursor.getString(cursor.getColumnIndex("title")), cursor.getString(cursor.getColumnIndex("author")), cursor.getString(cursor.getColumnIndex("path")), cursor.getInt(cursor.getColumnIndex("album_id")));
			array.add(music);
		}
		return array;
	}
	
	/**
	 * 调用系统数据库获取Audio(Slow)，并将获取到的数据存储到软件的数据库中
	 * @param array 需要写入的ArrayList<Music>
	 * @param inflater 调用的inflater
	 * @param kb 过滤多少kb以下的内容(用户控制过滤尚未实现，接下来会做)
	 * @param db 写入的SQLiteDatabase
	 * @return 写入数据后的array
	 * */
	
	private static ArrayList<MyFragment.Music> loadFromSystem(ArrayList<MyFragment.Music> array, LayoutInflater inflate, int kb, SQLiteDatabase db) throws FileNotFoundException{
		inflater = inflate;
		try{
			db.execSQL("create table music_info(_id integer primary key autoincrement, title, author, path, album_id integer)");
		}
		catch(SQLiteException e){
			db.execSQL("drop table music_info");
			db.execSQL("create table music_info(_id integer primary key autoincrement, title, author, path, album_id integer)");
		}
		CursorLoader cl = new CursorLoader(inflater.getContext(), Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.ARTIST, Media.TITLE, Media.ALBUM_ID}, null, null, null);
		Cursor cursor = cl.loadInBackground();
		cursor.moveToFirst();
		Music music = new Music(cursor.getString(cursor.getColumnIndex(Media.TITLE)), cursor.getString(cursor.getColumnIndex(Media.ARTIST)), cursor.getString(cursor.getColumnIndex(Media.DATA)),  cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
		ContentValues content;
		if(kb != 0){
			File in = new File(music.path);
			if(in.length() >= kb * 1024L * 8){
				array.add(music);
				content = new ContentValues();
				content.put("title", music.title);
				content.put("author", music.author);
				content.put("path", music.path);
				content.put("album_id", music.album_id);
				db.insert("music_info", null, content);
			}
			while(cursor.moveToNext()){
				music = new Music(cursor.getString(cursor.getColumnIndex(Media.TITLE)), cursor.getString(cursor.getColumnIndex(Media.ARTIST)), cursor.getString(cursor.getColumnIndex(Media.DATA)),  cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
				in = new File(music.path);
				if(in.length() >= kb * 1024L * 8){
					array.add(music);
					content = new ContentValues();
					content.put("title", music.title);
					content.put("author", music.author);
					content.put("path", music.path);
					content.put("album_id", music.album_id);
					db.insert("music_info", null, content);
				}
			}
		}
		else{
			array.add(music);
			content = new ContentValues();
			content.put("title", music.title);
			content.put("author", music.author);
			content.put("path", music.path);
			content.put("album_id", music.album_id);
			db.insert("music_info", null, content);
			while(cursor.moveToNext()){
				music = new Music(cursor.getString(cursor.getColumnIndex(Media.TITLE)), cursor.getString(cursor.getColumnIndex(Media.ARTIST)), cursor.getString(cursor.getColumnIndex(Media.DATA)), cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
				array.add(music);
				content = new ContentValues();
				content.put("title", music.title);
				content.put("author", music.author);
				content.put("path", music.path);
				content.put("album_id", music.album_id);
				db.insert("music_info", null, content);
			}
		}
		cursor.close();
		CursorLoader cl2 = new CursorLoader(inflater.getContext(), Media.INTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.ARTIST, Media.TITLE, Media.ALBUM_ID}, null, null, null);
		Cursor cursor2 = cl2.loadInBackground();
		cursor2.moveToFirst();
		music = new Music(cursor2.getString(cursor2.getColumnIndex(Media.TITLE)), cursor2.getString(cursor2.getColumnIndex(Media.ARTIST)), cursor2.getString(cursor2.getColumnIndex(Media.DATA)), cursor2.getInt(cursor2.getColumnIndex(Media.ALBUM_ID)));
		if(kb != 0){
			File in = new File(music.path);
			if(in.length() >= kb * 1024L * 8){
				array.add(music);
				content = new ContentValues();
				content.put("title", music.title);
				content.put("author", music.author);
				content.put("path", music.path);
				content.put("album_id", music.album_id);
				db.insert("music_info", null, content);
			}
			while(cursor2.moveToNext()){
				music = new Music(cursor2.getString(cursor2.getColumnIndex(Media.TITLE)), cursor2.getString(cursor2.getColumnIndex(Media.ARTIST)), cursor2.getString(cursor2.getColumnIndex(Media.DATA)), cursor2.getInt(cursor2.getColumnIndex(Media.ALBUM_ID)));
				in = new File(music.path);
				if(in.length() >= kb * 1024L * 8){
					array.add(music);
					content = new ContentValues();
					content.put("title", music.title);
					content.put("author", music.author);
					content.put("path", music.path);
					content.put("album_id", music.album_id);
					db.insert("music_info", null, content);
				}
			}
		}
		else{
			array.add(music);
			content = new ContentValues();
			content.put("title", music.title);
			content.put("author", music.author);
			content.put("path", music.path);
			content.put("album_id", music.album_id);
			db.insert("music_info", null, content);
			while(cursor2.moveToNext()){
				music = new Music(cursor2.getString(cursor2.getColumnIndex(Media.TITLE)), cursor2.getString(cursor2.getColumnIndex(Media.ARTIST)), cursor2.getString(cursor2.getColumnIndex(Media.DATA)), cursor2.getInt(cursor2.getColumnIndex(Media.ALBUM_ID)));
				array.add(music);
				content = new ContentValues();
				content.put("title", music.title);
				content.put("author", music.author);
				content.put("path", music.path);
				content.put("album_id", music.album_id);
				db.insert("music_info", null, content);
			}
		}
		cursor2.close();
		return array;
	}
	
	/**
	 * 将数据库文件内容清空
	 * */
	
	public static void dropData(){
		File f = new File(inflater.getContext().getFilesDir() + "/musicdata/data.db3");
		if(f.exists()){
			f.delete();
		}
		db = null;
		System.out.println("File--------->" + f.exists());
	}
	
	@SuppressLint("SdCardPath") @Override
	public View onCreateView(final LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		inflater = inflate;
		// TODO Auto-generated method stub
		System.out.println("create MediaPlayer");
		ScrollView scroll = new ScrollView(inflater.getContext());
		RelativeLayout re = new RelativeLayout(inflater.getContext());
		list = new ListView(inflater.getContext());
		scroll.addView(re);
		re.addView(list);
		System.out.println("Create cursor");
		ArrayList<Music> array = new ArrayList<MyFragment.Music>();
		try{
			if(db==null){db = SQLiteDatabase.openDatabase(inflater.getContext().getFilesDir() + "/musicdata/data.db3",  null, SQLiteDatabase.OPEN_READWRITE);}
			array = loadFromDatabase(db);
		}
		catch(SQLiteCantOpenDatabaseException e){
			try {
				File f = new File(inflater.getContext().getFilesDir() + "/musicdata/");
				if(!f.exists()){
					f.mkdirs();
					System.out.println("directory--------->" + f.isDirectory());
					System.out.println("result--------->" + f.exists());
				}
				db = SQLiteDatabase.openOrCreateDatabase(f.getPath() + "/data.db3", null);
				array = loadFromSystem(array, inflater, 500, db);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(array.size() == 0){
			View v = inflater.inflate(R.layout.nodata, null);
			return v;
		}
		MyService.setArray(array);
		System.out.println("get " + MyService.getLength() + " music");
		Toast.makeText(inflater.getContext(), "获取了" + array.size() + "首歌曲", Toast.LENGTH_LONG).show();
		list.setClickable(false);
		adapter = new BaseAdapter() {
			
			@SuppressLint("ViewHolder") @Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.musicadapter, null);
				view.setClickable(false);
				view.setId(position + 10000);
				TextView title = (TextView)view.findViewById(R.id.title);
				TextView author = (TextView)view.findViewById(R.id.author);
				ImageView button = (ImageView)view.findViewById(R.id.start_pause);
				button.setId(position + 100);
				title.setText(MyService.getItemAt(position).title);
				author.setText(MyService.getItemAt(position).author);
				button.setClickable(true);
				final LayoutParams params = button.getLayoutParams(); 
				button.setOnClickListener(new View.OnClickListener () {//设置每个列表后面的播放功能实现
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ImageView bu = (ImageView)v;
						if(MyService.getNowPlay() == v.getId() - 100){
							if(MyService.getPlayStatewioutThrow()){
								MyService.pause();
								bu.setImageResource(R.drawable.play);
								bu.setLayoutParams(params);
								MainActivity.updateNoti(2);
							}
							else{
								handleMeg(MyService.PLAY,MyService.PLAY_NO_CHANGE);
								bu.setImageResource(R.drawable.pause);
							}
						}
						else if(MyService.getNowPlay() == -1){
							MyService.setNowPlay(v.getId() - 100);
							handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
							previous = bu;
							bu.setImageResource(R.drawable.pause);
							change = 1;
						}
						else{
							try{
								if(MyService.getPlayStatewioutThrow()){
									MyService.stop();
									previous.setImageResource(R.drawable.play);
									MainActivity.updateNoti(2);
								}
							}
							catch(IllegalStateException e){
								Toast.makeText(inflater.getContext(), "请等待", Toast.LENGTH_LONG).show();
								return;
							}
							
							MyService.setNowPlay(v.getId() - 100);
							handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
							bu.setImageResource(R.drawable.pause);
							previous = bu;
							change = 1;
							
						}
					}
				});
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return MyService.getItemAt(position);
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return MyService.getLength();
			}
		};
		list.setAdapter(adapter);
		list.setDividerHeight(getPx(1));
		list.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getPx(height * array.size())));
		re.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, getPx(height)* array.size()));
		return scroll;
	}
	
	/***
	 * 暂停当前音乐(不管是否正在播放)
	 * 
	 * */	
	public static void pauseMusic(){
		if(MyService.getNowPlay() != -1){
			try{
				if(MyService.getPlayStatewioutThrow()){
					System.out.println("path------->1");
					MyService.pause();
					previous.setImageResource(R.drawable.play);
					MainActivity.updateNoti(2);
					Controller.update();
				}
			}
			catch(IllegalStateException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Controller按钮点击响应
	 * 
	 * */
	
	public static int clickMenu(){
		try{
			if(MyService.getPlayStatewioutThrow()){
				System.out.println("path------->1");
				MyService.pause();
				previous.setImageResource(R.drawable.play);
				MainActivity.updateNoti(2);
				return 2;
			}
			else{
				System.out.println("path------->2");
				if(previous != null){
					handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
					previous.setImageResource(R.drawable.pause);
					return 1;
				}else{
					return 0;
				}
			}
		}
		catch(IllegalStateException e){
			if(MyService.getNowPlay() != -1){
				System.out.println("path------->3");
				handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
				previous.setImageResource(R.drawable.pause);
				return 1;
			}
			System.out.println("path------->4");
			return 0;
		}
	}
}
