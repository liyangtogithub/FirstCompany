package com.desay.iwan2.common.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import com.desay.fitband.R;
import com.desay.iwan2.common.server.TelephonyServer;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.common.server.ble.RemoveBond;
import com.desay.iwan2.module.start.StartActivity;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.common.os.MyUncaughtHandler;
import dolphin.tools.util.LogUtil;

/**
 * @author 方奕峰
 */
public class MyService extends Service {

    private MyBinder mBinder = new MyBinder();

    private BleManager bleManager;
    private TelephonyServer telephonyServer;

    private static MyService service;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("Service onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (BleServer.getInstance(this).initialize()) {
                if (BleServer.getInstance(this).getmBluetoothAdapter().isEnabled()) {
                    RemoveBond RemoveBond = new RemoveBond(this);
                    try {
                        RemoveBond.removeDevice();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e("" + e.getMessage());
                    }
                    if (BleUtil.checkBleEnable(this) && bleManager == null) {
                        bleManager = new BleManager(this);
                        bleManager.init();
                    }
                    bleManager.scanAndConnectBle();
                }
            }
//                bleManager.openAndConnectBle();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i("Service onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i("Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        service = this;
        if (MyUncaughtHandler.uncaughtHandlerToggle)
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        LogUtil.i("Service onCreate");

        if (BleUtil.checkBleEnable(this)) {
            bleManager = new BleManager(this);
            bleManager.init();
        }

        telephonyServer = new TelephonyServer(this);
        telephonyServer.monitorIncomingTelegram();

        Notification notification = new Notification();
        notification.icon = R.drawable.ic_launcher;
//        notification.tickerText = getString(R.string.app_name);
//        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        notification.setLatestEventInfo(this, getString(R.string.app_name), "打开" + getString(R.string.app_name),
                PendingIntent.getActivity(this, 0, new Intent(this, StartActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(1, notification);
        startForeground(1, notification);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        service = null;
        LogUtil.i("Service onDestroy");

        telephonyServer.stop();
//        DatabaseHelper.destroyDataBaseHelper();
        if (BleUtil.checkBleEnable(this)) {
            bleManager.release();
            bleManager = null;
        }
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    public static final void bind(Context context, ServiceConnection conn) {
        LogUtil.i("Service bind");
        Intent intent = new Intent(context, MyService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public static final void unbind(Context context, ServiceConnection conn) {
        LogUtil.i("Service bind");
        context.unbindService(conn);
    }

    public static final void start(Context context) {
        LogUtil.i("Service start");
        Intent intent = new Intent(context, MyService.class);
        context.startService(intent);
    }

    public static final void stop(Context context) {
        LogUtil.i("Service stop");
        Intent intent = new Intent(context, MyService.class);
        context.stopService(intent);
    }

    public static MyService getService() {
        return service;
    }

    public boolean isBleReady() {
        if (BleUtil.checkBleEnable(this)) {
            return bleManager.isReady();
        }
        return false;
    }
}
