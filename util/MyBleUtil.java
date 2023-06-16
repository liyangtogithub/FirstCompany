package com.desay.iwan2.util;

import android.content.Context;
import com.desay.iwan2.common.server.ble.OrderQueue;
import dolphin.tools.ble.BleUtil;

/**
 * Created by 方奕峰 on 14-8-14.
 */
public class MyBleUtil {
    public static void disconnect(Context context){
        OrderQueue.clean(context);
        BleUtil.disconnect(context);
    }

    public static void close(Context context) {
        OrderQueue.clean(context);
        BleUtil.close(context);
    }
}
