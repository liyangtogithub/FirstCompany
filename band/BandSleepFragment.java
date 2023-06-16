package com.desay.iwan2.module.band;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SetServer;
import com.desay.wheel.widget.WheelView;
import com.desay.wheel.widget.adapters.NumericWheelAdapter;

import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BandSleepFragment extends BaseFragment implements OnClickListener {

    private Context context;
    private TextView tv_title,tv_sleep_start_set,tv_sleep_stop_set;
    private RelativeLayout relative_title,layout_sleep_start,layout_sleep_stop;
    CheckBox  checkbox_band_sleep;
    String sleepSetString = ""; 
    String sleepAlarmStringOld = "";
    boolean setStartTime = true;
    String sleepStartHour = "08";
	String sleepStartMinute  = "00";
	String sleepStopHour = "17";
	String sleepStopMinute  = "00";
    public static final String SLPTIME = "1,2230,0730";
    boolean clickAble = true;
 /*   龙厚成(龙厚成) 11:30:21
发送：at+slptime=1,2300,0800
返回：at+slptime:1,2300,0800

发送：at+slptime=0,2300,0800
返回：at+slptime:0,2460,2460


龙厚成(龙厚成) 11:30:31
1是开，0是关
江鸿(江鸿) 11:31:01
ok
龙厚成(龙厚成) 11:32:05
发送：at+slptime=1,2460,2460
返回：at+slptime:0,2460,2460*/

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
    	context =  getActivity();
        View v = inflater.inflate(R.layout.band_sleep_fragment, null);
        initView(v);
        initDbView();
        return v;
    }

	private void initView(View v) {
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.band_sleep_set));
        relative_title = (RelativeLayout) v.findViewById(R.id.relative_title);
        relative_title.setOnClickListener(this);
        tv_sleep_start_set = (TextView) v.findViewById(R.id.tv_sleep_start_set);
        tv_sleep_stop_set = (TextView) v.findViewById(R.id.tv_sleep_stop_set);
        layout_sleep_start = (RelativeLayout) v.findViewById(R.id.layout_sleep_start);
        layout_sleep_stop = (RelativeLayout) v.findViewById(R.id.layout_sleep_stop);
        
        checkbox_band_sleep = (CheckBox) v.findViewById(R.id.checkbox_band_sleep);
        layout_sleep_start.setOnClickListener(this);
        layout_sleep_stop.setOnClickListener(this);
        checkbox_band_sleep.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
					clickAble=isChecked;
					if (!isChecked){
						layout_sleep_start.setBackgroundResource(R.color.half_transparent);
						layout_sleep_stop.setBackgroundResource(R.color.half_transparent);
					}else {
						layout_sleep_start.setBackgroundResource(R.color.transparent);
						layout_sleep_stop.setBackgroundResource(R.color.transparent);
					}
			}
		});
    }
	
    private void initDbView() throws Exception{
    	Other slpTimeOther = new OtherServer(context).getOther(null, Other.Type.SlpTime);
    	sleepAlarmStringOld = sleepSetString = slpTimeOther == null ? SLPTIME : slpTimeOther.getValue();
    	if (slpTimeOther != null)
    		sleepAlarmStringOld = sleepSetString = slpTimeOther.getValue() == null ? SLPTIME : slpTimeOther.getValue();
	    LogUtil.i(" sleepAlarmString=="+sleepSetString);
	    if (sleepSetString.indexOf(",")!=-1){
	    	String sitStringItem[] = sleepSetString.split(",");
			sleepStartHour = sitStringItem[1].substring(0, 2);
			sleepStartMinute = sitStringItem[1].substring(2, 4);
			sleepStopHour = sitStringItem[2].substring(0, 2);
			sleepStopMinute = sitStringItem[2].substring(2, 4);
			checkbox_band_sleep.setChecked( "1".equals(sitStringItem[0]) ? true : false );
		}
		tv_sleep_start_set.setText(sleepStartHour+":"+sleepStartMinute);
		tv_sleep_stop_set.setText(sleepStopHour+":"+sleepStopMinute);
    
    }


	@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_title:
                back();
                break;
            case R.id.layout_sleep_start:
            	if (clickAble){
            		setStartTime = true;
    				try{
    					showTimeDialog();
    				} catch (Exception e){
    					e.printStackTrace();
    				}
				}
				break;
			case R.id.layout_sleep_stop:
				if (clickAble){
					setStartTime = false;
					try{
						showTimeDialog();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				break;    
        }
    }
	
	private void showTimeDialog(){
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
               
                final WheelView   sit_hours = (WheelView) layout.findViewById(R.id.time);
                final WheelView   sit_mins = (WheelView) layout.findViewById(R.id.sit_mins); 
                sit_mins.setVisibility(View.VISIBLE);
                sit_hours.setViewAdapter(new NumericWheelAdapter(context, 0, 23));
                sit_mins.setViewAdapter(new NumericWheelAdapter(context, 0, 59, "%02d"));
                sit_hours.setCyclic(true);
        		sit_mins.setCyclic(true);
        		if (setStartTime){
        			sit_hours.setCurrentItem(Integer.parseInt(sleepStartHour));
        			sit_mins.setCurrentItem(Integer.parseInt(sleepStartMinute));
				} else{
					sit_hours.setCurrentItem(Integer.parseInt(sleepStopHour));
					sit_mins.setCurrentItem(Integer.parseInt(sleepStopMinute));
				}
        		
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						int hour = sit_hours.getCurrentItem();
						int minute = sit_mins.getCurrentItem();
						if (setStartTime){
							sleepStartHour = hour < 10 ? "0"+hour : hour+"";
							sleepStartMinute =  minute < 10 ? "0"+minute : minute+"";
							tv_sleep_start_set.setText(sleepStartHour+":"+sleepStartMinute);
							
						} else{
							sleepStopHour = hour < 10 ? "0"+hour : hour+"";
							sleepStopMinute =  minute < 10 ? "0"+minute : minute+"";
							tv_sleep_stop_set.setText(sleepStopHour+":"+sleepStopMinute);	
						}
						ad.dismiss();
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

	@Override
    public void onPause(){
    	super.onPause();
    	sleepSetString =( checkbox_band_sleep.isChecked()?"1":"0" ) +","+sleepStartHour+
    			sleepStartMinute+","+sleepStopHour+sleepStopMinute;
    	if ( ! sleepSetString.equals(sleepAlarmStringOld) ){
    		try{
    			LogUtil.i(" sleepSetString=="+sleepSetString);
    			SetServer setServer = new SetServer(context);
    			setServer.storeSleep(sleepSetString, false);
				if (BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState)
					BleApi1.BizApi.setSlpTime( context, sleepSetString);
				ToastUtil.shortShow(context, getString(R.string.save_success));
    		} catch (Exception e){
				e.printStackTrace();
			}
		}
    }

}
