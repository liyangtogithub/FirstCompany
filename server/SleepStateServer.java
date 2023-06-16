package com.desay.iwan2.common.server;

import android.content.Context;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.SleepState;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

import java.sql.SQLException;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SleepStateServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<SleepState, Integer> stateDao;

    public SleepStateServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        stateDao = dbHelper.getSleepStateDao();
    }

    public GenericRawResults<SleepStateDuration> getStateDuration(Sleep sleep) throws SQLException {
        String sql = "select (sum(strftime('%s'," + SleepState.END_TIME + ")-strftime('%s'," + SleepState.START_TIME + ")))/60," + SleepState.STATE
                + " from " + SleepState.TABLE
                + " where " + SleepState.SLEEP_ID + " = '" + sleep.getId() + "' group by " + SleepState.STATE;
        return stateDao.queryRaw(
                sql.toString(), new RawRowMapper<SleepStateDuration>() {
            public SleepStateDuration mapRow(String[] columnNames,
                                             String[] resultColumns) {
                SleepStateDuration sleepStateDuration = new SleepStateDuration();
                sleepStateDuration.state = resultColumns[1];
                sleepStateDuration.duration = Integer.valueOf(resultColumns[0]);
                return sleepStateDuration;
            }
        });
    }

    public static class SleepStateDuration {
        public String state;
        public Integer duration;
    }
}
