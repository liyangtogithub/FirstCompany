package com.desay.iwan2.module.sleep;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.module.sleep.fragment.SleepStatisticsFragment;
import com.desay.iwan2.module.sleep.fragment.SleepSummaryFragment;
import dolphin.tools.util.LogUtil;

import java.util.Date;

/**
 * @author 方奕峰
 */
public class SleepActivity extends TemplateActivity {

    public static final String KEY = "key1";

    private Fragment fragment;
    private Date date;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
        date = (Date) bundle.getSerializable(KEY);
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            // 加入横屏要处理的代码
//            fragment = new SleepStatisticsFragment();
////            ((SleepStatisticsFragment) fragment).init(date);
//        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            // 加入竖屏要处理的代码
//            fragment = new SleepSummaryFragment();
////            ((SleepSummaryFragment) fragment).init(date);
////            back();
//        }
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.layout_content, fragment).commit();
        showFragment();
    }

    public void setDate(Date date) {
        this.date = date;
        bundle.putSerializable(KEY, date);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        LogUtil.i("屏幕状态=" + newConfig.orientation);
////        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////        transaction.remove(fragment).commit();
////        transaction.addToBackStack(null);
//        Date date = (Date) bundle.getSerializable(KEY);
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            // 加入横屏要处理的代码
//            fragment = new SleepStatisticsFragment(date);
////            ((SleepStatisticsFragment) fragment).init(date);
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            // 加入竖屏要处理的代码
//            fragment = new SleepSummaryFragment(date);
////            ((SleepSummaryFragment) fragment).init(date);
////            back();
//        }
//        super.onConfigurationChanged(newConfig);
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY, date);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment instanceof SleepStatisticsFragment) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void gotoActivity(Context context, Date date) {
        Intent intent = new Intent(context, SleepActivity.class);
        intent.putExtra(KEY, date);
        context.startActivity(intent);
    }

    //    @Override
//    protected void onDestroy() {
////        removeFragment();
//        super.onDestroy();
//    }
    private void showFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager!=null) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // 加入横屏要处理的代码
                fragment = new SleepStatisticsFragment();
//            ((SleepStatisticsFragment) fragment).init(date);
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // 加入竖屏要处理的代码
                fragment = new SleepSummaryFragment();
//            ((SleepSummaryFragment) fragment).init(date);
//            back();
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.layout_content, fragment).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        LogUtil.i("orientation = " + newConfig.orientation);
        showFragment();
    }
}