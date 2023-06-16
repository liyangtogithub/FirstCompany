package com.desay.iwan2.common.api.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;

import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.server.ble.OrderQueue;
import com.test.fragment.Test7Fragment;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.ble.Instruction;
import dolphin.tools.ble.InstructionType;
import dolphin.tools.common.os.MyUncaughtHandler;
import dolphin.tools.util.LogUtil;
import org.apache.commons.lang.ArrayUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by 方奕峰 on 14-6-9.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleApi1 {

    private static OrderQueue.Cmd generateCharacteristicWriteCmd() {
        OrderQueue.Cmd cmd = new OrderQueue.Cmd();
        cmd.instruction = new Instruction();
        cmd.instruction.type = InstructionType.characteristicWrite;
        return cmd;
    }

    public static void commonBytesExecute(Context context, UUID serverUuid, UUID characteristicUuid, String id, byte[] bytes,
                                          Integer priority, Integer timeout, Integer retryCount, Callback... callback) {
        if (BleUtil.checkBleEnable(context)) {
            try {
                OrderQueue.Cmd cmd = generateCharacteristicWriteCmd();
                cmd.id = id;
                cmd.priority = priority == null ? 500 : priority;
                if (timeout != null)
                    cmd.timeout = timeout;
                if (retryCount != null)
                    cmd.retryCount = retryCount;
                cmd.instruction.gattService = new BluetoothGattService(serverUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
                cmd.instruction.characteristic = new BluetoothGattCharacteristic(characteristicUuid,
                        BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
                cmd.instruction.characteristic.setValue(bytes);

                cmd.callback = callback;
                if (callback != null) {
                    for (Callback c : callback) {
                        c.onPre(cmd);
                    }
                }

                OrderQueue.send(context, cmd);

                if (callback != null) {
                    for (Callback c : callback) {
                        c.onSend2Queue(cmd);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void commonExecute(Context context, UUID serverUuid, UUID characteristicUuid, String str, Integer timeout, Callback... callback) {
        commonExecute(context, serverUuid, characteristicUuid, str, null, timeout, callback);
    }

    public static void commonExecute(Context context, UUID serverUuid, UUID characteristicUuid, String str, Integer priority, Integer timeout, Callback... callback) {
        commonExecute(context, serverUuid, characteristicUuid, str, priority, timeout, null, callback);
    }

    public static void commonExecute(Context context, UUID serverUuid, UUID characteristicUuid, String str, Integer priority, Integer timeout, Integer retryCount, Callback... callback) {
//        if (BleUtil.checkBleEnable(context)) {
//            try {
//                OrderQueue.Cmd cmd = generateCharacteristicWriteCmd();
//                cmd.id = str;
//                cmd.priority = priority == null ? 500 : priority;
//                if (timeout != null)
//                    cmd.timeout = timeout;
//                if (retryCount != null)
//                    cmd.retryCount = retryCount;
//                cmd.instruction.gattService = new BluetoothGattService(ServerUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
//                cmd.instruction.characteristic = new BluetoothGattCharacteristic(characteristicUuid,
//                        BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
//                cmd.instruction.characteristic.setValue(str.getBytes());
//
//                cmd.callback = callback;
//                if (callback != null) {
//                    for (Callback c : callback) {
//                        c.onPre(cmd);
//                    }
//                }
//
//                Test7Fragment.showData(context, "发送BLE指令 -> " + str);
//                OrderQueue.send(context, cmd);
//
//                if (callback != null) {
//                    for (Callback c : callback) {
//                        c.onSend2Queue(cmd);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        Test7Fragment.showData(context, "发送BLE指令 -> " + str);
        commonBytesExecute(context, serverUuid, characteristicUuid, str, str.getBytes(), priority, timeout, retryCount, callback);
    }

    public static class BizApi {
        public static final UUID UUID_SERVER = UUID.fromString("0000190a-0000-1000-8000-00805f9b34fb");
        private static final UUID UUID_REQUEST = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
        public static final UUID UUID_RESPONSE = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");

        private static void commonExecute(Context context, String str, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, str, null, callback);
        }

        public static final String bind = "AT+BOND";
        public static final String getEfm32Version = "AT+VER";
        public static final String setHeight = "AT+HEIGHT";
        public static final String setName = "AT+NAME";
        public static final String setWeight = "AT+WEIGHT";
        public static final String setSex = "AT+SEX";
        public static final String setTime = "AT+DT";
        public static final String enable = "AT+ACT";
        public static final String turnOff = "AT+OFF";
        public static final String setAlarmClock = "AT+ALARM";
        public static final String setAlarmClock2 = "AT+ALARM2";
        public static final String getBattery = "AT+BATT";
        public static final String setMonitorParam = "AT+MODEL";
        public static final String step = "AT+PACE";
        public static final String setSitAlarm = "AT+SIT";
        public static final String pushMsg = "AT+PUSH";
        public static final String requestPushMsg1 = "AT+ZI";
        public static final String requestData = "AT+DATA";
        public static final String getSn = "AT+SN";
        public static final String setLan = "AT+LAN";
        public static final String setSportAim = "AT+DEST";
        public static final String setCalibration = "AT+CALDC";
        public static final String stepStore = "AT+STEPSTORE";
        public static final String setHandsup = "AT+HANDSUP";
        public static final String setSlpTime = "AT+SLPTIME";

        public static void bind(Context context, Callback... callback) {
//            commonExecute(context, bind + "\r\n", callback);
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, bind + "\r\n", 1500);
        }

        public static void getEfm32Version(final Context context, Callback... callback) {
//            commonExecute(context, getEfm32Version + "\r\n", callback);
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, getEfm32Version + "\r\n", 400, null, callback);
        }

        public static void setName(Context context, String name, Callback... callback) {
            commonExecute(context, setName + "=" + name + "\r\n", callback);
        }

        public static void setHeight(Context context, int height, Callback... callback) {
            DecimalFormat format = new DecimalFormat("000");
            commonExecute(context, setHeight + "=" + format.format(height) + "\r\n", callback);
        }

        public static void setWeight(Context context, int weight, Callback... callback) {
            DecimalFormat format = new DecimalFormat("000");
            commonExecute(context, setWeight + "=" + format.format(weight) + "\r\n", callback);
        }

        public static void setSex(Context context, int sex, Callback... callback) {
            commonExecute(context, setSex + "=" + sex + "\r\n", callback);
        }

        public static void setTime(Context context, String time, Callback... callback) {
//            commonExecute(context, setTime + "=" + time + "\r\n", callback);
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, setTime + "=" + time + "\r\n", Integer.MAX_VALUE - 1, null, callback);
        }

        public static void enable(Context context, Callback... callback) {
            commonExecute(context, enable + "=1\r\n", callback);
        }

        //        public static void disable(Context context) {
//            commonExecute(context, "AT+SHUTDOWN");
//        }
        public static void turnOff(Context context, Callback... callback) {
        	BleApi1.commonExecute(context,UUID_SERVER, UUID_REQUEST, turnOff + "\r\n", null,null,1, callback);
        }
        public static void setAlarmClock(final Context context, String str, Callback... callback) {
            commonExecute(context, setAlarmClock + "=" + str + "\r\n", callback);
        }
        public static void setAlarmClock2(final Context context, String str, Callback... callback) {
        	commonExecute(context, setAlarmClock2 + "=" + str + "\r\n", callback);
        }

//        public static void setAiClock(Context context, String str, Callback... callback) {
//            setAlarmClock(context, str + "\r\n", callback);
//        }

        public static void getBattery(Context context, Callback... callback) {
            commonExecute(context, getBattery + "\r\n", callback);
        }

        public static void setMonitorParam(Context context, String sleepPeriod, String sportPeriod, Callback... callback) {
            commonExecute(context, setMonitorParam + "=" + sleepPeriod + "," + sportPeriod + "\r\n", callback);
        }

        public static void getStep(Context context, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, step + "\r\n", Integer.MAX_VALUE - 99, null, callback);
        }

        public static void stepStore(Context context, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, stepStore + "\r\n", Integer.MAX_VALUE - 99, null, callback);
        }

        public static void setStep(Context context, int step, Callback... callback) {
            DecimalFormat format = new DecimalFormat("00000");
            commonExecute(context, BizApi.step + "=" + format.format(step) + "\r\n", callback);
        }

        public static void setSitAlarm(final Context context, String str, Callback... callback) {
            commonExecute(context, setSitAlarm + "=" + str + "\r\n", callback);
        }

        public static void pushMsg(Context context, String type, String str, Callback... callback) {
//            commonExecute(context, pushMsg + "=" + type + "," + str + "\r\n", callback);
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, pushMsg + "=" + type + "," + (str == null ? "" : str) + "\r\n", null, null, 2, callback);
        }

        public static void requestPushMsg1(Context context, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, requestPushMsg1 + "\r\n", null, null, 2, callback);
        }

        public static void sendBytes(Context context, String id, byte[] bytes, Callback... callback) {
            byte[] bytes1 = ArrayUtils.addAll(bytes, new byte[]{'\r', '\n'});
            BleApi1.commonBytesExecute(context, UUID_SERVER, UUID_REQUEST, id, bytes1, null, null, 0, callback);

            LogUtil.i("发送字节流内容:");
            StringBuffer sb = new StringBuffer();
            for (byte b : bytes1) {
                sb.append(b).append(",");
            }
            LogUtil.i(sb.toString());
        }

        public static void requestData(Context context, int serialNo, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, requestData + "=" + serialNo + "\r\n",
                    Integer.MAX_VALUE - 100, SystemContant.syncTimeout, 0, callback);
        }

        public static final String generateData = "AT+DATA";

        public static void generateData(Context context, Callback... callback) {
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, generateData + "\r\n", null, null, 1, callback);
        }

        public static void getSn(Context context, Callback... callback) {
            commonExecute(context, getSn + "\r\n", callback);
        }

        public static void setLan(Context context, boolean isChinese, Callback... callback) {
            commonExecute(context, setLan + "=" + (isChinese ? 1 : 0) + "\r\n", callback);
        }

        public static void setSportAim(Context context, int step, Callback... callback) {
            DecimalFormat format = new DecimalFormat("00000");
            commonExecute(context, setSportAim + "=" + format.format(step) + "\r\n", callback);
        }

        public static void setCalibration(Context context, Callback... callback) {
            commonExecute(context, setCalibration + "\r\n", callback);
        }

        public static void setHandsup(Context context, String flag, Callback... callback) {
            commonExecute(context, setHandsup + "=" + flag + "\r\n", callback);
        }

        public static void setSlpTime(Context context, String time, Callback... callback) {
            commonExecute(context, setSlpTime + "=" + time + "\r\n", callback);
        }
    }

    public static class VersionApi {
        public static final UUID UUID_SERVER = UUID.fromString("0000190B-0000-1000-8000-00805f9b34fb");
        public static final UUID UUID_REQUEST = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");
        public static final UUID UUID_RESPONSE = UUID.fromString("00000004-0000-1000-8000-00805f9b34fb");

//        private static void commonExecute(Context context, String str, Integer priority, Integer timeout, Callback... callback) {
//            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, str, priority, timeout, callback);
//        }

        private static void commonExecute(Context context, String str, Callback... callback) {
//            OrderQueue.Cmd cmd = generateCharacteristicWriteCmd();
//
//            BluetoothGatt bluetoothGatt = BleServer.getInstance(context).mBluetoothGatt;
//            BluetoothGattService gattService = bluetoothGatt.getService(UUID_SERVER);
//            cmd.instruction.characteristic = gattService
//                    .getCharacteristic(UUID_REQUEST);
//            cmd.instruction.characteristic.setValue(str.getBytes());
//            Test7Fragment.showData(context, "发送BLE指令 -> " + str);
//            OrderQueue.send(context, cmd);

            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, str, null, callback);
        }

        public static final String setDfuMode4Nordic = "BT+UPGB";
        public static final String resetNordic = "BT+RESET";
        public static final String getNordicVersion = "BT+VER";
        public static final String setDfuMode4Efm32 = "BT+UPGE";
        public static final String setNormalMod = "BT+NOR";

        public static void setDfuMode4Nordic(Context context, Callback... callback) {
            commonExecute(context, setDfuMode4Nordic, callback);
        }

        public static void resetNordic(Context context, Callback... callback) {
	        BleApi1.commonExecute( context,UUID_SERVER, UUID_REQUEST, resetNordic, null, null, 0,callback);
        }

        public static void getNordicVersion(Context context, Callback... callback) {
//            commonExecute(context, getNordicVersion, callback);
            BleApi1.commonExecute(context, UUID_SERVER, UUID_REQUEST, getNordicVersion, 400, null, null);
        }

        public static void setDfuMode4Efm32(Context context, Callback... callback) {
            commonExecute(context, setDfuMode4Efm32, callback);
        }

        public static void setNormalMod(Context context, Callback... callback) {
            commonExecute(context, setNormalMod, callback);
        }
    }

    public interface Callback {
        void onPre(OrderQueue.Cmd cmd);

        void onSend2Queue(OrderQueue.Cmd cmd);

        void onSendBle(OrderQueue.Cmd cmd);

        void onPopFromQueue(OrderQueue.Cmd cmd);
    }

    public static class DefaultCallback implements Callback {
        @Override
        public void onPre(OrderQueue.Cmd cmd) {
            if (MyUncaughtHandler.uncaughtHandlerToggle)
                Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        }

        @Override
        public void onSend2Queue(OrderQueue.Cmd cmd) {
            if (MyUncaughtHandler.uncaughtHandlerToggle)
                Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        }

        @Override
        public void onSendBle(OrderQueue.Cmd cmd) {
            if (MyUncaughtHandler.uncaughtHandlerToggle)
                Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        }

        @Override
        public void onPopFromQueue(OrderQueue.Cmd cmd) {
            if (MyUncaughtHandler.uncaughtHandlerToggle)
                Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        }
    }
}
