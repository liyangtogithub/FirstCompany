package com.desay.iwan2.module.alarmclock;

import java.sql.SQLException;
import java.util.Calendar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SetServer;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

public class AlertActivity extends TemplateActivity implements OnClickListener
{
	private TextView tv_title,tv_alert_sit,tv_alert_alarm,tv_alert_alarm_2,
	                 tv_alert_week,tv_alert_week_2,tv_alarm_sync,tv_alarm_sync_2,tv_sit_sync,tv_sync;
	private RelativeLayout relative_title;
	RelativeLayout linear_1 ,linear_1_2 , linear_2;
	CheckBox alert_checkbox;
	Context context;
	SetServer setServer;
	String alertCallString = null ;
	String alertCallStringOld = null ;
	String sitString = null ;
	String alarmString ,alarmString1,alarmString2 ;
	String[] weekArray = null ;
	public static final String BROADCAST_UPDATE_AlERT = "com.desay.iwan2.BroadcastUpdateAlert";
	public static final String UPDATE_AlERT = "com.desay.iwan2.UpdateAlert";
	public static final int UPDATE_ALARM = 0;
	public static final int UPDATE_SIT = 1;
	
	@Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);
		setContentView(R.layout.my_alert);
		context = AlertActivity.this;
		weekArray = getResources().getStringArray(R.array.array_week);
		try
		{
			initDb();
			initView();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void initDb() throws Exception
	{
		setServer = new SetServer(context);
		if (setServer != null)
		{
			alertCallStringOld = alertCallString = setServer.getCallerSet();
		}
	}

	private void initView() throws Throwable
	{
		tv_title = (TextView) findViewById(R.id.tv_title);
		relative_title = (RelativeLayout) findViewById(R.id.relative_title);
		tv_title.setText(getString(R.string.alert_title));
		relative_title.setOnClickListener(this);
		tv_alert_sit = (TextView) findViewById(R.id.tv_alert_sit);
		tv_alert_alarm = (TextView) findViewById(R.id.tv_alert_alarm);
		tv_alert_week = (TextView) findViewById(R.id.tv_alert_week);
		tv_alert_week_2 = (TextView) findViewById(R.id.tv_alert_week_2);
		tv_alert_alarm_2 = (TextView) findViewById(R.id.tv_alert_alarm_2);
		tv_alarm_sync = (TextView) findViewById(R.id.tv_alarm_sync);
		tv_alarm_sync_2 = (TextView) findViewById(R.id.tv_alarm_sync_2);
		tv_sit_sync = (TextView) findViewById(R.id.tv_sit_sync);
		tv_sync = (TextView) findViewById(R.id.tv_sync);
		
		linear_1 = (RelativeLayout) findViewById(R.id.linear_1);
		linear_1_2 = (RelativeLayout) findViewById(R.id.linear_1_2);
		linear_2 = (RelativeLayout) findViewById(R.id.linear_2);
		linear_1.setOnClickListener(this);
		linear_1_2.setOnClickListener(this);
		linear_2.setOnClickListener(this);
		
		alert_checkbox=(CheckBox) findViewById(R.id.alert_checkbox);
		if (alertCallString == null)
			alertCallStringOld = alertCallString = "01";
		if ("01".equals(alertCallString)||"1".equals(alertCallString)){
		   alert_checkbox.setChecked( true );
		   tv_sync.setText(R.string.RemaindIncoingCallTips2);
		}
		else {
			tv_sync.setText(R.string.RemaindIncoingCallTips);
			alert_checkbox.setChecked( false );
		}
		alert_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if (isChecked){
					   tv_sync.setText(R.string.RemaindIncoingCallTips2);
					}
					else {
						tv_sync.setText(R.string.RemaindIncoingCallTips);
					}
			}
		});
		initDbView();
		registerMyReceiver();
	}
	
	 private void registerMyReceiver() {
	        IntentFilter filter = new IntentFilter();
	        filter.addAction(BROADCAST_UPDATE_AlERT);
	        registerReceiver(myReceiver, filter);
	    }
	 
	 private BroadcastReceiver myReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	String actionString =  intent.getAction();
	        	if (BROADCAST_UPDATE_AlERT.equals(actionString)){
	        		int update_alert = intent.getIntExtra(UPDATE_AlERT, UPDATE_ALARM);
					try{
						OtherServer otherServer = new OtherServer(context);
						if (update_alert == UPDATE_ALARM)
		        			updateAlarmUi(otherServer);
		        		else {
		        			updateSitUi(otherServer);
						}
					} catch (SQLException e){
						e.printStackTrace();
					}
				}
	        }
	    };
	    
	@Override
	protected void onResume1() throws Throwable
	{
			initDbView();
	}
	
	private void initDbView() throws Throwable
	{
		if (setServer != null)
		{
			sitString = setServer.getSedentarySet();
			alarmString = setServer.getAlarmSet();
		}	
			if (sitString == null)
				sitString = "030,0800,1700,1";
			String sitStringItem[] = sitString.split(",");
			String sitStart = sitStringItem[1].substring(0, 2)+":"+sitStringItem[1].substring(2, 4);
			String sitStop = sitStringItem[2].substring(0, 2)+":"+sitStringItem[2].substring(2, 4);
			tv_alert_sit.setText(sitStart + "~" + sitStop );
			
			if (alarmString == null)
				alarmString = "0,20,11111110,0700;0,20,11111110,0700";
			
			if (alarmString.indexOf(";")==-1){
				 alarmString+=";0,20,11111110,0700";
			}
			
			LogUtil.i("AlertActivity  alarmString=="+alarmString);
			String alarmStringArray[] = alarmString.split(";");
			String alarmStringItem[] = alarmStringArray[0].split(",");
			String alarmStringItem2[] = alarmStringArray[1].split(",");
			initAlarm1UI(alarmStringItem);
			initAlarm2UI(alarmStringItem2);
			OtherServer otherServer = new OtherServer(context);
			updateAlarmUi(otherServer);
			updateSitUi(otherServer);
	}
	
	private void initAlarm1UI(String alarmStringItem[]){
		String	alarmHour = alarmStringItem[3].substring(0, 2);
	    String	alarmMinute = alarmStringItem[3].substring(2, 4);
	    LogUtil.i("initAlarm1UI  alarmHour=="+alarmHour);
	    tv_alert_alarm.setText( getCleverAlarmStr( alarmStringItem,alarmHour,alarmMinute )
			+ alarmHour+":"+alarmMinute );
	    String weekString = getWeekString(alarmStringItem[2]);
	    tv_alert_week.setText( weekString );
	}
	
	private void initAlarm2UI(String alarmStringItem[]){
		String	alarmHour = alarmStringItem[3].substring(0, 2);
	    String	alarmMinute = alarmStringItem[3].substring(2, 4);
	    tv_alert_alarm_2.setText( getCleverAlarmStr( alarmStringItem,alarmHour,alarmMinute )
			+ alarmHour+":"+alarmMinute );
	    String weekString = getWeekString(alarmStringItem[2]);
	    tv_alert_week_2.setText( weekString );
	}

	private String getWeekString(String weekString){
		char[] weeks = weekString.toCharArray();
		String weekString2="";
		for (int i = 0; i < weekArray.length; i++){
			if (weeks[i]=='1'){
				weekString2 += (weekArray[i]+" ");
			}
		}
		return weekString2;
	}

	private String getCleverAlarmStr( String alarmStringItem[],String alarmHour,String alarmMinute)
	{
		String cleverAlarmStr = "";
		if ("1".equals(alarmStringItem[0]) ){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmHour) );
			calendar.set(Calendar.MINUTE, Integer.parseInt(alarmMinute) );
			calendar.add(Calendar.MINUTE, -Integer.parseInt(alarmStringItem[1]));
		
		    int cleverHour = calendar.get(Calendar.HOUR_OF_DAY) ;
		    String cleverHourString = ( cleverHour >= 10 ? cleverHour+"":"0"+cleverHour );
		    int cleverMinu = calendar.get(Calendar.MINUTE) ;
		    String cleverMinuString = ( cleverMinu >= 10 ? cleverMinu+"":"0"+cleverMinu );
		    cleverAlarmStr = cleverHourString+":"+cleverMinuString+"~";
		}
		return cleverAlarmStr;
	}


	private void updateSitUi(OtherServer otherServer) throws SQLException
	{
		Other sedentaryToBandOther = otherServer.getOther(null, Other.Type.sedentaryToBand);
		String sedentaryToBandString = sedentaryToBandOther == null ? "0" : sedentaryToBandOther.getValue();
		if ("1".equals(sedentaryToBandString))
			tv_sit_sync.setText(getString(R.string.alert_sync));
		else {
			tv_sit_sync.setText(getString(R.string.alert_wait_sync));
		}
	}

	private void updateAlarmUi(OtherServer otherServer) throws SQLException
	{
		Other alarmToBandOther = otherServer.getOther(null, Other.Type.alarmToBand);
		String alarmToBandString = alarmToBandOther == null ? "0" : alarmToBandOther.getValue();
		if ("1".equals(alarmToBandString))
			tv_alarm_sync.setText(getString(R.string.alert_sync));
		else {
			tv_alarm_sync.setText(getString(R.string.alert_wait_sync));
		}
		Other alarmToBandOther2 = otherServer.getOther(null, Other.Type.alarmToBand2);
		String alarmToBandString2 = alarmToBandOther2 == null ? "0" : alarmToBandOther2.getValue();
		if ("1".equals(alarmToBandString2))
			tv_alarm_sync_2.setText(getString(R.string.alert_sync));
		else {
			tv_alarm_sync_2.setText(getString(R.string.alert_wait_sync));
		}
	}

	@Override
	protected void onDestroy1() throws Throwable
	{
		if (myReceiver!=null)
		unregisterReceiver(myReceiver);
		alertCallString = alert_checkbox.isChecked() ? "01":"00" ;
		if ( !alertCallString.equals(alertCallStringOld) ){
			setServer.storeCaller( alertCallString ,false);
			ToastUtil.shortShow(context, getString(R.string.save_success));
		}
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.relative_title:
				back();
				break;
			case R.id.linear_1:
				AlarmActivity.goToActivity(context,true);
				break;
			case R.id.linear_1_2:
				AlarmActivity.goToActivity(context,false);
				break;
			case R.id.linear_2:
				LongSitActivity.goToActivity(context);
				break;	
			
		}
	}

	public static void goToActivity(Context context)
	{
		Intent intent = new Intent(context, AlertActivity.class);
		context.startActivity(intent);
	}
}
