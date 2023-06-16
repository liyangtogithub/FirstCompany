package com.desay.iwan2.common.app.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.baidu.mobstat.StatService;
import dolphin.tools.common.os.MyUncaughtHandler;

/**
 * @author 方奕峰
 */
public class BaseActivity extends FragmentActivity {

    @Override
    protected final void onCreate(Bundle arg0) {
        if (MyUncaughtHandler.uncaughtHandlerToggle)
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        getWindow().setFormat(PixelFormat.RGBA_8888);

        // getWindow().setSoftInputMode(
        // WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        super.onCreate(arg0);

        try {
            onCreate1(arg0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void onCreate1(Bundle arg0) throws Throwable {

    }

    @Override
    protected final void onDestroy() {
        try {
            onDestroy1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onDestroy();
    }

    protected void onDestroy1() throws Throwable {

    }

    @Override
    protected final void onPause() {
        StatService.onPause(this);
        try {
            onPause1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onPause();
    }

    protected void onPause1() throws Throwable {
    }

    @Override
    protected final void onResume() {
        StatService.onResume(this);
        try {
            onResume1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onResume();
    }

    protected void onResume1() throws Throwable {
    }

    @Override
    protected final void onStart() {
        try {
            onStart1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onStart();
    }

    protected void onStart1() throws Throwable {
        
    }

    @Override
    protected final void onStop() {
        try {
            onStop1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onStop();
    }

    protected void onStop1() throws Throwable{

    }

    @Override
    protected final void onRestart() {
        try {
            onRestart1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onRestart();
    }

    protected void onRestart1() throws Throwable{

    }
}
