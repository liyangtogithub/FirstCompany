package com.desay.iwan2.common.contant;

import android.content.Context;
import android.os.Environment;
import com.desay.fitband.R;
import dolphin.tools.util.AppUtil;

public class PathContant {

    private PathContant() {
    }

    public static final String IMG = "img";
    // public static final String PORTRAIT = "portrait";
    public static final String APK = "apk";

    public static String getApkDir(Context context) {
        // if (Environment.MEDIA_MOUNTED.equals(Environment
        // .getExternalStorageState())) {
        return getDir(context) + "/" + APK;
        // } else {
        // return getDir(context) + "/cache/" + APK;
        // }
    }

    public static String getDir(Context context) {
        // if (Environment.MEDIA_MOUNTED.equals(Environment
        // .getExternalStorageState())) {
        String appName = AppUtil.getAppName(context);
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + (appName == null ? context.getResources().getString(R.string.app_name) : appName);
        // } else {
        // return StorageUtils.getOwnCacheDirectory(context,
        // context.getString(R.string.app_name)).getAbsolutePath();
        // }
    }
}
