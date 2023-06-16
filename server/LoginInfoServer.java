package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.SharedPreferences;
import com.desay.iwan2.util.SharePreferencesUtil;
import dolphin.tools.util.StringUtil;

/**
 * @author 方奕峰
 */
public class LoginInfoServer {

    private Context context;

    public LoginInfoServer(Context context) {
        this.context = context;
    }

    public void saveLoginInfo(String account, String pwd) {
        SharedPreferences preferences = SharePreferencesUtil.getSharedPreferences(context);
        preferences.edit().putString(LoginInfo.KEY_ACCOUNT, account)
                .putString(LoginInfo.KEY_PWD, pwd).commit();
    }

    public boolean isAutoLogin() {
        return SharePreferencesUtil.getSharedPreferences(context).getBoolean(LoginInfo.KEY_ISAUTO, false);
    }

    public void setIsAutoLogin(boolean isAuto) {
        SharedPreferences preferences = SharePreferencesUtil.getSharedPreferences(context);
        preferences.edit().putBoolean(LoginInfo.KEY_ISAUTO, isAuto).commit();

//        if (!isAuto)
//            new TencentServer(context).save(null);
    }

    public LoginInfo getLoginInfo() {
        SharedPreferences preferences = SharePreferencesUtil.getSharedPreferences(context);

        LoginInfo entity = null;
        String account = preferences.getString(LoginInfo.KEY_ACCOUNT, null);
        if (!StringUtil.isBlank(account)) {
            entity = new LoginInfo();
            entity.setAccount(account);
            entity.setPwd(preferences.getString(LoginInfo.KEY_PWD, null));
        }

        return entity;
    }

    public static class LoginInfo {
        public static final String KEY_ISAUTO = "isAuto";
        public static final String KEY_ACCOUNT = "account";
        public static final String KEY_PWD = "pwd";

        private String account;
        private String pwd;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

    }
}
