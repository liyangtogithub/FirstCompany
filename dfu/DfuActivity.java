/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA. Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. This heading must NOT be removed from the file.
 ******************************************************************************/
package com.desay.iwan2.module.dfu;

import java.io.File;
import java.sql.SQLException;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.WindowManager;
import org.apache.commons.codec.binary.Hex;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.ble.handler.DfuUpdate;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.loadfile.UpdateManager;
import com.desay.iwan2.module.loadfile.Util;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.annotation.SuppressLint;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * DfuActivity is the main DFU activity It implements DFUManagerCallbacks to
 * receive callbacks from DFUManager class It implements
 * DeviceScannerFragment.OnDeviceSelectedListener callback to receive callback
 * when device is selected from scanning dialog The activity supports portrait
 * and landscape orientations
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DfuActivity extends TemplateActivity implements LoaderCallbacks<Cursor>,
		UploadCancelFragment.CancelFragmetnListener
{
	private static final String PREFS_DEVICE_NAME = "com.desay.iwan2.dfu.PREFS_DEVICE_NAME";
	//private static final String PREFS_FILE_NAME = "com.desay.iwan2.dfu.PREFS_FILE_NAME";
	//private static final String PREFS_FILE_SIZE = "com.desay.iwan2.dfu.PREFS_FILE_SIZE";

	public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
	public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
	public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";
	public static final String EXTRA_START_SERVICE = "start_service";
	//public static final String EXTRA_LOG_URI = "EXTRA_LOG_URI";

	private static final String EXTRA_URI = "uri";

	public static final int REQUEST_ENABLE_BT = 2;

	//private TextView mDeviceNameView;
	//private TextView mFileNameView;
	//private TextView mFileSizeView;
	//private TextView mFileStatusView;
	//private TextView mTextPercentage;
	//private TextView mTextUploading;
//	private AlertDialog ad;
    private ProgressBar mProgressBar;
    TextView tv_progress = null;
    TextView tv_file_name = null;
//    private static final int DOWNLOAD = 1;
//    private static final int DOWNLOAD_FINISH = 2;
//    private static final int DOWNLOAD_ERROR = 3;
    boolean dfuInProgress = false;

//	private Button  mUploadButton, mConnectButton;

//	private BluetoothDevice mSelectedDevice;
	/**
	 * 要下载的HEX文件的File路径
	 */
	private String mFilePath;
	/**
	 * 要下载的HEX文件的Uri地址
	 */
	//private Uri mFileStreamUri;
	/**
	 * 检查选中文件是否为bin文件
	 */
	private boolean mStatusOk;
	
	BluetoothAdapter adapter = null;
	/**
	 * 是否正扫�?
	 */
	private boolean mIsScanning = false;
	private Handler mHandler = null;
	public final static long SCAN_DURATION = 10000;
	//public static String mac = null; /*"DE:4A:17:86:A4:FC"*//*"FC:E7:9C:AA:E0:B2"*/
	public DfuService mBluetoothLeService;
	/**
	 * 蓝牙板当前模�?
	 */
	String macString =null;
	Context context=null;
	int model=0;
	
	/**
	 * 接收服务�?  处理过程�?
	 */
	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			// DFU is in progress or an error occurred
			final String action = intent.getAction();

			if (DfuService.BROADCAST_PROGRESS.equals(action))
			{
				final int progress = intent.getIntExtra(DfuService.EXTRA_DATA,0);
				updateProgressBar(progress, false);
			} else if (DfuService.BROADCAST_ERROR.equals(action))
			{
				final int error = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				updateProgressBar(error, true);
				// We have to wait a bit before canceling notification. This is
				// called before DfuService creates the last notification.
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						// if this activity is still open and upload process was
						// completed, cancel the notification
						final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						manager.cancel(DfuService.NOTIFICATION_ID);
					}
				}, 200);
			} 
		}
	};
	
	 private final BroadcastReceiver StartServiceReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            if (EXTRA_START_SERVICE.equals(action)) {
	            	startScan();
	            }
	        }
	    };

	@Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.dfu_activity_feature_dfu);
		context = DfuActivity.this;
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE))
		{
			showToast(R.string.no_ble);
			finish();
			return;
		}
		mHandler = new Handler();
		setGUI();
		if(!ensureSamplesExists()){
			finish();
			return;
		}
		try{
			macString = new MacServer(context).getMac();
		} catch (SQLException e){
			e.printStackTrace();
		}
		Intent intent = getIntent();
		boolean startService = intent.getBooleanExtra(EXTRA_START_SERVICE, false);
		if (startService)
			startScan();
		//showDownloadDialog(context);
	}
	
	private void setGUI()
	{
		tv_progress = (TextView) findViewById(R.id.tv_progress);
		tv_file_name = (TextView) findViewById(R.id.tv_file_name);
		mProgressBar = (ProgressBar) findViewById(R.id.pb1);
		mProgressBar.setProgress(0);
	}
	/**
	 * 只注册广�?
	 */
	@Override
	protected void onResume1() throws Throwable
	{
		// We are using LocalBroadcastReceiver instead of normal
		// BroadcastReceiver for optimization purposes
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager
				.getInstance(this);
		broadcastManager.registerReceiver(mDfuUpdateReceiver,
				makeDfuUpdateIntentFilter());
		IntentFilter filterStartService = new IntentFilter();
		filterStartService.addAction(EXTRA_START_SERVICE);
	    registerReceiver(StartServiceReceiver, filterStartService);
	}

	@Override
	protected void onPause1() throws Throwable
	{
		super.onPause1();
		DfuUpdate.toDfuActivity = false;
		UpdateManager.upgradeModel = MainFragment.HANDLE_UPGRADE_APP;//升级模式复位
	}
	
	@Override
	protected void onStop1() throws Throwable
	{
		stopService( new Intent(this, DfuService.class) );
	}
	
	@Override
	protected void onDestroy1()throws Throwable
	{
		clearUI();
		if (mIsScanning ||adapter!=null){
			adapter.stopLeScan(mLEScanCallback);
			adapter=null;
			mIsScanning = false;	
		}
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
		broadcastManager.unregisterReceiver(mDfuUpdateReceiver);
		if(StartServiceReceiver!=null)
		unregisterReceiver(StartServiceReceiver);
	}
	
	private static IntentFilter makeDfuUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_PROGRESS);
		intentFilter.addAction(DfuService.BROADCAST_ERROR);
		intentFilter.addAction(DfuService.BROADCAST_DFU_ENABLE);
		return intentFilter;
	}

	private void showToast(final int messageResId)
	{
		Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
	}

	private void showToast(final String message)
	{
		//Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		new Handler().post(new Runnable(){
			@Override
			public void run(){
				ToastUtil.shortShow(context, message);
			}});
		LogUtil.i("升级失败 Toast message=="+message);
		try
		{
			Thread.sleep(3500);
			finish();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private boolean isBLEEnabled()
	{
		 BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		 adapter = manager.getAdapter();
		return adapter != null && adapter.isEnabled();
	}
	/**
	 * 请求BLE是否使能的Dialog
	 */
	private void showBLEDialog()
	{
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
	/**
	 * 扫描BLE的Dialog
	 */
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_ENABLE_BT:
				try
				{
					Thread.sleep(2000);
					startScan();
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			break;
		
		}
	}
	
	/**
	 * 读SD卡上文件属性
	 */
	private boolean ensureSamplesExists()
	{
		String savePath = Util.getSdDirectory() + "/ds_download/";
		File file = null;
	   if (UpdateManager.upgradeModel ==  MainFragment.HANDLE_UPGRADE_NORDIC)
		{
			file = new File(savePath, "iwan2DFU.bin");
			tv_file_name.setText(getString(R.string.dfu_load_content,
					getString(R.string.band_upgrade_bluetooth)));
		} else if(UpdateManager.upgradeModel ==  MainFragment.HANDLE_UPGRADE_BAND)
		{
			file = new File(savePath, "iwan2EF.bin");
			tv_file_name.setText(getString(R.string.dfu_load_content,
					getString(R.string.band_upgrade_core)));
		}else {
			LogUtil.i("选错文件  UpdateManager.upgradeModel == "+UpdateManager.upgradeModel);
			return false;
		}
		mFilePath = file.getPath();//liyang
		ifLoadEnable(  file  );
		return mStatusOk;
	}
	/**
	 * 得到要下载的文件的路径，显示文件大小情况的UI，若蓝牙已连接，设置为可下载
	 * 
	 * @param rawResId 资源
	 * @param dest  路径
	 */
	private void ifLoadEnable(File file)
	{
		mStatusOk = "bin".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(mFilePath));
	}

    @Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Uri uri = args.getParcelable(EXTRA_URI);
		final String[] projection = new String[] {
				MediaStore.MediaColumns.DISPLAY_NAME,
				MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
		return new CursorLoader(this, uri, projection, null, null, null);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		mFilePath = null;
		mStatusOk = false;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		if (data.moveToNext())
		{
			final String fileName = data.getString(0 /* DISPLAY_NAME */);
			final int fileSize = data.getInt(1 /* SIZE */);
			final String filePath = data.getString(2 /* DATA */);
			if (!TextUtils.isEmpty(filePath)) mFilePath = filePath;
		}
	}

	/**启动服务下载文件
	 * Callback of UPDATE/CANCEL button on DfuActivity
	 */
	public void onUploadClicked(final View view)
	{
		goUpgradeService();
	}

	private void goUpgradeService()
	{// check whether the selected file is a HEX file (we are just checking
		// the extension)
		LogUtil.i("7777777777 goUpgradeService ");
		if (!mStatusOk)
		{
			LogUtil.i("不是bin文件 mStatusOk== "+mStatusOk);
			finish();
		}
		LogUtil.i("7777777777 mStatusOk== "+mStatusOk);
		try
		{
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		dfuInProgress = preferences.getBoolean(DfuService.PREFS_DFU_IN_PROGRESS, false);
		final Intent service = new Intent(this, DfuService.class);
		// 服务正在�?
		LogUtil.i("7777777777 dfuInProgress =="+dfuInProgress);
		if (dfuInProgress)
		{
			showUploadCancelDialoge();
			finish();
		}

		// Save current state in order to restore it if user quit the Activity
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFS_DEVICE_NAME, macString);
		//editor.putString(PREFS_FILE_NAME, mFileNameView.getText().toString());
	//	editor.putString(PREFS_FILE_SIZE, mFileSizeView.getText().toString());
		editor.commit();

		// 带着蓝牙信息和要下载的文件路径信息，去启动服务了
		//service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS,mSelectedDevice.getAddress());
		service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS,macString);
		service.putExtra(DfuService.EXTRA_DEVICE_NAME,getString(R.string.band_name));
		service.putExtra(DfuService.EXTRA_FILE_PATH, mFilePath);
		startService(service);
		LogUtil.i("7777777777 startService ==");
	}


	private void showUploadCancelDialoge()
	{
		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
		pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
		manager.sendBroadcast(pauseAction);

		UploadCancelFragment fragment = UploadCancelFragment.getInstance();
		fragment.show(getFragmentManager(),  "private void showUploadCancelDialoge()");
		
	}

	/**
	 * 搜索BLE蓝牙
	 * Callback of CONNECT/DISCONNECT button on DfuActivity
	 */

	/**
	 * 服务运行时，与设备的连接，下载等各个过程的UI反应
	 */
	private void updateProgressBar(final int progress, final boolean error)
	{
		switch (progress)
		{
			case DfuService.PROGRESS_CONNECTING:
				mProgressBar.setIndeterminate(true);
				//mTextPercentage.setText(R.string.dfu_status_connecting);
				break;
			case DfuService.PROGRESS_STARTING:
				mProgressBar.setIndeterminate(true);
				//mTextPercentage.setText(R.string.dfu_status_starting);
				break;
			case DfuService.PROGRESS_VALIDATING:
				mProgressBar.setIndeterminate(true);
				//mTextPercentage.setText(R.string.dfu_status_validating);
				break;
			case DfuService.PROGRESS_DISCONNECTING:
				mProgressBar.setIndeterminate(true);
				//mTextPercentage.setText(R.string.dfu_status_disconnecting);
				break;
			case DfuService.PROGRESS_COMPLETED:
				//mTextPercentage.setText(R.string.dfu_status_completed);
				// let's wait a bit until we reconnect to the device again.
				// Mainly because of the notification. When canceled immediately
				// it will be recreated by service again.
				//showFileTransferSuccessMessage();
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{

						// if this activity is still open and upload process was
						// completed, cancel the notification
						final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						manager.cancel(DfuService.NOTIFICATION_ID);
						ToastUtil.shortShow(context, getString(R.string.dfu_success_title));
					}
				}, 200);
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						finish();
					}
				}, 4000);
				break;
			default:// 失败
				mProgressBar.setIndeterminate(false);
				LogUtil.i("升级失败提示"+error);
				if (error)
				{
					showErrorMessage(progress);
				}
				else
				{
					 tv_progress.setText((99-progress) +"%");
					 mProgressBar.setProgress(progress);
					//mTextPercentage.setText(getString(R.string.progress,
					//		progress));
				}
				break;
		}
	}
	

	@Override
	public void onUploadCanceled()
	{
		clearUI();
		stopService(new Intent(this, DfuService.class));
		showToast(getString(R.string.dfu_cancel));
	}

	private void showErrorMessage(final int code)
	{
		clearUI();
		showToast(getString(R.string.dfu_fail_title) /*+ GattError.parse(code)*/ );
	}

	private void clearUI()
	{
		final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(DfuService.NOTIFICATION_ID);
	}
	
	private void startScan(){
		if (isBLEEnabled()){
			mIsScanning = true;
			LogUtil.i("startScan()");
			adapter.startLeScan(mLEScanCallback);
			mHandler.postDelayed(new Runnable(){
			  @Override
				public void run(){
						stopScan();
				}
			}, SCAN_DURATION);
		}else {
			
		}
		
	 
	}

	/**
	 * Stop scan if user tap Cancel button
	 */
	private void stopScan()
	{
		LogUtil.i("stopScan()  mIsScanning=="+mIsScanning+"****model=="+model);
		if (mIsScanning)
		{
			mIsScanning = false;
			adapter.stopLeScan(mLEScanCallback);
			if (model!=0)
			goUpgradeService();
			else {
				LogUtil.i("手环为正常模式");
				finish();
			}
			
		}
	}
	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be
	 * used to filter devices with custom BLE service UUID then the device will
	 * be added in a list
	 */
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
			LogUtil.i("DfuActivity  device.getAddress() = "+device.getAddress());
			LogUtil.i("DfuActivity  device.getAddress().equalsIgnoreCase(macString) = "+macString.equalsIgnoreCase(device.getAddress()));
			if ( device != null && macString.equalsIgnoreCase(device.getAddress()) ){
				model = checkModel(device,scanRecord);
				if (model!=0){
				stopScan();
				LogUtil.i("111111111111111111 LeScanCallback");
				}
				else{
					  LogUtil.i("手环未启动升级模式");
					  if (mIsScanning||adapter!=null){
						mIsScanning = false;
						adapter.stopLeScan(mLEScanCallback);
						adapter=null;
						finish();
					  }
				}
			}
		}
	};
	
	public static int checkModel(BluetoothDevice device, byte[] scanRecord)
	{
			int len = scanRecord.length;
			String scanHex = new String(Hex.encodeHex(scanRecord));
			LogUtil.i("DEBUG  len: " + len + " data:" + scanHex);
			String address = device.getAddress();
			    
		    String[] addArray = address.split(":");
			String checkString = ("0703"+addArray[1]+addArray[0]+addArray[3]+addArray[2]+addArray[5]+
					              addArray[4]).toLowerCase();
			int  model = Integer.parseInt(scanHex.substring(scanHex.indexOf(checkString)-2, scanHex.indexOf(checkString)));
			LogUtil.i("model = "+model);
			return model;
	}
	
	
	  public static void toNewActivity(Context context,boolean start) {
		  Intent intent = new  Intent(context, DfuActivity.class);
      	  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      	  intent.putExtra(EXTRA_START_SERVICE, start);
      	  context.startActivity(intent );
	    }
}
