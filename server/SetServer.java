package com.desay.iwan2.common.server;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitSetting;
import com.desay.iwan2.common.api.http.entity.request.Common;
import com.desay.iwan2.common.api.http.entity.response.ParamSetdata;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by 方奕峰 on 14-6-23.
 */
public class SetServer
{
	private Context context;
	public DatabaseHelper dbHelper;
	public Dao<Other, Integer> otherDao;
	User user = null;

	public SetServer(Context context) throws SQLException
	{
		this.context = context;
		dbHelper = DatabaseHelper.getDataBaseHelper(context);
		UserInfoServer userInfoServer = new UserInfoServer(context);
		user = userInfoServer.getUserInfo();
		otherDao = dbHelper.getOtherDao();
	}

	/**
	 * 提交设置
	 */
	public void commitSet(MyJsonHttpResponseHandler callback)
			throws SQLException
	{
		 LoginInfoServer loginInfoServer= new LoginInfoServer(context);
		 LoginInfoServer.LoginInfo loginInfo =loginInfoServer.getLoginInfo();
		if (loginInfo != null)
		{
			CommitSetting param = new CommitSetting();
			param.setUsername(loginInfo.getAccount());
			
            boolean isCommit = false;
            OtherServer otherServer = new OtherServer(context);
            final Other otherAlarm = otherServer.getOther(user, Other.Type.alarm);
            if (otherAlarm != null && !otherAlarm.getSync())
            {
            	isCommit=true;
            	param.setAlarm(otherAlarm.getValue());
            }
            final Other otherCaller = otherServer.getOther(user, Other.Type.caller);
            if (otherCaller != null && !otherCaller.getSync())
            {
            	isCommit=true;
            	param.setCaller(otherCaller.getValue());
            }
            final Other otherMusic = otherServer.getOther(user, Other.Type.music);
            if (otherMusic != null && !otherMusic.getSync())
            {
            	isCommit=true;
            	param.setMusic(otherMusic.getValue());
            }
            final Other otherSedentary = otherServer.getOther(user, Other.Type.sedentary);
            if (otherSedentary != null && !otherSedentary.getSync())
            {
            	isCommit=true;
            	param.setSedentary(otherSedentary.getValue());
            }
            final Other otherSlpTime = otherServer.getOther(user, Other.Type.SlpTime);
            if (otherSlpTime != null && !otherSlpTime.getSync())
            {
            	isCommit=true;
            	param.setSleep(otherSlpTime.getValue());
            }
			if (isCommit)
			{
				Api1.commitSetting(context, param,
						callback == null ? new MyJsonHttpResponseHandler(
								context)
						{
							@Override
							public void onSuccess(Context context, String str)
							{
								 otherUpdate(otherAlarm);
								 otherUpdate(otherCaller);
								 otherUpdate(otherMusic);
								 otherUpdate(otherSedentary);
								 otherUpdate(otherSlpTime);
	                             LogUtil.i( "提交设置成功");
							}
						} : callback);
			}
		}
	}
	

	private void otherUpdate(Other other)
	{
		 if (other != null && !other.getSync())
         {
			 other.setSync(true);
        	try
			{
				otherDao.createOrUpdate(other);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
         }
	}
	
	/**
	 * 下载设置
	 */
	public void loadSet(MyJsonHttpResponseHandler callback) throws SQLException
	{
		 LoginInfoServer loginInfoServer= new LoginInfoServer(context);
		 LoginInfoServer.LoginInfo loginInfo =loginInfoServer.getLoginInfo();
		if (loginInfo != null)
		{
			Common param = new Common();
			param.setUsername(loginInfo.getAccount());
			Api1.loadSetting(context, param,callback == null ? new MyJsonHttpResponseHandler(context)
					{
						@Override
						public void onSuccess(Context context, String str)
						{
							if (StringUtil.isBlank(str))
								return;
							ParamSetdata entity = JSON.parseObject(str,ParamSetdata.class);
							
							try {
                                OtherServer otherServer = new OtherServer(context);
                                User userInfo = new UserInfoServer(context).getUserInfo();
                                if (userInfo==null)return;

                                Other other = otherServer.getOther(userInfo, Other.Type.alarm);
                                if (other==null||other.getSync()){
                                    String alarm = entity.getAlarm();
                                    storeAlarm(alarm, true);
                                }
                                Other other1 = otherServer.getOther(userInfo, Other.Type.music);
                                if (other1==null||other1.getSync()){
                                    String music = entity.getMusic();
                                    storeMusic(music, true);
                                }
                                Other other2 = otherServer.getOther(userInfo, Other.Type.caller);
                                if (other2==null||other2.getSync()){
                                    String caller = entity.getCaller();
                                    storeCaller(caller, true);
                                }
                                Other other3 = otherServer.getOther(userInfo, Other.Type.sedentary);
                                if (other3==null||other3.getSync()){
                                    String sedentary = entity.getSedentary();
                                    storeSedentary(sedentary, true);
                                }
                                Other other4 = otherServer.getOther(userInfo, Other.Type.SlpTime);
                                if (other4==null||other4.getSync()){
                                	String SlpTime = entity.getSleep();
                                	storeSleep(SlpTime, true);
                                }

                                //  ToastUtil.shortShow(context, "下载设置成功");
                            } catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					} : callback);
		}
	}
	
	public void storeAlarm(String alarm,boolean sync) throws SQLException
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.alarm);
		storeData(alarm , other,sync);
	}
	
	public void storeMusic(String music,boolean sync) throws SQLException
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.music);
		storeData(music , other,sync);
	}
	public void storeCaller(String caller,boolean sync) throws SQLException
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.caller);
		storeData(caller , other,sync);
	}
	public void storeSedentary(String sedentary,boolean sync) throws SQLException
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.sedentary);
		storeData(sedentary , other,sync);
	}
	public void storeSleep(String SlpTime,boolean sync) throws SQLException
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.SlpTime);
		storeData(SlpTime , other,sync);
	}
	
	public void storeData(String data, Other other,boolean sync) throws SQLException
	{
		List<Other> entities = otherDao.queryForMatching(other);
		if (data == null || "".equals(data))
		{
			if (entities != null && entities.size() > 0)
			{
				otherDao.delete(other);
			}
		} else
		{
			if (entities != null && entities.size() > 0)
			{
				Other oldOther = entities.get(0);
				oldOther.setValue(data);
				oldOther.setSync(sync);
				otherDao.createOrUpdate(oldOther);
			}
			else {
				other.setValue(data);
				other.setSync(sync);
				otherDao.createOrUpdate(other);
			}
			commitSet(null);
		}
	}
	
	public String getAlarmSet() 
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.alarm);
		String alarmSet = getdata(other);
		if (alarmSet!=null)
		return alarmSet;
		return null;
	}
	public String getMusicSet() 
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.music);
		String musicSet = getdata(other);
		if (musicSet!=null)
			return musicSet;
		return null;
	}
	public String getCallerSet() 
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.caller);
		String callerSet = getdata(other);
		if (callerSet!=null)
			return callerSet;
		return null;
	}
	public String getSedentarySet() 
	{
		Other other = new Other();
		other.setUser(user);
		other.setType(Other.Type.sedentary);
		String sedentarySet = getdata(other);
		if (sedentarySet!=null)
			return sedentarySet;
		return null;
	}
	
	public String  getdata(Other other)
	{
		try
		{
			List<Other> entities = otherDao.queryForMatching(other);
			if (entities!=null && entities.size()>0)
			{
				Other Oldother = entities.get(0);
				return Oldother.getValue();
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
