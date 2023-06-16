package com.desay.iwan2.module.sport.server;

import android.content.Context;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.common.server.DayServer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SportDayServer {
    private Context context;
    private DatabaseHelper dbHelper;

    public Date date;
    private int totalStep;
    private Dao<Sport, Integer> sportDao;
    private List<Sport> daySport;

    public SportDayServer(Context context, Date date) throws SQLException {
        this.date = date;
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sportDao = dbHelper.getSportDao();

//        init();
    }

    public void init() throws SQLException {
        totalStep = 0;
        daySport = new ArrayList<Sport>();

        Day day = new DayServer(context).getDay(null, date);
        if (day == null) return;

        String sql = "select startTime,sum(stepCount),sum(distance),round(avg(aerobics),0),sum(calorie) " +
                " from sport" +
                " where " + Sport.DAY_ID + "='" + day.getId() + "'" +
                " group by strftime('%s',startTime)/" +
                (SystemContant.sportMotionInterval * 60) +
                " order by startTime";
        GenericRawResults<Sport> rawResults = sportDao.queryRaw(
                sql, new RawRowMapper<Sport>() {
                    public Sport mapRow(String[] columnNames,
                                        String[] resultColumns) {
                        String startTime = resultColumns[0];
                        String stepCount = resultColumns[1];
                        String distance = resultColumns[2];
                        String aerobics = resultColumns[3];
                        String calorie = resultColumns[4];

                        Sport sport = new Sport();
                        try {
                            sport.setStartTime(SystemContant.timeFormat8.parse(startTime));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        sport.setStepCount(StringUtil.isBlank(stepCount) ? 0 : Integer.valueOf(stepCount));
                        sport.setDistance(StringUtil.isBlank(distance) ? 0 : Integer.valueOf(distance));
                        sport.setAerobics(StringUtil.isBlank(aerobics) ? false : "1".equals(aerobics));
                        sport.setCalorie(StringUtil.isBlank(calorie) ? 0 : Float.valueOf(calorie));

                        totalStep += sport.getStepCount();
                        return sport;
                    }
                }
        );

        if (rawResults != null)
            daySport.addAll(rawResults.getResults());
    }

    public String getText1() {
        return SystemContant.timeFormat2.format(date);
    }

    public String getTotalStep() {
        return context.getString(R.string.SportStepLabel) + totalStep;
    }

    public List<Sport> getSport() {
        return daySport;
    }

}
