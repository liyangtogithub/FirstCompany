package com.desay.iwan2.module.music;

import java.sql.SQLException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.server.SetServer;
import com.desay.iwan2.module.MainActivity;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

public class MusicSet extends TemplateActivity
{
	RelativeLayout relative_title;
	TextView tv_title,tv_auto_str1,tv_auto_str3;
	CheckBox cbox_music = null;
	ListView listView = null;
	String[] dataArray = null;
	MusicSetAdapter adapter = null;
	SetServer setServer = null;
	String musicSetString = null;
	String  musicTimetringOld = "";
	int musicTimeIndex = 0;
	String musicEnableString ="0";
	String musicEnableStringOld ="0";
	
	@Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);
		setContentView(R.layout.music_set);
		initServer();
		initView();
		listListener();
	}

	private void initServer()
	{
		try
		{
		   setServer = new SetServer(this);
		   musicSetString = setServer.getMusicSet();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void initView(){
		relative_title = (RelativeLayout) findViewById(R.id.relative_title);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getString(R.string.music_set));
		relative_title.setOnClickListener(OnClick);
		listView = (ListView) findViewById(R.id.set_list);
		cbox_music = (CheckBox) findViewById(R.id.cbox_music);
		tv_auto_str1 = (TextView) findViewById(R.id.tv_auto_str1);
		tv_auto_str3 = (TextView) findViewById(R.id.tv_auto_str3);
		
		if ( musicSetString!=null ){
			if ( musicSetString.indexOf(",")==-1 ){
				musicEnableStringOld = musicEnableString = musicSetString.substring(0, 1);
				if ("1".equals(musicEnableString)){
					cbox_music.setChecked(true);
					tv_auto_str1.setText(getString(R.string.music_auto_str2));
				}
				String musicTimetring = musicSetString.substring(1, musicSetString.length());
				musicTimetringOld = musicTimetring;
				musicTimeIndex = getMusicTimeIndex(musicTimetring);
			}else {
				musicEnableStringOld = musicEnableString = musicSetString.substring(0, 2);
				if ("01".equals(musicEnableString)){
					cbox_music.setChecked(true);
					tv_auto_str1.setText(getString(R.string.music_auto_str2));
				}
				String musicTimetring = musicSetString.substring(3, musicSetString.length());
				musicTimetringOld = musicTimetring;
				musicTimeIndex = getMusicTimeIndex(musicTimetring);
			}
		}
		
		dataArray = getResources().getStringArray(R.array.music_set_array);
		adapter = new MusicSetAdapter(this,dataArray);
		adapter.setMusicIndex(musicTimeIndex);
		listView.setAdapter(adapter);
		tv_auto_str3.setText(getString(R.string.music_auto_str3,
				             getMusicTimeString( musicTimeIndex )));
		cbox_music.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if (isChecked)
					tv_auto_str1.setText(getString(R.string.music_auto_str2));
				else {
					tv_auto_str1.setText(getString(R.string.music_auto_str1));
				}
			}
		});
	}

	private int  getMusicTimeIndex(String musicTimetring)
	{
		int TimeIndex = Integer.parseInt(musicTimetring);
		if (TimeIndex==30){
			TimeIndex = 0;
		} else if (TimeIndex==60) {
			TimeIndex = 1;
		}else if (TimeIndex==90) {
			TimeIndex = 2;
		}else if (TimeIndex==120) {
			TimeIndex = 3;
		}else {
			TimeIndex = 0;
		}
		return TimeIndex;
	}

	private void listListener()
	{
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
					adapter.setMusicIndex(arg2);
					adapter.notifyDataSetChanged();
					listView.setAdapter(adapter);
					musicTimeIndex = arg2;
					tv_auto_str3.setText(getString(R.string.music_auto_str3,
							             getMusicTimeString( musicTimeIndex )));
			}
		});
	}

	OnClickListener OnClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.relative_title:
					back();
					break;
			}
		}
	};
	
	protected void onPause1() throws Throwable
	{
		musicEnableString = cbox_music.isChecked()?"01":"00";
		String musicTimeString = getMusicTimeString( musicTimeIndex );
		if (!musicEnableString.equals(musicEnableStringOld) || !musicTimeString.equals( musicTimetringOld)){
			setServer.storeMusic(musicEnableString+","+musicTimeString, false);
			ToastUtil.shortShow(this, getString(R.string.save_success));
		}
		
	};
	
	private String  getMusicTimeString(int TimeIndex)
	{
		String musicTimeString = "30";
		if (TimeIndex==0)
		{
			 musicTimeString = "30";
		} else if (TimeIndex==1) {
			 musicTimeString = "60";
		}else if (TimeIndex==2) {
			 musicTimeString = "90";
		}else if (TimeIndex==3) {
			 musicTimeString = "120";
		}
		return musicTimeString;
	}

	public static void gotoActivity(Context packageContext)
	{
		Intent intent = new Intent(packageContext, MusicSet.class);
		packageContext.startActivity(intent);
	}
}
