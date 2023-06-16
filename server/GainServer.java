package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.LoadCoin;
import com.desay.iwan2.common.api.http.entity.response.GetGolddata;
import com.desay.iwan2.common.api.http.entity.response.GoldListdata;
import com.desay.iwan2.common.api.http.entity.response.Golddetaildata;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Gain;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class GainServer {
    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<User, String> userDao;
    public Dao<Gain, Integer> gainDao;
    public Dao<Other, Integer> otherDao;
    public Dao<Day, Integer> dayDao;

    public GainServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        userDao = dbHelper.getUserDao();
        gainDao = dbHelper.getGainDao();
        otherDao = dbHelper.getOtherDao();
        dayDao = dbHelper.getDayDao();
    }

    public void network2Local(String startTime, String endTime, MyJsonHttpResponseHandler callback) {
        LoginInfoServer loginInfoServer = new LoginInfoServer(context);
        final LoginInfoServer.LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo == null) {
            return;
        }
        LoadCoin param = new LoadCoin();
        param.setUsername(loginInfo.getAccount());
        param.setStartDate(startTime);
        param.setEndDate(endTime);
        Api1.loadCoin(context, param,
                callback == null ? new MyJsonHttpResponseHandler(context) {
                    @Override
                    public void onSuccess(final Context context, String str) {
                        if (StringUtil.isBlank(str))
                            return;

                        final GoldListdata entity = JSON.parseObject(str, GoldListdata.class);
                        try {
                            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    User user = new UserInfoServer(context).getUserInfo();
                                    if (user == null)
                                        return null;

                                    //当前拥有金币
                                    new OtherServer(context).createOrUpdate(user, Other.Type.currentGain, "" + entity.getTotal());

                                    //每天金币
                                    for (GetGolddata every : entity.getEvery()) {
                                        saveFromNetworkResponse(user, every);
                                    }
                                    SyncServer.syncSuccess(context);
                                    return null;
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } : callback
        );
    }

    public void saveFromNetworkResponse(User user, GetGolddata entity) throws Exception {
        Day day = new DayServer(context).getDay(user, SystemContant.timeFormat6.parse(entity.getGdate()));
        if (day == null) {
            day = new Day();
            day.setUser(user);
            day.setDate(SystemContant.timeFormat6.parse(entity.getGdate()));
        } else {
            for (Golddetaildata golddetaildata : entity.getDetail()) {
                Gain p3 = new Gain();
                p3.setDay(day);
                p3.setTime(golddetaildata.getGtime());
                p3.setTypeCode(golddetaildata.getGtype());
                gainDao.delete(gainDao.queryForMatchingArgs(p3));
            }
        }
//        day.setGain(entity.getCount());
        day.setMaxGain(entity.getDayGold());
        dayDao.createOrUpdate(day);

//        day = dayDao.queryForMatchingArgs(day).get(0);
        for (Golddetaildata golddetaildata : entity.getDetail()) {
            Gain gain = new Gain();
            gain.setDay(day);
            gain.setCount(golddetaildata.getGold());
            gain.setEventCode(golddetaildata.getGevent());
            gain.setTime(golddetaildata.getGtime());
            gain.setTypeCode(golddetaildata.getGtype());
            gainDao.create(gain);
        }
    }

    public int getDayGain(Day day) throws SQLException {
        int result = 0;
//        if (user == null)
//            user = new UserInfoServer(context).getUserInfo();
//        if (user == null)
//            return result;
//
//        Day day = new DayServer(context).getDay(user, date);
        if (day != null) {
            Gain param = new Gain();
            param.setDay(day);
            List<Gain> gains = gainDao.queryForMatchingArgs(param);
            for (Gain gain : gains) {
                result += gain.getCount() == null ? 0 : gain.getCount();
            }
        }

        return result;
    }
}
