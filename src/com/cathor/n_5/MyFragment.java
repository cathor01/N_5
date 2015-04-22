package com.cathor.n_5;

import java.io.IOException;
import java.util.ArrayList;

import com.cathor.n_5.R.layout;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.rtp.AudioCodec;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MyFragment extends Fragment{
	public static ArrayList<Music> array;
	public static int nowPlay = -1;
	public static MediaPlayer player;
	public static int height = 53;
	public static ImageView previous;
	class Music{
		String title;
		String author;
		String path;
		Music(String title, String author, String path){
			this.title = title;
			this.author = author;
			this.path = path;
		}
	}
	
	
	
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.scale + 0.5f);
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		System.out.println("create MediaPlayer");
		player = MainActivity.media;
		System.out.println(player == null);
		ScrollView scroll = new ScrollView(inflater.getContext());
		RelativeLayout re = new RelativeLayout(inflater.getContext());
		ListView list = new ListView(inflater.getContext());
		scroll.addView(re);
		re.addView(list);
		System.out.println("Create cursor");
		CursorLoader cl = new CursorLoader(inflater.getContext(), Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.ARTIST, Media.TITLE}, null, null, null);
		Cursor cursor = cl.loadInBackground();
		cursor.moveToFirst();
		Music music = new Music(cursor.getString(cursor.getColumnIndex(Media.TITLE)), cursor.getString(cursor.getColumnIndex(Media.ARTIST)), cursor.getString(cursor.getColumnIndex(Media.DATA)));
		array = new ArrayList<MyFragment.Music>();
		array.add(music);
		while(cursor.moveToNext()){
			music = new Music(cursor.getString(cursor.getColumnIndex(Media.TITLE)), cursor.getString(cursor.getColumnIndex(Media.ARTIST)), cursor.getString(cursor.getColumnIndex(Media.DATA)));
			array.add(music);
		}
		CursorLoader cl2 = new CursorLoader(inflater.getContext(), Media.INTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.ARTIST, Media.TITLE}, null, null, null);
		Cursor cursor2 = cl2.loadInBackground();
		cursor2.moveToFirst();
		music = new Music(cursor2.getString(cursor2.getColumnIndex(Media.TITLE)), cursor2.getString(cursor2.getColumnIndex(Media.ARTIST)), cursor2.getString(cursor2.getColumnIndex(Media.DATA)));
		array.add(music);
		while(cursor2.moveToNext()){
			music = new Music(cursor2.getString(cursor2.getColumnIndex(Media.TITLE)), cursor2.getString(cursor2.getColumnIndex(Media.ARTIST)), cursor2.getString(cursor2.getColumnIndex(Media.DATA)));
			array.add(music);
		}
		System.out.println("get " + array.size() + " music");
		Toast.makeText(inflater.getContext(), "获取了" + array.size() + "首歌曲", Toast.LENGTH_LONG).show();
		list.setClickable(false);
		list.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder") @Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.musicadapter, null);
				view.setClickable(false);
				TextView title = (TextView)view.findViewById(R.id.title);
				TextView author = (TextView)view.findViewById(R.id.author);
				ImageView button = (ImageView)view.findViewById(R.id.start_pause);
				button.setId(position + 100);
				title.setText(array.get(position).title);
				author.setText(array.get(position).author);
				button.setClickable(true);
				final LayoutParams params = button.getLayoutParams(); 
				button.setOnClickListener(new View.OnClickListener () {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ImageView bu = (ImageView)v;
						if(nowPlay == v.getId() - 100){
							if(player.isPlaying()){
								player.pause();
								bu.setImageResource(R.drawable.play);
								bu.setLayoutParams(params);
							}
							else{
								player.start();
								bu.setImageResource(R.drawable.pause);
							}
						}
						else if(nowPlay == -1){
							try {
								player.setDataSource(array.get(v.getId() - 100).path);
								player.prepare();
								player.start();
								nowPlay = v.getId() - 100;
								previous = bu;
								bu.setImageResource(R.drawable.pause);
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						else{
							try{
								if(player.isPlaying()){
									player.stop();
									previous.setImageResource(R.drawable.play);
								}
							}
							catch(IllegalStateException e){
								Toast.makeText(inflater.getContext(), "请等待", Toast.LENGTH_LONG).show();
								return;
							}
							player.reset();
							try {
								player.setDataSource(array.get(v.getId() - 100).path);
								player.prepare();
								player.start();
								bu.setImageResource(R.drawable.pause);
								previous = bu;
								nowPlay = v.getId() - 100;
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
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
				return array.get(position);
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return array.size();
			}
		});
		list.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getPx(height * array.size())));
		re.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, getPx(height * array.size())));
		return scroll;
	}
	public static void playover(){
		previous.setImageResource(R.drawable.play);
	}
}
