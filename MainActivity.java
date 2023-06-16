package com.desay.iwan2.module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.StatService;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.BaseSlidingActivity;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.server.SocialServer;
import com.desay.iwan2.common.server.SyncServer;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.common.server.ble.handler.DfuUpdate;
import com.desay.iwan2.module.dfu.DfuActivity;
import com.desay.iwan2.module.dfu.DfuService;
import com.desay.iwan2.module.menu.MenuFragment;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;

import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.common.os.VoidAsyncTask;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.sql.SQLException;

/**
 * @author 方奕峰
 */
public class MainActivity extends BaseSlidingActivity implements
        MainFragment.ShowSlidInterface, Handler.Callback {

    private MainFragment fragment_content;
    static Activity activity = null;
    static Context context;
    private Handler handler;
    boolean reseverEnable = false;

    private BaseBroadcastReceiver receiver;
    public static final String ACTION = MainActivity.class.getName() + ".ACTION";
    public static final int CASE_SHOW_DIALOG_1 = 0x01;
    public static final int CASE_DISMISS_DIALOG_1 = 0x00;
    public static final int CASE_SHOW_DIALOG_2 = 0x11;
    public static final int CASE_DISMISS_DIALOG_2 = 0x10;
    public static final int CASE_DISMISS_SLIDING = 0x100;

    @Override
    public void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
//        boolean isLoadHistory=getIntent().getBooleanExtra("isLoadHistory", false);

        ShareSDK.initSDK(this);
        activity = MainActivity.this;
        context = MainActivity.this;
//        fragment_content = new MainFragment(isLoadHistory);
        fragment_content = new MainFragment();
        fragment_content.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_content, fragment_content).commit();
        setBehindContentView(R.layout.main_menu_view);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_menu, new MenuFragment()).commit();
        IntentFilter filterShare = new IntentFilter();
        filterShare.addAction(OnekeyShare.FITBAND_BROADCAST_SHARE);
        registerReceiver(ShareReceiver, filterShare);
        IntentFilter filterBlueTooth = new IntentFilter();
        filterBlueTooth.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(BlueToothReceiver, filterBlueTooth);
        handler = new Handler(this);

        receiver = new BaseBroadcastReceiver() {
            boolean flag = true;

            @Override
            public void onReceive1(Context context, Intent intent) throws Throwable {
                String action = intent.getAction();
                if (ACTION.equals(action)) {
                    int aCase = intent.getIntExtra("case", 0);
                    switch (aCase) {
                        case CASE_SHOW_DIALOG_1:
                            if (flag) {
                                flag = false;
                                showMatteLayer(true);
//                                ToastUtil.shortShow(context, "正在同步手环数据");
                            }
                            break;
                        case CASE_DISMISS_DIALOG_1:
                            String titleDialog = intent.getStringExtra("title");
                            if (titleDialog != null && getString(R.string.Label_downloadSuccess).equals(titleDialog))
                                ToastUtil.shortShow(context, getString(R.string.Label_downloadSuccess));
                            else {
                                ToastUtil.shortShow(context, getString(R.string.Label_downloadFail));
                            }
                            flag = true;
                            showMatteLayer(false);
                            break;
                        case CASE_SHOW_DIALOG_2:
                            int progress = intent.getIntExtra("progress", 0);
                            String title = intent.getStringExtra("title");

                            showProgressBarMatteLayer(true, progress, StringUtil.isBlank(title) ? getString(R.string.band_sync_data) : title);

                            handler.removeMessages(CASE_CANCEL_DIALOG);
                            Message msg = new Message();
                            msg.what = CASE_CANCEL_DIALOG;
                            handler.sendMessageDelayed(msg, progress == 100 ? 3000 : SystemContant.syncTimeout);
                            break;
                        case CASE_DISMISS_DIALOG_2:
                            showProgressBarMatteLayer(false, 0);
                            break;
                        case CASE_DISMISS_SLIDING:
                            toggle();
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION);
        registerReceiver(receiver, filter);

//        new VoidAsyncTask() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    SyncServer syncServer = SyncServer.getInstance(MainActivity.this);
////                    syncServer.syncBle();
//                    syncServer.syncNetwork();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                return super.doInBackground(params);
//            }
//        }.parallelExecute();

//        LogUtil.i("密度="+getResources().getDisplayMetrics().density);
//        LogUtil.i("densityDpi="+getResources().getDisplayMetrics().densityDpi);
//        LogUtil.i("w="+getWindowManager().getDefaultDisplay().getWidth());
//        LogUtil.i("h="+getWindowManager().getDefaultDisplay().getHeight());

        //TODO 恢复退出app退出服务
//        if (BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState){
//            BandManager.getBandEfm32Version(context);
//            BleApi1.VersionApi.getNordicVersion(context);
//        }
    }

    private final int CASE_CANCEL_DIALOG = 1;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CASE_CANCEL_DIALOG:
//                if (!BandManager.enableBand)
                sendBroadcast(MainActivity.this, MainActivity.CASE_DISMISS_DIALOG_2, null, null);
                break;
        }
        return false;
    }

    public static void sendBroadcast(Context context, int c, Integer progress, String title) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("case", c);
        intent.putExtra("progress", progress);
        intent.putExtra("title", title);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        StatService.onResume(this);
//        MyService.bind(this, serviceConnection);
        if (BleUtil.checkBleEnable(context)) {
            BleServer bleServer = BleServer.getInstance(context);
            if (bleServer.initialize()) {
                if (bleServer.getmBluetoothAdapter().isEnabled()) {
                } else if (userEnableBtFlag) {
                    BleUtil.requestTurnOnBt(this);
                }
            }
            MyService.start(this);
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        StatService.onPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
//        MyService.unbind(this, serviceConnection);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // unbindService(serviceConnection);
        if (receiver != null) unregisterReceiver(receiver);
//        MyService.stop(this);
        ShareSDK.stopSDK(this);
        if (BlueToothReceiver != null)
            unregisterReceiver(BlueToothReceiver);
        if (ShareReceiver != null)
            unregisterReceiver(ShareReceiver);
        super.onDestroy();
    }

    private boolean userEnableBtFlag = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BleUtil.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
//                    MyService.start(this);
                } else {
                    userEnableBtFlag = false;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean flagExit = false;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (flagExit) {
                MyService.stop(this);//TODO 退出APP不关闭后台服务
                finish();
            } else {
                flagExit = true;
                ToastUtil.shortShow(this, getString(R.string.main_exit)
                        + getString(R.string.app_name));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flagExit = false;
                    }
                }, 2000);
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void finish() {
//        if (!BleManager.getOriginalBtState(this)) {
//            BluetoothAdapter.getDefaultAdapter().disable();
//        }
        if (BandManager.soundPoolBeep != null)
            BandManager.soundPoolBeep.release();//释放找手机音乐资源
        super.finish();
    }

    public static void gotoActivity(Context packageContext) {
        gotoActivity(packageContext, false);
    }

    public static void gotoActivity(Context packageContext, boolean isLoadHistory) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        intent.putExtra("isLoadHistory", isLoadHistory);
        packageContext.startActivity(intent);
    }

    @Override
    public void showSlid() {
        toggle();
    }

    public static void closeActivity() {
        if (activity != null)
            activity.finish();
    }

    private final BroadcastReceiver BlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                LogUtil.i("蓝牙 广播 有效性==" + reseverEnable);
                LogUtil.i("蓝牙 状态==" + BluetoothAdapter.getDefaultAdapter().isEnabled());
                try {
                    if (DfuUpdate.toDfuActivity == true && reseverEnable) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            LogUtil.i("蓝牙准备开 ");
                            Thread.sleep(1000);
                            BluetoothAdapter.getDefaultAdapter().enable();
                        } else if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            sendBroadcast(new Intent(DfuActivity.EXTRA_START_SERVICE));
                        }
                    }else if ( DfuService.connectedException==true && reseverEnable) {
                    	if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
                    	     BluetoothAdapter.getDefaultAdapter().enable();
                    	     DfuService.connectedException=false ;
                    	}
					} 
                    reseverEnable = (reseverEnable == true ? false : true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private final BroadcastReceiver ShareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OnekeyShare.FITBAND_BROADCAST_SHARE.equals(action)) {
                LogUtil.i("分享信息广播收到");
                int id = intent.getIntExtra("id", 0);
                String userId = intent.getStringExtra("userId");
                if (id != 0 && userId != null) {
                    LogUtil.i("platform.getUserId()==" + userId);
                    LogUtil.i("platform.getId()==" + id);
                    String gevent = "010201";//sina
                    if (id == 3)
                        gevent = "010203";   //qq
                    else if (id == 4 || id == 5) {
                        gevent = "010202";   //wechat
                    }
                    try {
                        new SocialServer(activity).commitShare(gevent, userId, null);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
}
