package com.desay.iwan2.common.app.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author 方奕峰
 */
public class SyncNetworkHandler extends BaseBroadcastReceiver {

    public static final String ACTION = SyncNetworkHandler.class.getName();
    public static final String DATA = "DATA";

    private Context context;

    public SyncNetworkHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive1(final Context context, Intent intent) throws Throwable{
        super.onReceive(context, intent);
        String action = intent.getAction();
    }

    public void start() {
        IntentFilter filter = new IntentFilter(ACTION);
        context.registerReceiver(this, filter);

//        if (looperThread == null) {
//            looperThread = new LooperThread();
//            looperThread.start();
//        }
    }

    public void stop() {
        context.unregisterReceiver(this);
    }

    public static void sync(Context context) {
        Intent intent = new Intent(ACTION);
        context.sendBroadcast(intent);
    }

//    private LooperThread looperThread;
//
//    private class LooperThread extends Thread {
//
//        public Handler handler;
//
//        @Override
//        public void run() {
//            Looper.prepare();
//            handler = new Handler(new Handler.Callback() {
//
//                @Override
//                public boolean handleMessage(Message msg) {
//                    return false;
//                }
//            });
//            Looper.loop();
//        }
//    }
}
