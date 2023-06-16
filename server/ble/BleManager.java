package com.desay.iwan2.common.server.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.server.BtDevServer;
import com.desay.iwan2.common.server.ble.handler.DfuUpdate;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.dfu.DfuActivity;
import com.desay.iwan2.util.MyBleUtil;
import com.desay.iwan2.util.SharePreferencesUtil;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.common.os.VoidAsyncTask;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 方奕峰 on 14-8-12.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManager implements Handler.Callback {

    public static final String ACTION_RECONNECT = BleManager.class.getName() + ".reconnect";

    private Context context;
    private OrderQueue orderQueue;
    private BaseBroadcastReceiver receiver;
    private BleServer bleServer;
    private boolean isReady = false;
    private Handler handler;
    private static boolean reconnectFlag = true;
    private boolean isScanning = false;
    String macString = null;
    public static final String IS_DFU_MODEL_NAME = "isDfuModel";
    public static final String WHICH_DFU_MODEL_NAME = "whichDfuModel";

    public BleManager(Context context) {
        this.context = context;
        bleServer = BleServer.getInstance(context);
    }

    private boolean initBleServer() {
        bleServer = BleServer.getInstance(context);
        if (BleUtil.checkBleEnable(context)) {
            if (bleServer.getBleCallback() == null) {
//                if (bleCallback==null) {
//                    bleCallback = new BleCallback(context);
//                }
//                bleServer.setBleCallback(bleCallback);
                bleServer.setBleCallback(new BleCallback(context));
            }
            if (bleServer.initialize()) {
                isReady = true;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

//    private BleCallback bleCallback;

    public void init() {
        if (BleUtil.checkBleEnable(context)) {
            if (bleServer.getBleCallback() == null) {
//                if (bleCallback==null) {
//                    bleCallback = new BleCallback(context);
//                }
//                bleServer.setBleCallback(bleCallback);
                bleServer.setBleCallback(new BleCallback(context));
            }
            if (bleServer.initialize()) {
                orderQueue = new OrderQueue(context);
                orderQueue.start();
                isReady = true;
            }

            receiver = new BaseBroadcastReceiver() {
                @Override
                public void onReceive1(Context context, Intent intent) throws Throwable {
                    if (DfuUpdate.toDfuActivity == true)
                        return;
                    String action = intent.getAction();
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_TURNING_ON);
                        if (state == BluetoothAdapter.STATE_ON)
                            scanAndConnectBle();
                        else if (state == BluetoothAdapter.STATE_OFF) {
                            isReady = false;
                            MyBleUtil.close(context);
                            bleServer.release();
                        }
                    } else if (ACTION_RECONNECT.equals(action)) {
                        if (intent.getBooleanExtra("flag", true)) {
                            reconnect();
                        } else {
//                            bleServer.scanLeDevice(false);
                            stopScan();

                        }
                    } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                        Locale locale = context.getResources().getConfiguration().locale;
                        boolean isChinese = (Locale.CHINA.equals(locale) || Locale.CHINESE.equals(locale) ||
                                Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale));
                        BleApi1.BizApi.setLan(context, isChinese);
                    } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action)) {
                        BleApi1.BizApi.setTime(context, SystemContant.timeFormat7.format(new Date()));
                    }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(ACTION_RECONNECT);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
//            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            context.registerReceiver(receiver, filter);
        }

        handler = new Handler(this);
    }

    public void openAndConnectBle() {
        LogUtil.i("BLE manager openAndConnectBle");
        if (BleUtil.checkBleEnable(context)) {
            if (bleServer.getmBluetoothAdapter().isEnabled()) {
                scanAndConnectBle();
            } else if (BluetoothAdapter.STATE_TURNING_ON != bleServer.getmBluetoothAdapter().getState()) {
                BleUtil.turnOnBtDirect(context);
            }
        }
    }

    public void scanAndConnectBle() {
        LogUtil.i("BLE manager scanAndConnectBle");
        if (BleUtil.checkBleEnable(context)) {
            if (bleServer.initialize()) {
                if (bleServer.getmBluetoothAdapter().isDiscovering()) {
                    LogUtil.i("BleManager scanAndConnectBle discovering");
                    delayScanAndConnectBle();
                    return;
                }
                connectBle();
            } else {
                LogUtil.i("BleManager bleServer not initialize");
                delayScanAndConnectBle();
            }
        }
    }

    private void delayScanAndConnectBle() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                scanAndConnectBle();
//            }
//        }, 2000);
        handler.sendEmptyMessageDelayed(CASE_SCAN, 2000);
    }

    private void connectBle() {
        if (initBleServer()) {
            if (BleConnectState.DISCONNECTED == bleServer.mConnectionState) {
                new VoidAsyncTask() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            BtDev btDev = new BtDevServer(context).getBtDev(null);
                            if (btDev == null) {
                                handler.removeMessages(CASE_SCAN);
                            } else {
                                macString = btDev.getMac();
                                if (StringUtil.isBlank(macString)) {
                                    handler.removeMessages(CASE_SCAN);
                                } else {
                                    LogUtil.i(BleConnectState.DISCONNECTED == bleServer.mConnectionState ? "DISCONNECTED" : "CONNECTED");
                                    startScan();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return super.doInBackground(params);
                    }
                }.parallelExecute();
            }
        }
    }

    private void startScan() {
        isScanning = true;
        BluetoothAdapter bluetoothAdapter = BleServer.getInstance(context).getmBluetoothAdapter();
        bluetoothAdapter.startLeScan(mLEScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null && macString.equalsIgnoreCase(device.getAddress())) {
                stopScan();
//                LogUtil.i("111111111111111111 bt state="+
//                        BleServer.getInstance(context).getmBluetoothAdapter().getState()+
//                        " ; "+BleServer.getInstance(context).getmBluetoothAdapter().isEnabled());
//                if (!BleServer.getInstance(context).getmBluetoothAdapter().isEnabled())return;
                final int model = DfuActivity.checkModel(device, scanRecord);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        goUpgradeOrNormal(model);
                    }
                });
            }
        }
    };

    private void stopScan() {
        LogUtil.i("BleManager initBleServer()==" + initBleServer() + " isScanning==" + isScanning);
        handler.removeMessages(CASE_SCAN);
        if (initBleServer() && isScanning) {
            BleServer.getInstance(context).getmBluetoothAdapter().stopLeScan(mLEScanCallback);
            isScanning = false;
        }
    }

    private void goUpgradeOrNormal(int model) {
        Intent intent = new Intent(MainFragment.UPGRADE_DIALOG);
        intent.putExtra(IS_DFU_MODEL_NAME, true);

        if (model == 0) {
            BleUtil.connect(context, macString);
        } else if (model == 1) {
            intent.putExtra(WHICH_DFU_MODEL_NAME, MainFragment.HANDLE_UPGRADE_NORDIC);
            context.sendBroadcast(intent);
        } else if (model == 2) {
            intent.putExtra(WHICH_DFU_MODEL_NAME, MainFragment.HANDLE_UPGRADE_BAND);
            context.sendBroadcast(intent);
        }
    }

    public void release() {
        if (BleUtil.checkBleEnable(context)) {
            orderQueue.stop();
            stopScan();
            MyBleUtil.close(context);
        }
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public void reconnect() {
//        BtDev btDev = new BtDevServer(context).getBtDev(null);
//        if (btDev != null) {
//            BleUtil.scanUntilConnect(context, btDev.getMac(), null);
//        }
        if (reconnectFlag) {
            LogUtil.i("重连BLE");
            connectBle();
        }
    }

    public static synchronized void reconnect(Context context, boolean flag) {
        Intent intent = new Intent(ACTION_RECONNECT);
        intent.putExtra("flag", flag);
        context.sendBroadcast(intent);
    }

    public static void setReconnectFlag(boolean flag) {
        reconnectFlag = flag;
    }

    private final int CASE_SCAN = 1;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CASE_SCAN:
                scanAndConnectBle();
                break;
        }
        return false;
    }

    public static boolean getOriginalBtState(Context context) {
        return SharePreferencesUtil.getSharedPreferences(context).getBoolean("btState", false);
    }

    public static void updateOriginalBtState(Context context) {
        SharePreferencesUtil.getSharedPreferences(context).edit()
                .putBoolean("btState", BluetoothAdapter.getDefaultAdapter().isEnabled())
                .commit();
    }
}
