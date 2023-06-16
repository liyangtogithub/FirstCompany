package com.desay.iwan2.module.dfu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import com.desay.fitband.R;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.ble.BleGattUtils;
import com.desay.iwan2.common.server.ble.handler.DfuUpdate;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.band.BandManageFragment;
import com.desay.iwan2.module.loadfile.UpdateManager;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
@SuppressLint("NewApi")
public class DfuService extends IntentService
{
	private static final String TAG = "DfuService";

	public static final String EXTRA_DEVICE_ADDRESS = "com.desay.iwan2.dfu.EXTRA_DEVICE_ADDRESS";
	public static final String EXTRA_DEVICE_NAME = "com.desay.iwan2.dfu.EXTRA_DEVICE_NAME";
	public static final String EXTRA_FILE_PATH = "com.desay.iwan2.dfu.EXTRA_FILE_PATH";
	public static final String EXTRA_FILE_URI = "com.desay.iwan2.dfu.EXTRA_FILE_URI";
	public static final String EXTRA_DATA = "com.desay.iwan2.dfu.EXTRA_DATA";

	public static final String PREFS_DFU_IN_PROGRESS = "com.desay.iwan2.dfu.PREFS_DFU_IN_PROGRESS";

	public static final String BROADCAST_ERROR = "com.desay.iwan2.dfu.BROADCAST_ERROR";
	public static final int ERROR_MASK = 0x0100;
	public static final int ERROR_DEVICE_DISCONNECTED = ERROR_MASK | 0x00;
	public static final int ERROR_FILE_NOT_FOUND = ERROR_MASK | 0x01;
	public static final int ERROR_FILE_CLOSED = ERROR_MASK | 0x02;
	public static final int ERROR_FILE_INVALID = ERROR_MASK | 0x03;
	public static final int ERROR_FILE_IO_EXCEPTION = ERROR_MASK | 0x04;
	public static final int ERROR_SERVICE_DISCOVERY_NOT_STARTED = ERROR_MASK | 0x05;
	public static final int ERROR_UNSUPPORTED_DEVICE = ERROR_MASK | 0x06;
	/** Look for DFU specification to get error codes */
	public static final int ERROR_REMOTE_MASK = 0x0200;
	public static final int ERROR_CONNECTION_MASK = 0x0400;

	public static final String BROADCAST_PROGRESS = "com.desay.iwan2.dfu.BROADCAST_PROGRESS";
	public static final int PROGRESS_CONNECTING = -1;
	public static final int PROGRESS_STARTING = -2;
	public static final int PROGRESS_VALIDATING = -4;
	public static final int PROGRESS_DISCONNECTING = -5;
	public static final int PROGRESS_COMPLETED = -6;
	public static final int PROGRESS_ABORTED = -7;

	/**
	 * The log events are only broadcasted when there is no LogView application
	 * installed
	 */
	public static final String BROADCAST_DFU_ENABLE = "com.desay.iwan2.dfu.DFU_ENABLE";
	public static final String BROADCAST_START_SCAN = "com.desay.iwan2.dfu.START_SCAN";

	/**
	 * Activity may broadcast this broadcast in order to pause, resume or abort
	 * DFU process
	 */
	public static final String BROADCAST_ACTION = "com.desay.iwan2.dfu.BROADCAST_ACTION";
	public static final String EXTRA_ACTION = "com.desay.iwan2.dfu.EXTRA_ACTION";
	public static final int ACTION_PAUSE = 0;
	public static final int ACTION_RESUME = 1;
	public static final int ACTION_ABORT = 2;

	public static final int NOTIFICATION_ID = 283; // a random number

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	private String mDeviceAddress;
	private String mDeviceName;
	/** Lock used in synchronization purposes */
	private final Object mLock = new Object();

	/**
	 * The number of the last error that has occurred or 0 if there was no error
	 */
	private int mErrorState;
	/**
	 * The current connection state. If its value is > 0 than an error has
	 * occurred. Error number is a negative value of mConnectionState
	 */
	private int mConnectionState;
	private final  int STATE_DISCONNECTED = 0;
	private final  int STATE_CONNECTING = -1;
	private final  int STATE_CONNECTED = -2;
	private final  int STATE_CONNECTED_AND_READY = -3; // indicates that
																// services were
																// discovered
	private final  int STATE_DISCONNECTING = -4;
	private final  int STATE_CLOSED = -5;

	/**
	 * Flag set when we got confirmation from the device that notifications are
	 * enabled.
	 */
	private boolean mNotificationsEnabled;

	private final  int MAX_PACKET_SIZE = 20; // the maximum number of
													// bytes in one packet is
													// 20. May be less.
	/**
	 * The number of packets of firmware data to be send before receiving a new
	 * Packets receipt notification. 0 disables the packets notifications
	 */
	private int mPacketsBeforeNotification = 20;

	private byte[] mBuffer = new byte[MAX_PACKET_SIZE];
	InputStream inputStream = null;
	/**
	 * 文件总大小
	 */
	private int fileSizeInBytes;
	private int mBytesSent;
	private int mPacketsSendSinceNotification;
	private boolean mPaused;
	private boolean mAborted;

	/**
	 * Flag indicating whether the image size has been already transfered or not
	 */
	private boolean mImageSizeSent;
	/** Flag indicating whether the request was completed or not */
	private boolean mRequestCompleted;

	/** Latest data received from device using notification. */
	private byte[] mReceivedData = null;
	private static final int OP_CODE_RECEIVE_START_DFU_KEY = 0x01; // 1
	private static final int OP_CODE_INIT_DFU_PARAMS_KEY = 0x02; // 2
	private static final int OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY = 0x03; // 3
	private static final int OP_CODE_RECEIVE_VALIDATE_KEY = 0x04; // 4
	private static final int OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY = 0x05; // 5
	private static final int OP_CODE_RECEIVE_RESET_KEY = 0x06; // 6
	// private static final int OP_CODE_PACKET_REPORT_RECEIVED_IMAGE_SIZE_KEY =
	// 0x07; // 7
	private static final int OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY = 0x08; // 8
	private static final int OP_CODE_RESPONSE_CODE_KEY = 0x10; // 16
	private static final int OP_CODE_PACKET_RECEIPT_NOTIF_KEY = 0x11; // 11
	private static final byte[] OP_CODE_START_DFU = new byte[] { OP_CODE_RECEIVE_START_DFU_KEY };
	private static final byte[] OP_CODE_INIT_DFU_PARAMS = new byte[] { OP_CODE_INIT_DFU_PARAMS_KEY };
	private static final byte[] OP_CODE_RECEIVE_FIRMWARE_IMAGE = new byte[] { OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY };
	private static final byte[] OP_CODE_VALIDATE = new byte[] { OP_CODE_RECEIVE_VALIDATE_KEY };
	private static final byte[] OP_CODE_ACTIVATE_AND_RESET = new byte[] { OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY };
	private static final byte[] OP_CODE_RESET = new byte[] { OP_CODE_RECEIVE_RESET_KEY };
	// private static final byte[] OP_CODE_REPORT_RECEIVED_IMAGE_SIZE = new
	// byte[] { OP_CODE_PACKET_REPORT_RECEIVED_IMAGE_SIZE_KEY };
	private static final byte[] OP_CODE_PACKET_RECEIPT_NOTIF_REQ = new byte[] {
			OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY, 0x00, 0x00 };

	public static final int DFU_STATUS_SUCCESS = 1;
	public static final int DFU_STATUS_INVALID_STATE = 2;
	public static final int DFU_STATUS_NOT_SUPPORTED = 3;
	public static final int DFU_STATUS_DATA_SIZE_EXCEEDS_LIMIT = 4;
	public static final int DFU_STATUS_CRC_ERROR = 5;
	public static final int DFU_STATUS_OPERATION_FAILED = 6;

	//public static final UUID EFM_SERVICE_UUID = UUID.fromString("0000190C-0000-1000-8000-00805f9b34fb");
	//public static final UUID EFM_SEND_UUID = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
	//public static final UUID EFM_RECEIVE_UUID = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
	public static final UUID BOARD_SERVICE_UUID = UUID.fromString("0000190C-0000-1000-8000-00805f9b34fb");
	private static final UUID BOARD_SEND_UUID = UUID.fromString("00000005-0000-1000-8000-00805f9b34fb");
	private static final UUID BOARD_STATE_UUID = UUID.fromString("00000006-0000-1000-8000-00805f9b34fb");
	public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	String receiveString = null;
	public static boolean connectedException = false;
	

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
	{
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt,
				final int status, final int newState)
		{
			LogUtil.i("22222222222 BluetoothGattCallback status ="+status+"newState ="+newState);
			// check whether an error occurred
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				if (newState == BluetoothGatt.STATE_CONNECTED)
				{
					mConnectionState = STATE_CONNECTED;
					
					LogUtil.i("22222222222 BluetoothGattCallback 连接后，找服务");
					final boolean success = mBluetoothGatt.discoverServices();

					if (!success)
					{
						mErrorState = -ERROR_SERVICE_DISCOVERY_NOT_STARTED;
					} else
					{
						// just return here, lock will be notified when service
						// discovery finishes
						return;
					}
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED)
				{
					//sendStartScanBroadcast();
					LogUtil.i("22222222222 BluetoothGattCallback 连接断开");
					mConnectionState = STATE_DISCONNECTED;
				}
			} else
			{
				mErrorState = ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock)
			{
				mLock.notifyAll();
				return;
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt,
				final int status)
		{
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				LogUtil.i("22222222222 onServicesDiscovered 服务OK");
				 List<BluetoothGattService> services = gatt.getServices();
				 for (BluetoothGattService s : services) {
		                List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();
		                LogUtil.i("服务:" + s.getUuid());
		                for (BluetoothGattCharacteristic c : characteristics) {
		                    LogUtil.i(c.getUuid().toString());
		                }
		            }
				mConnectionState = STATE_CONNECTED_AND_READY;
				   DfuUpdate.toDfuActivity = false;				
			} else
			{
				mErrorState = ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock)
			{
				mLock.notifyAll();
				return;
			}
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt,
				final BluetoothGattDescriptor descriptor, final int status)
		{
			LogUtil.i("打开通知情况  0为成功  status=="+status);
			
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				if (CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid()))
				{
					// we have enabled or disabled characteristic
					mNotificationsEnabled = descriptor.getValue()[0] == 1;
				}
			} else
			{
				mErrorState = ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock)
			{
				mLock.notifyAll();
				return;
			}
		};

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt,
				final BluetoothGattCharacteristic characteristic,
				final int status)
		{
			
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				/*
				 * This method is called when either a CONTROL POINT or PACKET
				 * characteristic has been written. If it is the CONTROL POINT
				 * characteristic, just set the flag to true. If the PACKET
				 * characteristic was written we must: - if the image size was
				 * written in DFU Start procedure, just set flag to true - else
				 * - send the next packet, if notification is not required at
				 * that moment - do nothing, because we have to wait for the
				 * notification to confirm the data received
				 */
				if (BOARD_SEND_UUID.equals(characteristic.getUuid()))
				{
					if (mImageSizeSent)
					{
						// if the PACKET characteristic was written with image
						// data, update counters
						mBytesSent += characteristic.getValue().length;
						mPacketsSendSinceNotification++;

						// if a packet receipt notification is expected, or the
						// last packet was sent, do nothing. There
						// onCharacteristicChanged listener will catch either
						// a packet confirmation (if there are more bytes to
						// send) or the image received notification (it upload
						// process was completed)
						final boolean notificationExpected = mPacketsBeforeNotification > 0
								&& mPacketsSendSinceNotification == mPacketsBeforeNotification;
						final boolean lastPacketTransfered = mBytesSent == fileSizeInBytes;

						if (notificationExpected || lastPacketTransfered)
							return;
						LogUtil.i("onCharacteristicWrite=====()  ");
						// when neither of them is true, send the next packet
						try
						{
							
							if (mAborted)
							{
								// notify waiting thread
								synchronized (mLock)
								{
									mLock.notifyAll();
									return;
								}
							}
							final byte[] buffer = mBuffer;
							final int size = inputStream.read(buffer);
							writePacket(gatt, characteristic, buffer, size);
							updateProgressNotification();
							return;
						}  catch (final IOException e)
						{
							mErrorState = ERROR_FILE_IO_EXCEPTION;
						}
					} else
					{
						// we've got confirmation that the image size was sent
						mImageSizeSent = true;
					}
				} else if(BOARD_STATE_UUID.equals(characteristic.getUuid()))
				{
					// if the CONTROL POINT characteristic was written just set
					// the flag to true
					mRequestCompleted = true;
				}
			} else
			{
				mErrorState = ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock)
			{
				mLock.notifyAll();
				return;
			}
		};

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt,
				final BluetoothGattCharacteristic characteristic)
		{
			mReceivedData = characteristic.getValue();
			receiveString = BleGattUtils.getStringValue(mReceivedData, 0); 
			LogUtil.i("999999999999 底层数据"+new String(Hex.encodeHex(mReceivedData)));
			if ( receiveString.indexOf("NT+UPEND")!=-1 )
			{
				LogUtil.i("999999999999 底层Notify的EFM32结束");
				// notify waiting thread
				synchronized (mLock)
				{
					mLock.notifyAll();
					return;
				}
			}else if ( receiveString.indexOf("NT+HEART")!=-1 ) {
				LogUtil.i("999999999999 底层Notify的EFM32升级心跳包");
			}else if ( receiveString.indexOf("BT+DEF")!=-1 ) {
				if (receiveString.indexOf("OK")!=-1)
				{
					LogUtil.i("999999999999 底层Notify通知EFM32开始升级启动");
				}else {
					LogUtil.i("999999999999 底层Notify通知EFM32开始升级失败");
				}
			}else {
				final int responseType = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, 0);

				switch (responseType)
				{
					case OP_CODE_PACKET_RECEIPT_NOTIF_KEY:
						final BluetoothGattCharacteristic packetCharacteristic = gatt
								.getService(BOARD_SERVICE_UUID).getCharacteristic(BOARD_SEND_UUID);
						LogUtil.i("999999999999 底层Notify的不明白的数据 = "+new String(Hex.encodeHex(characteristic.getValue())));
						try
						{
							mPacketsSendSinceNotification = 0;

							if (mAborted)
								break;

							final byte[] buffer = mBuffer;
							final int size = inputStream.read(buffer);
							writePacket(gatt, packetCharacteristic, buffer, size);
							updateProgressNotification();
							return;
						}  catch (final IOException e)
						{
							mErrorState = ERROR_FILE_IO_EXCEPTION;
						}
						break;
					default:
						
						LogUtil.i("999999999999 底层Notify的数据 = "+mReceivedData);
						for (int i = 0; i < mReceivedData.length; i++)
						{
							LogUtil.i(mReceivedData[i]+",");
						}
						
//						int status = mReceivedData[2];
//						if (status == DFU_STATUS_SUCCESS)
//						{	
//						}else if(UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_NORDIC){							
//							//mErrorState = ERROR_FILE_IO_EXCEPTION;
//							//BluetoothAdapter.getDefaultAdapter().disable();//关蓝牙，否则主界面连接不上
//						}
						synchronized (mLock)
						{
							mLock.notifyAll();
							//return;
						}
						break;
				}
			}
		};
		
	};
	///////////////////////////////////////////////////////////////////////blue tooth over


	public DfuService()
	{
		super(TAG);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		final LocalBroadcastManager manager = LocalBroadcastManager
				.getInstance(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		close();
		final LocalBroadcastManager manager = LocalBroadcastManager
				.getInstance(this);
		//manager.unregisterReceiver(mDfuActionReceiver);

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// In order to let DfuActivity know whether DFU is in progress, we have
		// to use Shared Preferences
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(PREFS_DFU_IN_PROGRESS, false);
		editor.commit();
	}

	@Override
	protected void onHandleIntent(final Intent intent)
	{
		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// In order to let DfuActivity know whether DFU is in progress, we have
		// to use Shared Preferences
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(PREFS_DFU_IN_PROGRESS, true);
		editor.commit();

		if ( !initialize() ){
			sendErrorBroadcast(ERROR_FILE_NOT_FOUND);
			return;
		}

		final String deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
		final String deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
		final String filePath = intent.getStringExtra(EXTRA_FILE_PATH);

		mDeviceAddress = deviceAddress;
		mDeviceName = deviceName;
		mConnectionState = STATE_DISCONNECTED;
		
		
		
		try
		{
			try
			{
				File file = new File(filePath);  
				inputStream = new FileInputStream(file);
				//inputStream.mark(inputStream.available());
				fileSizeInBytes = inputStream.available();
				//mImageSizeInPackets = inputStream.sizeInPackets(MAX_PACKET_SIZE);
				//mHexInputStream = his;
			} catch (final FileNotFoundException e)
			{
				sendErrorBroadcast(ERROR_FILE_NOT_FOUND);
				return;
			} catch (final IOException e)
			{
				sendErrorBroadcast(ERROR_FILE_CLOSED);
				return;
			}
		
			// Let's connect to the device            要连接了
			updateProgressNotification(PROGRESS_CONNECTING);

			final BluetoothGatt gatt = connect(deviceAddress);
			// Are we connected?
			if (mErrorState > 0)
			{ // error occurred
				terminateConnection(gatt, mErrorState);
				return;
			}
			if (mAborted)
			{
				terminateConnection(gatt, PROGRESS_ABORTED);
				return;
			}
			                          LogUtil.i("0000000     找服务)");
			// We have connected to DFU device and services are discoverer
			final BluetoothGattService dfuService = gatt.getService(BOARD_SERVICE_UUID); // we are sure it exists
			if (dfuService == null)
			{
				                      LogUtil.i("000000000  dfuService == null");
				terminateConnection(gatt, ERROR_UNSUPPORTED_DEVICE);
				return;
			}
			final BluetoothGattCharacteristic stateCharacteristic = dfuService
					.getCharacteristic(BOARD_STATE_UUID);
			final BluetoothGattCharacteristic sendCharacteristic = dfuService
					.getCharacteristic(BOARD_SEND_UUID);
			                          LogUtil.i("11111111已经得到了写数据和状态的属性，去打开notify");
			try
			{
				// enable notifications
				updateProgressNotification(PROGRESS_STARTING);
				
				setCharacteristicNotification(gatt, stateCharacteristic,true);
				//setCharacteristicNotification(gatt, stateCharacteristic,true);
				 LogUtil.i(" 打开通知  setCharacteristicNotification  结束");
				try
				{
					byte[] response = null;
					LogUtil.i("xxxxxx     状态码0x01 DFU请求开始");
					writeOpCode(gatt, stateCharacteristic,OP_CODE_START_DFU);
					LogUtil.i("aaaaaaa  发送文件总大小");
					writeImageSize(gatt, sendCharacteristic,fileSizeInBytes);
					LogUtil.i("bbbbbb  发送完毕，检验是否有返回");
					response = readNotificationResponse();
					LogUtil.i("3333333333   从返回数据判，底层DFU状态是否成功");
					int status = response[2];
					if (status != DFU_STATUS_SUCCESS)
						throw new RemoteDfuException("Starting DFU failed",status);
					LogUtil.i("xxxxxx     状态码0x02 CRC请求开始");
					writeOpCode(gatt, stateCharacteristic, OP_CODE_INIT_DFU_PARAMS);
					int crcValue = 0; 
					try{
					   if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_NORDIC){
					   Other crcNodicValue = new OtherServer(DfuService.this).
							   getOther(null, Other.Type.crcNodicValue);
			           if (crcNodicValue != null && crcNodicValue.getValue() != null){
			        	   crcValue = Integer.parseInt(crcNodicValue.getValue());
			        	   writeImageSize(gatt, sendCharacteristic,crcValue );	 
			           }LogUtil.i(" NORDIC CRC校验码:"+crcValue);
				   }else if(UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_BAND){
					   Other crcEf32Value = new OtherServer(DfuService.this).
							   getOther(null, Other.Type.crcEf32Value);
			           if (crcEf32Value != null && crcEf32Value.getValue() != null){
			        	   crcValue = Integer.parseInt(crcEf32Value.getValue());
			        	   writeImageSize(gatt, sendCharacteristic,crcValue );	 
			           }LogUtil.i(" BAND CRC校验码:"+crcValue);
				   }
					} catch (Exception e){
						e.printStackTrace();
					}
					status = response[2];
					if (status != DFU_STATUS_SUCCESS)
						throw new RemoteDfuException("Init CRC Exception",status);
					LogUtil.i("CRC校验码发送完成");
					// Send the number of packets of firmware before receiving a
					// receipt notification
					final int numberOfPacketsBeforeNotification = mPacketsBeforeNotification;
					if (numberOfPacketsBeforeNotification > 0)
					{
						setNumberOfPackets(OP_CODE_PACKET_RECEIPT_NOTIF_REQ,
								numberOfPacketsBeforeNotification);
						writeOpCode(gatt, stateCharacteristic,OP_CODE_PACKET_RECEIPT_NOTIF_REQ);
					}

					// 初始化固件升级
					LogUtil.i("xxxxxxxxxxxxx   状态码0x03 初始化固件上传");
					writeOpCode(gatt, stateCharacteristic,OP_CODE_RECEIVE_FIRMWARE_IMAGE);
					LogUtil.i("5555555555 更新UI 开始发送文件，此处暂停");
					// This allow us to calculate upload time
					updateProgressNotification();
					try
					{
						response = uploadFirmwareImage(gatt,sendCharacteristic, inputStream);
					} catch (final DeviceDisconnectedException e)
					{
						throw e;
					}

					// Check the result of the operation
					status = response[2];
					if (status != DFU_STATUS_SUCCESS)
						throw new RemoteDfuException("Device returned error after sending file",
								status);

                    LogUtil.i("xxxxxxxxxxxxx   状态码   确认校验成功否？ ");
					writeOpCode(gatt, stateCharacteristic,OP_CODE_VALIDATE);

					 LogUtil.i("5555555555 等待notify数据 "); 
					response = readNotificationResponse();
					status = response[2];
					 LogUtil.i("status== "+status);
					if (status != DFU_STATUS_SUCCESS){
						LogUtil.i("CRC结果  RemoteDfuException ");
						throw new RemoteDfuException(
								"Device returned validation error", status);
					}
					// only be used when update EFM32
					if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_BAND)
					writeOpCode(gatt, stateCharacteristic,"BT+DEF".getBytes());
					
					// Disable notifications locally (we don't need to disable
					// them on the device, it will reset)
					updateProgressNotification(PROGRESS_DISCONNECTING);
					gatt.setCharacteristicNotification(stateCharacteristic, false);

					// Send Activate and Reset signal.
					 LogUtil.i("xxxxxxxxxxxx   状态码  发送激活和重启命令 ");
					writeOpCode(gatt, stateCharacteristic,OP_CODE_ACTIVATE_AND_RESET);

					// The device will reset so we don't have to send Disconnect
					// signal.
					waitUntilDisconnected();
					LogUtil.i("666666666等待结束  已经断开连接  清理设备内存");
					// Close the device
					refreshDeviceCache(gatt);
					LogUtil.i("666666666释放资源 mBluetoothGatt");
					close();
					updateProgressNotification(PROGRESS_COMPLETED);
				} catch (final RemoteDfuException e){
					final int error = ERROR_REMOTE_MASK | e.getErrorNumber();
					 LogUtil.i("xxxxxxxxxxxx  RemoteDfuException 状态码   重启命令 ");
					writeOpCode(gatt, stateCharacteristic, OP_CODE_RESET);
					terminateConnection(gatt, error);
					
				}
			} catch (final UploadAbortedException e)
			{
				if (mConnectionState == STATE_CONNECTED_AND_READY)
					try
					{
						mAborted = false;
						 LogUtil.i("xxxxxxxxxxxx  UploadAbortedException 状态码   重启命令 ");
						writeOpCode(gatt, stateCharacteristic,OP_CODE_RESET);
					} catch (final Exception e1)
					{
						// do nothing
					}
				terminateConnection(gatt, PROGRESS_ABORTED);
			} catch (final DeviceDisconnectedException e)
			{
				 LogUtil.i("xxxxxxxxxxxx  DeviceDisconnectedException ");
				if (mNotificationsEnabled)
					gatt.setCharacteristicNotification(
							stateCharacteristic, false);
				close();
				updateProgressNotification(ERROR_DEVICE_DISCONNECTED);
				connectedException = true;
				BluetoothAdapter.getDefaultAdapter().disable();//关蓝牙，否则主界面连接不上
				return;
			} catch (final DfuException e)
			{
				final int error = e.getErrorNumber() & ~ERROR_CONNECTION_MASK;
				if (mConnectionState == STATE_CONNECTED_AND_READY)
					try
					{
						 LogUtil.i("xxxxxxxxxxxx  DfuException 状态码   OP_CODE_RESET ");
						writeOpCode(gatt, stateCharacteristic,OP_CODE_RESET);
					} catch (final Exception e1)
					{
						// do nothing
					}
				terminateConnection(gatt, e.getErrorNumber());
				BluetoothAdapter.getDefaultAdapter().disable();
			}
		} finally
		{
			try
			{
				// upload has finished (success of fail)
				editor.putBoolean(PREFS_DFU_IN_PROGRESS, false);
				editor.commit();

				// ensure that input stream is always closed
				if (inputStream != null)
					inputStream.close();
				inputStream = null;
			} catch (Exception e)
			{
			}
		}
}

	/**
	 * Sets number of data packets that will be send before the notification
	 * will be received
	 * 
	 * @param data
	 *            control point data packet
	 * @param value
	 *            number of packets before receiving notification. If this value
	 *            is 0, then the notification of packet receipt will be disabled
	 *            by the DFU target.
	 */
	private void setNumberOfPackets(final byte[] data, final int value)
	{
		data[1] = (byte) (value & 0xFF);
		data[2] = (byte) ((value >> 8) & 0xFF);
	}

	/**
	 * Connects to the BLE device with given address. This method is
	 * SYNCHRONOUS, it wait until the connection status change from
	 * {@link #STATE_CONNECTING} to {@link #STATE_CONNECTED_AND_READY} or an
	 * error occurs.
	 * 
	 * @param address
	 *            the device address
	 * @return the GATT device
	 */
	private BluetoothGatt connect(final String address)
	{
		mConnectionState = STATE_CONNECTING;
       LogUtil.i("2222222222222222  connect(final String address) 连接 ");
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		final BluetoothGatt gatt = mBluetoothGatt = device.connectGatt(this,
				false, mGattCallback);

		// We have to wait until the device is connected and services are
		// discovered
		// Connection error may occur as well.
		try
		{
			synchronized (mLock)
			{
				while (((mConnectionState == STATE_CONNECTING || mConnectionState == STATE_CONNECTED)
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		return gatt;
	}

	/**
	 * Disconnects from the device and cleans local variables in case of error.
	 * This method is SYNCHRONOUS and wait until the disconnecting process will
	 * be completed.
	 * 
	 * @param gatt
	 *            the GATT device to be disconnected
	 * @param error
	 *            error number
	 */
	private void terminateConnection(final BluetoothGatt gatt, final int error)
	{
		if (mConnectionState != STATE_DISCONNECTED)
		{
			updateProgressNotification(PROGRESS_DISCONNECTING);

			// disable notifications
			try
			{
				final BluetoothGattService dfuService = gatt
						.getService(BOARD_SERVICE_UUID); // we are sure it exists
				if (dfuService != null)
				{
					final BluetoothGattCharacteristic controlPointCharacteristic = dfuService
							.getCharacteristic(BOARD_STATE_UUID);
					setCharacteristicNotification(gatt,
							controlPointCharacteristic, false);
				}
			} catch (final DeviceDisconnectedException e)
			{
				// do nothing
			} catch (final DfuException e)
			{
				// do nothing
			} catch (final Exception e)
			{
				// do nothing
			}

			// Disconnect from the device
			disconnect(gatt);
		}

		// Close the device
		close();
		updateProgressNotification(error);
	}

	/**
	 * Disconnects from the device. This is SYNCHRONOUS method and waits until
	 * the callback returns new state. Terminates immediately if device is
	 * already disconnected. Do not call this method directly, use
	 * {@link #terminateConnection(BluetoothGatt, int)} instead.
	 * 
	 * @param gatt
	 *            the GATT device that has to be disconnected
	 */
	private void disconnect(final BluetoothGatt gatt)
	{
		if (mConnectionState == STATE_DISCONNECTED)
			return;

		mConnectionState = STATE_DISCONNECTING;

		gatt.disconnect();

		// We have to wait until device gets disconnected or an error occur
		waitUntilDisconnected();
	}

	/**
	 * Wait until the connection state will change to
	 * {@link #STATE_DISCONNECTED} or until an error occurs.
	 */
	private void waitUntilDisconnected()
	{
		try
		{
			synchronized (mLock)
			{
				while (mConnectionState != STATE_DISCONNECTED
						&& mErrorState == 0)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
	}

	/**
	 * Closes the GATT device and cleans up.
	 * 
	 * @param gatt
	 *            the GATT device to be closed
	 */
	private void close()
	{
		if (mBluetoothGatt!= null)
		{
			if (mConnectionState != STATE_DISCONNECTED)
			disconnect(mBluetoothGatt);
		    mBluetoothGatt.close();
		    mBluetoothGatt = null;
		}
		mConnectionState = STATE_CLOSED;
	}

	/**
	 * Clears the device cache. After uploading new firmware the DFU target will
	 * have other services than before.
	 * 
	 * @param gatt
	 *            the GATT device to be refreshed
	 */
	private void refreshDeviceCache(final BluetoothGatt gatt)
	{
		/*
		 * There is a refresh() method in BluetoothGatt class but for now it's
		 * hidden. We will call it using reflections.
		 */
		try
		{
			final Method refresh = gatt.getClass().getMethod("refresh");
			if (refresh != null)
			{
				final boolean success = (Boolean) refresh.invoke(gatt);
			}
		} catch (Exception e)
		{
		}
	}

	/**
	 * Enables or disables the notifications for given characteristic. This
	 * method is SYNCHRONOUS and wait until the
	 * {@link BluetoothGattCallback#onDescriptorWrite(BluetoothGatt, BluetoothGattDescriptor, int)}
	 * will be called or the connection state will change from
	 * {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or
	 * an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to enable or disable notifications for
	 * @param enable
	 *            <code>true</code> to enable notifications, <code>false</code>
	 *            to disable them
	 * @throws DfuException
	 * @throws UploadAbortedException
	 */
	private void setCharacteristicNotification(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic characteristic,
			final boolean enable) throws DeviceDisconnectedException,
			DfuException, UploadAbortedException
	{
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException(
					"Unable to set notifications state", mConnectionState);
		mErrorState = 0;

		if (mNotificationsEnabled == enable)
			return;

		// enable notifications locally
		gatt.setCharacteristicNotification(characteristic, enable);

		// enable notifications on the device
		final BluetoothGattDescriptor descriptor = characteristic
				.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
		descriptor
				.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
						: BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);

		// We have to wait until device gets disconnected or an error occur
		try
		{
			synchronized (mLock)
			{
				while ((mNotificationsEnabled != enable
						&& mConnectionState == STATE_CONNECTED_AND_READY
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		LogUtil.i("  synchronized 结束 ");
		if (mAborted)
			throw new UploadAbortedException();
		LogUtil.i("  mAborted == "+mAborted);
		LogUtil.i("  mErrorState == "+mErrorState);
		if (mErrorState != 0)
			throw new DfuException("Unable to set notifications state",
					mErrorState);
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException(
					"Unable to set notifications state", mConnectionState);
	}

	/**
	 * Writes the operation code to the characteristic. This method is
	 * SYNCHRONOUS and wait until the
	 * {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}
	 * will be called or the connection state will change from
	 * {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or
	 * an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU CONTROL
	 *            POINT
	 * @param value
	 *            the value to write to the characteristic
	 * @throws DeviceDisconnectedException
	 * @throws DfuException
	 * @throws UploadAbortedException
	 */
	private void writeOpCode(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic characteristic, final byte[] value)
			throws DeviceDisconnectedException, DfuException,UploadAbortedException
	{
		mReceivedData = null;
		mErrorState = 0;
		mRequestCompleted = false;

		characteristic.setValue(value);
		gatt.writeCharacteristic(characteristic);
		LogUtil.i("发送OpCode=="+value[0]);
		// We have to wait for confirmation
		try
		{
			synchronized (mLock)
			{
				while ((mRequestCompleted == false
						&& mConnectionState == STATE_CONNECTED_AND_READY
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		if (mAborted)
			throw new UploadAbortedException();
		if (mErrorState != 0)
			throw new DfuException("Unable to write Op Code " + value[0],
					mErrorState);
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Op Code "
					+ value[0], mConnectionState);
	}

	/**
	 * Writes the image size to the characteristic. This method is SYNCHRONOUS
	 * and wait until the
	 * {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}
	 * will be called or the connection state will change from
	 * {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or
	 * an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param imageSize
	 *            the image size in bytes
	 * @throws DeviceDisconnectedException
	 * @throws DfuException
	 * @throws UploadAbortedException
	 */
	private void writeImageSize(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic characteristic,
			final int imageSize) throws DeviceDisconnectedException,
			DfuException, UploadAbortedException
	{
		mReceivedData = null;
		mErrorState = 0;
		mImageSizeSent = false;

		characteristic
				.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		characteristic.setValue(imageSize,BluetoothGattCharacteristic.FORMAT_UINT32, 0);
		gatt.writeCharacteristic(characteristic);

		// We have to wait for confirmation
		try
		{
			synchronized (mLock)
			{
				while ((mImageSizeSent == false
						&& mConnectionState == STATE_CONNECTED_AND_READY
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		if (mAborted)
			throw new UploadAbortedException();
		if (mErrorState != 0)
			throw new DfuException("Unable to write Image Size", mErrorState);
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Image Size",
					mConnectionState);
	}

	/**
	 * Starts sending the data. This method is SYNCHRONOUS and terminates when
	 * the whole file will be uploaded or the connection status will change from
	 * {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or
	 * an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device (DFU target)
	 * @param packetCharacteristic
	 *            the characteristic to write file content to. Must be the DFU
	 *            PACKET
	 * @return The response value received from notification with Op Code = 3
	 *         when all bytes will be uploaded successfully.
	 * @throws DeviceDisconnectedException
	 *             Thrown when the device will disconnect in the middle of the
	 *             transmission. The error core will be saved in
	 *             {@link #mConnectionState}.
	 * @throws DfuException
	 *             Thrown if DFU error occur
	 * @throws UploadAbortedException
	 */
	private byte[] uploadFirmwareImage(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic packetCharacteristic,final InputStream inputStream)
			throws DeviceDisconnectedException, DfuException,UploadAbortedException
	{
		mReceivedData = null;
		mErrorState = 0;

		final byte[] buffer = mBuffer;
		try
		{
			final int size = inputStream.read(buffer);
			writePacket(gatt, packetCharacteristic, buffer, size);
		} catch (final IOException e)
		{
			throw new DfuException("Error while reading file",
					ERROR_FILE_IO_EXCEPTION);
		}

		try
		{
			synchronized (mLock)
			{
				while ((mReceivedData == null
						&& mConnectionState == STATE_CONNECTED_AND_READY
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		if (mAborted)
			throw new UploadAbortedException();
		if (mErrorState != 0)
			throw new DfuException("Uploading Fimrware Image failed",
					mErrorState);
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException(
					"Uploading Fimrware Image failed: device disconnected",
					mConnectionState);

		return mReceivedData;
	}

	/**
	 * Writes the buffer to the characteristic. The maximum size of the buffer
	 * is 20 bytes. This method is ASYNCHRONOUS and returns immediately after
	 * adding the data to TX queue.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param buffer
	 *            the buffer with 1-20 bytes
	 * @param size
	 *            the number of bytes from the buffer to send
	 */
	private void writePacket(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic characteristic,
			final byte[] buffer, final int size)
	{
		byte[] locBuffer = buffer;
		if (buffer.length != size)
		{
			locBuffer = new byte[size];
			System.arraycopy(buffer, 0, locBuffer, 0, size);
		}
		characteristic.setValue(locBuffer);
		gatt.writeCharacteristic(characteristic);
		// FIXME BLE buffer overflow
		// after writing to the device with WRITE_NO_RESPONSE property the
		// onCharacteristicWrite callback is received immediately after writing
		// data to a buffer.
		// The real sending is much slower than adding to the buffer. This
		// method does not return false if writing didn't succeed.. just the
		// callback is not invoked.
		//
		// More info: this works fine on Nexus 5 (Andorid 4.4) (4.3 seconds) and
		// on Samsung S4 (Android 4.3) (20 seconds) so this is a driver issue.
		// Nexus 4 and 7 uses Qualcomm chip, Nexus 5 and Samsung uses Broadcom
		// chips.
	}

	private void waitIfPaused()
	{
		synchronized (mLock)
		{
			try
			{
				while (mPaused)
					mLock.wait();
			} catch (final InterruptedException e)
			{
			}
		}
	}

	/**
	 * Waits until the notification will arrive. Returns the data returned by
	 * the notification. This method will block the thread if response is not
	 * ready or connection state will change from
	 * {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or
	 * an error will occur, an exception will be thrown.
	 * 
	 * @return the value returned by the Control Point notification
	 * @throws DeviceDisconnectedException
	 * @throws DfuException
	 * @throws UploadAbortedException
	 */
	private byte[] readNotificationResponse()
			throws DeviceDisconnectedException, DfuException,
			UploadAbortedException
	{
		mErrorState = 0;
		try
		{
			synchronized (mLock)
			{
				while ((mReceivedData == null
						&& mConnectionState == STATE_CONNECTED_AND_READY
						&& mErrorState == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e)
		{
		}
		if (mAborted)
			throw new UploadAbortedException();
		if (mErrorState != 0)
			throw new DfuException("Unable to write Op Code", mErrorState);
		if (mConnectionState != STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Op Code",
					mConnectionState);
		return mReceivedData;
	}

	/**
	 * Stores the last progress percent. Used to lower number of calls of
	 * {@link #updateProgressNotification(int)}.
	 */
	private int mLastProgress = -1;

	/**
	 * Creates or updates the notification in the Notification Manager. Sends
	 * broadcast with current progress to the activity.
	 */
	private void updateProgressNotification()
	{
		final int progress = (int) (100.0f * mBytesSent / fileSizeInBytes);
		if (mLastProgress == progress)
			return;

		mLastProgress = progress;
		updateProgressNotification(progress);
	}

	/**
	 * Creates or updates the notification in the Notification Manager. Sends
	 * broadcast with given progress or error state to the activity.
	 * 
	 * @param progress
	 *            the current progress state or an error number, can be one of
	 *            {@link #PROGRESS_CONNECTING}, {@link #PROGRESS_STARTING},
	 *            {@link #PROGRESS_VALIDATING}, {@link #PROGRESS_DISCONNECTING},
	 *            {@link #PROGRESS_COMPLETED} or {@link #ERROR_FILE_CLOSED},
	 *            {@link #ERROR_FILE_INVALID} , etc
	 */
	private void updateProgressNotification(final int progress)
	{
		final String deviceAddress = mDeviceAddress;
		final String deviceName = mDeviceName;

		final Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);

		final Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(android.R.drawable.stat_sys_upload)
				.setOnlyAlertOnce(true).setLargeIcon(largeIcon);
		switch (progress)
		{
			case PROGRESS_CONNECTING:
				builder.setOngoing(true)
						.setContentTitle(
								getString(R.string.dfu_status_connecting))
						.setContentText(
								getString(R.string.dfu_status_connecting_msg,
										deviceName)).setProgress(100, 0, true);
				break;
			case PROGRESS_STARTING:
				builder.setOngoing(true)
						.setContentTitle(
								getString(R.string.dfu_status_starting))
						.setContentText(
								getString(R.string.dfu_status_starting_msg,
										deviceName)).setProgress(100, 0, true);
				break;
			case PROGRESS_VALIDATING:
				builder.setOngoing(true)
						.setContentTitle(
								getString(R.string.dfu_status_validating))
						.setContentText(
								getString(R.string.dfu_status_validating_msg,
										deviceName)).setProgress(100, 0, true);
				break;
			case PROGRESS_DISCONNECTING:
				builder.setOngoing(true)
						.setContentTitle(
								getString(R.string.dfu_status_disconnecting))
						.setContentText(
								getString(
										R.string.dfu_status_disconnecting_msg,
										deviceName)).setProgress(100, 0, true);
				break;
			case PROGRESS_COMPLETED:
				builder.setOngoing(false)
						.setContentTitle(
								getString(R.string.dfu_status_completed))
						.setContentText(
								getString(R.string.dfu_status_completed_msg))
						.setAutoCancel(true);
				break;
			case PROGRESS_ABORTED:
				builder.setOngoing(false)
						.setContentTitle(getString(R.string.dfu_status_abored))
						.setContentText(
								getString(R.string.dfu_status_aborted_msg))
						.setAutoCancel(true);
				break;
			default:
				if (progress >= ERROR_MASK)
				{
					// progress is an error number
					builder.setOngoing(false)
							.setContentTitle(
									getString(R.string.dfu_status_error))
							.setContentText(
									getString(R.string.dfu_status_error_msg))
							.setAutoCancel(true);
				} else
				{
					// progress is in percents
					builder.setOngoing(true)
							.setContentTitle(
									getString(R.string.dfu_status_uploading))
							.setContentText(
									getString(
											R.string.dfu_status_uploading_msg,
											deviceName))
							.setProgress(100, progress, false);
				}
		}
		LogUtil.i("发通知说明升级情况 progress=="+progress);
		// send progress or error broadcast
		if (progress < ERROR_MASK)
			sendProgressBroadcast(progress);
		else
			sendErrorBroadcast(progress & ~ERROR_CONNECTION_MASK);

		// We cannot set two activities at once (using
		// PendingIntent.getActivities(...)) because we have to start the
		// BluetoothLeService first. Service is created in DeviceListActivity.
		// When creating activities the parent Activity is not created, it's
		// just inserted to the history stack.
		final Intent intent = new Intent(this, NotificationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(DfuActivity.EXTRA_DEVICE_ADDRESS, deviceAddress);
		intent.putExtra(DfuActivity.EXTRA_DEVICE_NAME, deviceName);
		intent.putExtra(DfuActivity.EXTRA_PROGRESS, progress); // this may
																// contains
																// ERROR_CONNECTION_MASK
																// bit!
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}

	private void sendProgressBroadcast(final int progress)
	{
		final Intent broadcast = new Intent(BROADCAST_PROGRESS);
		broadcast.putExtra(EXTRA_DATA, progress);
		broadcast.putExtra(EXTRA_DEVICE_ADDRESS, mDeviceAddress);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	private void sendErrorBroadcast(final int error)
	{
		final Intent broadcast = new Intent(BROADCAST_ERROR);
		broadcast.putExtra(EXTRA_DATA, error & ~ERROR_CONNECTION_MASK);
		broadcast.putExtra(EXTRA_DEVICE_ADDRESS, mDeviceAddress);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	/**
	 * only Initializes bluetooth adapter
	 * 
	 * @return <code>true</code> if initialization was successful
	 */
	private boolean initialize()
	{
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null)
		{
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null)
			{
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null)
		{
			return false;
		}

		return true;
	}
}
