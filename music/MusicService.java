package com.desay.iwan2.module.music;

import java.util.ArrayList;
import java.util.List;
import com.desay.fitband.R;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.module.userinfo.LoginFragment;
import dolphin.tools.util.LogUtil;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;

public class MusicService extends Service{

	public static final String RECEIVER_EXITSPORT  = "com.desay.iwan2.exitsport";
	private static final int MUSIC_BEGIN = 1;
	private static final int MUSIC_PAUSE = 2;
	private static final int MUSIC_STOP = 3;
	private static final String NOTIFICATION_PRE="com.iwan2.notification.pre";
	private static final String NOTIFICATION_NEXT="com.iwan2.notification.next";
	private static final String NOTIFICATION_PLAY="com.iwan2.notification.play";
	private static final String NOTIFICATION_STOP="com.iwan2.notification.stop";
	public static final String NOTIFICATION_EXIT="com.iwan2.notification.exit";
	public static final String MUSIC_GOON="com.iwan2.notification.goon";
	private static  NotificationManager notificationManager;
	public static List<Mp3Info> list=new ArrayList<Mp3Info>();
	private MediaPlayer mp = null;
	public static int MusicId;
	int progress;
	public static boolean isReleased = true;
	public static  boolean isPlaying=false;
	public static boolean  isBegin=false;
	private sportDB db;
	private  int currentPosition;
	public static int position=0;
	public static final String MUSIC_MODEL="music_model";
	public static final int MUSIC_MODEL_SELF=0;
	public static final int MUSIC_MODEL_EXTERNAL=1;
	private int musicModel = MUSIC_MODEL_EXTERNAL;
	Context context =null;
	private Handler handler=null;
	private static final int CURRENTTIME = 1;
	
	@Override
	public void onCreate() {
		super.onCreate();
		db=new sportDB(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PHONE_STATE");
		filter.addAction(NOTIFICATION_NEXT);
		filter.addAction(NOTIFICATION_PRE);
		filter.addAction(NOTIFICATION_PLAY);
		filter.addAction(NOTIFICATION_STOP);
		filter.addAction(NOTIFICATION_EXIT);
		filter.addAction(MUSIC_GOON);
		filter.addAction(RECEIVER_EXITSPORT);
		registerReceiver(InComingSMSReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mp!=null){
			mp.stop();
			mp = null;
		}
		isPlaying=false;
		isReleased = true;
		position = 0;
		if (InComingSMSReceiver!=null)
		unregisterReceiver(InComingSMSReceiver);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = MusicService.this;
		try {
			musicModel=intent.getIntExtra(MUSIC_MODEL, MUSIC_MODEL_EXTERNAL);
			if (musicModel == MUSIC_MODEL_EXTERNAL)
			list=db.GetMusic();
			
			position=intent.getIntExtra("position", 0);
			int flag=intent.getIntExtra("op", MUSIC_PAUSE);
			LogUtil.i("11111111111111 onStartCommand  position=="+position);
			LogUtil.i("11111111111111 onStartCommand  op=="+flag);
			switch(flag){
			case MUSIC_BEGIN:
				begin();
				break;
			case MUSIC_PAUSE:
				pause();
				break;
			case MUSIC_STOP:
				stop();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void begin(){		
		    LogUtil.i("11111111111111 musicModel  =="+musicModel);
			if (musicModel == MUSIC_MODEL_SELF )
			{
				stop();
				mp=MediaPlayer.create(this, position);
				mp.setLooping(true);
			}else {
				stop();
				LogUtil.i("11111111111111 Mp3Info position  =="+position);
				if (position<10){
					Mp3Info m=list.get(position);
					String urlString=m.getMusicPath();
					int selfId = urlString.indexOf(LoginFragment.MUSIC_SELF);
					LogUtil.i("11111111111111 selfId  =="+selfId);
					if (selfId!=-1){
						position = Integer.parseInt(urlString.substring(selfId + LoginFragment.MUSIC_SELF.length()));
						selectMusic();
					}
				}
				else {
					selectMusic();
				}
				mp.setLooping(true);
			}
		LogUtil.i("11111111111111 mp.start();  ");
		mp.start();
		init();
		setUp();
		context.sendBroadcast(new Intent(MusicMain.MUSIC_UPDATE_UI));
		isBegin=true;
		isPlaying=true;
		isReleased=false;
	}
	
	private void init(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case CURRENTTIME:
					try {
						Intent intent = new Intent(MusicMain.MUSIC_CURRENT);
						currentPosition=mp.getCurrentPosition();
						intent.putExtra("currentTime", (currentPosition/1000));
						sendBroadcast(intent);
						handler.sendEmptyMessageDelayed(CURRENTTIME, 1000);
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
					}
				super.handleMessage(msg);						
				}
		};
}
	
	private void setUp(){
		mp.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				handler.sendEmptyMessage(CURRENTTIME);
			}
		});
	}
	
	private void selectMusic()
	{
		mp=MediaPlayer.create(this, position);
		LogUtil.i("11111111111111 position  =="+position);
		switch (position)
		{
			case R.raw.sleep1:
				BandManager.selectMixedMusic(context,R.raw.sleep1_bo);
				break;
            case R.raw.sleep2:
            	BandManager.selectMixedMusic(context,R.raw.sleep2_bo);
				break;
            case R.raw.sleep3:
            	BandManager.selectMixedMusic(context,R.raw.sleep3_bo);
				break;
            case R.raw.sleep4:
            	BandManager.selectMixedMusic(context,R.raw.sleep4_bo);
	 			break;
	    }
	}

	private void pause(){
		LogUtil.i("pause() isPlaying=="+MusicService.isPlaying);
		if (isPlaying){
			if (BandManager.mp!=null)
			BandManager.mp.pause();
			mp.pause();
		}else {
			if (BandManager.mp!=null)
			BandManager.mp.start();
			mp.start();
		}
		isPlaying=isPlaying?false:true;
	}
	
	private void stop(){
		if(mp!=null){
				if(!isReleased){
					mp.stop();
					mp.release();
					isReleased=true;
				isPlaying=false;
			}
		}
	}

	protected BroadcastReceiver InComingSMSReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.PHONE_STATE".equals(intent.getAction())){
				TelephonyManager telephonymanager = 
					(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				switch (telephonymanager.getCallState()) {
					case TelephonyManager.CALL_STATE_RINGING:
						if(mp!=null){
							if(isPlaying){
								if(!isReleased){
						mp.pause();
								}}}
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						if(mp!=null){
							if(isPlaying){
								if(!isReleased){
						mp.pause();
								}}}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
							if(mp!=null){
								if(isPlaying){
									if(!isReleased){	
						mp.start();}}}
						break;
					 default:
						 break;
				}
			}else if(NOTIFICATION_NEXT.equals(intent.getAction())){
				stop();
				if(position==list.size()-1)
				{
					position=0;
				}
				else
				{
					position++;
				}
				begin();
				MusicMain.position=position;
				if(MusicMain.isExit){
					sendBroadcast(new Intent(MusicMain.MUSIC_EXIST));
				}
			}else if(NOTIFICATION_PRE.equals(intent.getAction())){
				stop();
				if(position==0)
				{
					position=list.size()-1;
				}
				else if(position>0)
				{
					position--;
				}
				begin();
				MusicMain.position=position;
				if(MusicMain.isExit){
					sendBroadcast(new Intent(MusicMain.MUSIC_EXIST));
				}
					
			}else if(NOTIFICATION_STOP.equals(intent.getAction())){
				stop();
				if(notificationManager!=null)
				notificationManager.cancel(0);
			}else if(NOTIFICATION_PLAY.equals(intent.getAction())){
				if(MusicMain.isExit){
					sendBroadcast(new Intent(MusicMain.MUSIC_PLAY));
				}else {
					pause();
				}				
				
			}else if(NOTIFICATION_EXIT.equals(intent.getAction())){
				stop();
				if(notificationManager!=null)
				notificationManager.cancel(0);
			}else if(RECEIVER_EXITSPORT.equals(intent.getAction())){
				if(mp!=null){
					if(isPlaying){
						if(!isReleased){
					mp.pause();
						}}
				}
			}else if(MUSIC_GOON.equals(intent.getAction())){
				if(mp!=null){
					if(isPlaying){
						if(!isReleased){
					mp.start();
						}}
				}
			}
			
		}
	};
	
	

}
