package com.desay.iwan2.common.server;

import android.content.Context;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class DayServer {

    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<Day, Integer> dayDao;
    public Dao<User, String> userDao;

    public DayServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        dayDao = dbHelper.getDayDao();
        userDao = dbHelper.getUserDao();
    }

    public Day getDay(User user, Date date) throws SQLException {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return null;

        Day param = new Day();
        param.setUser(user);
        param.setDate(date);
        List<Day> dayList = dayDao.queryForMatchingArgs(param);
        if (dayList != null && dayList.size() > 0) {
            return dayList.get(0);
        }
        return null;
    }

    public Date[] getRange(User user) throws Exception {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return null;

        Date[] range = new Date[2];

        Day day1 = dayDao.queryBuilder().orderBy(Day.DATE, true).where()
                .eq(Day.USER_ID, user.getId())
                .queryForFirst();
        if (day1 == null) {
            range[0] = range[1] = new Date();
            return range;
        }
        range[0] = day1.getDate();

        Day day2 = dayDao.queryBuilder().orderBy(Day.DATE, false).where()
                .eq(Day.USER_ID, user.getId())
                .queryForFirst();
        range[1] = day2.getDate();

        return range;
    }
}
