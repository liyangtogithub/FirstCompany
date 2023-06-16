package com.desay.iwan2.module.sport.server;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.DayServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.UserInfoServer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SportSummaryServer {
    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<User, String> userDao;

    public Date date;
    public int aimRate;
    public int step;
    public int stepAim;
    public int aerobicStep;
    public int aerobicStepAim;
    public int calorie;
    public int calorieAim;
    public int distance;
    public int distanceAim;
    private Date startDate;
    private Date endDate;
    private Dao<Sport, Integer> sportDao;
    private List<Sport> daySport;


    public SportSummaryServer(Context context, Date date) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sportDao = dbHelper.getSportDao();

        this.date = date;
        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        cl.set(Calendar.HOUR_OF_DAY, 0);
        cl.set(Calendar.MINUTE, 0);
        this.startDate = cl.getTime();
        cl.set(Calendar.HOUR_OF_DAY, 23);
        cl.set(Calendar.MINUTE, 59);
        this.endDate = cl.getTime();

//        init();
    }

    public void init() throws SQLException {
        step = 0;
        aerobicStep = 0;
        calorie = 0;
        distance = 0;
        daySport = new ArrayList<Sport>();

        User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return;

        Day day = new DayServer(context).getDay(user, date);
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

                        SportSummaryServer.this.step += sport.getStepCount();
//                        if (sport.getAerobics())
//                            SportSummaryServer.this.aerobicStep += sport.getStepCount();
                        if (sport.getStepCount() > SystemContant.aerobicsBoundary)
                            SportSummaryServer.this.aerobicStep += sport.getStepCount();
                        SportSummaryServer.this.calorie += sport.getCalorie();
                        SportSummaryServer.this.distance += sport.getDistance();
                        return sport;
                    }
                }
        );

        if (rawResults != null)
            daySport.addAll(rawResults.getResults());
    }

    public List<Sport> getSportList() {
        return daySport;
    }

    public String getTitle() {
        return SystemContant.timeFormat2.format(date);
    }

    public String getAimRate() {
        return aimRate + "%";
    }

    public int getStepRate() {
        return step * 100 / stepAim;
    }

    public int getAerobicStepRate() {
        return aerobicStep * 100 / aerobicStepAim;
    }

    public int getCalorieRate() {
        return calorie * 100 / calorieAim;
    }

    public int getDistanceRate() {
        return distance * 100 / distanceAim;
    }

    public String getDistance() {
        return ((double) distance) / 1000 + "";
    }

    public String getDistanceAim() {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(((double) distanceAim) / 1000d);
    }

    public String getStep() {
        return step + "";
    }

    public String getStepAim() {
        return stepAim + "";
    }

    public String getAerobicStep() {
        return aerobicStep + "";
    }

    public String getAerobicStepAim() {
        return aerobicStepAim + "";
    }

    public String getCalorie() {
        return calorie + "";
    }

    public String getCalorieAim() {
        return calorieAim + "";
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
