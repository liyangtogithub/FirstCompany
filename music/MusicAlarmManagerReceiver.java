package com.desay.iwan2.module.music;

import com.desay.iwan2.common.server.ble.handler.BandManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MusicAlarmManagerReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent){
		BandManager.exitAllMusic( context);
	}
}
