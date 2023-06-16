package com.desay.iwan2.common.app.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.widget.MatteLayer;
import dolphin.tools.common.widget.NumberProgressBar;

/**
 * @author 方奕峰
 */
public class TemplateActivity extends BaseActivity {

    public Bundle bundle;

    //    private View layout_matte;
//    private View layout_circle;
//    private View layout_numProgressBar;
//    private View imageView_refresh;
//    private NumberProgressBar progressBar;
    private MatteLayer matteLayer;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);
        setContentView(R.layout.main_content_view);

//        initMatteLayer();
        matteLayer = (MatteLayer) findViewById(R.id.matteLayer);

        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
        } else {
            bundle = savedInstanceState;
        }
    }

//    private void initMatteLayer() {
//        layout_matte = findViewById(R.id.layout_matte);
//        layout_circle = findViewById(R.id.layout_circle);
//        imageView_refresh = findViewById(R.id.imageView_refresh);
//
//        layout_numProgressBar = findViewById(R.id.layout_numProgressBar);
//        progressBar = (NumberProgressBar) findViewById(R.id.numberbar);
//    }

    //    public void showMatteLayer(boolean isShow) {
//        layout_circle.setVisibility(View.VISIBLE);
//        layout_numProgressBar.setVisibility(View.GONE);
//        if (isShow) {
//            layout_matte.setVisibility(View.VISIBLE);
//            Animation animation = AnimationUtils.loadAnimation(this, R.anim.matta_0);
//            animation.setInterpolator(new LinearInterpolator());
//            imageView_refresh.startAnimation(animation);
//        } else {
//            layout_matte.setVisibility(View.GONE);
//            imageView_refresh.clearAnimation();
//        }
//    }
//
//    public void showProgressBarMatteLayer(boolean isShow, int progress) {
//        layout_circle.setVisibility(View.GONE);
//        layout_numProgressBar.setVisibility(View.VISIBLE);
//        if (isShow) {
//            layout_matte.setVisibility(View.VISIBLE);
//            progressBar.setProgress(progress);
//        } else {
//            layout_matte.setVisibility(View.GONE);
//            progressBar.setProgress(0);
//        }
//    }
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

    public void back() {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_BACK);
        dispatchKeyEvent(event);
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
        dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (matteLayer.isShown()) {
                    matteLayer.showMatteLayer(false);
                    matteLayer.showProgressBarMatteLayer(false, 0);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}