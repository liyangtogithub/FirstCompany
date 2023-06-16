package com.desay.iwan2.module.music;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.desay.fitband.R;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MusicMain extends Activity
{
	TextView tv_title,list_title;
	private RelativeLayout relative_title,relative_list_title;
	Context context;
	ListView lv_list;
	public MusicListAdapter adapter;
	private static List<Mp3Info> alllist;
	private Set<Integer> setCheck = new HashSet<Integer>();
	public static int position;
	public static boolean isExit = false;
//	private static boolean isbegin = true;
	private TextView tv_musicname,durationTime,playtime=null;
	ImageView music_set;
	private ImageButton ib_premusic, ib_playmusic, ib_nextmusic;
	private TextView list_tv_add = null;
	private TextView list_tv_remove = null;
	private  View music_list_ui = null;
	private SeekBar seekbar=null;
	private int currentPosition;
	public static final String MUSIC_CURRENT = "com.fitband.currentTime";
	private static final String MUSIC_NEXT = "com.alex.next";
	public static final String MUSIC_EXIST = "com.music.exist";
	private static final String MUSIC_FINISH = "com.music.finish";
	public static final String MUSIC_PLAY = "com.music.play";
	public static final String MUSIC_UPDATE_UI = "com.music.update_ui";
	public static final int MUSIC_BEGIN = 1;
	public static final int MUSIC_PAUSE = 2;
	private static final int MUSIC_STOP = 3;
	//private static final int PROGRESS_CHANGE = 4;
	String sleepArray[] = null;
    int musicTimeArray[] = {163,252,64,10};
	private sportDB db;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		context = MusicMain.this;
		db = new sportDB(this);
		isExit = true;
		position = MusicService.position;
		if (MusicService.position<10)
			position = R.raw.sleep1;
		lv_list = (ListView) findViewById(R.id.lv_list);
		alllist = db.GetMusic();
		ib_premusic = (ImageButton) findViewById(R.id.ib_premusic);
		ib_playmusic = (ImageButton) findViewById(R.id.ib_playmusic);
		ib_nextmusic = (ImageButton) findViewById(R.id.ib_nextmusic);
		tv_musicname = (TextView) findViewById(R.id.tv_musicname);
		playtime=(TextView)findViewById(R.id.playtime);
		durationTime=(TextView)findViewById(R.id.duration);
		music_set = (ImageView) findViewById(R.id.iv_music_set);
		
		relative_title =  (RelativeLayout )findViewById(R.id.relative_title) ;
		relative_title.setOnClickListener(OnClick) ;
		relative_list_title =  (RelativeLayout )findViewById(R.id.relative_list_title) ;
		relative_list_title.setOnClickListener(OnClick) ;
		tv_title =  (TextView)findViewById(R.id.tv_title) ;
		tv_title.setText(getString(R.string.music_title));
		list_title =  (TextView)findViewById(R.id.list_title) ;
		list_title.setText(getString(R.string.music_list));
		( (ImageView)findViewById(R.id.ib_music_list) ).setOnClickListener(OnClick);	
		music_list_ui = (View)findViewById(R.id.music_list_ui) ;
		Intent intent = getIntent();
		if (intent.getBooleanExtra("music_add", false))
			music_list_ui.setVisibility(View.VISIBLE);		
		list_tv_add = (TextView)findViewById(R.id.list_tv_add) ;
		list_tv_remove = (TextView)findViewById(R.id.list_tv_remove) ;
		sleepArray = getResources().getStringArray(R.array.music_sleep_array);
		seekbar  = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event){
				return true;//unable touch
			}
		});
				
		ib_premusic.setOnClickListener(OnClick);
		ib_playmusic.setOnClickListener(OnClick);
		ib_nextmusic.setOnClickListener(OnClick);
		music_set.setOnClickListener(OnClick);
		list_tv_remove.setOnClickListener(OnClick);
		list_tv_add.setOnClickListener(OnClick);
		if (MusicService.isPlaying)
		{
			ib_playmusic.setBackgroundResource(R.drawable.music_selector_pause);
		}
		else
		{
			ib_playmusic.setBackgroundResource(R.drawable.music_selector_play);
		}

		
		lv_list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				if (getString(R.string.music_add).equals(list_tv_add.getText()))
				{
					music_list_ui.setVisibility(View.GONE);
					if (MusicService.isReleased)
					{
						position = arg2;
						loadClip();
						begin();
					}
					else
					{
						if (arg2 == position)
						{
							play();
						}
						else
						{
							stop();
							position = arg2;
							loadClip();
							begin();
						}
					}
				}
				else if ( getString(R.string.music_cancle).equals(list_tv_add.getText()) ) 
				{
					ImageView music_iv_check =(ImageView)arg1.findViewById(R.id.music_iv_check);
					ImageView music_iv_nocheck =(ImageView)arg1.findViewById(R.id.music_iv_nocheck);
					
					if (music_iv_check.getVisibility() == View.VISIBLE)
					{
						setCheck.remove(arg2);
						music_iv_check.setVisibility(View.INVISIBLE);
						music_iv_nocheck.setVisibility(View.VISIBLE);
					}else {
						setCheck.add(arg2);
						music_iv_nocheck.setVisibility(View.INVISIBLE);
						music_iv_check.setVisibility(View.VISIBLE);
					}
					System.out.println("setCheck.size() = "+setCheck.size());
					
				}
				
			}
		});

		
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		context = MusicMain.this;
		alllist = db.GetMusic();
		adapter = new MusicListAdapter(MusicMain.this, sleepArray);
		lv_list.setAdapter(adapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_NEXT);
		filter.addAction(MUSIC_EXIST);
		filter.addAction(MUSIC_FINISH);
		filter.addAction(MUSIC_PLAY);
		filter.addAction(MUSIC_UPDATE_UI);
		registerReceiver(musicReceiver, filter);
		loadClip();
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(musicReceiver);
		super.onDestroy();
	};

	OnClickListener OnClick = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.relative_title:
					isExit = false;
					finish();
					break;
				case R.id.iv_music_set:
					MusicSet.gotoActivity(MusicMain.this);
					break;
				case R.id.ib_premusic:
					latestOne();
					break;

				case R.id.ib_playmusic:
					if (alllist.size() != 0)
					{
						if (MusicService.isReleased)
						{
							position = 0;
							loadClip();
							begin();
						}
						else
						{
							play();
						}
					}
					break;

				case R.id.ib_nextmusic:
					nextOne();
					break;
				case R.id.ib_music_list:
					music_list_ui.setVisibility(v.VISIBLE);
					list_title.setText(getString(R.string.music_list));
					list_tv_add.setText(getString(R.string.music_add));
					break;
				case R.id.relative_list_title:
					music_list_ui.setVisibility(v.GONE);
					break;
				case R.id.list_tv_add:
					if (getString(R.string.music_add).equals(list_tv_add.getText()))
					{
						Intent intent1 = new Intent();
						intent1.setClass(context, MusicLib.class);
						startActivity(intent1);
					}
					else if ( getString(R.string.music_cancle).equals(list_tv_add.getText()) ) {
						//lv_list.setAdapter(new MusicListAdapter(context, alllist));
						list_tv_add.setText(getString(R.string.music_add));
						list_title.setText(getString(R.string.music_list));
						setCheck.clear();
					}
					break;
				case R.id.list_tv_remove:
					if (getString(R.string.music_add).equals(list_tv_add.getText()))
					{
					list_tv_add.setText(getString(R.string.music_cancle));
					list_title.setText(getString(R.string.music_remove_title));
					}else
					{
						if (setCheck.size() == 0)
						{
							ToastUtil.shortShow(context, getString(R.string.delete__choice_title));
							break;
						}
						showRemoveDialog();
					}
					break;
			}
		}
	};

	private void showRemoveDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MusicMain.this);
		builder.setMessage(R.string.delete__check_title)
				.setPositiveButton(R.string.sure_str,
						new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								Iterator it = setCheck.iterator();
								db.openOrCreateDB();
								  while(it.hasNext())
								  {
								   Mp3Info m = alllist.get(Integer.parseInt(String.valueOf(it.next()) ));
								   db.DelMusic(m.getId());
								  }
								  db.closeDB();
								  setCheck.clear();
								  alllist  = db.GetMusic();
								MusicService.list = db.GetMusic();
								if (alllist.size() == 0)
								{
									tv_musicname.setText("");
									sendBroadcast(new Intent(
											MusicService.NOTIFICATION_EXIT));
								}
							//	MusicListAdapter adapter = new MusicListAdapter(
							//			MusicMain.this, alllist);
							//	
								adapter.notifyDataSetChanged();
								lv_list.setAdapter(adapter);
							}
						}).setNegativeButton(R.string.music_cancle, null)
				.show();
	}
	
	@Override
	public void onBackPressed()
	{
		if (music_list_ui.getVisibility() == View.VISIBLE )
		{
			music_list_ui.setVisibility(View.GONE);
		} else
		{
			isExit = false;
			finish();
		}
	};

	public void loadClip(){
		if (alllist.size() != 0){
			if (position==0 || position== R.raw.sleep1){
				initMusicUi(0);
			}
			else if (position==1 || position== R.raw.sleep2) {
				initMusicUi(1);
			}
			else if (position==2 || position== R.raw.sleep3) {
				initMusicUi(2);
			}
			else if (position==3 || position== R.raw.sleep4) {
				initMusicUi(3);
			}
			else {
				initMusicUi(0);
			}
			playtime.setText(toTime(0));
		}
	}

	private void initMusicUi(int musicId){
		tv_musicname.setText(sleepArray[musicId]);
		seekbar.setMax(musicTimeArray[musicId]);
		durationTime.setText(toTime(musicTimeArray[musicId]));
	}

	private void begin()
	{
		//isbegin = true;
		MusicService.isPlaying = true;
		ib_playmusic.setBackgroundResource(R.drawable.music_selector_pause);
		Intent intent = new Intent();
		intent.setClass(this, MusicService.class);
		intent.putExtra("op", MUSIC_BEGIN);
		intent.putExtra("position", position);
		startService(intent);
		//adapter = new MusicListAdapter(context, alllist);
		//adapter.setItemIcon(position);
		//adapter.notifyDataSetChanged();
		//lv_list.setAdapter(adapter);
		//lv_list.setSelection(position);
		//isbegin = false;
	}

	public void play()
	{
		if (MusicService.isPlaying)
		{
			ib_playmusic.setBackgroundResource(R.drawable.music_selector_play);
		}
		else
		{
			ib_playmusic.setBackgroundResource(R.drawable.music_selector_pause);
		}
		Intent intent = new Intent();
		intent.setClass(this, MusicService.class);
		intent.putExtra("position", position);
		intent.putExtra("op", MUSIC_PAUSE);
		startService(intent);
	}

	private void stop()
	{
		Intent intent = new Intent();
		intent.setClass(this, MusicService.class);
		intent.putExtra("op", MUSIC_STOP);
		startService(intent);
	}

	public void latestOne()
	{
		//stop();
		if (position == R.raw.sleep1)
		{
			position = R.raw.sleep4;
		}
		else if (position > 0)
		{
			position=position-2;
		}
		LogUtil.i("44444444444  position=="+position);
		loadClip();
		begin();
	}

	/**
	 * 下一首
	 */
	public void nextOne()
	{
		//stop();
		if (position == R.raw.sleep4)
		{
			position = R.raw.sleep1;
		}
		else
		{
			position=position+2;
		}
		LogUtil.i("44444444444  position=="+position);
		loadClip();
		begin();
	}

	protected BroadcastReceiver musicReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT))
			{
				currentPosition = intent.getExtras().getInt("currentTime");
				playtime.setText(toTime(currentPosition));
				seekbar.setProgress(currentPosition);
			}

			if (MUSIC_NEXT.equals(action))
			{
				nextOne();
			}
			if (MUSIC_EXIST.equals(action))
			{
				loadClip();
			}
			if (MUSIC_PLAY.equals(action))
			{
				if (MusicService.isReleased)
				{
					position = 0;
					loadClip();
					begin();
				}
				else
				{
					play();
				}
			}
			if (MUSIC_FINISH.equals(action))
			{
				finish();
				isExit = false;
			}
			if (MUSIC_UPDATE_UI.equals(action))
			{
				position = MusicService.position;
				loadClip();
				if (MusicService.isPlaying)
				{
					ib_playmusic.setBackgroundResource(R.drawable.music_selector_pause);
				}
				else
				{
					ib_playmusic.setBackgroundResource(R.drawable.music_selector_play);
				}
			}
		}
	};

	public String toTime(int time){
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

}
