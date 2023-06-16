package com.desay.iwan2.module.sleep.server;

import android.content.Context;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.SleepState;
import com.desay.iwan2.common.server.DayServer;
import com.desay.iwan2.common.server.SleepStateServer;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import dolphin.tools.util.LogUtil;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SleepDayServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Sleep, Integer> sleepDao;

    public Date date;
    public Sleep sleep;
    private int shallowDuration, deepDuration, dreamDuration, wakeupDuration = 0;
    private int deepRate;
    private float score;

    public SleepDayServer(Context context, Date date) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sleepDao = dbHelper.getSleepDao();
        this.date = date;
    }

    public Sleep initData() throws SQLException {
//        LogUtil.i("b1111111111111,date="+date);
        Day day = new DayServer(context).getDay(null, date);
//        LogUtil.i("c1111111111111");
        if (day == null) return null;

//        LogUtil.i("b1111111111112");
        sleep = sleepDao.queryBuilder().orderBy(Sleep.TOTAL_DURATION, false)
                .where().eq(Sleep.DAY_ID, day.getId()).queryForFirst();

//        LogUtil.i("b1111111111113");
        if (sleep == null) return null;

//        LogUtil.i("b1111111111114");
        score = sleep.getScore() == null ? 0 : sleep.getScore();

//        LogUtil.i("a1111111111111");
        GenericRawResults<SleepStateServer.SleepStateDuration> rawResults = new SleepStateServer(context).getStateDuration(sleep);
//        LogUtil.i("a1111111111112");
        for (SleepStateServer.SleepStateDuration sleepStateDuration : rawResults) {
            switch (SleepState.State.valueOf(sleepStateDuration.state)) {
                case shallow:
                    shallowDuration = sleepStateDuration.duration;
                    break;
                case deep:
                    deepDuration = sleepStateDuration.duration;
                    break;
                case dream:
                    dreamDuration = sleepStateDuration.duration;
                    break;
                case wake:
                case insleep:
                    wakeupDuration += sleepStateDuration.duration;
                    break;
            }
        }
//        LogUtil.i("a1111111111113");
        deepRate = (sleep.getTotalDuration() == null || sleep.getTotalDuration() == 0) ? 0 : deepDuration * 100 / sleep.getTotalDuration();
//        LogUtil.i("a1111111111114");

        return sleep;
    }

    public String getText1() {
        return SystemContant.timeFormat2.format(date);
    }

    public String getTotalDuration() {
        return context.getString(R.string.SleepDurationLabel) + (sleep == null ? "00h00'" :
                TimeUtil.formatTimeString(sleep.getTotalDuration() == null ? 0 : sleep.getTotalDuration()));
    }

    public String getShallowDuration() {
        return context.getString(R.string.SleepLight) + ":" + TimeUtil.formatTimeString(shallowDuration);
    }

    public String getDeepDuration() {
        return context.getString(R.string.SleepDeep) + ":" + TimeUtil.formatTimeString(deepDuration);
    }

    public String getDreamDuration() {
        return context.getString(R.string.SleepDream) + ":" + TimeUtil.formatTimeString(dreamDuration);
    }

    public String getWakeupDuration() {
        return context.getString(R.string.SleepWake) + ":" + TimeUtil.formatTimeString(wakeupDuration);
    }

    public String getDeepRate() {
        return deepRate + "%";
    }

    public String getScore() {
        DecimalFormat format = new DecimalFormat("0");
        return format.format(score) + context.getString(R.string.SleepScoreUnit);
    }
}
