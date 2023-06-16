package com.desay.iwan2.module.sleep.server;

import android.content.Context;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.SleepState;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.SleepStateServer;
import com.desay.iwan2.common.server.UserInfoServer;
import com.desay.iwan2.module.sleep.adapter.SleepSummaryAdapter;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SleepSummaryServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Day, Integer> dayDao;
    private Dao<Sleep, Integer> sleepDao;
    private Dao<SleepState, Integer> stateDao;

    public Sleep mainSleep;

    public Date date;

    public SleepSummaryServer(Context context, Date date) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        dayDao = dbHelper.getDayDao();
        sleepDao = dbHelper.getSleepDao();
        stateDao = dbHelper.getSleepStateDao();
        this.date = date;
    }

    public String getTitle() {
        return SystemContant.timeFormat2.format(date);
    }

    public ArrayList<SleepSummaryAdapter.Entity> getSleepRecordList() throws Exception {
        ArrayList<SleepSummaryAdapter.Entity> l = new ArrayList<SleepSummaryAdapter.Entity>();
        SleepSummaryAdapter.Entity entity = null;
//        entity = new SleepSummaryAdapter.Entity();
//        l.add(entity);
//        entity.duration = "1h14'";
//        entity.range = "12:42~13:56";
//        entity.shallow = "43'";
//        entity.deep = "26'";
//        entity.dream = "0'";
//        entity.wakeup = "5'";

        User user = new UserInfoServer(context).getUserInfo();
        if (user != null) {
            Day p1 = new Day();
            p1.setUser(user);
            p1.setDate(date);
            List<Day> dayList = dayDao.queryForMatchingArgs(p1);
            for (Day day : dayList) {
                Sleep p2 = new Sleep();
                p2.setDay(day);
                List<Sleep> sleepList = sleepDao.queryForMatchingArgs(p2);
                for (Sleep sleep : sleepList) {
                    if (mainSleep == null ||
                            (mainSleep.getTotalDuration() == null ? 0 : mainSleep.getTotalDuration()) < (sleep.getTotalDuration() == null ? 0 : sleep.getTotalDuration())) {
                        mainSleep = sleep;
                    }

//                    if (sleep.getTotalDuration() == null) continue;

                    entity = new SleepSummaryAdapter.Entity();
                    l.add(entity);
                    entity.duration = TimeUtil.formatTimeString(sleep.getTotalDuration() == null ? 0 : sleep.getTotalDuration());
                    entity.range = SystemContant.timeFormat3.format(sleep.getStartTime())
                            + "~"
                            + SystemContant.timeFormat3.format(sleep.getEndTime());

//                    String sql = "select (sum(strftime('%s'," + SleepState.END_TIME + ")-strftime('%s'," + SleepState.START_TIME + ")))/60," + SleepState.STATE
//                            + " from " + SleepState.TABLE
//                            + " where " + SleepState.SLEEP_ID + " = '" + sleep.getId() + "' group by " + SleepState.STATE;
//                    GenericRawResults<SleepStateDuration> rawResults = stateDao.queryRaw(
//                            sql.toString(), new RawRowMapper<SleepStateDuration>() {
//                        public SleepStateDuration mapRow(String[] columnNames,
//                                        String[] resultColumns) {
//                            SleepStateDuration sleepStateDuration = new SleepStateDuration();
//                            sleepStateDuration.state = resultColumns[1];
//                            sleepStateDuration.duration = Integer.valueOf(resultColumns[0]);
//                            return sleepStateDuration;
//                        }
//                    });
                    int a = 0;

                    GenericRawResults<SleepStateServer.SleepStateDuration> rawResults = new SleepStateServer(context).getStateDuration(sleep);
                    int wakeupDuration = 0;
                    for (SleepStateServer.SleepStateDuration sleepStateDuration : rawResults) {
                        a += sleepStateDuration.duration == null ? 0 : sleepStateDuration.duration;

                        String str = TimeUtil.formatTimeString(sleepStateDuration.duration);
                        switch (SleepState.State.valueOf(sleepStateDuration.state)) {
                            case shallow:
                                entity.shallow = str;
                                break;
                            case deep:
                                entity.deep = str;
                                break;
                            case dream:
                                entity.dream = str;
                                break;
                            case wake:
                            case insleep:
//                                entity.wakeup = str;
                                wakeupDuration += sleepStateDuration.duration;
                                break;
                        }
                    }
                    entity.wakeup = TimeUtil.formatTimeString(wakeupDuration);
                    if (a == 0) {
                        l.remove(entity);
                    }
                }
            }
        }

        return l;
    }

    public String getMainSleepDuration() {
        return TimeUtil.formatTimeString(mainSleep.getTotalDuration());
    }

    public String getMainSleepScore() {
        DecimalFormat format = new DecimalFormat("0");
        return format.format(mainSleep.getScore());
    }

    public String getMainSleepStartTime() throws ParseException {
        return SystemContant.timeFormat3.format(mainSleep.getStartTime());
    }

    public String getMainSleepEndTime() throws ParseException {
        return SystemContant.timeFormat3.format(mainSleep.getEndTime());
    }
}
