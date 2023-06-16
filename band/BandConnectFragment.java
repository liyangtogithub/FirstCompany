package com.desay.iwan2.module.band;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.ble.BleCallback;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.help.HelpActivity;
import com.desay.iwan2.util.MyBleUtil;

import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import com.desay.iwan2.common.db.entity.Other;

import dolphin.tools.ble.BleUtil;
import dolphin.tools.common.os.VoidAsyncTask;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BandConnectFragment extends BaseFragment implements OnClickListener {

    private TemplateActivity act;
    private ImageView title_return;
    private TextView tv_title,tv_connnect_pass;
    private RelativeLayout relative_title;
    private HashMap<BluetoothDevice, Integer> scanDeviceCache = new HashMap<BluetoothDevice, Integer>();
    private BleServer bleServer = null;
    MacServer macServer = null;
    boolean macReplace = false;
    private Handler handler = new Handler();
    BluetoothDevice oldDevice = null;

    public static final String BROADCAST_BOND_OK = "com.desay.iwan2.bondOkReceiver";

    private void registerMyReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_BOND_OK);
        filter.addAction(MainFragment.UPDATE_CONNECT_STATE);
        getActivity().registerReceiver(myReceiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(BluetoothDevice.ACTION_FOUND);
        filter1.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter1);
    }

    private BroadcastReceiver myReceiver = new BaseBroadcastReceiver() {

        @Override
        public void onReceive1(Context context, Intent intent) {
            String action = intent.getAction();
            if (BROADCAST_BOND_OK.equals(action)) {
                ToastUtil.shortShow(context, getString(R.string.band_bond_success));
                layout_2.setVisibility(View.GONE);
                layout_3.setVisibility(View.VISIBLE);
            } else if (MainFragment.UPDATE_CONNECT_STATE.equals(action)) {
                boolean isConnect = intent.getBooleanExtra(BleCallback.CONNECT_STATE, false);
                if (isConnect) {
                    cancelProgress();
                } else {
                    connectLeFail();
                }
                layout_1.setVisibility(View.GONE);
                layout_2.setVisibility(View.VISIBLE);
            }
        }

    };

    private BaseBroadcastReceiver receiver = new BaseBroadcastReceiver() {
        @Override
        public void onReceive1(Context context, Intent intent) throws Throwable {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if (device.getName() != null && getString(R.string.band_name).equals(device.getName())) {
                    LogUtil.i("搜索到BLE设备:mac=" + device.getAddress() + ";name=" + device.getName() + ";rssi=" + rssi);
                    cacheScanDevice(device, (int) rssi);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (scanDeviceCache.size() > 0) {
                    ToastUtil.shortShow(context, getString(R.string.band_connecting2));
                    connectDevice(scanDeviceCache);
                } else {
                    ToastUtil.shortShow(context, getString(R.string.band_findless));
                    cancelProgress();
                }
            }
        }
    };

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = (TemplateActivity) getActivity();
        bleServer = BleServer.getInstance(act);
        bleServer = BleServer.getInstance(act);
        if (bleServer.initialize()) {
            if (!bleServer.getmBluetoothAdapter().isEnabled()) {
                BleUtil.requestTurnOnBt(act);
            }
        }

        View v = inflater.inflate(R.layout.band_connect_fragment, null);
        initView(v);
        registerMyReceiver();
        BleManager.reconnect(act, false);
        return v;
    }

    private View layout_1, layout_2, layout_3;

    private void initView(View v) throws SQLException {
        layout_1 = v.findViewById(R.id.layout_1);
        layout_2 = v.findViewById(R.id.layout_2);
        layout_3 = v.findViewById(R.id.layout_3);
        title_return = (ImageView) v.findViewById(R.id.title_return);
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.band_manage));
        relative_title = (RelativeLayout) v.findViewById(R.id.relative_title);
        tv_connnect_pass = (TextView) v.findViewById(R.id.tv_connnect_pass);

        v.findViewById(R.id.bt_connect).setOnClickListener(this);
        v.findViewById(R.id.bt_repeat).setOnClickListener(this);
        v.findViewById(R.id.bt_finish).setOnClickListener(this);
        tv_connnect_pass.setOnClickListener(this);
        firstOrPacificUi();

    }

    private void firstOrPacificUi() throws SQLException
	{
    	 OtherServer otherServer = new OtherServer(act);
    	 macServer = new MacServer(act);
    	 Other isFirstOther = otherServer.getOther(null, Other.Type.isFirst);
    	 String isFirststrString = isFirstOther==null?null:isFirstOther.getValue();
    	 LogUtil.i("isFirststrString =="+isFirststrString);
         if ( (macServer == null || macServer.getMac() == null
                 || "".equals(macServer.getMac()) )&& StringUtil.isBlank(isFirststrString) ) {
        	// 只有首次使用没有MAC会进入这里
        	 title_return.setVisibility(View.GONE);
         }else {
        	 tv_connnect_pass.setVisibility(View.GONE);
        	 relative_title.setOnClickListener(this);
		 }
         otherServer.createOrUpdate(null, Other.Type.isFirst, "notFirst");
	}

	@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_title:
                back();
                break;
            case R.id.bt_connect:
            case R.id.bt_repeat:
            	checkBand();
                //  startBand();
            	
                break;
            case R.id.bt_finish:
            	if (title_return.getVisibility() == View.GONE)
            	HelpActivity.gotoActivity(act);
            	getActivity().finish();
            	break;
            case R.id.tv_connnect_pass:
            	HelpActivity.gotoActivity(act);
            	getActivity().finish();
            	break;
        }
    }

    private void checkBand()
	{
    	if (bleServer.getmBluetoothAdapter() != null && !bleServer.getmBluetoothAdapter().isEnabled()) {
            ToastUtil.shortShow(act, getString(R.string.band_ble_closed));
//            BleUtil.requestTurnOnBt(act);
        } else {
            try {
                if (macServer == null || macServer.getMac() == null || "".equals(macServer.getMac()) || macReplace) {
                    startBand();
                } else {
                    new AlertDialog.Builder(act).setTitle(getString(R.string.band_replace))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton(getString(R.string.RemaindSetOK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    macReplace = true;
                                    startBand();
                                }
                            })
                            .setNegativeButton(getString(R.string.RemaindSetCancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}


	private void startBand(){
		if (bleServer.initialize() && bleServer.isScanning()) {
            bleServer.scanLeDevice(false);
        }
        act.showMatteLayer(true, getString(R.string.band_connecting1));
        scanLe();
        BandManager.enableBand = true;//绑定开始
	}

	private void connectLeFail() {
        cancelProgress();
        ToastUtil.shortShow(act, getString(R.string.band_connect_fail));
    }

    private void scanLe() {
        MyService service = MyService.getService();
        if (service != null && MyService.getService().isBleReady()) {
            if (bleServer.initialize()) {
                MyBleUtil.close(act);
//                bleServer.release();
                try {
                    if (bleServer.getmBluetoothAdapter().isDiscovering()) {
                        bleServer.getmBluetoothAdapter().cancelDiscovery();
                        Thread.sleep(1000);
                    }
                    if (bleServer.isScanning()) {
                        bleServer.scanLeDevice(false);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new VoidAsyncTask() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                                    @Override
                                    public void onLeScan(final BluetoothDevice device, int rssi,
                                                         byte[] scanRecord) {
                                        if (getString(R.string.band_name).equals(device.getName())) {
                                            LogUtil.i("搜索到BLE设备:mac=" + device.getAddress() + ";name=" + device.getName() + ";rssi=" + rssi);
                                            cacheScanDevice(device, rssi);
                                        }
                                    }
                                };
                                bleServer.scanLeDeviceForever(true, null, leScanCallback);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        bleServer.scanLeDeviceForever(false, null, leScanCallback);
                                        if (scanDeviceCache.size() > 0) {
                                            connectDevice(scanDeviceCache);
                                        } else {
                                            LogUtil.i("BLE搜索不到FITBAND，使用bt2.0搜索");
                                            bleServer.getmBluetoothAdapter().startDiscovery();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    bleServer.getmBluetoothAdapter().cancelDiscovery();
                                                }
                                            }, bleServer.DEFAULT_SCAN_PERIOD);
                                        }
                                    }
                                }, bleServer.DEFAULT_SCAN_PERIOD);
                                return super.doInBackground(params);
                            }
                        }.parallelExecute();
                    }
                }, 1000);
            }
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLe();
                }
            }, 2000);
        }
    }

    private void cancelProgress() {
        act.showMatteLayer(false);
        BleManager.reconnect(act, false);
    }

    private synchronized void connectDevice(Map<BluetoothDevice, Integer> map) {
        Integer maxRssi = null;
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice device : map.keySet()) {
            if (maxRssi == null) {
                maxRssi = map.get(device);
                targetDevice = device;
            } else {
                maxRssi = maxRssi > map.get(device) ? maxRssi : map.get(device);
            }
        }
        map.clear();
        if (targetDevice != null) {
            BandManager.enableBand = true;
            LogUtil.i("添加，连接,targetMac=" + targetDevice);
            if (!bleServer.connect(targetDevice.getAddress())) {
                LogUtil.i("connectDevice() 添加，尝试连接失败");
                connectLeFail();
            }
        }
    }

    private void cacheScanDevice(BluetoothDevice device, int rssi) {
        if (scanDeviceCache.size() > 0) {
            if (!scanDeviceCache.containsKey(device)) {
                if (scanDeviceCache.get(oldDevice) < rssi){
                	scanDeviceCache.clear();
                    scanDeviceCache.put(device, rssi);
                    oldDevice = device;
                }
            }
        } else {
            scanDeviceCache.put(device, rssi);
            oldDevice = device;
        }
    }

    @Override
    public void onDestroyView1() throws Throwable {
        if (myReceiver != null)
            act.unregisterReceiver(myReceiver);
        if (receiver != null)
            act.unregisterReceiver(receiver);
    }

    public static void bandConnectBroadcast(Context context, boolean isConnect) {
        Intent intent = new Intent();
        intent.setAction(MainFragment.UPDATE_CONNECT_STATE);
        intent.putExtra(BleCallback.CONNECT_STATE, isConnect);
        context.sendBroadcast(intent);
    }
}
