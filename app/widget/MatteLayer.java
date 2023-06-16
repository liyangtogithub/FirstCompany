package com.desay.iwan2.common.app.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.desay.fitband.R;
import dolphin.tools.common.widget.NumberProgressBar;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

/**
 * Created by 方奕峰 on 14-8-15.
 */
public class MatteLayer extends FrameLayout implements Handler.Callback {

    //    private View layout_matte;
    private View layout_circle;
    private View layout_numProgressBar;
    private View imageView_refresh;
    private NumberProgressBar progressBar;
    private TextView textView_progressBarTitle, textView_circleProgressBarTitle;
    private Handler handler;

    public MatteLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.matte_view, this);
        initMatteLayer();
        setVisibility(View.GONE);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        handler = new Handler(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initMatteLayer() {
//        layout_matte = findViewById(R.id.layout_matte);
        layout_circle = findViewById(R.id.layout_circle);
        imageView_refresh = findViewById(R.id.imageView_refresh);
        textView_circleProgressBarTitle = (TextView) findViewById(R.id.textView_circleProgressBarTitle);

        layout_numProgressBar = findViewById(R.id.layout_numProgressBar);
        progressBar = (NumberProgressBar) findViewById(R.id.numberbar);
        textView_progressBarTitle = (TextView) findViewById(R.id.textView_progressBarTitle);
    }

    public void showMatteLayer(final boolean isShow) {
        showMatteLayer(isShow, "正在处理");
    }

    public void showMatteLayer(final boolean isShow, String title) {
        this.setVisibility(isShow ? View.VISIBLE : View.GONE);
        layout_circle.setVisibility(View.VISIBLE);
        layout_numProgressBar.setVisibility(View.GONE);
        if (isShow) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.matta_0);
            animation.setInterpolator(new LinearInterpolator());
            imageView_refresh.startAnimation(animation);
            if (StringUtil.isBlank(title)) {
                textView_circleProgressBarTitle.setVisibility(GONE);
            } else {
                textView_circleProgressBarTitle.setVisibility(VISIBLE);
                textView_circleProgressBarTitle.setText(title);
            }
        } else {
            imageView_refresh.clearAnimation();
        }
    }

    public int showProgressBarMatteLayer(final boolean isShow, int progress) {
        return showProgressBarMatteLayer(isShow, progress, "正在处理");
    }

    public int showProgressBarMatteLayer(final boolean isShow, int progress, String title) {
        this.setVisibility(isShow ? View.VISIBLE : View.GONE);
        layout_circle.setVisibility(View.GONE);
        layout_numProgressBar.setVisibility(View.VISIBLE);

        handler.removeMessages(CASE_CHECK_PROGRESS);
        if (isShow) {
            if (StringUtil.isBlank(title)) {
                textView_progressBarTitle.setVisibility(GONE);
            } else {
                textView_progressBarTitle.setVisibility(VISIBLE);
                textView_progressBarTitle.setText(title);
            }
            progressBar.setProgress(true, progress);

            if (progress == 100) {
                Message msg = new Message();
                msg.what = CASE_CHECK_PROGRESS;
                handler.sendMessageDelayed(msg, 300);
            }
        } else {
            progressBar.setProgress(false, 0);
        }
        return progressBar.getProgress();
    }

    private final int CASE_CHECK_PROGRESS = 1;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CASE_CHECK_PROGRESS:
                if (progressBar.getProgress() < 100) {
                    Message msg1 = new Message();
                    msg1.what = CASE_CHECK_PROGRESS;
                    handler.sendMessageDelayed(msg1, 1000);
                } else {
                    showProgressBarMatteLayer(false, 0);
                }
                break;
        }
        return false;
    }

//    public boolean isShowing() {
//        return getVisibility() == VISIBLE;
//    }
}
