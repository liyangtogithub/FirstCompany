package com.desay.iwan2.module.alarmclock;

import java.sql.SQLException;

import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.AimServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SetServer;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.module.music.MusicSet;
import com.desay.iwan2.module.userinfo.CommonData;
import com.desay.wheel.widget.WheelView;
import com.desay.wheel.widget.adapters.ArrayWheelAdapter;
import com.desay.wheel.widget.adapters.NumericWheelAdapter;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class LongSitActivity extends TemplateActivity implements OnClickListener
{
	private TextView tv_title,tv_sit_start_set,tv_sit_stop_set,tv_sit_time_set;
	private RelativeLayout relative_title;
	String sitStartHour = "08";
	String sitStartMinute  = "00";
	String sitStopHour = "17";
	String sitStopMinute  = "00";
	int    timeIndex = 0;
	boolean setStartTime = true;
	String sitTime[] = null;
	SetServer setServer;
	String sitString = null ;
	String sitStringOld = null ;
	Context context;
	CheckBox checkbox_sit_alert; 
	
	@Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);
		setContentView(R.layout.my_alert_sit);
		context = LongSitActivity.this;
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
			sitStringOld = sitString = setServer.getSedentarySet();

	}

	private void initView()
	{
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getString(R.string.alert_long_sit));
		relative_title = (RelativeLayout) findViewById(R.id.relative_title);
		relative_title.setOnClickListener(this);
		tv_sit_start_set = (TextView) findViewById(R.id.tv_sit_start_set);
		tv_sit_stop_set = (TextView) findViewById(R.id.tv_sit_stop_set);
		tv_sit_time_set = (TextView) findViewById(R.id.tv_sit_time_set);
		checkbox_sit_alert =  (CheckBox) findViewById(R.id.checkbox_sit_alert);
		tv_sit_start_set.setOnClickListener(this);
		tv_sit_stop_set.setOnClickListener(this);
		tv_sit_time_set.setOnClickListener(this);
		
		if (sitString==null)
			sitStringOld = sitString="030,0800,1700,1";
		String sitStringItem[] = sitString.split(",");
		timeIndex = getIndexFromTime( Integer.parseInt(sitStringItem[0].substring(0, 3)) );
		sitStartHour = sitStringItem[1].substring(0, 2);
		sitStartMinute = sitStringItem[1].substring(2, 4);
		sitStopHour = sitStringItem[2].substring(0, 2);
		sitStopMinute = sitStringItem[2].substring(2, 4);
		checkbox_sit_alert.setChecked( "1".equals(sitStringItem[3]) ? true : false );

		sitTime = getResources().getStringArray(R.array.array_sit);
		tv_sit_time_set.setText(sitTime[timeIndex]);
		tv_sit_start_set.setText(sitStartHour+":"+sitStartMinute);
		tv_sit_stop_set.setText(sitStopHour+":"+sitStopMinute);
		
	}

	@Override
	protected void onPause1() throws Throwable
	{
		sitString = getTimeFromIndex(timeIndex)+","+sitStartHour+sitStartMinute+","
		            +sitStopHour+sitStopMinute+","
			        +( checkbox_sit_alert.isChecked()==true ? 1 : 0 );
		if ( !sitString.equals(sitStringOld) ){
			LogUtil.i("sitString=="+sitString);
			OtherServer otherServer = new OtherServer(context);
			otherServer.createOrUpdate(null, Other.Type.sedentaryToBand, "0", true);
			setServer.storeSedentary(sitString,false);
			if (BleConnectState.CONNECTED==BleServer.getInstance(context).mConnectionState)
				BandManager.setBandSitAlarm( this, sitString);
			ToastUtil.shortShow(context, getString(R.string.save_success));
		}
	}
	
	private int getIndexFromTime(int time)
	{
		if (time == 30)
		{
			return 0;
		} else if (time == 60)
		{
			return 1;
		} else if (time == 75)
		{
			return 2;
		} else if (time == 90)
		{
			return 3;
		}
		return 1;
	}

	private String getTimeFromIndex(int index)
	{
		 if (index == 0)
		{
			return "030";
		} else if (index == 1)
		{
			return "060";
		} else if (index == 2)
		{
			return "075";
		} else if (index == 3)
		{
			return "090";
		}
		return "030";
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.relative_title:
				back();
				break;
			case R.id.tv_sit_start_set:
				setStartTime = true;
				try
				{
					showTimeDialog();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case R.id.tv_sit_stop_set:
				setStartTime = false;
				try
				{
					showTimeDialog();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case R.id.tv_sit_time_set:
				try
				{
					showTimeDialogSit();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
		}
	}

	private void showTimeDialogSit()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final Dialog ad = builder.create();
	    LayoutInflater inflater;
	    View layout=null;	  
        if (!ad.isShowing())
        {
                ad.show();       	   
                inflater=  LayoutInflater.from(this);
        		layout = inflater.inflate(R.layout.dialog_long_sit, null);
                ad.getWindow().setContentView(layout);
                ad.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
               
                final WheelView   time = (WheelView) layout.findViewById(R.id.time);
                time.setViewAdapter( new ArrayWheelAdapter<Object>( this, sitTime) );
                time.setCyclic(false);
        		time.setCurrentItem(timeIndex);
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
				{
					ad.dismiss();
					timeIndex = time.getCurrentItem();
					tv_sit_time_set.setText(sitTime[timeIndex]);
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
        		layout = inflater.inflate(R.layout.dialog_long_sit, null);
                ad.getWindow().setContentView(layout);
                ad.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
               
                final WheelView   sit_hours = (WheelView) layout.findViewById(R.id.time);
                final WheelView   sit_mins = (WheelView) layout.findViewById(R.id.sit_mins); 
                sit_mins.setVisibility(View.VISIBLE);
                sit_hours.setViewAdapter(new NumericWheelAdapter(this, 0, 23));
                sit_mins.setViewAdapter(new NumericWheelAdapter(this, 0, 59, "%02d"));
                sit_hours.setCyclic(true);
        		sit_mins.setCyclic(true);
        		if (setStartTime)
				{
        			sit_hours.setCurrentItem(Integer.parseInt(sitStartHour));
        			sit_mins.setCurrentItem(Integer.parseInt(sitStartMinute));
				} else
				{
					sit_hours.setCurrentItem(Integer.parseInt(sitStopHour));
					sit_mins.setCurrentItem(Integer.parseInt(sitStopMinute));
				}
        		
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						int hour = sit_hours.getCurrentItem();
						int minute = sit_mins.getCurrentItem();
						if (setStartTime)
						{
							sitStartHour = hour < 10 ? "0"+hour : hour+"";
							sitStartMinute =  minute < 10 ? "0"+minute : minute+"";
							tv_sit_start_set.setText(sitStartHour+":"+sitStartMinute);
							
						} else
						{
							sitStopHour = hour < 10 ? "0"+hour : hour+"";
							sitStopMinute =  minute < 10 ? "0"+minute : minute+"";
							tv_sit_stop_set.setText(sitStopHour+":"+sitStopMinute);
							
						}
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
	
	public static void goToActivity(Context context)
	{
		Intent intent = new Intent(context, LongSitActivity.class);
		context.startActivity(intent);
	}

}
