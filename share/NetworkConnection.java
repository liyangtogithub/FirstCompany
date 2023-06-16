package com.desay.iwan2.module.share;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkConnection {
//	private Context _context;

	/*
	* �ж������Ƿ����� 
	* @return true/false 
	*/  
	public static boolean isConnectingToInternet(Context context){
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (connectivity != null){  
			NetworkInfo[] info = connectivity.getAllNetworkInfo();  
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true; 
					}  
				}  
			}  
		}  
		
		return false;
	}
	//�������ر�wifi
		public static boolean iswifiEnabled(Context context)
		{
			WifiManager	wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE); 
			boolean isOpen = wifiManager.isWifiEnabled(); 
            return isOpen;
		}
}
