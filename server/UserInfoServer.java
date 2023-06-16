package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitUserInfo;
import com.desay.iwan2.common.api.http.entity.request.Login;
import com.desay.iwan2.common.api.http.entity.response.UserInfodata;
import com.desay.iwan2.common.contant.Sex;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class UserInfoServer {
    private Context context;
    public Dao<User, String> userDao;
    private LoginInfoServer loginInfoServer;

//    LoginInfoServer.LoginInfo loginInfo = null;

    public UserInfoServer(Context context) throws SQLException {
        this.context = context;
        DatabaseHelper dbHelper = DatabaseHelper.getDataBaseHelper(context);
        userDao = dbHelper.getUserDao();
        loginInfoServer = new LoginInfoServer(context);
//        loginInfo = new LoginInfoServer(context).getLoginInfo();
    }

    public void save(User user) throws SQLException {
        userDao.createOrUpdate(user);
    }

    /**
     * 获取用户信息
     *
     * @return
     * @throws SQLException
     */
    public User getUserInfo() throws SQLException {
        LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
        if (loginInfo != null) {
            List<User> userList = userDao.queryForEq(User.ID, loginInfo.getAccount());
            for (User user : userList) {
                return user;
            }
        }
        return null;
    }

    public void network2Local(MyJsonHttpResponseHandler callback) {
        final LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
        if (loginInfo != null) {
            Login param = new Login();
            param.setUsername(loginInfo.getAccount());
            param.setPasswd(loginInfo.getPwd());
            Api1.login(context, param,
                    callback == null ? new MyJsonHttpResponseHandler(context) {
                        @Override
                        public void onSuccess(Context context, String str) {
                            if (StringUtil.isBlank(str))
                                return;

                            UserInfodata entity = JSON.parseObject(str, UserInfodata.class);
                            try {
                                List<User> userList = userDao.queryForEq(
                                        User.ID, loginInfo.getAccount());
                                User user = null;
                                if (userList == null || userList.size() == 0) {
                                    user = new User();
                                    user.setId(loginInfo.getAccount());
                                } else {
                                    user = userList.get(0);
                                }
                                user.setNickname(entity.getNickname());
                                user.setPortraitUrl(entity.getPortraitUrl());
                                user.setAddress(entity.getAddress());
                                user.setBirthday(entity.getBirthday());
                                user.setSex(StringUtil.isBlank(entity.getSexCode()) ? null : Sex.convert(entity.getSexCode()));
                                String height = null;
                                if (entity.getHeight() != null) {
                                    int dotIndex = entity.getHeight().indexOf(".");
                                    height = dotIndex == -1 ? entity.getHeight() : entity.getHeight().substring(0, dotIndex);
                                }
                                user.setHeight(height);

                                String weight = null;
                                if (entity.getWeight() != null) {
                                    int dotIndex = entity.getWeight().indexOf(".");
                                    weight = dotIndex == -1 ? entity.getWeight() : entity.getWeight().substring(0, dotIndex);
                                }
                                user.setWeight(weight);
                                user.setSync(true);
                                userDao.createOrUpdate(user);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                            : callback);
        }
    }

    /**
     * 提交个人信息
     */
    public void local2Network(MyJsonHttpResponseHandler callback) {
        LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
        if (loginInfo != null) {
            List<User> userList = null;
            try {
                userList = userDao.queryForEq(User.ID, loginInfo.getAccount());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (userList == null)
                return;
            final User user = userList.get(0);
            if (user.getSync()) return;
            CommitUserInfo param = CommitUserInfo.generateFromDb(user);
            Api1.commitUserInfo(context, param,
                    callback == null ? new MyJsonHttpResponseHandler(context) {
                        @Override
                        public void onSuccess(Context context, String str) {
                            user.setSync(true);
                            try {
                                userDao.update(user);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

//                            ToastUtil.shortShow(context, "提交个人信息成功");
                        }
                    } : callback);
        }
    }

    public void storeInfo(User newUser) {
        try {
            LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
            List<User> userList = userDao.queryForEq(User.ID, loginInfo.getAccount());
            User user = null;
            if (userList == null || userList.size() == 0) {
                newUser.setSync(false);
                userDao.createOrUpdate(newUser);
            } else {
                user = userList.get(0);
                user.setId(loginInfo.getAccount());
                user.setPortrait(newUser.getPortrait());
                user.setAddress(newUser.getAddress());
                user.setBirthday(newUser.getBirthday());
                user.setSex(newUser.getSex());
                user.setHeight(newUser.getHeight());
                user.setWeight(newUser.getWeight());
                user.setIsEmpty(newUser.getIsEmpty());
                user.setSync(false);
                userDao.createOrUpdate(user);
            }
            local2Network(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//	private User getInfo() {
//        List<User> userList = null;
//        try {
//            userList = userDao.queryForEq(User.ID, loginInfo.getAccount());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        if (userList != null || userList.size() > 0) {
//            userList.get(0);
//        }
//        return null;
//    }

    public void storeImage(User newUser) {
        try {
            LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
            List<User> userList = userDao.queryForEq(User.ID, loginInfo.getAccount());
            User user = null;
            if (userList == null || userList.size() == 0) {
                newUser.setSync(false);
                userDao.createOrUpdate(newUser);
            } else {
                user = userList.get(0);
                user.setPortrait(newUser.getPortrait());
                user.setSync(false);
                userDao.createOrUpdate(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
