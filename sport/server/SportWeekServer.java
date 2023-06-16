package com.desay.iwan2.module.sport.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SportWeekServer {
    private Context context;
    private DatabaseHelper dbHelper;

    public Calendar[] rangeCalendars;
    private int avgStep;
    private String sportType;
	private Dao<Sport, Integer> sportDao;
	private List<SportPoint> listPoint;

    public SportWeekServer(Context context ,Date date) throws SQLException {
        this.context = context;
        rangeCalendars = new Calendar[]{
                Calendar.getInstance(), Calendar.getInstance()
        };
        rangeCalendars[0].setTime(date);
        rangeCalendars[1].setTime(rangeCalendars[0].getTime());
        rangeCalendars[1].add(Calendar.DATE, -7);
    }

    public List<SportPoint> init() throws SQLException {
    	User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return null;

        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        sportDao = dbHelper.getSportDao();
        
        String sql = "select sum(" + Sport.COUNT + ")," + Day.DATE + " , strftime('%Y-%m-%W',startTime)  "
                + " from " + Sport.TABLE + " s," + Day.TABLE + " d where s."
                + Sport.DAY_ID + " = d." + Day.ID
                + " and d." + Day.USER_ID + "='" + user.getId() + "' group by d." + Day.ID
                + " order by d." + Day.DATE;
        GenericRawResults<SportPoint> rawResults = sportDao.queryRaw(
                sql.toString(), new RawRowMapper<SportPoint>() {
            public SportPoint mapRow(String[] columnNames,
                                        String[] resultColumns) {
            	SportPoint sportPoint = new SportPoint();
                try {
                	sportPoint.date = SystemContant.timeFormat8.parse(resultColumns[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sportPoint.step = Integer.valueOf(resultColumns[0]);
                return sportPoint;
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
        return SystemContant.timeFormat4.format(rangeCalendars[1].getTime()) + "~" + SystemContant.timeFormat4.format(rangeCalendars[0].getTime());
    }

    public String getAvgStep() {
        return "平均每日" + avgStep + "步";
    }

    public String getSportType() {
        return sportType;
    }
}
