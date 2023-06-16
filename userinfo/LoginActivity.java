package com.desay.iwan2.module.userinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.server.TencentServer;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

public class LoginActivity extends TemplateActivity {

    LoginFragment loginFragment = null;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
        loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_content, loginFragment).commit();

        LogUtil.i("QQ测试7,flag = "+getIntent().getIntExtra("flag",0));
        if (getIntent().getIntExtra("flag",0) == 1) {
            TencentServer tencentServer = new TencentServer(this);
            if (tencentServer.getQqInfo() != null) {
                ToastUtil.shortShow(this, "登录后即可完成绑定QQ健康");
                LogUtil.i("QQ测试10");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (loginFragment.status != CommonData.WINDOW_LOGIN) {
                loginFragment.changeWindowsUI(CommonData.WINDOW_LOGIN);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void gotoActivity(Context packageContext) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        packageContext.startActivity(intent);
    }

    public static void gotoActivity(Context packageContext, int flag) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        intent.putExtra("flag", flag);
        packageContext.startActivity(intent);
    }


//    @Override
//    public void onResume1() {
//
//    }

//    @Override
//    protected void onPause1() throws Throwable {
//        Bundle bundle1 = getIntent().getExtras();
//        if (bundle1 != null) {
//            bundle1.remove("flag");
//        }
//    }
}
