package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitSportData;
import com.desay.iwan2.common.api.http.entity.request.LoadSportData;
import com.desay.iwan2.common.api.http.entity.request.ThirdBinddata;
import com.desay.iwan2.common.api.http.entity.response.GetGolddata;
import com.desay.iwan2.common.api.http.entity.response.SportListdata;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Gain;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.module.sport.server.SportAimServer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class SportServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Day, Integer> dayDao;
    private Dao<Sport, Integer> sportDao;
    public Dao<Gain, Integer> gainDao;

    public SportServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        dayDao = dbHelper.getDayDao();
        sportDao = dbHelper.getSportDao();
        gainDao = dbHelper.getGainDao();
    }

    public void network2Local(String startTime, String endTime, MyJsonHttpResponseHandler callback) {
        LoginInfoServer loginInfoServer = new LoginInfoServer(context);
        final LoginInfoServer.LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo == null) return;

        LoadSportData param = new LoadSportData();
        param.setUsername(loginInfo.getAccount());
        param.setStartDate(startTime);
        param.setEndDate(endTime);
        Api1.loadSportData(context, param,
                callback == null ? new MyJsonHttpResponseHandler(context) {
                    @Override
                    public void onSuccess(final Context context, String str) {
                        if (StringUtil.isBlank(str))
                            return;

                        final List<SportListdata> entities = JSON.parseArray(str, SportListdata.class);
                        try {
                            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    User user = new UserInfoServer(context).getUserInfo();
                                    if (user == null) return null;

                                    for (SportListdata entity : entities) {
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

    public void saveFromNetworkResponse(User user, SportListdata entity) throws Exception {
        String dateStr = StringUtil.subString(entity.getStartTime(), 0, 8);
        Date date = SystemContant.timeFormat6.parse(dateStr);
        Day day = new DayServer(context).getDay(user, date);
        if (day == null) {
            day = new Day();
            day.setUser(user);
            day.setDate(date);
            dayDao.createOrUpdate(day);
        } else {
            Sport param = new Sport();
            param.setDay(day);
            param.setStartTime(SystemContant.timeFormat9.parse(entity.getStartTime()));
            List<Sport> sports = sportDao.queryForMatchingArgs(param);
            sportDao.delete(sports);
        }

        Sport sport = new Sport();
        sport.setDay(day);
        try {
            if (!StringUtil.isBlank(entity.getStartTime()) && !"null".equals(entity.getStartTime()))
                sport.setStartTime(SystemContant.timeFormat9.parse(entity.getStartTime()));
            if (!StringUtil.isBlank(entity.getEndTime()) && !"null".equals(entity.getEndTime()))
                sport.setEndTime(SystemContant.timeFormat9.parse(entity.getEndTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sport.setTypeCode(entity.getSportTypeCode());
        sport.setDistance(entity.getDistance());
        sport.setCalorie(entity.getCalorie());
        sport.setAerobics("1".equals(entity.getLivenCode()));
        sport.setStepCount(entity.getPace());
        sport.setSync(true);
        sportDao.create(sport);
    }

//    public void saveFromBleResponse(User user, Date time, int step, boolean isAerobics) throws SQLException {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(time);
//        calendar.set(Calendar.HOUR, 0);
//        calendar.clear(Calendar.MINUTE);
//        calendar.clear(Calendar.SECOND);
//        calendar.clear(Calendar.MILLISECOND);
//        Date date = calendar.getTime();
//        Day day = new DayServer(context).getDay(user, date);
//        if (day == null) {
//            day = new Day();
//            day.setUser(user);
//            day.setDate(date);
//            dayDao.createOrUpdate(day);
//        } else {
//            Sport param = new Sport();
//            param.setDay(day);
//            param.setStartTime(time);
//            sportDao.delete(sportDao.queryForMatchingArgs(param));
//        }
//
//        Sport sport = new Sport();
//        sport.setDay(day);
//        sport.setStartTime(time);
//        calendar.setTime(time);
//        calendar.add(Calendar.MINUTE, SystemContant.sportMotionInterval);
//        sport.setEndTime(calendar.getTime());
//        sport.setTypeCode(0);//TODO 运动类型？
//
//        sport.setDistance((int) SportAimServer.getDistanceByStep(step, Integer.valueOf(user.getHeight())));
//        sport.setCalorie((float) SportAimServer.getCalorie(user.getWeight(), sport.getDistance()));
//        sport.setAerobics(isAerobics);
//        sport.setStepCount(step);
//        sportDao.create(sport);
//    }

    public void local2Network(MyJsonHttpResponseHandler callback) throws SQLException {
        final User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return;

        com.desay.iwan2.common.api.http.entity.request.CommitSportData param = new com.desay.iwan2.common.api.http.entity.request.CommitSportData();
        param.setUsername(user.getId());
        param.setSportdata(new ArrayList<CommitSportData.Day>());
        param.setBind(new ArrayList<ThirdBinddata>());//TODO 第三方健康
        ThirdBinddata binddata = new TencentServer(context).getBinddata();
        if (binddata != null)
            param.getBind().add(binddata);

        Day p1 = new Day();
        p1.setUser(user);
        List<Day> dayList = dayDao.queryForMatchingArgs(p1);
        final List<Sport> sportList = new ArrayList<Sport>();
        for (Day day : dayList) {
            Sport p2 = new Sport();
            p2.setDay(day);
            p2.setSync(false);
            List<Sport> sportList1 = sportDao.queryForMatchingArgs(p2);
            if (sportList1.size() == 0) continue;

            sportList.addAll(sportList1);

            CommitSportData.Day record = new CommitSportData.Day();
            param.getSportdata().add(record);
            record.setGdate(SystemContant.timeFormat6.format(day.getDate()));
            record.setDetail(new ArrayList<CommitSportData.Detail>());

            for (Sport sport : sportList1) {
                CommitSportData.Detail detail = new CommitSportData.Detail();
                record.getDetail().add(detail);
                detail.setStartTime(SystemContant.timeFormat9.format(sport.getStartTime()));
//                detail.setEndTime(SystemContant.timeFormat9.format(sport.getEndTime()));
                detail.setCalorie(sport.getCalorie());
                detail.setDistance(sport.getDistance());
                detail.setGmode(sport.getMode());
                detail.setLivenCode(sport.getAerobics() ? "1" : "0");
                detail.setPace(sport.getStepCount());
                detail.setSportTypeCode(sport.getTypeCode());
            }
        }

        if (param.getSportdata().size() == 0) {
            LogUtil.i("无新运动数据");
            return;
        }

        Api1.commitSportData(context, param, callback == null ? new MyJsonHttpResponseHandler(context) {
            @Override
            public void onSuccess(final Context context, String str) {
                if (StringUtil.isBlank(str))
                    return;

                final List<GetGolddata> entities = JSON.parseArray(str, GetGolddata.class);

                try {
                    TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            GainServer gainServer = new GainServer(context);
                            if (entities != null)
                                for (GetGolddata entity : entities) {
                                    gainServer.saveFromNetworkResponse(user, entity);
                                }

                            for (Sport sport : sportList) {
                                sport.setSync(true);
                                sportDao.update(sport);
                            }

                            return null;
                        }
                    });

                    String currDate = SystemContant.timeFormat6.format(new Date());
                    new GainServer(context).network2Local(currDate, currDate, null);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                SyncServer.syncSuccess(context);
            }
        } : callback);
    }
}
