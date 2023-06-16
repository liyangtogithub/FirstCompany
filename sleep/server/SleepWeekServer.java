package com.desay.iwan2.module.sleep.server;

import android.content.Context;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.UserInfoServer;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import dolphin.tools.util.LogUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SleepWeekServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Sleep, Integer> sleepDao;

    public Calendar[] rangeCalendars;
    //    private int avgDuration;
    public Double sleepAim = SystemContant.defaultSleepAim;

    public SleepWeekServer(Context context, Date date) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sleepDao = dbHelper.getSleepDao();

        rangeCalendars = new Calendar[]{
                Calendar.getInstance(), Calendar.getInstance()
        };
        rangeCalendars[0].setTime(date);
        rangeCalendars[1].setTime(rangeCalendars[0].getTime());
        rangeCalendars[1].add(Calendar.DATE, -7);
    }

    private synchronized void setDateRange(int offset) {
        rangeCalendars[0].add(Calendar.DATE, offset);
        rangeCalendars[1].add(Calendar.DATE, offset);
    }

    public List<SleepDuration> initData() throws Exception {
        User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return null;

        String sql = "select sum(" + Sleep.TOTAL_DURATION + ")," + Day.DATE + "," + Sleep.START_TIME + "," + Sleep.END_TIME
                + " from " + Sleep.TABLE + " s," + Day.TABLE + " d where s."
                + Sleep.DAY_ID + " = d." + Day.ID
                + " and d." + Day.USER_ID + "='" + user.getId() + "' group by d." + Day.ID
                + " order by d." + Day.DATE;
        GenericRawResults<SleepDuration> rawResults = sleepDao.queryRaw(
                sql.toString(), new RawRowMapper<SleepDuration>() {
                    public SleepDuration mapRow(String[] columnNames,
                                                String[] resultColumns) {
                        SleepDuration sleepStateDuration = new SleepDuration();
                        try {
                            sleepStateDuration.date = SystemContant.timeFormat8.parse(resultColumns[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        sleepStateDuration.duration = resultColumns[0] == null ? 0 : Integer.valueOf(resultColumns[0]);
                        try {
                            sleepStateDuration.startTime = SystemContant.timeFormat8.parse(resultColumns[2]);
                            sleepStateDuration.endTime = SystemContant.timeFormat8.parse(resultColumns[3]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return sleepStateDuration;
                    }
                });

        OtherServer otherServer = new OtherServer(context);
        Other other = otherServer.getOther(user, Other.Type.sleepAim);
        if (other != null) {
            sleepAim = Double.valueOf(other.getValue()) / 60;
        }

        return rawResults.getResults();
    }
//
//    public String getText1() {
//        return SystemContant.timeFormat4.format(rangeCalendars[1].getTime()) + "~" + SystemContant.timeFormat4.format(rangeCalendars[0].getTime());
//    }
//
//    public String getAvgDuration() {
//        return "平均时长：" + TimeUtil.formatTimeString(avgDuration);
//    }

    public static class SleepDuration {
        public int duration;
        public Date date;
        public Date startTime;
        public Date endTime;
    }
}
