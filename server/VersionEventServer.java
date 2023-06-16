package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.contant.SupportLanguageConstant;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.module.userinfo.WechatCityServer;
import com.desay.iwan2.util.SharePreferencesUtil;
import dolphin.tools.util.AppUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by 方奕峰 on 14-8-22.
 */
public class VersionEventServer {
    private Context context;

    private final String KEY_APP_VER = "KEY_APP_VER";

    public VersionEventServer(Context context) {
        this.context = context;
    }

    public void handle() throws Exception {
        int oldVer = getRegistAppVersion();
        int newVer = AppUtil.getVerCode(context);
        if (newVer > oldVer) {
            onUpgradeApp(oldVer, newVer);
            registAppVersion();
        }
    }

    private boolean registAppVersion() {
        return SharePreferencesUtil.getSharedPreferences(context)
                .edit().putInt(KEY_APP_VER, AppUtil.getVerCode(context))
                .commit();
    }

    private int getRegistAppVersion() {
        return SharePreferencesUtil.getSharedPreferences(context)
                .getInt(KEY_APP_VER, 0);
    }

    private void onUpgradeApp(int oldVer, int newVer) throws Exception {
        LogUtil.i("oldVer==" + oldVer);
        if (oldVer == 0) {
            new LoginInfoServer(context).setIsAutoLogin(false);
            MyService.stop(context);

            if (checkAppInstalled(context, "com.desay.iwan2")) {
                uninstallIwan2();
            }
        } else {
            if (oldVer < 8) {
                versionLess8Event();
            }
            if (oldVer < 17) {
                versionLess17Event();
            }
        }
    }

    public static boolean checkAppInstalled(Context context, String packageName) {
        if (StringUtil.isBlank(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void versionLess8Event() throws SQLException {
        SetServer setServer = new SetServer(context);
        if (setServer != null) {
            String sitString = setServer.getSedentarySet();
            LogUtil.i(" sitString==" + sitString);
            String sitStringItem[] = sitString.split(",");
            String sitStart = sitStringItem[2].substring(0, 2) + "00";
            String sitStop = sitStringItem[2].substring(2, 4) + "00";
            sitString = sitStringItem[0] + "," + sitStart + "," + sitStop + "," + sitStringItem[3];
            LogUtil.i(" sitString==" + sitString);
            setServer.storeSedentary(sitString, false);
        }
    }

    private void uninstallIwan2() {
        String appName = AppUtil.getAppName(context);
        ToastUtil.longShow(context, String.format(context.getString(R.string.tips_1), appName, appName));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:com.desay.iwan2"));
                context.startActivity(intent);
            }
        }, 2000);
    }

    private void versionLess17Event() throws SQLException, IOException {
        uninstallIwan2();

        UserInfoServer userInfoServer = new UserInfoServer(context);
        User user = userInfoServer.getUserInfo();
        if (user == null) return;
        WechatCityServer wechatCityServer = new WechatCityServer();

        String allData = null;
        WechatCityServer.Address address = null;
        for (Locale locale : SupportLanguageConstant.supportLocale) {
            try {
                allData = wechatCityServer.getAllData(context, locale);
            } catch (IOException e) {
                e.printStackTrace();
            }
            address = wechatCityServer.getAddressBySuffix(allData, user.getAddress());
            if (address == null) {
                allData = wechatCityServer.getAllData(context, Locale.ENGLISH);
                address = wechatCityServer.getAddressBySuffix(allData, user.getAddress());
                if (address == null) {
                    allData = wechatCityServer.getAllData(context, Locale.SIMPLIFIED_CHINESE);
                    address = wechatCityServer.getAddressBySuffix(allData, user.getAddress());
                }
            }
            if (address != null) {
                user.setAddress(address.code);
                user.setSync(false);
                userInfoServer.save(user);
                break;
            }
        }
    }
}
