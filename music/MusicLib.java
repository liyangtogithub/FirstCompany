package com.desay.iwan2.module.music;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.desay.fitband.R;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MusicLib extends Activity{
	Button complishBt;
	//CheckBox allSelectBt;
	TextView tv_title;
	private RelativeLayout relative_title;
	boolean allcheck=false;
	ListView lib_list;
	private AlertDialog.Builder  builder = null;
	private AlertDialog ad = null;
	private  MusicLibListAdapter adapter;
	private  List<Mp3Info> mp3List = null;
	private sportDB db;
	private ScanSDcardReceiver receiver;
	private String sleepArray[] = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
  		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.music_lib);
		complishBt=(Button) findViewById(R.id.complish);
	//	allSelectBt=(CheckBox) findViewById(R.id.allselect);
		relative_title = (RelativeLayout) findViewById(R.id.relative_title);
		relative_title.setOnClickListener(OnClick);
		tv_title = ((TextView) findViewById(R.id.tv_title));
		tv_title.setText(getString(R.string.music_song));
		complishBt.setOnClickListener(OnClick);
	//	allSelectBt.setOnClickListener(OnClick);
		lib_list=(ListView) findViewById(R.id.lib_list);	
		mp3List=new ArrayList<Mp3Info>();
		db=new sportDB(this);
		setListData();
		sleepArray = getResources().getStringArray(R.array.music_sleep_array);
		IntentFilter intentfilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addDataScheme("file");
		receiver = new ScanSDcardReceiver();
		registerReceiver(receiver, intentfilter);
	}

	OnClickListener OnClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.complish:
				Iterator i = adapter.getchoseMap().values().iterator();		
				List<Mp3Info> list=new ArrayList<Mp3Info>();
				list=db.GetMusic();
				if(list.size()==0){
					sendBroadcast(new Intent("com.music.finish"));
					MusicMain.isExit=false;
				}
				if(!i.hasNext()){					
					finish();
					return;
				}
				while (i.hasNext()) {
					Mp3Info o = (Mp3Info) i.next();
					//mp3List.add(o);
					HashMap<String , Object> values=new HashMap<String, Object>();
					values.put("_singer", o.getSinger());
					values.put("_time", o.getTime()+"");
					values.put("_musicName", o.getMusicName());
					values.put("_musicPath", o.getMusicPath());
					values.put("_albumKey", o.getAlbumkey());
					values.put("_musicId", o.getId()+"");
					values.put("_isself", "0");
					db.InsertMusic(values);
					MusicService.list=db.GetMusic();
				}
				if(!MusicMain.isExit){
				Intent intent=new Intent();
				intent.putExtra("music_add", true);
				intent.setClass(MusicLib.this, MusicMain.class);
				startActivity(intent);
				}
				finish();
				break;
//			case R.id.allselect:
//				adapter = new MusicLibListAdapter(MusicLib.this, mp3List,lib_list);
//				if(allcheck){	
//
//					adapter.CancelAll();					
//					allcheck=false;
//				}else{
//			
//					adapter.SelecteAll();
//					allcheck=true;
//				}
//				lib_list.setAdapter(adapter);
//				break;
			case R.id.relative_title:
				musicBack();
				break;
		}};
};
	private void musicBack() {		
				if(!MusicMain.isExit){
				Intent backIntent=new Intent(MusicLib.this,MusicMain.class);
				startActivity(backIntent);}
				finish();			
	}
		@Override
		public void onBackPressed() {
			musicBack();
		};
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			unregisterReceiver(receiver);
			super.onDestroy();
		}
		private void setListData(){
			Cursor c =null;
		try {
			c=this.getContentResolver()
			.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.ALBUM_KEY},
					null, null, null);
		    if (c==null || c.getCount()==0){
		    	builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.nomusic).setPositiveButton(R.string.sure_str, null);
				ad = builder.create();
				ad.show();
		    }
		    c.moveToFirst();		    
		    
		    for(int i=0;i<c.getCount();i++){
		    	Mp3Info m=new Mp3Info();
		    	m.setId(c.getInt(3));
		    	m.setMusicName(c.getString(4));
		    	m.setSinger(c.getString(2));
		    	m.setTime(c.getInt(1));
		    	m.setMusicPath(c.getString(5));
		    	m.setAlbumkey(c.getString(6));
		    	mp3List.add(m);
		    	c.moveToNext();	
		    }
		    adapter = new MusicLibListAdapter(this, mp3List,sleepArray);
		    adapter.notifyDataSetChanged();
		    lib_list.setAdapter(adapter);
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			   c.close();
		}
			
		}
		
		/*@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			menu.add(0, 1, 0, R.string.scan_music);
			return true;
		}*/
		/*@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			scan();
			return super.onOptionsItemSelected(item);
		}*/
		public void scan() {
			
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory().getAbsolutePath())));
		}
		class ScanSDcardReceiver extends BroadcastReceiver {
			private AlertDialog.Builder builder = null;
			private AlertDialog ad = null;

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
					builder = new AlertDialog.Builder(context);
					builder.setMessage(R.string.scanning);
					ad = builder.create();
					ad.show();
				} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
					mp3List.clear();
					setListData();
					ad.cancel();
                    ToastUtil.shortShow(context, getString(R.string.scanned));
				}
			}
		}	

}
