package com.desay.iwan2.module.sport.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.UserInfoServer;
import com.desay.iwan2.module.sport.server.SportMonthServer.SportPoint;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SportYearServer {
    private Context context;
    private DatabaseHelper dbHelper;

    public Calendar[] rangeCalendars;
    private int avgStep;
    private String sportType;
	private Dao<Sport, Integer> sportDao;
	private List<SportPoint> listPoint;

    public SportYearServer(Context context , Date date) throws SQLException{
    	this.context = context;
        rangeCalendars = new Calendar[]{
                Calendar.getInstance(), Calendar.getInstance()
        };
        rangeCalendars[0].setTime(date);
        rangeCalendars[1].setTime(rangeCalendars[0].getTime());
        rangeCalendars[1].add(Calendar.YEAR, -1);
    }

    public List<SportPoint> init() throws SQLException {
    	User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return null;
        
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sportDao = dbHelper.getSportDao();

        String sql = "select avg(stepsum),date from ("
                + "select sum(" + Sport.COUNT + ") stepsum, " + Day.DATE + " date"
                + " from " + Sport.TABLE + " s," + Day.TABLE + " d where s."
                + Sport.DAY_ID + " = d." + Day.ID
                + " and d." + Day.USER_ID + "='" + user.getId() + "' group by d." + Day.DATE
                + ") group by strftime('%Y%m',date)" +
                " order by date";
        GenericRawResults<SportPoint> rawResults = sportDao.queryRaw(
        		sql.toString(), new RawRowMapper<SportPoint>() {
            public SportPoint mapRow(String[] columnNames,
                                        String[] resultColumns) {
            	SportPoint sleepStateDuration = new SportPoint();
                try {
                    sleepStateDuration.date = SystemContant.timeFormat8.parse(resultColumns[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sleepStateDuration.step = Math.round(Float.valueOf(resultColumns[0]));
                return sleepStateDuration;
            }
        });
        
        listPoint = rawResults.getResults();
        if(listPoint.size() > 0){
        	int sumStep = 0;
            for(int i = 0; i < listPoint.size(); ++i)
            {
            	sumStep += listPoint.get(i).step;
            }
            avgStep = sumStep / listPoint.size();
        }
        
        return listPoint;
    }

    public List<SportPoint> getSportPointList()
    {
    	return listPoint;
    }

    public String getText1() {
        return SystemContant.timeFormat5.format(rangeCalendars[1].getTime()) 
        		+ "~" 
        		+ SystemContant.timeFormat5.format(rangeCalendars[0].getTime());
    }

    public String getAvgStep() {
        return "平均每月" + avgStep + "步";
    }

    public String getSportType() {
        return sportType;
    }

	
}
