package com.desay.iwan2.common.app.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dolphin.tools.common.os.MyUncaughtHandler;

public class BaseBroadcastReceiver extends BroadcastReceiver {

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (MyUncaughtHandler.uncaughtHandlerToggle)
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());

        try {
            onReceive1(context, intent);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onReceive1(Context context, Intent intent) throws Throwable{
        
    }
}
