package com.desay.iwan2.common.server.ble;

import java.lang.reflect.Method;
import java.util.Set;

import com.desay.fitband.R;

import dolphin.tools.ble.BleServer;
import dolphin.tools.util.LogUtil;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class RemoveBond {
    Context context;

    public RemoveBond(Context context) {
        this.context = context;
    }

    public void removeDevice() throws Exception {
        Set<BluetoothDevice> pairedDevices = BleServer.getInstance(context).getmBluetoothAdapter().getBondedDevices();
        LogUtil.i(" pairedDevices.size()  :" + pairedDevices.size());
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice bondDevice : pairedDevices) {
                if (context.getString(R.string.band_name).equalsIgnoreCase(bondDevice.getName())) {
                    boolean a = removeBond(bondDevice.getClass(), bondDevice);
                    LogUtil.i(" remove :" + a);
                }
            }
        }
    }

    private boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue;
    }
}
