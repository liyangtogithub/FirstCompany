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
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SleepYearServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Sleep, Integer> sleepDao;

    public Calendar[] rangeCalendars;
    public Double sleepAim = SystemContant.defaultSleepAim;

    public SleepYearServer(Context context, Date date) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sleepDao = dbHelper.getSleepDao();

        rangeCalendars = new Calendar[]{
                Calendar.getInstance(), Calendar.getInstance()
        };
        rangeCalendars[0].setTime(date);
        rangeCalendars[1].setTime(rangeCalendars[0].getTime());
        rangeCalendars[1].add(Calendar.YEAR, -1);
    }

    private synchronized void setDateRange(int offset) {
        rangeCalendars[0].add(Calendar.MONTH, offset);
        rangeCalendars[1].add(Calendar.MONTH, offset);
    }

    public List<SleepDuration> initData() throws SQLException {
        User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return null;

        String sql = "select avg(duration),date from ("
                + "select sum(" + Sleep.TOTAL_DURATION + ") duration," + Day.DATE + " date"
                + " from " + Sleep.TABLE + " s," + Day.TABLE + " d where s."
                + Sleep.DAY_ID + " = d." + Day.ID
                + " and d." + Day.USER_ID + "='" + user.getId() + "' group by d." + Day.DATE
                + ") group by strftime('%Y%m',date)" +
                " order by date";
        GenericRawResults<SleepDuration> rawResults = sleepDao.queryRaw(
                sql, new RawRowMapper<SleepDuration>() {
            public SleepDuration mapRow(String[] columnNames,
                                        String[] resultColumns) {
                SleepDuration sleepStateDuration = new SleepDuration();
                try {
                    sleepStateDuration.date = SystemContant.timeFormat8.parse(resultColumns[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sleepStateDuration.duration = resultColumns[0] == null ? 0 : Double.valueOf(resultColumns[0]);
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

    public static class SleepDuration {
        public double duration;
        public Date date;
    }
}
