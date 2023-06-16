package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSON;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.LoadVersion;
import com.desay.iwan2.common.api.http.entity.response.Devicedata;
import com.desay.iwan2.common.api.http.entity.response.GoldRuledata;
import com.desay.iwan2.common.api.http.entity.response.Upgradedata;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.GainEvent;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.module.MainFragment;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.util.AppUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by 方奕峰 on 14-7-14.
 */
public class VersionServer {
    private Context context;
    private DatabaseHelper dbHelper;
    public Dao<GainEvent, Integer> gainEventDao;
    public static final String BAND_VERTION = "band_vertion";
    public static final String NORDIC_VERTION = "nordic_vertion";
    public static final String APP_VERTION = "app_vertion";
    public static final String VERTION_ADDRESS = "vertion_address";
    public static final String UPGRADE_MD5_OR_EXPLAIN = "upgrade_md5_or_explains";
    public static final String UPGRADE_BAND_EXPLAIN = "upgrade_band_explains";
    String vertionAddress = null;
    String md5_or_explains = null;
    String bandExplains = null;

    public VersionServer(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        gainEventDao = dbHelper.getGainEventDao();
    }

    public void save(GainEvent gainEvent) throws SQLException {
        gainEventDao.createOrUpdate(gainEvent);
    }

    public void network2Local(MyJsonHttpResponseHandler callback) throws SQLException {
        final OtherServer otherServer = new OtherServer(context);
        Other gainEventVer = otherServer.getOther(null, Other.Type.gainEventVer);
        int ver = 0;
        if (gainEventVer != null)
            ver = gainEventVer.getValue() == null ? 0 : Integer.valueOf(gainEventVer.getValue());

        LoadVersion param = new LoadVersion();
        param.setGtype(context.getString(R.string.customer_type_code));
        param.setGoldVer(ver);
//        param.setGoldVer(1);
        Api1.loadVersion(context, param,
                callback == null ? new MyJsonHttpResponseHandler(context) {
                    @Override
                    public void onSuccess(final Context context, String str) {
                        if (StringUtil.isBlank(str))
                            return;

                        final Upgradedata entity = JSON.parseObject(str, Upgradedata.class);
                        try {
                            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    List<Devicedata> entityList = entity.getDeviceVer();
                                    for (int i = entityList.size()-1; i >= 0; i--) {
                                        Devicedata deviceData = entityList.get(i);
                                        String gType = deviceData.getGtype();
                                        int vertion = deviceData.getVer();
                                        vertionAddress = deviceData.getAppurl();
                                        if (vertionAddress == null) continue;
                                        LogUtil.i("010203 vertion==" + vertion);
                                        if (context.getString(R.string.core_type_code).equals(gType)) {
                                        	md5_or_explains = deviceData.getMd5();
                                        	bandExplains = deviceData.getExplains()==null?context.getString(R.string.upgrade_app_info):deviceData.getExplains();
                                            Other netEfm32Ver = new OtherServer(context).getOther(null, Other.Type.netEf32Ver);
                                            String netCoreVerString = netEfm32Ver == null ? "0" : netEfm32Ver.getValue();
                                            int netCoreVertion = 0;
                                            if (netCoreVerString != null)
                                                netCoreVertion = Integer.parseInt(netCoreVerString);
                                            LogUtil.i("01 localCoreVertion==" + netCoreVertion);
                                            LogUtil.i("01  vertionAddress==" + vertionAddress);
                                            LogUtil.i("01  md5==" + md5_or_explains);                      
                                            if (vertion > netCoreVertion) {
                                                goUpgrade(MainFragment.UPGRADE_BAND, BAND_VERTION, vertion);
                                                break;
                                            }
                                        } else if (context.getString(R.string.bluetoooth_type_code).equals(gType)) {
                                            md5_or_explains = deviceData.getMd5();
                                            bandExplains = deviceData.getExplains()==null?context.getString(R.string.upgrade_app_info):deviceData.getExplains();
                                            LogUtil.i("02 vertion==" + vertion);
//                                            String netNordicVertion = netVertionServer.getNetNodicVer();
                                            Other netNordicVer = new OtherServer(context).getOther(null, Other.Type.netNodicVer);
                                            String netNordicVertion = netNordicVer == null ? "0" : netNordicVer.getValue();
                                            int netNodicVertion = 0;
                                            if (netNordicVertion != null)
                                                netNodicVertion = Integer.parseInt(netNordicVertion);
                                            LogUtil.i("02  localNodicVertion==" + netNodicVertion);
                                            LogUtil.i("02 vertionAddress==" + vertionAddress);
                                            LogUtil.i("02 md5==" + md5_or_explains);
                                            if (vertion > netNodicVertion) {
                                                goUpgrade(MainFragment.UPGRADE_NORDIC, NORDIC_VERTION, vertion);
                                                break;
                                            }

                                        } else if (context.getString(R.string.customer_type_code).equals(gType)) {
                                            md5_or_explains = deviceData.getExplains();
                                            String typeCode = context.getString(R.string.customer_type_code);
                                            LogUtil.i(typeCode + " app explains=" + md5_or_explains +
                                                    "\nvertion=" + vertion +
                                                    "\nAppUtil.getVerCode(context)=" + AppUtil.getVerCode(context) +
                                                    "\nvertionAddress=" + vertionAddress);
                                            if (vertion > AppUtil.getVerCode(context)) {
                                                goUpgrade(MainFragment.UPGRADE_APP, APP_VERTION, vertion);
                                                break;
                                            }
                                        }

                                    }

                                    otherServer.createOrUpdate(null, Other.Type.gainEventVer, "" + entity.getGoldVer());
                                    // 金币事件
                                    List<GoldRuledata> goldRuleList = entity.getGoldRule();
                                    if (goldRuleList != null && goldRuleList.size() > 0) {
                                        gainEventDao.delete(gainEventDao.deleteBuilder().prepare());
                                        for (GoldRuledata goldRuledata : goldRuleList) {
                                            GainEvent gainEvent = new GainEvent();
                                            gainEvent.setCode(goldRuledata.getGevent());
                                            gainEvent.setCnMsg(goldRuledata.getCHmsg());
                                            gainEvent.setEnMsg(goldRuledata.getENmsg());
                                            gainEventDao.create(gainEvent);
                                        }
                                    }
                                    return null;
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } : callback);
    }

    private void goUpgrade(String action, String vertionName, int vertion) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(vertionName, vertion);
        intent.putExtra(VERTION_ADDRESS, vertionAddress);
        intent.putExtra(UPGRADE_MD5_OR_EXPLAIN, md5_or_explains);
        intent.putExtra(UPGRADE_BAND_EXPLAIN, bandExplains);
        context.sendBroadcast(intent);
        LogUtil.i("goUpgrade   action==" + action);
    }
}
