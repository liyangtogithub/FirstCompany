package com.desay.iwan2.common.server;

import android.content.Context;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 方奕峰 on 14-7-30.
 */
public class BtDevServer {

    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<BtDev, Integer> btDevDao;

    public BtDevServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        btDevDao = dbHelper.getBtDevDao();
    }

    public void createOrUpdate(User user, String mac, String coreVersion, String nordicVersion) throws SQLException {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return;

        BtDev btDev = getBtDev(user);
        if (btDev == null) {
            btDev = new BtDev();
            btDev.setUser(user);
        }
        btDev.setSync(false);
        btDev.setMac(mac);
        btDev.setCoreVersion(coreVersion);
        btDev.setNordicVersion(nordicVersion);
        btDevDao.createOrUpdate(btDev);
    }
    
    public void storeCoreVersion(User user,  String coreVersion)  {
    	try
		{
    		if (user == null)
        		user = new UserInfoServer(context).getUserInfo();
        	if (user == null)
        		return;
        	
        	BtDev btDev = getBtDev(user);
        	if (btDev == null) {
        		btDev = new BtDev();
        		btDev.setUser(user);
        	}
        	btDev.setSync(false);
        	btDev.setCoreVersion(coreVersion);
        	btDevDao.createOrUpdate(btDev);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    public void storeNordicVersion(User user,  String nordicVersion)  {
    	try
    	{
    		if (user == null)
    			user = new UserInfoServer(context).getUserInfo();
    		if (user == null)
    			return;
    		
    		BtDev btDev = getBtDev(user);
    		if (btDev == null) {
    			btDev = new BtDev();
    			btDev.setUser(user);
    		}
    		btDev.setSync(false);
    		btDev.setNordicVersion(nordicVersion);
    		btDevDao.createOrUpdate(btDev);
    	} catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    public BtDev getBtDev(User user) throws SQLException {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return null;

        BtDev param = new BtDev();
        param.setUser(user);
        List<BtDev> btDevList = btDevDao.queryForMatchingArgs(param);
        if (btDevList.size() > 0) {
            return btDevList.get(0);
        }
        return null;
    }
    
    public String getNordicVertion(User user)  {
    
    	String nordicVersion = null;
    	try
		{
			if (user == null)
				user = new UserInfoServer(context).getUserInfo();
			if (user == null)
				return null;
			BtDev btDev = getBtDev(user);
			if (btDev != null)
			{
				nordicVersion = btDev.getNordicVersion();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return nordicVersion;
    }
    
    public String getBandVertion(User user)  {
    	String bandVersion = null;
    	try
    	{
			if (user == null)
				user = new UserInfoServer(context).getUserInfo();
			if (user == null)
				return null;
			BtDev btDev = getBtDev(user);
			if (btDev != null)
			{
				bandVersion = btDev.getCoreVersion();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return bandVersion;
    }
    
    
}
