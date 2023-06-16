package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.SharedPreferences;

import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitMac;
import com.desay.iwan2.common.api.http.entity.request.Common;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.LoginInfoServer.LoginInfo;
import com.j256.ormlite.dao.Dao;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 李鼠 on 14-6-23.
 */
public class MacServer {
    private Context context;
    public DatabaseHelper dbHelper;
    public Dao<BtDev, Integer> btDevDao;
    public LoginInfoServer.LoginInfo loginInfo;
    User user = null;
    public static final String KEY_STATE = "mac_state";

    public MacServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        btDevDao = dbHelper.getBtDevDao();
        UserInfoServer userInfoServer = new UserInfoServer(context);
        user = userInfoServer.getUserInfo();
        loginInfo = new LoginInfoServer(context).getLoginInfo();
    }

    /**
     * 提交Mac
     */
    public void commitMac(MyJsonHttpResponseHandler callback) throws SQLException {
        if (loginInfo != null) {
            final BtDev oldBtDev = getBtDev();
            if (oldBtDev == null || oldBtDev.getSync())
                return;
            CommitMac param = new CommitMac();
            param.setUsername(loginInfo.getAccount());
            param.setMac(oldBtDev.getMac());
            Api1.commitMac(context, param,
                    callback == null ? new MyJsonHttpResponseHandler(context) {
                        @Override
                        public void onSuccess(Context context, String str) {
                            // TODO 提交完Mac后需要处理的业务
                            oldBtDev.setSync(true);
                            try {
                                btDevDao.update(oldBtDev);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
//                            ToastUtil.shortShow(context, "提交Mac成功");
                        }
                    } : callback);
        }
    }

    private BtDev getBtDev() {
        BtDev btDev = new BtDev();
        btDev.setUser(user);
        List<BtDev> btDevList = null;
        try {
            btDevList = btDevDao.queryForMatching(btDev);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (btDevList != null && btDevList.size() > 0)
            return btDevList.get(0);
        return null;
    }

    /**
     * 下载Mac
     */
    public void loadMac(MyJsonHttpResponseHandler callback) throws SQLException {
        if (loginInfo != null) {
            if (callback == null) {
                BtDev btDev = getBtDev();
                if (btDev != null && !btDev.getSync()) {
                    return;
                }
            }

            Common param = new Common();
            param.setUsername(loginInfo.getAccount());
            Api1.loadMac(context, param,
                    callback == null ? new MyJsonHttpResponseHandler(context) {
                        @Override
                        public void onSuccess(Context context, String str) {
//                            if (StringUtil.isBlank(str))
//                                return;

                            storeMac(str, true);
                            // TODO 下载完Mac后需要处理的业务
                            //ToastUtil.shortShow(context, "下载Mac成功");
                        }
                    } : callback);
        }
    }

    public void storeMac(String mac, boolean sycn) {
        try {
            BtDev oldBtDev = getBtDev();
            BtDev btDev = null;
            if (oldBtDev == null) {
                btDev = new BtDev();
                btDev.setUser(user);
                btDev.setSync(sycn);
                btDev.setMac(mac);
                btDevDao.createOrUpdate(btDev);
            } else {
                oldBtDev.setMac(mac);
                oldBtDev.setSync(sycn);
                btDevDao.createOrUpdate(oldBtDev);
            }
            commitMac(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getMac() {
        BtDev oldBtDev = getBtDev();
        if (oldBtDev != null)
            return oldBtDev.getMac();
        return null;
    }

}
