package com.desay.iwan2.common.app.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.widget.MatteLayer;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import dolphin.tools.common.os.MyUncaughtHandler;

/**
 * @author 方奕峰
 */
public class BaseSlidingActivity extends SlidingFragmentActivity {

    private ActionBar bar;
    private SlidingMenu sm;

    private TextView textView_title;
    private View progressBar;
    // private RelativeLayout layout_titlebarRight;
    private MatteLayer matteLayer;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        if (MyUncaughtHandler.uncaughtHandlerToggle)
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtHandler());
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);

        try {
            onCreate1(savedInstanceState);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void onCreate1(Bundle arg0) throws Throwable {
        // initActionBar();
        initSlidingMenu();

        setContentView(R.layout.main_content_view);
        initCustomActionBar();

//        initMatteLayer();
        matteLayer = (MatteLayer) findViewById(R.id.matteLayer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMatteLayer(boolean isShow) {
        matteLayer.showMatteLayer(isShow);
    }

    public void showMatteLayer(boolean isShow, String title) {
        matteLayer.showMatteLayer(isShow, title);
    }

    public int showProgressBarMatteLayer(boolean isShow, int progress) {
        return matteLayer.showProgressBarMatteLayer(isShow, progress);
    }

    public int showProgressBarMatteLayer(boolean isShow, int progress, String title) {
        return matteLayer.showProgressBarMatteLayer(isShow, progress, title);
    }

    private void initActionBar() {
        bar = getSupportActionBar();
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        // bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(0x17, 0x9e,
        // 0xfc)));
    }

    private void initSlidingMenu() {
        sm = getSlidingMenu();
//		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.LEFT);
    }

    private void initCustomActionBar() {
        textView_title = (TextView) findViewById(R.id.textView_title);
        progressBar = findViewById(R.id.progressBar);
        // layout_titlebarRight = (RelativeLayout) findViewById(R.id.layout_titlebarRight);
    }

    private void initCustomTitleView() {
        View v = LayoutInflater.from(this).inflate(R.layout.title_template,
                null);
        textView_title = (TextView) v.findViewById(R.id.textView_title);
        progressBar = v.findViewById(R.id.progressBar);

        ActionBar.LayoutParams aParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        bar.setCustomView(v, aParams);
        bar.setDisplayShowCustomEnabled(true);
    }

    public void showProgressBar(boolean b) {
        progressBar.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    public void setCustomTitle(final String title) {
        // bar.setTitle(" ");
        // initCustomTitleView();
        textView_title.setText(title);
    }

//    public void setCustomActionItem(View v) {
//        LayoutParams tParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.MATCH_PARENT);
//        layout_titlebarRight.addView(v, tParams);
//    }
}
