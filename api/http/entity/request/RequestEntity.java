package com.desay.iwan2.common.api.http.entity.request;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.server.BtDevServer;
import dolphin.tools.util.AppUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 方奕峰 on 14-6-17.
 */
public class RequestEntity {
    private Sysdata sysdata;
    private Object rqdata;

    public Sysdata getSysdata() {
        return sysdata;
    }

    public void setSysdata(Sysdata sysdata) {
        this.sysdata = sysdata;
    }

    public Object getRqdata() {
        return rqdata;
    }

    public void setRqdata(Object rqdata) {
        this.rqdata = rqdata;
    }

    public static class Sysdata {
        private String protocolType;
        private String appVer;
        private String deviceVer;
        private String blueVer;
        private String deviceId;
        private String mobileType;
        private String systemVer;
        private String systemLang;
        private String sendDate;

        public String getProtocolType() {
            return protocolType;
        }

        public void setProtocolType(String protocolType) {
            this.protocolType = protocolType;
        }

        public String getAppVer() {
            return appVer;
        }

        public void setAppVer(String appVer) {
            this.appVer = appVer;
        }

        public String getDeviceVer() {
            return deviceVer;
        }

        public void setDeviceVer(String deviceVer) {
            this.deviceVer = deviceVer;
        }

        public String getBlueVer() {
            return blueVer;
        }

        public void setBlueVer(String blueVer) {
            this.blueVer = blueVer;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getMobileType() {
            return mobileType;
        }

        public void setMobileType(String mobileType) {
            this.mobileType = mobileType;
        }

        public String getSystemVer() {
            return systemVer;
        }

        public void setSystemVer(String systemVer) {
            this.systemVer = systemVer;
        }

        public String getSystemLang() {
            return systemLang;
        }

        public void setSystemLang(String systemLang) {
            this.systemLang = systemLang;
        }

        public String getSendDate() {
            return sendDate;
        }

        public void setSendDate(String sendDate) {
            this.sendDate = sendDate;
        }
    }

    public static Sysdata generateSysData(Context context) {
        Sysdata sysdata = new Sysdata();
        sysdata.setProtocolType("1");
        sysdata.setAppVer(AppUtil.getVerName(context));
        BtDev btDev = null;
        try {
            btDev = new BtDevServer(context).getBtDev(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (btDev != null) {
            sysdata.setDeviceVer(btDev.getCoreVersion());
            sysdata.setBlueVer(btDev.getNordicVersion());
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        sysdata.setDeviceId(tm.getDeviceId());
        sysdata.setMobileType(android.os.Build.MODEL);
        sysdata.setSystemVer(android.os.Build.VERSION.RELEASE);
        Locale locale = context.getResources().getConfiguration().locale;
        String language = (Locale.CHINA.equals(locale) || Locale.CHINESE.equals(locale) ||
                Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale)) ?
                "zh" : "en";
        sysdata.setSystemLang(language);
        sysdata.setSendDate(SystemContant.timeFormat1.format(new Date()));
        return sysdata;
    }
}
