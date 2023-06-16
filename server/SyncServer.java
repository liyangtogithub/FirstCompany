package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.alibaba.fastjson.JSON;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.LoadBizData;
import com.desay.iwan2.common.api.http.entity.response.GetGolddata;
import com.desay.iwan2.common.api.http.entity.response.SleepListdata;
import com.desay.iwan2.common.api.http.entity.response.SportListdata;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.ble.OrderQueue;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.common.server.ble.handler.MonitorDataHandler;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.band.BandSleepFragment;
import com.desay.iwan2.module.band.BandUpFragment;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.common.os.MyUncaughtHandler;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.http.Header;

/**
 * Created by 方奕峰 on 14-7-15.
 */
public class SyncServer implements Handler.Callback {

    private Context context;
    public DatabaseHelper dbHelper;

    private Handler handler;

    public static final String ACTION_SYNC_SUCCESS = SyncServer.class.getName() + ".sync";

    private SyncServer(Context context) {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler(SyncServer.this);
                Looper.loop();
                super.run();
            }
        }.start();
    }

//    public void syncNetwork() throws SQLException {
//        network2Local(false, null, null, null);
//        local2Network();
//    }

    public void network2Local(boolean loadBizDataFlag, String startTime, String endTime, MyJsonHttpResponseHandler callback) throws SQLException {
        new UserInfoServer(context).network2Local(null);
        new NcServer(context).local2Network();
        new VersionServer(context).network2Local(null);
        new MacServer(context).loadMac(null);
        new SetServer(context).loadSet(null);
        new AimServer(context).loadSet(null);

//        new SleepServer(context).network2Local(null, null, null);
//        new SportServer(context).network2Local(null, null, null);
//        new GainServer(context).network2Local(null, null, null);

        LoginInfoServer loginInfoServer = new LoginInfoServer(context);
        final LoginInfoServer.LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo == null) return;

        if (loadBizDataFlag) {
            LoadBizData param = new LoadBizData();
            param.setUsername(loginInfo.getAccount());
            param.setStartDate(startTime);
            param.setEndDate(endTime);
            Api1.loadBizData(context, param, callback == null ? new MyJsonHttpResponseHandler(context) {
                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      String responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);
                    MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_1, null, null);
                }

                @Override
                public void onFailure(Context context, String stateCode,
                                      String msg) {
                    super.onFailure(context, stateCode, msg);
                    MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_1, null, null);
                }

                @Override
                public void onSuccess(final Context context, String str) {
                    if (StringUtil.isBlank(str)) return;

                    final com.desay.iwan2.common.api.http.entity.response.LoadBizData entity = JSON.parseObject(str, com.desay.iwan2.common.api.http.entity.response.LoadBizData.class);

                    try {
                        TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                User user = new UserInfoServer(context).getUserInfo();
                                if (user == null) return null;

                                SleepServer sleepServer = new SleepServer(context);
                                for (SleepListdata sleepListdata : entity.getSleeps()) {
                                    sleepServer.saveFromNetworkResponse(user, sleepListdata);
                                }

                                SportServer sportServer = new SportServer(context);
                                for (SportListdata sportListdata : entity.getSports()) {
                                    sportServer.saveFromNetworkResponse(user, sportListdata);
                                }

                                new OtherServer(context).createOrUpdate(user, Other.Type.currentGain, "" + entity.getGolds().getTotal());
                                GainServer gainServer = new GainServer(context);
                                for (GetGolddata getGolddata : entity.getGolds().getEvery()) {
                                    gainServer.saveFromNetworkResponse(user, getGolddata);
                                }

//                                context.sendBroadcast(new Intent(ACTION_SYNC_SUCCESS));
                                syncSuccess(context);
                                MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_1, null,
                                        context.getString(R.string.Label_downloadSuccess));
                                return null;
                            }
                        });
                    } catch (SQLException e) {
                        MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_1, null, null);
                        e.printStackTrace();
                    }
                }
            } : callback);
        }
    }

    public void local2Network() throws SQLException {
        new UserInfoServer(context).local2Network(null);
        new MacServer(context).commitMac(null);
        new SetServer(context).commitSet(null);
        new AimServer(context).commitAim(null);

        new SportServer(context).local2Network(null);
        new SleepServer(context).local2Network(null);

        String currDate = SystemContant.timeFormat6.format(new Date());
        new GainServer(context).network2Local(currDate, currDate, null);

//        LogUtil.i("测试百度1");TODO test
//        Api1.executeCommon("http://www.bai123du.com",new MyJsonHttpResponseHandler(context){
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
//                LogUtil.i("测试百度2");
//            }
//        });
    }

    public void syncBleOption(boolean isSetTime) throws SQLException {
        if (isSetTime)
            BleApi1.BizApi.setTime(context, SystemContant.timeFormat7.format(new Date()));

        User userInfo = new UserInfoServer(context).getUserInfo();
        if (userInfo == null) return;

        OtherServer otherServer = new OtherServer(context);

        Other alarmClock = otherServer.getOther(userInfo, Other.Type.alarm);
        LogUtil.i("888888 alarmClock=="+alarmClock);
        if (alarmClock != null && alarmClock.getValue()!= null){
        	String alarmString = alarmClock.getValue();
        	if (alarmString.indexOf(";")==-1){
				 alarmString+=";0,20,01111100,0700";
			}
        	String alarmStringArray[] = alarmString.split(";");
            BandManager.setBandAlarmClock(context,alarmStringArray[0]);
            BandManager.setBandAlarmClock2(context, alarmStringArray[1]);
            LogUtil.i("888888 alarmString=="+alarmString);
        }

        Other sedentary = otherServer.getOther(userInfo, Other.Type.sedentary);
        if (sedentary != null)
            BandManager.setBandSitAlarm(context, sedentary.getValue());

        BleApi1.BizApi.setHeight(context, Integer.valueOf(userInfo.getHeight()));
        BleApi1.BizApi.setWeight(context, Integer.valueOf(userInfo.getWeight()));

        Locale locale = context.getResources().getConfiguration().locale;
        boolean isChinese = (Locale.CHINA.equals(locale) || Locale.CHINESE.equals(locale) ||
                Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale));
        BleApi1.BizApi.setLan(context, isChinese);

        syncBleSportAim(userInfo);

        Other handsUpOther = otherServer.getOther(null, Other.Type.handsUp);
        String handsUpString = handsUpOther == null ? BandUpFragment.BAND_UP_LEFT : handsUpOther.getValue();
        if (handsUpOther != null)
            handsUpString = handsUpOther.getValue() == null ? BandUpFragment.BAND_UP_LEFT : handsUpOther.getValue();
        BleApi1.BizApi.setHandsup(context, handsUpString);

        Other slpTimeOther = otherServer.getOther(null, Other.Type.SlpTime);
        String slpTimeString = (slpTimeOther == null || StringUtil.isBlank(slpTimeOther.getValue())) ? BandSleepFragment.SLPTIME : slpTimeOther.getValue();
//        if (slpTimeOther != null)
//            slpTimeString = slpTimeOther.getValue() == null ? BandSleepFragment.SLPTIME : slpTimeOther.getValue();
        BleApi1.BizApi.setSlpTime(context, slpTimeString);
    }

    public void syncBleSportAim(User userInfo) throws SQLException {
        Other sportAimOption = new OtherServer(context).getOther(userInfo, Other.Type.sportAim);
        int sportAim = SystemContant.defaultSportAim;
        try {
            sportAim = sportAimOption == null ? sportAim : Integer.valueOf(sportAimOption.getValue());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        BleApi1.BizApi.setSportAim(context, sportAim);
    }

    public void syncBleData(final int serialNo, final DelayCallback delayCallback) {
        if (serialNo > 0)
            handler.removeMessages(serialNo - 1);
        else {
            handler.removeMessages(CASE_CYCLE_SYNC_MONITOR_DATA);
            Message msg = new Message();
            msg.what = CASE_CYCLE_SYNC_MONITOR_DATA;
            msg.obj = delayCallback;
            handler.sendMessageDelayed(msg, SystemContant.syncBleDataInterval);
        }
        BleApi1.BizApi.requestData(context, serialNo, new BleApi1.DefaultCallback() {
            @Override
            public void onSendBle(final OrderQueue.Cmd cmd) {
                super.onSendBle(cmd);
                LogUtil.i("请求数据 onSendBle");

                Message msg = new Message();
                msg.what = serialNo;
                msg.obj = delayCallback;
                delayCallback.cmd = cmd;
                handler.sendMessageDelayed(msg, cmd.timeout);
            }
        });
    }

    public MonitorDataHandler motionDataHandler;

    public void syncBle() {
        BleApi1.BizApi.setTime(context, new SimpleDateFormat(SystemContant.timeFormat9Str).format(new Date()));
        BleApi1.BizApi.getStep(context);
        BleApi1.BizApi.stepStore(context);
        syncServer.syncBleData(0, new SyncServer.DelayCallback() {
            @Override
            public void callback(OrderQueue.Cmd cmd) {
                super.callback(cmd);
//                OrderQueue.response(context, cmd.id);
//                if (motionDataHandler != null)
//                    motionDataHandler.clean();
                if (motionDataHandler == null) {
                    OrderQueue.response(context, cmd.id);
                } else {
                    motionDataHandler.timeoutEvent(cmd.id);
                    motionDataHandler = null;
                }
            }
        });
        try {
            syncServer.syncBleOption(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private final int CASE_CYCLE_SYNC_MONITOR_DATA = -20;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CASE_CYCLE_SYNC_MONITOR_DATA:
//                syncBleData(0, new SyncServer.DelayCallback() {
//                    @Override
//                    public void callback(OrderQueue.Cmd cmd) {
//                        super.callback(cmd);
//                        OrderQueue.response(context, cmd.id);
//                        MainActivity.sendBroadcast(context, MainActivity.CASE_DISMISS_DIALOG_2, null);
//                    }
//                });
                syncBleData(0, (DelayCallback) msg.obj);
                break;
            default:
                DelayCallback callback = (DelayCallback) msg.obj;
                if (callback != null) {
                    callback.callback(callback.cmd);
                }
                break;
        }
        return false;
    }

    public static class DelayCallback {
        public OrderQueue.Cmd cmd;

        public void callback(OrderQueue.Cmd cmd) {
            if (MyUncaughtHandler.uncaughtHandlerToggle)
                Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        }
    }

    private static SyncServer syncServer;

    public static synchronized SyncServer getInstance(Context context) {
        if (syncServer == null) {
            syncServer = new SyncServer(context);
        }
        return syncServer;
    }

    public static void syncSuccess(Context context) {
        Intent intent = new Intent(ACTION_SYNC_SUCCESS);
        context.sendBroadcast(intent);
    }
}
