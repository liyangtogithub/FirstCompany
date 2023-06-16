package com.desay.iwan2.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.desay.iwan2.common.db.entity.*;
import dolphin.tools.util.LogUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * @author 方奕峰
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "iwan2.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DatabaseHelper databaseHelper;

    public synchronized static DatabaseHelper getDataBaseHelper(Context context) {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context
                    .getApplicationContext(),
                    DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public static void destroyDataBaseHelper() {
        OpenHelperManager.releaseHelper();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            LogUtil.i("Create db");
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, TempData.class);
            TableUtils.createTable(connectionSource, Other.class);
            TableUtils.createTable(connectionSource, BtDev.class);
            TableUtils.createTable(connectionSource, Day.class);
            TableUtils.createTable(connectionSource, Sleep.class);
            TableUtils.createTable(connectionSource, SleepMotion.class);
            TableUtils.createTable(connectionSource, SleepState.class);
            TableUtils.createTable(connectionSource, HeartRate.class);
            TableUtils.createTable(connectionSource, Sport.class);
            TableUtils.createTable(connectionSource, Gain.class);
            TableUtils.createTable(connectionSource, GainEvent.class);
        } catch (SQLException e) {
            LogUtil.e("Can't create database:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
//        try {
//            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
//            if (oldVersion < 3) {
//                TableUtils.createTable(connectionSource, TempDataEntity.class);
//            }
//        } catch (SQLException e) {
//            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
//            throw new RuntimeException(e);
//        }
    }

    // the DAO object we use to access the SimpleData table
    private Dao<User, String> userDao;

    public Dao<User, String> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    private Dao<TempData, Integer> tempdataDao;

    public Dao<TempData, Integer> getTempdataDao() throws SQLException {
        if (tempdataDao == null) {
            tempdataDao = getDao(TempData.class);
        }
        return tempdataDao;
    }

    private Dao<Other, Integer> otherDao;

    public Dao<Other, Integer> getOtherDao() throws SQLException {
        if (otherDao == null) {
            otherDao = getDao(Other.class);
        }
        return otherDao;
    }

    private Dao<BtDev, Integer> btDevDao;

    public Dao<BtDev, Integer> getBtDevDao() throws SQLException {
        if (btDevDao == null) {
            btDevDao = getDao(BtDev.class);
        }
        return btDevDao;
    }

    private Dao<Day, Integer> dayDao;

    public Dao<Day, Integer> getDayDao() throws SQLException {
        if (dayDao == null) {
            dayDao = getDao(Day.class);
        }
        return dayDao;
    }

    private Dao<Sleep, Integer> sleepDao;

    public Dao<Sleep, Integer> getSleepDao() throws SQLException {
        if (sleepDao == null) {
            sleepDao = getDao(Sleep.class);
        }
        return sleepDao;
    }

    private Dao<SleepMotion, Integer> sleepMotionDao;

    public Dao<SleepMotion, Integer> getSleepMotionDao() throws SQLException {
        if (sleepMotionDao == null) {
            sleepMotionDao = getDao(SleepMotion.class);
        }
        return sleepMotionDao;
    }

    private Dao<SleepState, Integer> sleepStateDao;

    public Dao<SleepState, Integer> getSleepStateDao() throws SQLException {
        if (sleepStateDao == null) {
            sleepStateDao = getDao(SleepState.class);
        }
        return sleepStateDao;
    }

    private Dao<HeartRate, Integer> heartRateDao;

    public Dao<HeartRate, Integer> getHeartRateDao() throws SQLException {
        if (heartRateDao == null) {
            heartRateDao = getDao(HeartRate.class);
        }
        return heartRateDao;
    }

    private Dao<Sport, Integer> sportDao;

    public Dao<Sport, Integer> getSportDao() throws SQLException {
        if (sportDao == null) {
            sportDao = getDao(Sport.class);
        }
        return sportDao;
    }

    private Dao<Gain, Integer> gainDao;

    public Dao<Gain, Integer> getGainDao() throws SQLException {
        if (gainDao == null) {
            gainDao = getDao(Gain.class);
        }
        return gainDao;
    }

    private Dao<GainEvent, Integer> gainEventDao;

    public Dao<GainEvent, Integer> getGainEventDao() throws SQLException {
        if (gainEventDao == null) {
            gainEventDao = getDao(GainEvent.class);
        }
        return gainEventDao;
    }

    @Override
    public void close() {
        super.close();
        LogUtil.i("Close db");
        userDao = null;
        tempdataDao = null;
        otherDao = null;
        btDevDao = null;
        dayDao = null;
        sleepDao = null;
        sleepMotionDao = null;
        sleepStateDao = null;
        heartRateDao = null;
        sportDao = null;
        gainDao = null;
        gainEventDao = null;
    }
}
