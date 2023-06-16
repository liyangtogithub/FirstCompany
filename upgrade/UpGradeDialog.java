package com.desay.iwan2.module.upgrade;


import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.PathContant;
import com.desay.iwan2.common.server.VersionServer;
import com.desay.iwan2.module.MainFragment;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import dolphin.tools.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;

public class UpGradeDialog {
    public void showAlertDialog(final Context context, final int appVertion, final String vertionAddress,
    		String explains) {
        AlertDialog.Builder builder = new Builder(context);
        View layout = null;
        final AlertDialog ad = builder.create();
        if (!ad.isShowing()) {
            ad.show();
            LayoutInflater inflater = LayoutInflater.from(context);
            layout = inflater.inflate(R.layout.dialog_update_alert, null);
            ad.getWindow().setContentView(layout);
            ad.setCanceledOnTouchOutside(false);
            ad.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                        ad.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            Button btn_wait = (Button) layout.findViewById(R.id.btn_wait);
            Button btn_update = (Button) layout.findViewById(R.id.btn_update);
            TextView tv_alerttoast1 = (TextView) layout.findViewById(R.id.alerttoast1);
            if (explains!=null)
            tv_alerttoast1.setText(explains);
            OnClickListener clicklistener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad.dismiss();
                    try {
                        upgradeApp(context, appVertion, vertionAddress);//6 要删
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.shortShow(context, context.getString(R.string.upgrade_app_explorer));

                        Uri uri = Uri.parse(vertionAddress);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                }
            };
            btn_update.setOnClickListener(clicklistener);
            OnClickListener clicklistenerWait = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad.dismiss();
                }
            };
            btn_wait.setOnClickListener(clicklistenerWait);
        }
    }


    public void upgradeApp(Context context, int newVersionCode, String vertionAddress) throws Exception {
        String fileName = AppUtil.getAppName(context);
        if (newVersionCode > AppUtil.getVerCode(context)) {
            boolean hadNewApk = false;
            //检查SD安装包
            File apkDirFile = new File(PathContant.getApkDir(context));
            Collection<File> files = null;
            if (apkDirFile != null && apkDirFile.exists()) {
                files = FileUtils.listFiles(apkDirFile, null, false);
            }
            if (files != null && apkDirFile.exists()) {
                for (File f : files) {
                    hadNewApk = checkAndInstallApk(context, newVersionCode, f.getName(), fileName);
                    if (hadNewApk) return;
                }
            }

            if (Build.VERSION_CODES.GINGERBREAD <= Build.VERSION.SDK_INT) {
                //检查cache安装包
                DownloadManager.Query query = new DownloadManager.Query();
                Cursor cursor = DownloadUtil.getDownloadManager(context).query(query);
                if (cursor == null) {
                    LogUtil.i("no download record");
                } else {
                    while (cursor.moveToNext()) {
                        String localFileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        if (!StringUtils.isBlank(localFileName)) {
                            hadNewApk = checkAndInstallApk(context, newVersionCode, localFileName, fileName);
                            if (hadNewApk) return;
                        }
                    }
                }
            }

            //下载新安装包
            if (!hadNewApk) {
                String url = vertionAddress;/*"http://care.desay.com/apk_update/SleepAndHealth.apk"*/
                if (Build.VERSION_CODES.GINGERBREAD > Build.VERSION.SDK_INT) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                } else {
                    DownloadUtil.downloadByDownloadManager(context, url);
                }
            }
        }
    }

    private static boolean checkAndInstallApk(Context context, int versionCode, String f1, String f2) {
        if (f1.contains(f2)) {
            PackageInfo apkInfo = ApkUtil.getApkInfo(context, f1);
            if (apkInfo != null && versionCode <= apkInfo.versionCode) {
                File file = new File(f1);
                ApkUtil.installApk(context, file);
                return true;
            }
        }
        return false;
    }
}
