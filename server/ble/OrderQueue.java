package com.desay.iwan2.common.server.ble;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import com.desay.iwan2.common.api.ble.BleApi1;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.ble.Instruction;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import dolphin.tools.util.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by 方奕峰 on 14-6-9.
 */
public class OrderQueue extends BaseBroadcastReceiver implements Handler.Callback {
    public static final String ACTION_CMD = OrderQueue.class.getName() + ".action_cmd";
    public static final String ACTION_RESPONSE = OrderQueue.class.getName() + ".action_response";
    public static final String ACTION_READY = OrderQueue.class.getName() + ".action_ready";
    public static final String ACTION_CLEAN = OrderQueue.class.getName() + ".action_clean";

    public static final String KEY = "key1";

    private boolean isLock = true;
    private static ArrayList<Cmd> cmdQueue = new ArrayList<Cmd>();
    private Handler handler;
    private static final int TIMEOUT = 2000;

    private Cmd lastSendCmd;

    private Context context;

    public OrderQueue(Context context) {
        this.context = context;
        handler = new Handler(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive1(Context context, Intent intent) throws Throwable {
        LogUtil.i("onReceive(Context context, Intent intent)---------- cmdQueue.size()=" + cmdQueue.size());
        String action = intent.getAction();
        if (ACTION_CMD.equals(action)) {
        } else if (ACTION_RESPONSE.equals(action)) {
            BleResponse response = (BleResponse) intent.getSerializableExtra(KEY);
            LogUtil.i("从队列中去除指令,response=" + (response == null ? "null" : response.id));
            if (response == null) {
                synchronized (cmdQueue) {
                    if (cmdQueue.size() > 0) {
                        handler.removeMessages(CASE_REMOVE_CMD);
                        if (lastSendCmd != null) {
//                        cmdQueue.remove(lastSendCmd);
                            lastSendCmd.retryCount = -1;
                            popFromCmdQueue(lastSendCmd);
                        }
                    }
                }
            } else {
                synchronized (cmdQueue) {
                    if (cmdQueue.size() > 0) {
                        handler.removeMessages(CASE_REMOVE_CMD);
                        Cmd tmp = null;
                        for (Cmd cmd : cmdQueue) {
//                        if (cmd.instruction.gattService.getUuid().toString().equals(response.serverUuid.toString()))
//                            if (cmd.instruction.characteristic.getUuid().toString().equals(response.characteristicUuid.toString()))
                            LogUtil.i("剩余cmd.id=" + cmd.id + " ; response.id=" + response.id +
                                    " ; equals=" + cmd.id.contains(response.id));
                            if (cmd.id.contains(response.id)) {
                                tmp = cmd;
                                break;
                            }
                        }
                        if (tmp != null) {
//                        cmdQueue.remove(tmp);
                            tmp.retryCount = -1;
                            popFromCmdQueue(tmp);
                        }
                    }
                }
            }
            isLock = false;
        } else if (ACTION_READY.equals(action)) {
            LogUtil.i("BLE已就绪");
            isLock = false;
//            ToastUtil.shortShow(context, "BLE 已连接");
        } else if (ACTION_CLEAN.equals(action)) {
            synchronized (cmdQueue) {
                cmdQueue.clear();
            }
            handler.removeMessages(CASE_REMOVE_CMD);
            return;
        }

        synchronized (cmdQueue) {
            if (cmdQueue.size() > 0) {
                try {
//                Thread.sleep(1000);
                    LogUtil.i("cmdQueue待发指令：");
                    synchronized (cmdQueue) {
                        for (Cmd cmd : cmdQueue) {
                            LogUtil.i("----- " + cmd.id);
                        }

                        send(cmdQueue.get(0));
                    }
//                LogUtil.i("发出");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private synchronized boolean send(Cmd cmd) throws Exception {
        LogUtil.i("send isLock=" + isLock);
        if (!isLock) {
//            if (cmd.instruction.characteristic!=null&&new String(cmd.instruction.characteristic.getValue()).contains("\r\n"))
            isLock = true;
            lastSendCmd = cmd;
            if (cmd.callback != null)
                for (BleApi1.Callback callback : cmd.callback) {
                    callback.onSendBle(cmd);
                }

            BleUtil.executeInstruction(context, cmd.instruction);
//            if (InstructionType.characteristicNotify == cmd.instruction.type) {
//                response(context, null);
//            } else {

            Message msg = new Message();
            msg.what = CASE_REMOVE_CMD;
            msg.obj = cmd;
            handler.sendMessageDelayed(msg, cmd.timeout);
//            }
            return true;
        }
        return false;
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CMD);
        intentFilter.addAction(ACTION_RESPONSE);
        intentFilter.addAction(ACTION_READY);
        intentFilter.addAction(ACTION_CLEAN);
        context.registerReceiver(this, intentFilter);
    }

    public void stop() {
        context.unregisterReceiver(this);
    }

    private final int CASE_REMOVE_CMD = 0;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CASE_REMOVE_CMD:
                isLock = false;
                Cmd cmd = (Cmd) msg.obj;
                if (cmd.retryCount > 0) {
                    cmd.retryCount--;
                    LogUtil.i("重发ble:" + cmd.id);
                    try {
                        send(cmd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
//                    cmdQueue.remove(cmd);
                    LogUtil.i("超时失效ble:" + cmd.id);
                    popFromCmdQueue(cmd);
                    if (cmdQueue.size() > 0) {
                        Intent intent = new Intent(ACTION_CMD);
                        context.sendBroadcast(intent);
                    }
                }
                break;
        }
        return false;
    }

    private void popFromCmdQueue(Cmd cmd) {
        LogUtil.i("从队列中去除：" + cmd.id);
        synchronized (cmdQueue) {
            cmdQueue.remove(cmd);
        }
        if (cmd.callback != null) {
            for (BleApi1.Callback callback : cmd.callback) {
                callback.onPopFromQueue(cmd);
            }

        }
    }

    public static class Cmd implements Serializable {
        public String id;
        public Instruction instruction;
        public int priority;
        public int timeout = TIMEOUT;
        public int retryCount = 3;
        public BleApi1.Callback[] callback;
    }

    public static class BleResponse implements Serializable {
        public String id;
        public UUID serverUuid;
        public UUID characteristicUuid;
        public int priority;
    }

    public synchronized static void send(Context context, Cmd cmd) {
        int insertIndex = 0;
        synchronized (cmdQueue) {
            for (int i = 0; i < cmdQueue.size(); i++) {
                Cmd cmdObj1 = cmdQueue.get(i);
                if (cmd.priority > cmdObj1.priority) {
                    insertIndex = i;
                    break;
                } else
                    insertIndex = i + 1;
            }
//            if (insertIndex > -1)
            cmdQueue.add(insertIndex, cmd);
        }

//        cmdQueue.add(cmd);
        LogUtil.i("添加指令,id=" + cmd.id);
        Intent intent = new Intent(ACTION_CMD);
        context.sendBroadcast(intent);
    }

    public static void response(Context context, BleResponse bleResponse) {
        Intent intent = new Intent(ACTION_RESPONSE);
        intent.putExtra(KEY, bleResponse);
        context.sendBroadcast(intent);
    }

    public static void ready(Context context) {
        Intent intent = new Intent(ACTION_READY);
        context.sendBroadcast(intent);
    }

    public static void response(Context context, String id) {
        OrderQueue.BleResponse bleResponse = new OrderQueue.BleResponse();
        bleResponse.id = id;
        OrderQueue.response(context, bleResponse);
    }

    public static void clean(Context context) {
        Intent intent = new Intent(ACTION_CLEAN);
        context.sendBroadcast(intent);
    }
}
