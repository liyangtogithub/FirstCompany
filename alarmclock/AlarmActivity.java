package com.desay.iwan2.module.alarmclock;

import java.util.Calendar;
import cn.sharesdk.evernote.o;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SetServer;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.wheel.widget.WheelView;
import com.desay.wheel.widget.adapters.ArrayWheelAdapter;
import com.desay.wheel.widget.adapters.NumericWheelAdapter;
import com.j256.ormlite.stmt.query.IsNotNull;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AlarmActivity extends TemplateActivity implements OnClickListener,OnCheckedChangeListener
{
	private TextView tv_title,textView_clock1,tv_clever;
	private RelativeLayout relative_title,clever_layout,repeat_layout;
	//public static String FILE_ALARM_TIME2 = "alarm_time_set";
	String alarmHour = null;
	String alarmMinute  = null;
	Context context;
	SetServer setServer;
	String alarmString = null ;
	String alarmStringOld = null ;
	CheckBox  checkbox_clockSw1;
	CheckBox  checkbox_sun,checkbox_mon,checkbox_tue,checkbox_wed,checkbox_thu,checkbox_fri,checkbox_sat;
	CheckBox  weekArray[] = new CheckBox[7] ;
	int timeIdIndex = 1;
	static boolean isAlarm1=true;
	public static String IS_ALARM1 = "is_alarm1";
	boolean clickAble = true;
	String array_clever[];
	String array_week[];
	private ListView lv= null;
	static boolean isCleverAble=false;
	 AlertDialog alertDialog;
	
	@Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
		super.onCreate1(savedInstanceState);
		setContentView(R.layout.my_alert_alarm);
		context = AlarmActivity.this;
		isAlarm1=getIntent().getBooleanExtra(IS_ALARM1, true);
		array_clever = getResources().getStringArray(R.array.array_clever);
		array_week = getResources().getStringArray(R.array.array_week);
		try{
			initDb();
			initView();
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	
	private void initDb() throws Exception{
		setServer = new SetServer(context);
		if (setServer != null){
			alarmStringOld = alarmString = setServer.getAlarmSet();
		}
	}

	private void initView()
	{
		tv_title = (TextView) findViewById(R.id.tv_title);
		relative_title = (RelativeLayout) findViewById(R.id.relative_title);
		tv_title.setText(getString(R.string.alarm));
		relative_title.setOnClickListener(this);
		textView_clock1 = (TextView) findViewById(R.id.textView_clock1);
		textView_clock1.setOnClickListener(this);
		clever_layout = (RelativeLayout) findViewById(R.id.clever_layout);
		repeat_layout = (RelativeLayout) findViewById(R.id.repeat_layout);
		tv_clever = (TextView) findViewById(R.id.tv_clever);
		clever_layout.setOnClickListener(this);
		repeat_layout.setOnClickListener(this);
		checkbox_sun = (CheckBox) findViewById(R.id.checkbox_sun1);weekArray[0]=checkbox_sun;
		checkbox_mon = (CheckBox) findViewById(R.id.checkbox_mon1);weekArray[1]=checkbox_mon;
		checkbox_tue = (CheckBox) findViewById(R.id.checkbox_tue1);weekArray[2]=checkbox_tue;
		checkbox_wed = (CheckBox) findViewById(R.id.checkbox_wed1);weekArray[3]=checkbox_wed;
		checkbox_thu = (CheckBox) findViewById(R.id.checkbox_thu1);weekArray[4]=checkbox_thu;
		checkbox_fri = (CheckBox) findViewById(R.id.checkbox_fri1);weekArray[5]=checkbox_fri;
		checkbox_sat = (CheckBox) findViewById(R.id.checkbox_sat1);weekArray[6]=checkbox_sat;
		checkbox_sun.setOnCheckedChangeListener(this);
		checkbox_mon.setOnCheckedChangeListener(this);
		checkbox_tue.setOnCheckedChangeListener(this);
		checkbox_wed.setOnCheckedChangeListener(this);
		checkbox_thu.setOnCheckedChangeListener(this);
		checkbox_fri.setOnCheckedChangeListener(this);
		checkbox_sat.setOnCheckedChangeListener(this);
		checkbox_clockSw1=(CheckBox) findViewById(R.id.checkbox_clockSw1);
		checkbox_clockSw1.setOnCheckedChangeListener(this);
		if (alarmString == null)
			alarmString = "0,20,11111110,0700;0,20,11111110,0700";
		
		if (alarmString.indexOf(";")==-1){
			 alarmString+=";0,20,11111110,0700";
		}
		alarmStringOld = alarmString;
		String alarmStringArray[] = alarmString.split(";");
		
		if (isAlarm1){
			String alarmStringItem[] = alarmStringArray[0].split(",");
			initAlarmUI(alarmStringItem);
		}else {
			String alarmStringItem[] = alarmStringArray[1].split(",");
			initAlarmUI(alarmStringItem);
		}
		
		initCleverTime();
		if (!checkbox_clockSw1.isChecked()){
			clickAble = false;
			isCleverAble = false;
			clever_layout.setBackgroundResource(R.color.half_transparent);
			repeat_layout.setBackgroundResource(R.color.half_transparent);
		}
	}
	
	private void initCleverTime(){
		LogUtil.i("AlarmActivity isCleverAble =="+isCleverAble);
		if (!clickAble||!isCleverAble) 
			tv_clever.setText(array_clever[0]);
		else if (timeIdIndex==30)
			tv_clever.setText(array_clever[3]);
		else if (timeIdIndex==20) {
			tv_clever.setText(array_clever[2]);
		}else if (timeIdIndex==10){
			tv_clever.setText(array_clever[1]);
		} 
	}

	private void initAlarmUI(String[] alarmStringItem){
		LogUtil.i("AlarmActivity alarmStringItem[0] =="+alarmStringItem[0]);
		isCleverAble=( "1".equals(alarmStringItem[0]) ?  true:false);
		timeIdIndex = Integer.parseInt(alarmStringItem[1]);
		alarmHour = alarmStringItem[3].substring(0, 2);
		alarmMinute = alarmStringItem[3].substring(2, 4);
		textView_clock1.setText( getCleverAlarmStr(alarmStringItem[1])+alarmHour+":"+alarmMinute );
		checkbox_clockSw1.setChecked( "1".equals(alarmStringItem[2].substring(7)) ?  true:false);
		char[] weeks = alarmStringItem[2].toCharArray();
		for (int i = 0; i < weekArray.length; i++){
			weekArray[i].setVisibility(weeks[i]=='1' ? View.VISIBLE:View.GONE);
		}
	}

	private String getCleverAlarmStr( String cleverAlarmString){
		String cleverAlarmStr = "";
		if (clickAble && isCleverAble){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmHour) );
			calendar.set(Calendar.MINUTE, Integer.parseInt(alarmMinute) );
			calendar.add(Calendar.MINUTE, -Integer.parseInt(cleverAlarmString));
		
		    int cleverHour = calendar.get(Calendar.HOUR_OF_DAY) ;
		    String cleverHourString = ( cleverHour >= 10 ? cleverHour+"":"0"+cleverHour );
		    int cleverMinu = calendar.get(Calendar.MINUTE) ;
		    String cleverMinuString = ( cleverMinu >= 10 ? cleverMinu+"":"0"+cleverMinu );
		    cleverAlarmStr = cleverHourString+":"+cleverMinuString+"~";
		}
		return cleverAlarmStr;
	}
	@Override
	protected void onPause1() throws Throwable{
		String alarmStringArray[] = alarmString.split(";");
		String alarmStringTemp = (isCleverAble ? 1 : 0) + ","
				+ getCleverTime() + "," + getWeekSting()
				+ (checkbox_clockSw1.isChecked() == true ? 1 : 0) + ","
				+ alarmHour + alarmMinute;
		if (isAlarm1){
			alarmString = alarmStringTemp+";"+alarmStringArray[1];
		}else {
			alarmString = alarmStringArray[0] + ";" + alarmStringTemp;
		}
		if ( !alarmString.equals(alarmStringOld) ){
			OtherServer otherServer = new OtherServer(context);
			setServer.storeAlarm(alarmString, false);
			if (isAlarm1){
				otherServer.createOrUpdate(null, Other.Type.alarmToBand, "0", true);
			}else {
				otherServer.createOrUpdate(null, Other.Type.alarmToBand2, "0", true); 
			}
			LogUtil.i("AlarmActivity  alarmString==" + alarmString);
			if (BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState){
				LogUtil.i("AlarmActivity  isAlarm1=="+isAlarm1);
				if (isAlarm1){
					BandManager.setBandAlarmClock(this, alarmStringTemp);
				}else {
					BandManager.setBandAlarmClock2(this, alarmStringTemp);
				}
			}
			ToastUtil.shortShow(context, getString(R.string.save_success));
		}
	}
	
	private String getWeekSting()
	{
		String weekString="";
		for (int i = 0; i < weekArray.length; i++)
		{
			weekString=weekString+(  weekArray[i].isShown()==true ? "1" : "0");
		}
		return weekString;
	}
	
	private String getCleverTime()
	{
		String CleverTime="";
		if (timeIdIndex==30)
			CleverTime = "30";
		else if (timeIdIndex==20) {
			CleverTime = "20";
		}else if (timeIdIndex==10){
			CleverTime = "10";
		}else {
			CleverTime = "10";
		}
		return CleverTime;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		//为了保证在闹钟关闭状态下，智能提醒也在关闭状态，且不能再开
		if (buttonView.getId()==R.id.checkbox_clockSw1){
			if (isChecked){
				clickAble = true;
				clever_layout.setBackgroundResource(R.drawable.backrepeat);
				repeat_layout.setBackgroundResource(R.drawable.backrepeat);
			}
			else {
				clickAble = false;
				isCleverAble = false;
				tv_clever.setText(array_clever[0]);
				textView_clock1.setText( getCleverAlarmStr( getCleverTime() )+alarmHour+":"+alarmMinute );
				clever_layout.setBackgroundResource(R.color.half_transparent);
				repeat_layout.setBackgroundResource(R.color.half_transparent);
			}
		}
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.relative_title:
				back();
				break;
			case R.id.textView_clock1:
				try{
					showTimeDialog();
				} catch (Exception e){
					e.printStackTrace();
				}
				break;
			case R.id.repeat_layout:
				if (clickAble)
				    showMultDialog();
				break;
			case R.id.clever_layout:
				if (clickAble)
				    showCleverDialog();
				break;
		}
	}
	
	private void showCleverDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final Dialog ad = builder.create();
	    LayoutInflater inflater;
	    View layout=null;	  
        if (!ad.isShowing()){
                ad.show();       	   
                inflater=  LayoutInflater.from(context);
        		layout = inflater.inflate(R.layout.dialog_long_sit, null);
                ad.getWindow().setContentView(layout);
                ad.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
                final WheelView   clever_alarm = (WheelView) layout.findViewById(R.id.time);
                clever_alarm.setViewAdapter( new ArrayWheelAdapter<Object>( context, array_clever) );
                clever_alarm.setCyclic(false);
                if (timeIdIndex==30)
                	 clever_alarm.setCurrentItem( 3 );
        		else if (timeIdIndex==20) {
        			clever_alarm.setCurrentItem( 2 );
        		}else if (timeIdIndex==10){
        			clever_alarm.setCurrentItem( 1 );
        		}else {
        			clever_alarm.setCurrentItem( 0 );
        		}
               
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					ad.dismiss();
					isCleverAble = true;
					if (clever_alarm.getCurrentItem()==0){
						isCleverAble = false;
						timeIdIndex=0;
					}else if (clever_alarm.getCurrentItem()==1) {
						timeIdIndex=10;
					}else if (clever_alarm.getCurrentItem()==2) {
						timeIdIndex=20;
					}else {
						timeIdIndex=30;
					}
					initCleverTime();
					textView_clock1.setText( getCleverAlarmStr( getCleverTime() )+alarmHour+":"+alarmMinute );
				}
				});
        		dialog_cancle.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						ad.dismiss();
					}
				});
			}
	}
	
	private void showMultDialog(){
	    final boolean weekState[] =	new boolean[weekArray.length];
	    
		for (int i = 0; i < weekArray.length; i++)
			weekState[i] =weekArray[i].isShown()?true:false;
		  alertDialog = new AlertDialog.Builder(this).
				setTitle(getString(R.string.RemaindRepeatLabel))
				.setMultiChoiceItems(array_week, weekState, 
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								LogUtil.i("which=="+which+"++++++isChecked=="+isChecked);
								if (isChecked){
									weekState[which]=true;
								}else {
									weekState[which]=false;
								}
								boolean buttonEnable = false;
								for (int i = 0; i < weekState.length; i++){
									if (weekState[i])
										buttonEnable = true;
								}
								if (!buttonEnable)
								    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
								else {
									alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
								}
							}
						} ).setPositiveButton(getString(R.string.RemaindSetOK), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								LogUtil.i("which=="+which+"++++++lv.getCheckedItemPositions().size()=="+lv.getCheckedItemPositions().size());
								if(lv.getCheckedItemPositions().size()>0){
									for(int i = 0;i<weekArray.length;i++){
										if(lv.getCheckedItemPositions().get(i)){
											weekArray[i].setVisibility( View.VISIBLE);
										}else {
											weekArray[i].setVisibility(View.GONE);
										}
									}
								}
							}
						}).setNegativeButton(getString(R.string.RemaindSetCancel), null).create();
		lv = alertDialog.getListView();
		alertDialog.show();
	}

	private void showTimeDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final Dialog ad = builder.create();
	    LayoutInflater inflater;
	    View layout=null;	  
	   
        if (!ad.isShowing())
        {
                ad.show();
        	    
                inflater=  LayoutInflater.from(this);
        		layout = inflater.inflate(R.layout.dialog_alarm_set, null);
                ad.getWindow().setContentView(layout);
                ad.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
               
                final WheelView   hours = (WheelView) layout.findViewById(R.id.hour);
                final WheelView   mins = (WheelView) layout.findViewById(R.id.mins); 
                hours.setViewAdapter(new NumericWheelAdapter(this, 0, 23));
        		mins.setViewAdapter(new NumericWheelAdapter(this, 0, 59, "%02d"));
        		hours.setCyclic(true);
        		mins.setCyclic(true);
        		hours.setCurrentItem(Integer.parseInt(alarmHour));
        		mins.setCurrentItem(Integer.parseInt(alarmMinute));
        		
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						int hour = hours.getCurrentItem();
						int minute = mins.getCurrentItem();
						alarmHour = hour < 10 ? "0"+hour : hour+"";
						alarmMinute =  minute < 10 ? "0"+minute : minute+"";
						textView_clock1.setText( getCleverAlarmStr( getCleverTime() )+alarmHour+":"+alarmMinute );
						ad.dismiss();
					}
				});
        		dialog_cancle.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						ad.dismiss();
					}
				});
			}
	}
	
	public static void goToActivity(Context context,boolean isAlarm1)
	{
		Intent intent = new Intent(context, AlarmActivity.class);
		intent.putExtra(IS_ALARM1, isAlarm1);
		context.startActivity(intent);
	}

	

}
