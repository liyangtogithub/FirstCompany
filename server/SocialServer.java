package com.desay.iwan2.common.server;

import android.R.integer;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitSocialShare;
import com.desay.iwan2.common.api.http.entity.request.LoadCoin;
import com.desay.iwan2.common.api.http.entity.response.GetGolddata;
import com.desay.iwan2.common.api.http.entity.response.GoldListdata;
import com.desay.iwan2.common.api.http.entity.response.Golddetaildata;
import com.desay.iwan2.common.api.http.entity.response.ShareGetdata;
import com.desay.iwan2.common.api.http.entity.response.ShareListdata;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Day;
import com.desay.iwan2.common.db.entity.Gain;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;


public class SocialServer {
    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<User, String> userDao;
    public Dao<Gain, Integer> gainDao;
    public Dao<Other, Integer> otherDao;
   // public Dao<Day, Integer> dayDao;

    public SocialServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        userDao = dbHelper.getUserDao();
        gainDao = dbHelper.getGainDao();
        otherDao = dbHelper.getOtherDao();
 //       dayDao = dbHelper.getDayDao();
    }

    public void commitShare(String gevent, String socialID, MyJsonHttpResponseHandler callback) {
        LoginInfoServer loginInfoServer = new LoginInfoServer(context);
        final LoginInfoServer.LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo == null) {
            return;
        }
        CommitSocialShare param = new CommitSocialShare();
        param.setUsername(loginInfo.getAccount());
        param.setGevent(gevent);
        param.setSocialID(socialID);
        Api1.commitSocialShare(context, param,
                callback == null ? new MyJsonHttpResponseHandler(context) {
                    @Override
                    public void onSuccess(final Context context, String str) {
                    	LogUtil.i("分享回调  str == "+str);
                        if (StringUtil.isBlank(str))
                            return;

                        final ShareListdata entity = JSON.parseObject(str, ShareListdata.class);
                        try {
                            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    User user = new UserInfoServer(context).getUserInfo();
                                    if (user == null)
                                        return null;
                                    Date date = SystemContant.timeFormat6.parse(entity.getGdate());
                                    
                                    //每天金币
                                    for (ShareGetdata detail : entity.getDetail()) {
                                        saveFromNetworkResponse(user, detail, date);
                                    }
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

    public void saveFromNetworkResponse(User user, ShareGetdata entity, Date date) throws Exception {
    	//当前拥有金币
    	OtherServer otherServer = new OtherServer(context);
    	Other other = otherServer.getOther(user, Other.Type.currentGain);
    	if (other!=null){
    		int currentGain =0;
    	    currentGain = Integer.parseInt("".equals(other.getValue()) ? "0":other.getValue());
    	    otherServer.createOrUpdate(user, Other.Type.currentGain, "" + (currentGain + entity.getGold()) );
    	}
        Day day = new DayServer(context).getDay(user, date);
		if (day != null){
				Gain gain = new Gain();
				gain.setDay(day);
				gain.setCount(entity.getGold());
				gain.setEventCode(entity.getGevent());
				gain.setTime(entity.getGtime());
				gain.setTypeCode(entity.getGtype());
				gainDao.create(gain);
		}
		context.sendBroadcast(new Intent(SyncServer.ACTION_SYNC_SUCCESS));
		LogUtil.i("发出更新UI广播");
    }
}
