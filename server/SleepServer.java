package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitSleep;
import com.desay.iwan2.common.api.http.entity.request.LoadSleepData;
import com.desay.iwan2.common.api.http.entity.request.ThirdBinddata;
import com.desay.iwan2.common.api.http.entity.response.Golddetaildata;
import com.desay.iwan2.common.api.http.entity.response.HeartEntity;
import com.desay.iwan2.common.api.http.entity.response.SleepListdata;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.*;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class SleepServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Day, Integer> dayDao;
    private Dao<Sleep, Integer> sleepDao;
    private Dao<com.desay.iwan2.common.db.entity.SleepState, Integer> stateDao;
    private Dao<HeartRate, Integer> heartRateDao;
    public Dao<Gain, Integer> gainDao;

    public SleepServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        dayDao = dbHelper.getDayDao();
        sleepDao = dbHelper.getSleepDao();
        stateDao = dbHelper.getSleepStateDao();
        heartRateDao = dbHelper.getHeartRateDao();
        gainDao = dbHelper.getGainDao();
    }

    public void network2Local(String startTime, String endTime, MyJsonHttpResponseHandler callback) {
        LoginInfoServer loginInfoServer = new LoginInfoServer(context);
        final LoginInfoServer.LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo == null) return;

        LoadSleepData param = new LoadSleepData();
        param.setUsername(loginInfo.getAccount());
        param.setStartDate(startTime);
        param.setEndDate(endTime);
        Api1.loadSleepData(context, param,
                callback == null ? new MyJsonHttpResponseHandler(context) {
                    @Override
                    public void onSuccess(final Context context, String str) {
                        if (StringUtil.isBlank(str))
                            return;

                        final List<SleepListdata> entities = JSON.parseArray(str, SleepListdata.class);
                        try {
                            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    User user = new UserInfoServer(context).getUserInfo();
                                    if (user == null) return null;

                                    for (SleepListdata entity : entities) {
                                        saveFromNetworkResponse(user, entity);
                                    }
                                    return null;
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } : callback
        );
    }

    public void saveFromNetworkResponse(User user, SleepListdata entity) throws Exception {
        Day day = new DayServer(context).getDay(user, SystemContant.timeFormat6.parse(entity.getGdate()));
        if (day == null) {
            day = new Day();
            day.setUser(user);
            day.setDate(SystemContant.timeFormat6.parse(entity.getGdate()));
        } else {
            Sleep param = new Sleep();
            param.setDay(day);
            param.setEndTime(SystemContant.timeFormat7.parse(entity.getWakeupTime()));
            sleepDao.delete(sleepDao.queryForMatchingArgs(param));
        }
        dayDao.createOrUpdate(day);

        // 睡眠记录
        Sleep sleep = new Sleep();
        sleep.setDay(day);
        sleep.setStartTime(SystemContant.timeFormat7.parse(entity.getSleepTime()));
        sleep.setEndTime(SystemContant.timeFormat7.parse(entity.getWakeupTime()));
        sleep.setTotalDuration(entity.getGtime());
        sleep.setScore(entity.getQuantity());
        sleepDao.create(sleep);

//        sleep = sleepDao.queryForMatchingArgs(sleep).get(0);
        // 状态
        List<com.desay.iwan2.common.api.http.entity.response.SleepState> sleepStateList = entity.getSleepState();
        for (com.desay.iwan2.common.api.http.entity.response.SleepState state : sleepStateList) {
            com.desay.iwan2.common.db.entity.SleepState sleepState = new com.desay.iwan2.common.db.entity.SleepState();
            sleepState.setSleep(sleep);
            sleepState.setStartTime(SystemContant.timeFormat7.parse(state.getStartTime()));
            sleepState.setEndTime(SystemContant.timeFormat7.parse(state.getEndTime()));
            sleepState.setState(com.desay.iwan2.common.db.entity.SleepState.State.code2State(state.getStateCode()));
            stateDao.create(sleepState);
        }
        // 心率
        List<HeartEntity> heartRateList = entity.getHeartRates();
        for (HeartEntity heartEntity : heartRateList) {
            HeartRate heartRate = new HeartRate();
            heartRate.setSleep(sleep);
            heartRate.setTime(SystemContant.timeFormat7.parse(heartEntity.getGt()));
            heartRate.setValue(heartEntity.getGv());
            heartRateDao.create(heartRate);
        }

        sleep.setSync(true);
        sleepDao.update(sleep);
    }

    public void local2Network(MyJsonHttpResponseHandler callback) throws SQLException {
        final User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return;

        CommitSleep param = new CommitSleep();
        param.setUsername(user.getId());
        param.setBaseSleep(new ArrayList<CommitSleep.Record>());
        param.setBind(new ArrayList<ThirdBinddata>());//TODO 第三方健康
        ThirdBinddata binddata = new TencentServer(context).getBinddata();
        if (binddata != null)
            param.getBind().add(binddata);

        final SimpleDateFormat timeFormat = new SimpleDateFormat(SystemContant.timeFormat7Str);
        Day p1 = new Day();
        p1.setUser(user);
        List<Day> dayList = dayDao.queryForMatchingArgs(p1);
        for (Day day : dayList) {
            Sleep p2 = new Sleep();
            p2.setDay(day);
            p2.setSync(false);
            List<Sleep> sleepList = sleepDao.queryForMatchingArgs(p2);
            if (sleepList.size() == 0) continue;
            for (Sleep sleep : sleepList) {
                CommitSleep.Record record = new CommitSleep.Record();
                param.getBaseSleep().add(record);
                if (sleep.getEndTime() != null)
                    record.setGtime(timeFormat.format(sleep.getEndTime()));
                record.setHeartRates(new ArrayList<CommitSleep.HeartRate>());
                record.setActs(new ArrayList<CommitSleep.Motion>());

                for (HeartRate heartRate : sleep.getHeartRates()) {
                    CommitSleep.HeartRate rate = new CommitSleep.HeartRate();
                    record.getHeartRates().add(rate);
                    rate.setGt(timeFormat.format(heartRate.getTime()));
                    rate.setGv(heartRate.getValue());
                }

                for (SleepMotion sleepMotion : sleep.getSleepMotions()) {
                    CommitSleep.Motion motion = new CommitSleep.Motion();
                    record.getActs().add(motion);
                    motion.setGt(timeFormat.format(sleepMotion.getTime()));
                    motion.setGv(sleepMotion.getValue());
                }
            }
        }
        if (param.getBaseSleep().size() == 0) {
            LogUtil.i("无新睡眠数据");
            return;
        }

        Api1.commitSleepData(context, param, callback == null ? new MyJsonHttpResponseHandler(context) {
            @Override
            public void onSuccess(final Context context, String str) {
                if (StringUtil.isBlank(str))
                    return;

                final List<SleepListdata> entities = JSON.parseArray(str, SleepListdata.class);

                try {
                    TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            DayServer dayServer = new DayServer(context);
                            GainServer gainServer = new GainServer(context);
                            for (SleepListdata entity : entities) {
                                Day day = dayServer.getDay(user, SystemContant.timeFormat6.parse(entity.getGdate()));
//                                day.setMaxGain(entity.getDayGold());
//                                dayServer.dayDao.update(day);
                                //睡眠
//                                Sleep param = new Sleep();
//                                param.setDay(day);
////                                param.setStartTime(SystemContant.timeFormat7.parse(entity.getSleepTime()));
//                                param.setEndTime(SystemContant.timeFormat7.parse(entity.getWakeupTime()));
//                                List<Sleep> sleeps = sleepDao.queryForMatchingArgs(param);
//                                if (sleeps.size() > 0) {
//                                    Sleep sleep = sleeps.get(0);
                                Date startTime = SystemContant.timeFormat7.parse(entity.getSleepTime());
                                Date endTime = SystemContant.timeFormat7.parse(entity.getWakeupTime());
                                Sleep sleep = getSleepByRange(day, startTime, endTime);
                                if (sleep != null) {
                                    sleep.setStartTime(startTime);
                                    sleep.setEndTime(endTime);
                                    sleep.setTotalDuration(entity.getGtime());
                                    sleep.setScore(entity.getQuantity());

                                    if (sleep.getSleepStates().size() > 0)
                                        stateDao.delete(sleep.getSleepStates());
                                    // 状态
                                    List<com.desay.iwan2.common.api.http.entity.response.SleepState> sleepStateList = entity.getSleepState();
                                    if (sleepStateList != null) {
                                        for (com.desay.iwan2.common.api.http.entity.response.SleepState state : sleepStateList) {
                                            com.desay.iwan2.common.db.entity.SleepState sleepState = new com.desay.iwan2.common.db.entity.SleepState();
                                            sleepState.setSleep(sleep);
                                            SimpleDateFormat dateFormat = new SimpleDateFormat(SystemContant.timeFormat7Str);
                                            sleepState.setStartTime(dateFormat.parse(state.getStartTime()));
                                            sleepState.setEndTime(dateFormat.parse(state.getEndTime()));
                                            sleepState.setState(com.desay.iwan2.common.db.entity.SleepState.State.code2State(state.getStateCode()));
                                            stateDao.create(sleepState);
                                        }

                                        sleep.setSync(true);
                                        sleepDao.update(sleep);
                                    }
                                }
//                                }

                                //金币
                                if (entity.getDetail() != null) {
//                                    for (Golddetaildata golddetaildata : entity.getDetail()) {
//                                        Gain gain = new Gain();
//                                        gain.setDay(day);
//                                        gain.setCount(golddetaildata.getGold());
//                                        gain.setEventCode(golddetaildata.getGevent());
//                                        gain.setTime(golddetaildata.getGtime());
//                                        gain.setTypeCode(golddetaildata.getGtype());
//                                        gainDao.create(gain);
//                                    }
                                    gainServer.saveFromNetworkResponse(user, entity);
                                }
                            }
                            return null;
                        }
                    });

                    String currDate = SystemContant.timeFormat6.format(new Date());
                    new GainServer(context).network2Local(currDate, currDate, null);

                    SyncServer.syncSuccess(context);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } : callback);
    }

    public Sleep getSleepByRange(Day day, Date time1, Date time2) throws SQLException {
        String sql = "select " + Sleep.ID + "," + Sleep.START_TIME + "," + Sleep.END_TIME +
                " from " + Sleep.TABLE +
                " where " + Sleep.DAY_ID + " = '" + day.getId() +
                "' and (" +
                "strftime('%Y-%m-%d %H:%M'," + Sleep.END_TIME + ") between '" + SystemContant.timeFormat12.format(time1) + "' and '" + SystemContant.timeFormat12.format(time2) + "'" +
                " or " +
                "'" + SystemContant.timeFormat12.format(time2) + "' between strftime('%Y-%m-%d %H:%M'," + Sleep.START_TIME + ")" + " and strftime('%Y-%m-%d %H:%M'," + Sleep.END_TIME + ")" +
                ")";
        GenericRawResults<Sleep> rawResults = sleepDao.queryRaw(
                sql.toString(), new RawRowMapper<Sleep>() {
                    public Sleep mapRow(String[] columnNames,
                                        String[] resultColumns) {
                        Sleep sleep = new Sleep();
                        sleep.setId(Integer.valueOf(resultColumns[0]));
                        try {
                            if (!StringUtil.isBlank(resultColumns[1]))
                                sleep.setStartTime(SystemContant.timeFormat8.parse(resultColumns[1]));
                            if (!StringUtil.isBlank(resultColumns[2]))
                                sleep.setEndTime(SystemContant.timeFormat8.parse(resultColumns[2]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return sleep;
                    }
                });
        for (Sleep rawResult : rawResults) {
            return sleepDao.queryForId(rawResult.getId());
        }
        return null;
    }

//    public void saveHeartRateFromBleResponse(User user, Date time, int value) throws SQLException {
//
//    }
}
