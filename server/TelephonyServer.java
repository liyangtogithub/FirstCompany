package com.desay.iwan2.common.server;

import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.util.MatrixUtil;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 方奕峰 on 14-8-6.
 */
public class TelephonyServer implements Handler.Callback {

    public final static String ACTION = TelephonyServer.class.getName();
    private Context context;
    private Handler handler;
    private boolean flag = false;
    private TelephonyManager tm;
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:                   //电话响铃的状态
                    try {
                        Other caller = new OtherServer(context).getOther(null, Other.Type.caller);
                        if (caller != null && caller.getValue().equals("00")) {
                            return;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    LogUtil.i("CALL_STATE_RINGING");
                    if (BleUtil.checkBleEnable(context)) {
                        if (BleConnectState.CONNECTED == BleServer.getInstance(context).mConnectionState) {
//                            if (!flag) {
//                                LogUtil.i("开始来电提醒 flag = " + flag + " ; " + this);
                            flag = true;
//                                LogUtil.i("开始来电提醒1 flag = " + flag);

                            Message msg = new Message();
                            msg.what = 0;
                            msg.arg1 = CASE_NUMBER;
                            String sendStr = incomingNumber;
                            // 显示来电联系人
                            LogUtil.i("来电 incomingNumber = " + incomingNumber);
                            if (!StringUtil.isBlank(incomingNumber)) {
                                String name = getDisplayName(incomingNumber);
                                LogUtil.i("来电 name = " + name);
                                if (!StringUtil.isBlank(name)) {
                                    name = name.replace(" ", "");
                                    //判断是否有中文
                                    final Pattern pattern1 = Pattern.compile("[\\u4e00-\\u9fa5]");
                                    Matcher matcher = pattern1.matcher(name);
                                    if (matcher.find()) {
                                        name = name.replaceAll("[^\\u4e00-\\u9fa5\\da-z]", "");//保留中/英/数字
//                                        name = name.replaceAll("[^\\u4e00-\\u9fa5]", "");
                                        if (!StringUtil.isBlank(name)) {
                                            if (name.length() > 4)
                                                name = StringUtil.subString(name, 0, 4);
                                            msg.arg1 = CASE_CALLER;
                                            sendStr = name;
                                        }
                                    } else {
                                        final Pattern pattern2 = Pattern.compile("[\\da-z]");
                                        Matcher matcher1 = pattern2.matcher(name);
                                        if (matcher1.find()) {
                                            sendStr = name.replaceAll("[^\\da-z]", "");
                                            if (sendStr.length() > 18) {
                                                sendStr = StringUtil.subString(sendStr, 0, 18);
                                            }
                                        }
                                    }
                                }
                            }

                            handler.removeMessages(msg.arg1);
                            msg.obj = sendStr;
                            handler.sendMessage(msg);
//                            }
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:                   //电话通话的状态
                    LogUtil.i("CALL_STATE_OFFHOOK");
//                        handler.removeMessages(0);
//                        break;
                case TelephonyManager.CALL_STATE_IDLE:
                    LogUtil.i("CALL_STATE_IDLE");
                default:
                    callerName.clear();
                    handler.removeMessages(0);
                    flag = false;
                    LogUtil.i("停止来电提醒");
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    public TelephonyServer(Context context) {
        this.context = context;
        handler = new Handler(this);
    }

    public void monitorIncomingTelegram() {
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter filter = new IntentFilter(ACTION);
        context.registerReceiver(receiver, filter);
    }

    private final int CASE_NUMBER = 0;
    private final int CASE_CALLER = 1;

    @Override
    public boolean handleMessage(Message msg) {
//        String incomingNumber = msg.getData().getString("data");
        String sendStr = (String) msg.obj;
        if (StringUtil.isBlank(sendStr)) {
            BleApi1.BizApi.pushMsg(context, "1", "");
        } else {
            switch (msg.arg1) {
                case CASE_NUMBER:
                    BleApi1.BizApi.pushMsg(context, "0", sendStr);
                    break;
                case CASE_CALLER:
                    if (callerName.size() == 0) {
                        if (msg.arg2 == 1) {
                            pushCallerName(context, true);
                        } else {
                            try {
                                LogUtil.i("来电提醒 名称 = " + sendStr);
                                ArrayList<byte[]> bytes = generateCallerName(sendStr);
                                if (bytes != null)
                                    callerName.addAll(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            BleApi1.BizApi.requestPushMsg1(context);
//                Bitmap bitmap = MatrixUtil.font2Bitmap(100, 20, 20, sendStr);
//                callerName = MatrixUtil.bitmap2Bytes(1, bitmap, Color.LTGRAY);
                        }
                    }
                    break;
            }
        }

        if (flag) {
            Message msg1 = new Message();
            msg1.arg1 = msg.arg1;
            msg1.arg2 = 1;
            msg1.what = msg.what;
            msg1.obj = msg.obj;
//            handler.sendMessageDelayed(msg1, msg.arg1 == CASE_CALLER ? 10000 : 5000);//TODO
            handler.sendMessageDelayed(msg1, 6000);
        }
        return false;
    }

    public static synchronized void pushCallerName(Context context, boolean isRequest) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("isRequest", isRequest);
        context.sendBroadcast(intent);
    }

    public void stop() {
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        context.unregisterReceiver(receiver);
    }

    private BaseBroadcastReceiver receiver = new BaseBroadcastReceiver() {
        @Override
        public void onReceive1(Context context, Intent intent) throws Throwable {
            super.onReceive1(context, intent);
            String action = intent.getAction();
            if (ACTION.equals(action)) {
                if (callerName != null) {
                    if (callerName.size() > 0) {
                        if (intent.getBooleanExtra("isRequest", true)) {
                            BleApi1.BizApi.requestPushMsg1(context);
                        } else {
                            LogUtil.i("来电提醒 发字节流");
                            BleApi1.BizApi.sendBytes(context, "caller", callerName.get(0));
                            callerName.remove(0);
                        }
                    } else {
                        LogUtil.i("来电提醒 发送完毕");
                        BleApi1.BizApi.pushMsg(context, "1", null);
                    }
                }
            }
        }
    };
    private Vector<byte[]> callerName = new Vector<byte[]>();

    private String getDisplayName(String phoneNum) {
//        LogUtil.i("aaaaaa1");
        ContentResolver resolver = context.getContentResolver();
//        LogUtil.i("aaaaaa2");
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                "replace("+ContactsContract.CommonDataKinds.Phone.NUMBER+",' ','')" + " like ?",
                new String[]{"%"+phoneNum}, null);
//        LogUtil.i("aaaaaa3");
        try {
            if (cursor != null) {
//                LogUtil.i("aaaaaa4");
                if (cursor.moveToFirst()) {
                    LogUtil.i("1 = " + cursor.getString(0));
                    return cursor.getString(0);
                }
            }
//            LogUtil.i("aaaaaa5");
        } finally {
            cursor.close();
        }
//        LogUtil.i("aaaaaa6");
        return null;
    }

    private ArrayList<byte[]> generateCallerName(String str) throws IOException {
        char[] chars = str.toCharArray();
        ArrayList<byte[]> bytes = new ArrayList<byte[]>();

        for (char c : chars) {
            bytes.add(MatrixUtil.unicode2Bytes(context, c));
        }

        return bytes;
    }
}
