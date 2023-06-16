package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitAim;
import com.desay.iwan2.common.api.http.entity.request.CommitMac;
import com.desay.iwan2.common.api.http.entity.request.CommitSetting;
import com.desay.iwan2.common.api.http.entity.request.CommitUserInfo;
import com.desay.iwan2.common.api.http.entity.request.Common;
import com.desay.iwan2.common.api.http.entity.request.Login;
import com.desay.iwan2.common.api.http.entity.request.Register;
import com.desay.iwan2.common.api.http.entity.response.AimSetdata;
import com.desay.iwan2.common.api.http.entity.response.ParamSetdata;
import com.desay.iwan2.common.api.http.entity.response.UserInfodata;
import com.desay.iwan2.common.contant.Sex;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class AimServer {
    private Context context;
    public DatabaseHelper dbHelper;
    public LoginInfoServer.LoginInfo loginInfo;
    public Dao<Other, Integer> otherDao;
    User user = null;

    public AimServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        UserInfoServer userInfoServer = new UserInfoServer(context);
        user = userInfoServer.getUserInfo();
        loginInfo = new LoginInfoServer(context).getLoginInfo();
        otherDao = dbHelper.getOtherDao();
    }

    /**
     * 提交目标
     */
    public void commitAim(MyJsonHttpResponseHandler callback) throws SQLException {
        if (loginInfo != null) {
            CommitAim param = new CommitAim();
            param.setUsername(loginInfo.getAccount());

            boolean isCommit = false;
            final Other otherSleep = getSleepAim();
            if (otherSleep != null && !otherSleep.getSync())
            {
            	isCommit=true;
            	param.setSleepAim(otherSleep.getValue());
            }
            final Other otherSport = getSportAim();
            if (otherSport != null && !otherSport.getSync())
            {
            	isCommit=true;
            	param.setSportAim(otherSport.getValue());
            }	
            if (isCommit)
			{
               Api1.commitAim(context, param,
                    callback == null ? new MyJsonHttpResponseHandler(context) {
                        @Override
                        public void onSuccess(Context context, String str) {
                            // TODO 提交完设置后需要处理的业务
                        	
                        	 if (otherSleep != null && !otherSleep.getSync())
                             {
                             	otherSleep.setSync(true);
				            	try
								{
									otherDao.createOrUpdate(otherSleep);
								} catch (SQLException e)
								{
									e.printStackTrace();
								}
                             }
                             if (otherSport != null && !otherSport.getSync())
                             {
                            	 otherSport.setSync(true);
 				            	try
 								{
 									otherDao.createOrUpdate(otherSport);
 								} catch (SQLException e)
 								{
 									e.printStackTrace();
 								}
                             }	
                           LogUtil.i( "提交目标成功");
                        }
                    } : callback);
			}
        }
    }

    /**
     * 下载目标
     */
    public void loadSet(MyJsonHttpResponseHandler callback) throws SQLException {
        if (loginInfo != null) {
            Common param = new Common();
            param.setUsername(loginInfo.getAccount());
            Api1.loadAim(context, param, callback == null ? new MyJsonHttpResponseHandler(context) {
                @Override
                public void onSuccess(Context context, String str) {
                    if (StringUtil.isBlank(str))
                        return;
                    AimSetdata entity = JSON.parseObject(str, AimSetdata.class);

                    try {
                        OtherServer otherServer = new OtherServer(context);
                        User userInfo = new UserInfoServer(context).getUserInfo();
                        if (userInfo==null)return;

                        Other other = otherServer.getOther(userInfo, Other.Type.sleepAim);
                        if (other==null||other.getSync()){
                            String sleepAim = entity.getSleepAim();
                            storeAim(sleepAim,null,true);
                        }
                        Other other1 = otherServer.getOther(userInfo, Other.Type.sportAim);
                        if (other1==null||other1.getSync()){
                            String sportAim = "" + entity.getSportAim();
                            storeAim(null,sportAim,true);
                        }

                        //ToastUtil.shortShow(context, "下载目标成功");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } : callback);
        }
    }

    public void storeAim(String sleepAim,String sportAim,boolean sync) throws SQLException {
        Other otherSleep = new Other();
        otherSleep.setUser(user);
        otherSleep.setType(Other.Type.sleepAim);
        storeData(sleepAim, otherSleep,sync);
        
        Other otherSport = new Other();
        otherSport.setUser(user);
        otherSport.setType(Other.Type.sportAim);
        storeData(sportAim, otherSport,sync);
        SyncServer.getInstance(context).syncBleSportAim(user);//TODO 问题：多次发运动目标指令给手环
        commitAim(null);
    }

	private void storeData(String data, Other other, boolean sync)
			throws SQLException
	{
		if (other == null || data == null)
			return;
		List<Other> entities = otherDao.queryForMatching(other);
		if (entities != null && entities.size() > 0)
		{
			Other oldOther = entities.get(0);
			oldOther.setValue(data);
			oldOther.setSync(sync);
			otherDao.createOrUpdate(oldOther);
		} else
		{
			other.setValue(data);
			other.setSync(sync);
			otherDao.createOrUpdate(other);
		}
	}

    public Other getSleepAim() {
        Other other = new Other();
        other.setUser(user);
        other.setType(Other.Type.sleepAim);
        List<Other> entities = null;
        try {
            entities = otherDao.queryForMatching(other);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }

    public Other getSportAim() {
        Other other = new Other();
        other.setUser(user);
        other.setType(Other.Type.sportAim);
        List<Other> entities = null;
        try {
            entities = otherDao.queryForMatching(other);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }


}
