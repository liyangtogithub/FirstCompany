package com.desay.iwan2.module.band;

import java.sql.SQLException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.BtDevServer;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.correct.fragment.CorrectHeartrateFragment;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;


public class BandManageFragment extends BaseFragment implements OnClickListener {

    private Context context;
    private TextView tv_title,tv_band_add,tv_band_add_notice,tv_up_hand,tv_check_sleep,
                     tv_upgrade_notice ;
    RelativeLayout relative_title,band_layout1,band_layout2,band_layout3,band_layout4
                   ,band_layout5,band_layout6;
    LinearLayout band_use_layout;
    ImageView  iv_band_upgrade_title ,iv_no_add;
    public static boolean  manualUpgrade = false;
    public static final String  BAND_NO_UPGRADE = "com.desay.fitband.no_upgrade";
    public static final String  BAND_UPGRADE = "com.desay.fitband.band_upgrade";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {

        context = getActivity();
      //  context.sendBroadcast(new Intent(BleManager.STOP_SCAN));
        View v = inflater.inflate(R.layout.band_manage_fragment, null);
        initView(v);
        registBroadcast();
        return v;
    }
    
    private void registBroadcast(){
    	 IntentFilter filter = new IntentFilter();
         filter.addAction(BAND_NO_UPGRADE);
         filter.addAction(BAND_UPGRADE);
         context.registerReceiver(receiver, filter);
	}

	private void initView(View v) throws Exception {
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.band_manage));
        relative_title = (RelativeLayout) v.findViewById(R.id.relative_title);
        relative_title.setOnClickListener(this);
        
        tv_band_add = (TextView) v.findViewById(R.id.tv_band_add);
        tv_band_add_notice = (TextView) v.findViewById(R.id.tv_band_add_notice);
        tv_up_hand = (TextView) v.findViewById(R.id.tv_up_hand);
        tv_check_sleep = (TextView) v.findViewById(R.id.tv_check_sleep);
        tv_upgrade_notice = (TextView) v.findViewById(R.id.tv_upgrade_notice);
        band_use_layout = (LinearLayout) v.findViewById(R.id.band_use_layout);
        iv_no_add= (ImageView) v.findViewById(R.id.iv_no_add);
        band_layout1 = (RelativeLayout) v.findViewById(R.id.band_layout1);
        band_layout2 = (RelativeLayout) v.findViewById(R.id.band_layout2);
        band_layout3 = (RelativeLayout) v.findViewById(R.id.band_layout3);
        band_layout4 = (RelativeLayout) v.findViewById(R.id.band_layout4);
        band_layout5 = (RelativeLayout) v.findViewById(R.id.band_layout5);
        band_layout6 = (RelativeLayout) v.findViewById(R.id.band_layout6);
        band_layout1.setOnClickListener(this);
        band_layout2.setOnClickListener(this);
        band_layout3.setOnClickListener(this);
        band_layout4.setOnClickListener(this);
        band_layout5.setOnClickListener(this);
        band_layout6.setOnClickListener(this);
        bandUseCheck(v);
        
    }

	private void bandUseCheck(View v){
		try{
			MacServer macServer = new MacServer(context);
			if (macServer == null || macServer.getMac() == null
					|| "".equals(macServer.getMac())){
				band_use_layout.setVisibility(View.GONE);
				tv_band_add.setVisibility(View.GONE);
				iv_no_add.setVisibility(View.VISIBLE);
				tv_band_add_notice.setText(R.string.band_add_notice1);
				    
			} else{
				initUpView();
				initSleepView();
				bandUpgradeCheck(v);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	  private void initUpView() throws Exception{
	    	Other handsUpOther = new OtherServer(context).getOther(null, Other.Type.handsUp);
	    	String handsUpString = handsUpOther == null ? BandUpFragment.BAND_UP_LEFT : handsUpOther.getValue();
	    	if (handsUpOther != null)
	    	    handsUpString = handsUpOther.getValue() == null ? BandUpFragment.BAND_UP_LEFT : handsUpOther.getValue();
	    	if (BandUpFragment.BAND_UP_LEFT.equals(handsUpString)){
	    		tv_up_hand.setText(R.string.band_up_left);
			}else if (BandUpFragment.BAND_UP_RIGHT.equals(handsUpString)) {
				tv_up_hand.setText(R.string.band_up_right);
			}else if (BandUpFragment.BAND_UP_CLOSE.equals(handsUpString)) {
				tv_up_hand.setText(R.string.band_up_close);
			}
		}
	  
	  private void initSleepView() throws Exception{
	    	Other slpTimeOther = new OtherServer(context).getOther(null, Other.Type.SlpTime);
	    	String sleepSetString = slpTimeOther == null ? BandSleepFragment.SLPTIME : slpTimeOther.getValue();
	    	if (slpTimeOther != null)
	    		sleepSetString = slpTimeOther.getValue() == null ? BandSleepFragment.SLPTIME : slpTimeOther.getValue();
		        LogUtil.i(" sleepAlarmString=="+sleepSetString);
		    if (sleepSetString.indexOf(",")!=-1){
		    	String sitStringItem[] = sleepSetString.split(",");
		    	if( "1".equals(sitStringItem[0]) ){
		    		String sleepStartHour = sitStringItem[1].substring(0, 2);
		    		String sleepStartMinute = sitStringItem[1].substring(2, 4);
		    		String sleepStopHour = sitStringItem[2].substring(0, 2);
		    		String sleepStopMinute = sitStringItem[2].substring(2, 4);
					tv_check_sleep.setText(sleepStartHour+":"+sleepStartMinute+"~"+
							               sleepStopHour+":"+sleepStopMinute);
		    	}
		    	else {
		    		tv_check_sleep.setText(R.string.band_sleep_closed);
				}
			}
	    }

	private void bandUpgradeCheck(View v) throws Exception{
    	if (BleUtil.checkBleEnable(context)) {
            if ( BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState) {
            	iv_band_upgrade_title = (ImageView) v.findViewById(R.id.iv_band_upgrade_title);
				try{
					BtDev btDev = new BtDevServer(context).getBtDev(null);
					if (btDev != null) {
						coreVersionCheck(btDev);
						nodicVersionCheck(btDev);
					}
				} catch (SQLException e){
					e.printStackTrace();
				}
            }
    	}
	}

	private void nodicVersionCheck(BtDev btDev ) throws SQLException{
		String btNordicVer = btDev.getNordicVersion();
        if(!StringUtil.isBlank(btNordicVer)) {
        	 int nordicVer = Integer.parseInt(btNordicVer);
             Other netNordicVer = new OtherServer(context).getOther(null, Other.Type.netNodicVer);
             String netNordicVertion = netNordicVer == null ? "0" : netNordicVer.getValue();
             int netVer = 0;
             if (netNordicVertion != null)
                 netVer = Integer.parseInt(netNordicVertion);
             LogUtil.i("nordicVer=="+nordicVer+"//netVer=="+netVer);
             if (nordicVer < netVer){ 
            	 tv_upgrade_notice.setVisibility(View.GONE);
            	 iv_band_upgrade_title.setVisibility(View.VISIBLE);
             }
        }
	}

	private void coreVersionCheck(BtDev btDev ) throws Exception{
		 String btCoreVer = btDev.getCoreVersion();
         if(!StringUtil.isBlank(btCoreVer)) {
        	 int coreVer = Integer.parseInt(btCoreVer);
             Other netEfm32Ver = new OtherServer(context).getOther(null, Other.Type.netEf32Ver);
             String netCoreVertion = netEfm32Ver == null ? "0" : netEfm32Ver.getValue();
             int netVer = 0;
             if (netCoreVertion != null)
                 netVer = Integer.parseInt(netCoreVertion);
             LogUtil.i("coreVer=="+coreVer+"//netVer=="+netVer);
             if (coreVer < netVer) {
            	 tv_upgrade_notice.setVisibility(View.GONE);
            	 iv_band_upgrade_title.setVisibility(View.VISIBLE);
             }
          }
	}

	@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_title:
                back();
                break;
            case R.id.band_layout1:
                BandConnectFragment bandConnectFragment = new BandConnectFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.layout_content, bandConnectFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.band_layout2:
            	bandUpgrade();
                break;
            case R.id.band_layout3:
                CorrectHeartrateFragment correctHeartrateFragment = new CorrectHeartrateFragment();
                FragmentTransaction transaction1 = getActivity().getSupportFragmentManager().beginTransaction();
                transaction1.replace(R.id.layout_content, correctHeartrateFragment);
                transaction1.addToBackStack(null);
                transaction1.commit();
                break;
            case R.id.band_layout4:
            	 BandUpFragment bandUpFragment = new BandUpFragment();
                 FragmentTransaction transactionUp = getActivity().getSupportFragmentManager().beginTransaction();
                 transactionUp.replace(R.id.layout_content, bandUpFragment);
                 transactionUp.addToBackStack(null);
                 transactionUp.commit();
            	break;
            case R.id.band_layout5:
            	 BandSleepFragment bandSleepFragment = new BandSleepFragment();
                 FragmentTransaction transactionSleep = getActivity().getSupportFragmentManager().beginTransaction();
                 transactionSleep.replace(R.id.layout_content, bandSleepFragment);
                 transactionSleep.addToBackStack(null);
                 transactionSleep.commit();
            	break;
            case R.id.band_layout6:
            	showDialogOff();
            	break;
        }
    }

	private void showDialogOff(){
		new AlertDialog.Builder(context).setTitle(getString(R.string.band_off_dialog))
	    .setIcon(android.R.drawable.ic_dialog_info) 
	    .setPositiveButton(getString(R.string.RemaindSetOK), new DialogInterface.OnClickListener() { 
	        @Override 
	        public void onClick(DialogInterface dialog, int which) { 
	        	BleApi1.BizApi.turnOff(context); 
	        } 
	    }) 
	    .setNegativeButton(getString(R.string.RemaindSetCancel), new DialogInterface.OnClickListener() { 
	        @Override 
	        public void onClick(DialogInterface dialog, int which) { 
	        } 
	    }).show(); 
	}

	private void bandUpgrade(){
		LogUtil.i("bandUpgrade   receiver=="+receiver);
		manualUpgrade = true;
		MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_SLIDING,null,null);
    	BandManager.getBandEfm32Version(context);
    	LogUtil.i("  手动升级命令=="+manualUpgrade);
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		manualUpgrade = false;
		if (receiver!=null){
		    context.unregisterReceiver(receiver);
		    receiver=null;
		}
	}
	
	 private BaseBroadcastReceiver receiver = new BaseBroadcastReceiver() {
	        @Override
	        public void onReceive1(Context context, Intent intent) throws Throwable {
	            String action = intent.getAction();
	            LogUtil.i("  手动升级接收广播action=="+action);
	            if (BAND_UPGRADE.equalsIgnoreCase(action) && manualUpgrade) {
	            	getActivity().finish();
	            }else if (BAND_NO_UPGRADE.equalsIgnoreCase(action) && manualUpgrade) {
					ToastUtil.shortShow(context, getString(R.string.band_no_upgrade));
				}
	        }
	    };
}
