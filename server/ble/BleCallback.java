package com.desay.iwan2.common.server.ble;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.content.Context;
import android.os.Build;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SyncServer;
import com.desay.iwan2.common.server.TelephonyServer;
import com.desay.iwan2.common.server.ble.handler.*;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.band.BandConnectFragment;
import com.desay.iwan2.module.correct.fragment.CorrectHeartrateFragment;
import com.desay.iwan2.module.dfu.DfuActivity;
import com.desay.iwan2.util.MyBleUtil;
import com.desay.iwan2.util.VibratorUtil;
import com.test.fragment.Test7Fragment;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 方奕峰 on 14-5-5.
 * Implements callback methods for GATT events that the app cares about.  For example,
 * connection change and services discovered.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleCallback extends dolphin.tools.ble.BleCallback {
    private Context context;
    public static final String CONNECT_STATE = "connect_state";
    private ExecutorService executorService;

    public BleCallback(Context context) {
        super(context);
        this.context = context;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        BleServer bleServer = BleServer.getInstance(context);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            //连接上
            Test7Fragment.showData(context, "BLE连接成功");
//            BluetoothDevice device = gatt.getDevice();
//            LogUtil.i("连接BLE设备\nname=" + device.getName() + "\nmac=" + device.getAddress() + "\n");
//            Test7Fragment.showData(context, "name=" + device.getName() + ";mac=" + device.getAddress());
            bleServer.mConnectionState = BleConnectState.CONNECTED;
            LogUtil.i("Connected to GATT server.");
            // Attempts to discover services after successful connection.
            LogUtil.i("Attempting to start service discovery:" +
                    gatt.discoverServices());
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            //连接断开
            Test7Fragment.showData(context, "BLE连接断开");
            bleServer.mConnectionState = BleConnectState.DISCONNECTED;
            LogUtil.i("Disconnected from GATT server.连接断开");

            BandConnectFragment.bandConnectBroadcast(context, false);

            NotifyServer notifyServer = new NotifyServer();
            notifyServer.turnOnBizNotify(context, gatt, false);
            notifyServer.turnOnVersionNotify(context, gatt, false);

            if (DfuUpdate.toDfuActivity == true) {
                MyBleUtil.close(context);
                // if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_NORDIC) {
                BluetoothAdapter.getDefaultAdapter().disable();
                MyService.stop(context);
                LogUtil.i("断开**********==");
                //  }
                     /*else if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_BAND) {
                     DfuUpdate.toDfuActivity = false;
                     LogUtil.i("断开***********Efm32==");
                }*/
            } else {
                BleManager.reconnect(context, true);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            LogUtil.i("发现GATT服务:");
            Test7Fragment.showData(context, "发现GATT服务");
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService s : services) {
                List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();
                LogUtil.i("服务:" + s.getUuid());
                for (BluetoothGattCharacteristic c : characteristics) {
                    LogUtil.i(c.getUuid().toString());
                }
            }

            OrderQueue.ready(context);
            //开启通知
            NotifyServer notifyServer = new NotifyServer();
            //升级相关通知
            notifyServer.turnOnVersionNotify(context, gatt, true);
            //业务相关通知
            notifyServer.turnOnBizNotify(context, gatt, true);
        } else {
            LogUtil.w("onServicesDiscovered received: " + status);
        }
    }

    private StringBuffer notifyStringBuffer = new StringBuffer();

    @Override
    public synchronized void onCharacteristicChanged(BluetoothGatt gatt,
                                                     BluetoothGattCharacteristic characteristic) {
        LogUtil.i("BLE 收到通知");
//        LogUtil.i("BLE接收UUID:" + characteristic.getUuid());

        SyncServer syncServer = SyncServer.getInstance(context);
        try {
            if (syncServer.motionDataHandler != null && syncServer.motionDataHandler.isReceiving) {
                LogUtil.i("接收监测数据中");
                syncServer.motionDataHandler.receiveData(characteristic.getValue());
            } else {
//                String tempStr = BleGattUtils.getStringValue(characteristic.getValue(), 0);
                String tempStr = characteristic.getStringValue(0);
                LogUtil.i("BLE接收内容 <- " + tempStr + " ; contains \\r=" + (tempStr.contains("\r")) +
                        " ; contains \\n=" + (tempStr.contains("\n")));
                if (StringUtil.isBlank(tempStr) && !(tempStr.contains("\r") || tempStr.contains("\n"))) return;
                notifyStringBuffer.append(tempStr);
                String str = null;
                if (tempStr.contains("\r\n")) {
                    str = notifyStringBuffer.toString();
                    notifyStringBuffer.delete(0, notifyStringBuffer.length());
                } else {
                    return;
                }
                Test7Fragment.showData(context, "接收BLE响应 <- " + str);
                // TODO 添加处理业务
                if (BleApi1.VersionApi.UUID_RESPONSE.equals(characteristic.getUuid())) {
                    executorService.execute(new DfuUpdate(context, str, gatt));
                } else if (BleApi1.BizApi.UUID_RESPONSE.equals(characteristic.getUuid())) {
                    String[] strArr0 = str.split(":");
                    String orderStr = strArr0[0];
                    String contentStr = null;
                    if (strArr0.length > 1)
                        contentStr = strArr0[1].substring(0, strArr0[1].indexOf("\r\n"));

                    if (BleApi1.BizApi.getEfm32Version.equalsIgnoreCase(orderStr)) {
                        // 查询EFM32版本
                        BandManager.storeOrUpgradeCore(context, contentStr);
                    } else if (BleApi1.BizApi.setName.equalsIgnoreCase(orderStr)) {
                        BandManager.bandName(context);
                    } else if (BleApi1.BizApi.setHeight.equalsIgnoreCase(orderStr)) {
                        // 配置手环用户身高
                        BandManager.bandHeight(context);
                    } else if (BleApi1.BizApi.setWeight.equalsIgnoreCase(orderStr)) {
                        // 配置手环用户体重
                        BandManager.bandWeight(context);
                    } else if (BleApi1.BizApi.setTime.equalsIgnoreCase(orderStr)) {
                        // 配置手环日期时间
                        BandManager.bandTime(context);
                    } else if (BleApi1.BizApi.enable.equalsIgnoreCase(orderStr)) {
                        // 激活手环
                        BandManager.bandEnable(context);
                    } else if (BleApi1.BizApi.setAlarmClock.equalsIgnoreCase(orderStr)) {
                        // 设置闹钟
                        if (!"ERR".equalsIgnoreCase(contentStr))
                            OrderQueue.response(context, BleApi1.BizApi.setAlarmClock);
                        if (!BandManager.enableBand)
                            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 94, null);
                    }else if (BleApi1.BizApi.setAlarmClock2.equalsIgnoreCase(orderStr)) {
                        // 设置闹钟
                        if (!"ERR".equalsIgnoreCase(contentStr))
                            OrderQueue.response(context, BleApi1.BizApi.setAlarmClock2);
                        if (!BandManager.enableBand)
                            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 94, null);
                    }else if (BleApi1.BizApi.getBattery.equalsIgnoreCase(orderStr)) {
                        //TODO 查询电池电量
                    } else if (BleApi1.BizApi.setMonitorParam.equalsIgnoreCase(orderStr)) {
                        //TODO 设置运动和睡眠参数
                    } else if (BleApi1.BizApi.step.equalsIgnoreCase(orderStr)) {
                        // 查询当前总步数
                        OrderQueue.response(context, BleApi1.BizApi.step);
                        new OtherServer(context).createOrUpdate(null, Other.Type.tempTotalStep,
                                contentStr + ";" + new SimpleDateFormat(SystemContant.timeFormat7Str).format(new Date()));
                    } else if (BleApi1.BizApi.stepStore.equalsIgnoreCase(orderStr)) {
                        // 通知手环保存数据
                        OrderQueue.response(context, orderStr);
                    } else if (BleApi1.BizApi.setSitAlarm.equalsIgnoreCase(orderStr)) {
                        // 设置久坐提醒参数
                        if (!"ERR".equalsIgnoreCase(contentStr))
                            OrderQueue.response(context, BleApi1.BizApi.setSitAlarm);
                        if (!BandManager.enableBand)
                            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 96, null);
                    } else if (BleApi1.BizApi.pushMsg.equalsIgnoreCase(orderStr)) {
                        // 推送消息给EFM32
                        OrderQueue.response(context, BleApi1.BizApi.pushMsg);
                    } else if (BleApi1.BizApi.requestData.equalsIgnoreCase(orderStr)) {
                        // 获取监测数据
                        if (syncServer.motionDataHandler == null)
                            syncServer.motionDataHandler = new MonitorDataHandler(context);
                        syncServer.motionDataHandler.generateMotionData(contentStr);
                        LogUtil.i("收到监测数据头; isReceiving = "+syncServer.motionDataHandler.isReceiving);
                        if (syncServer.motionDataHandler.isReceiveAllFinish()) {
                            LogUtil.i("接收完毕处理监测数据");
                            OrderQueue.response(context, BleApi1.BizApi.requestData);
                            syncServer.motionDataHandler.handle();
                            syncServer.motionDataHandler = null;
                        }
                    } else if (BleApi1.BizApi.bind.equalsIgnoreCase(orderStr)) {
                        //去查询EFM32版本
//                        if ("OK".equalsIgnoreCase(contentStr)) {
                        BandManager.bandBind(context, contentStr, gatt);
//                        }
                    } else if (str.indexOf("NT+BEEP") != -1) {
                        //去找手机
                        BandManager.bandBeep(context);
                        VibratorUtil.Vibrate(context, 800);
                    } else if (str.indexOf("NT+SLEEP") != -1) {
                        //睡眠音乐
                        BandManager.bandSleep(context, str);
                    } else if (BleApi1.BizApi.pushMsg.equalsIgnoreCase(orderStr)) {
                        OrderQueue.response(context, BleApi1.BizApi.pushMsg);
                    } else if (BleApi1.BizApi.requestPushMsg1.equalsIgnoreCase(orderStr)) {
                        //TODO 来电提醒联系人推送
                        if ("RDY".equalsIgnoreCase(contentStr)) {
                            OrderQueue.response(context, orderStr);
                            TelephonyServer.pushCallerName(context, false);
                        } else if ("OK".equalsIgnoreCase(contentStr)) {
                            TelephonyServer.pushCallerName(context, true);
                        }
                    } else if (BleApi1.BizApi.getSn.equalsIgnoreCase(orderStr)) {
                        OrderQueue.response(context, orderStr);
                        if (!StringUtil.isBlank(contentStr)) {
                            executorService.execute(new NcHandler(context, contentStr));
                        }
                    } else if (BleApi1.BizApi.setLan.equalsIgnoreCase(orderStr)) {
                        OrderQueue.response(context, orderStr);
                        MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 99, null);
                    } else if (BleApi1.BizApi.setSportAim.equalsIgnoreCase(orderStr)) {
                        OrderQueue.response(context, orderStr);
                        MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 100, null);
                    } else if (BleApi1.BizApi.setCalibration.equalsIgnoreCase(orderStr)) {
                        OrderQueue.response(context, orderStr);
                        CorrectHeartrateFragment.notice(context);
                    } else if (BleApi1.BizApi.setHandsup.equalsIgnoreCase(orderStr)) {
                        // 抬手检测开关与左右手设置
                        OrderQueue.response(context, orderStr);
                    } else if (BleApi1.BizApi.setSlpTime.equalsIgnoreCase(orderStr)) {
                        // 自动睡眠设置
                    	OrderQueue.response(context, orderStr);
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            LogUtil.e("" + e.getMessage());
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        UUID uuid = descriptor.getCharacteristic().getService().getUuid();

        if (BleApi1.VersionApi.UUID_SERVER.compareTo(uuid) == 0) {
//            OrderQueue.BleResponse bleResponse = new OrderQueue.BleResponse();
//            bleResponse.id = "notify1";
//            OrderQueue.response(context, bleResponse);
            LogUtil.i("1111111111111111   notify(BT)=" + status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                OrderQueue.response(context, "notify1");
                if (!BandManager.enableBand)
                    BleApi1.VersionApi.getNordicVersion(context);
            }
        } else if (BleApi1.BizApi.UUID_SERVER.compareTo(uuid) == 0) {
            LogUtil.i("222222222222222  notify(AT)=" + status);
//            OrderQueue.BleResponse bleResponse = new OrderQueue.BleResponse();
//            bleResponse.id = "notify2";
//            OrderQueue.response(context, bleResponse);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                OrderQueue.response(context, "notify2");

                if (!BandManager.enableBand){
                    final SyncServer syncServer = SyncServer.getInstance(context);
                    syncServer.syncBle();
                    BandManager.getBandEfm32Version(context);
                }else {
                    BleApi1.BizApi.bind(context);
                    return;
                }

//                BleApi1.BizApi.setTime(context, SystemContant.timeFormat7.format(new Date()));
//                BleApi1.BizApi.getStep(context);
//                syncServer.syncBleData(0, new SyncServer.DelayCallback() {
//                    @Override
//                    public void callback(OrderQueue.Cmd cmd) {
//                        super.callback(cmd);
//                        OrderQueue.response(context, cmd.id);
////                    MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_2, null);
////                    if (motionDataHandler != null) {
////                        motionDataHandler.clean();
//                        syncServer.motionDataHandler = null;
////                    }
//                    }
//                });
//                try {
//                    syncServer.syncBleOption(false);
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
            }
        }

        //更新蓝牙连接的状态
        BandConnectFragment.bandConnectBroadcast(context, true);
        super.onDescriptorWrite(gatt, descriptor, status);
    }
}
