package com.desay.iwan2.common.server;

import android.content.Context;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class OtherServer {

    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<Other, Integer> otherDao;

    public OtherServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        otherDao = dbHelper.getOtherDao();
    }

    public Other createOrUpdate(User user, Other.Type type, String value) throws SQLException {

        return createOrUpdate(user, type, value, false);
    }

    public Other createOrUpdate(User user, Other.Type type, String value, boolean sync) throws SQLException {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return null;

        Other other = getOther(user, type);
        if (other == null) {
            other = new Other();
            other.setUser(user);
            other.setType(type);
        }
        other.setSync(sync);
        other.setValue(value);
        otherDao.createOrUpdate(other);

        return other;
    }

    public int delete(Other other) throws SQLException {
        return otherDao.delete(other);
    }

    public int update(Other other) throws SQLException {
        return otherDao.update(other);
    }

    public Other getOther(User user, Other.Type type) throws SQLException {
        if (user == null)
            user = new UserInfoServer(context).getUserInfo();
        if (user == null)
            return null;

        Other param = new Other();
        param.setUser(user);
        param.setType(type);
        List<Other> otherList = otherDao.queryForMatchingArgs(param);
        if (otherList.size() > 0) {
            return otherList.get(0);
        }
        return null;
    }
}
