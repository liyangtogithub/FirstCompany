package com.desay.iwan2.module.start;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.BaseActivity;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.*;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.band.BandManageActivity;
import com.desay.iwan2.module.data.DataManageActivity;
import com.desay.iwan2.module.userinfo.InfoActivity;
import com.desay.iwan2.module.userinfo.LoginActivity;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;

/**
 * @author 方奕峰
 */
public class StartActivity extends BaseActivity {
    Context context;
    private LoginInfoServer loginInfoServer;
    private UserInfoServer userInfoServer;
    private OtherServer otherServer;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
        this.context = StartActivity.this;

        loginInfoServer = new LoginInfoServer(this);
        userInfoServer = new UserInfoServer(context);
        otherServer = new OtherServer(context);

        handle();
        PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, "Wjh9DGD2xCG2nKDbho6oYfHB");//TODO 百度推送
    }

    private void handle() throws Throwable {
        // MyService.start(this);
        Intent intent = getIntent();

        int flag = intent.getIntExtra("flag", 0);

        String customType = getString(R.string.customer_type_code);
        if ("10".equals(customType) || "11".equals(customType)) {
            LogUtil.setToggle(0);
        } else {
            LogUtil.setToggle(-1);
        }
        BleManager.updateOriginalBtState(this);

        try {
            new VersionEventServer(this).handle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        User user = userInfoServer.getUserInfo();

        //QQ
//        String openid = intent.getStringExtra("openid");
//        String accessToken = intent.getStringExtra("accesstoken");
//        String accessTokenExpireTime = intent.getStringExtra("accesstokenexpiretime");
//        LogUtil.i("a1 openid = " + openid + " ; accessToken = " + accessToken + " ; accessTokenExpireTime = " + accessTokenExpireTime);
//        if (!StringUtil.isBlank(openid) && !StringUtil.isBlank(accessToken) && !StringUtil.isBlank(accessTokenExpireTime)) {
//            DataManageActivity.openid1 = openid;
//            DataManageActivity.accessToken1 = accessToken;
//            DataManageActivity.accessTokenExpireTime1 = accessTokenExpireTime;
//            DataManageActivity.qqBind(this, openid, accessToken, accessTokenExpireTime);
//            LogUtil.i("QQ拉起测试 1");
//        }else {
            if (loginInfoServer.isAutoLogin()) {
                if (user == null || user.getIsEmpty() == null || user.getIsEmpty()) {
                    InfoActivity.gotoActivity(this);
                } else {
                    MacServer macServer = new MacServer(context);
                    Other other = otherServer.getOther(user, Other.Type.isFirst);
                    String isfirst = null;
                    if (other != null)
                        isfirst = other.getValue();
                    if (flag == 1) {
                        MainActivity.gotoActivity(this, true);
                    } else {
                        MainActivity.gotoActivity(this);
                    }
                    if (other == null || isfirst == null || "".equals(isfirst)) {
                        if (macServer == null || StringUtil.isBlank(macServer.getMac())) {
                            BandManageActivity.goToActivity(this, BandManageActivity.FLAG_OPEN_SERVICE);
                        }
                    }
                }
            } else {
                LoginActivity.gotoActivity(this);
            }
//        }
        finish();
    }

    public static final int CASE_QQ = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.i("QQ拉起测试 4");
        switch (requestCode) {
            case CASE_QQ:
                if (resultCode==1) {
                    if (loginInfoServer.isAutoLogin()) {
                        try {
                            User user = userInfoServer.getUserInfo();
                            if (user == null || user.getIsEmpty() == null || user.getIsEmpty()) {
                                InfoActivity.gotoActivity(this);
                            } else {
                                MacServer macServer = new MacServer(context);
                                Other other = otherServer.getOther(user, Other.Type.isFirst);
                                String isfirst = null;
                                if (other != null)
                                    isfirst = other.getValue();
                                MainActivity.gotoActivity(this);
                                if (other == null || isfirst == null || "".equals(isfirst)) {
                                    if (macServer == null || StringUtil.isBlank(macServer.getMac())) {
                                        BandManageActivity.goToActivity(this, BandManageActivity.FLAG_OPEN_SERVICE);
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LoginActivity.gotoActivity(this, 1);
                    }
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void gotoActivity(Context packageContext) {
        gotoActivity(packageContext, 0);
    }

    public static void gotoActivity(Context packageContext, int flag) {
        Intent intent = new Intent(packageContext, StartActivity.class);
        intent.putExtra("flag", flag);
        packageContext.startActivity(intent);
    }
}
