package com.desay.iwan2.module.band;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.sharesdk.dropbox.e;

import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.wheel.widget.WheelView;
import com.desay.wheel.widget.adapters.ArrayWheelAdapter;

import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BandUpFragment extends BaseFragment implements OnClickListener {

    private Context context;
    private TextView tv_title,tv_band_up;
    private RelativeLayout relative_title,band_up_layout;
    CheckBox  checkbox_band_up;
    String handsUpString ="";
    String handsUpStringOld ="";
    public static final String BAND_UP_CLOSE = "0";
    public static final String BAND_UP_LEFT = "2";
    public static final String BAND_UP_RIGHT = "3";
    boolean clickAble = true;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
    	context =  getActivity();
        View v = inflater.inflate(R.layout.band_up_fragment, null);
        initView(v);
        initDbView();
        return v;
    }

	private void initView(View v) {
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.band_up_text));
        relative_title = (RelativeLayout) v.findViewById(R.id.relative_title);
        band_up_layout = (RelativeLayout) v.findViewById(R.id.band_up_layout);
        tv_band_up = (TextView) v.findViewById(R.id.tv_band_up);
        checkbox_band_up = (CheckBox) v.findViewById(R.id.checkbox_band_up);
        relative_title.setOnClickListener(this);
        tv_band_up.setOnClickListener(this);
        band_up_layout.setOnClickListener(this);
        tv_band_up.setOnClickListener(this);
        checkbox_band_up.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
					clickAble=isChecked;
					if (!isChecked)
					   band_up_layout.setBackgroundResource(R.color.half_transparent);
					else {
						 band_up_layout.setBackgroundResource(R.color.transparent);
					}
			}
		});
    }
	
    private void initDbView() throws Exception{
    	Other handsUpOther = new OtherServer(context).getOther(null, Other.Type.handsUp);
    	handsUpStringOld = handsUpString = handsUpOther == null ? BAND_UP_LEFT : handsUpOther.getValue();
    	if (handsUpOther != null)
    	    handsUpStringOld = handsUpString = handsUpOther.getValue() == null ? BAND_UP_LEFT : handsUpOther.getValue();
    	if (BAND_UP_LEFT.equals(handsUpString)){
    		checkbox_band_up.setChecked( true );
    		tv_band_up.setText(R.string.band_up_left);
		}else if (BAND_UP_RIGHT.equals(handsUpString)) {
			checkbox_band_up.setChecked( true );
			tv_band_up.setText(R.string.band_up_right);
		}else if (BAND_UP_CLOSE.equals(handsUpString)) {
			checkbox_band_up.setChecked( false );
		}
	}

	@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_title:
                back();
                break;
            case R.id.band_up_layout:
            case R.id.tv_band_up:
            	if (clickAble){
            		showHandUpDialog();
				}
        }
    }
	
	private void showHandUpDialog(){
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
                String upArray[] = getResources().getStringArray(R.array.band_up_array);
                final WheelView   band_up = (WheelView) layout.findViewById(R.id.time);
                band_up.setViewAdapter( new ArrayWheelAdapter<Object>( context, upArray) );
                band_up.setCyclic(false);
                band_up.setCurrentItem( BAND_UP_LEFT.equals(handsUpString)?0:1 );
        		Button   dialog_sure = (Button) layout.findViewById(R.id.dialog_sure);
        		Button   dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle); 
        		dialog_sure.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					ad.dismiss();
					if (band_up.getCurrentItem()==0){
						handsUpString =  BAND_UP_LEFT;
						tv_band_up.setText(getString(R.string.band_up_left));
					}else {
						handsUpString =  BAND_UP_RIGHT;
						tv_band_up.setText(getString(R.string.band_up_right));
					}
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
    	if (checkbox_band_up.isChecked()){
    		if (getString(R.string.band_up_left).equals(tv_band_up.getText().toString())){
    			handsUpString = BAND_UP_LEFT;
			}else {
				handsUpString = BAND_UP_RIGHT;
			}
		}else {
			handsUpString = BAND_UP_CLOSE;
		}
    	if (!handsUpString.equals(handsUpStringOld)){
    		try{
				new OtherServer(context).createOrUpdate(null, Other.Type.handsUp, handsUpString);
				LogUtil.i("handsUpString==" + handsUpString);
				if (BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState)
					BleApi1.BizApi.setHandsup( context, handsUpString);
				ToastUtil.shortShow(context, getString(R.string.save_success));
    		} catch (Exception e){
				e.printStackTrace();
			}
		}
    }

}
