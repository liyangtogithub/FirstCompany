package com.desay.iwan2.common.server.ble.handler;

import java.sql.SQLException;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.server.BtDevServer;
import com.desay.iwan2.common.server.LoginInfoServer;
import com.desay.iwan2.common.server.VersionServer;
import com.desay.iwan2.common.server.ble.BleCallback;
import com.desay.iwan2.common.server.ble.OrderQueue;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.loadfile.DownLoadHandle;
import com.desay.iwan2.module.loadfile.UpdateManager;
import com.desay.iwan2.util.MyBleUtil;

import dolphin.tools.ble.BleUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.R.integer;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;

public class DfuUpdate implements Runnable {
    public static boolean toDfuActivity = false;

    private Context context;
    private String notifyString;
    private BluetoothGatt gatt;
   // public static final String NORDIC_VERTION = "nordic_vertion";

    public DfuUpdate(Context context, String notifyString,BluetoothGatt gatt){
        this.context=context;
        this.notifyString = notifyString;
        this.gatt = gatt;
    }

	public void dfuNotify(Context context, String notifyString,BluetoothGatt gatt)
	{
		// 清除上条命令
		if (notifyString.indexOf("BT+UPGB") != -1)
		{
			BandManager.deleteOder(context, BleApi1.VersionApi.setDfuMode4Nordic);
			if (notifyString.indexOf("OK") != -1)
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				toDfuActivity = true;
				BleApi1.VersionApi.resetNordic(context);
			} else
			{
				BleApi1.VersionApi.setDfuMode4Nordic(context);
			}
		} else if ( notifyString.indexOf("BT+UPGE") != -1)
		{
			BandManager.deleteOder(context, BleApi1.VersionApi.setDfuMode4Efm32);
			if (notifyString.indexOf("OK") != -1)
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				toDfuActivity = true;
				BleApi1.VersionApi.resetNordic(context);
			} else
			{
				BleApi1.VersionApi.setDfuMode4Efm32(context);
			}
		} else if (notifyString.indexOf("BT+VER") != -1)
		{
			    BandManager.storeOrUpgradeNordic(context,notifyString);
		}
	}

    @Override
    public void run() {
        dfuNotify(context, notifyString,gatt);
    }
}
