package com.desay.iwan2.module.sport.index;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.artfulbits.aiCharts.ChartView;
import com.desay.fitband.R;

/**
 * @author 方奕峰
 */
public class SportSummaryViewIndex {

    public View rootView;

    public View btn_toLandscape;
    public Button btn_back;
    public TextView textView_aimRate, textView_startTime, textView_endTime, textView_title;
    public ChartView chartView;
    public RelativeLayout sport_empty_layout;

    public TextView textView_step, textView_stepAim,
            textView_aerobicStep, textView_aerobicStepAim,
            textView_calorie, textView_calorieAim,
            textView_distance, textView_distanceAim;

    public ProgressBar progressBar_step, progressBar_aerobicStep, progressBar_calorie, progressBar_distance;

    public SportSummaryViewIndex(Activity act, ViewGroup viewGroup) {
        rootView = act.getLayoutInflater().inflate(R.layout.sport_summary_view, viewGroup);

        btn_back = (Button) rootView.findViewById(R.id.btn_back);
        btn_toLandscape = rootView.findViewById(R.id.btn_toLandscape);
        textView_title = (TextView) rootView.findViewById(R.id.textView_title);
        textView_aimRate = (TextView) rootView.findViewById(R.id.textView_aimRate);
        textView_startTime = (TextView) rootView.findViewById(R.id.textView_startTime);
        textView_endTime = (TextView) rootView.findViewById(R.id.textView_endTime);

        chartView = (ChartView) rootView.findViewById(R.id.chartView);
        chartView.getChart().setSpacing(0);

        textView_step = (TextView) rootView.findViewById(R.id.textView_step);
        textView_stepAim = (TextView) rootView.findViewById(R.id.textView_stepAim);
        textView_aerobicStep = (TextView) rootView.findViewById(R.id.textView_aerobicStep);
        textView_aerobicStepAim = (TextView) rootView.findViewById(R.id.textView_aerobicStepAim);
        textView_calorie = (TextView) rootView.findViewById(R.id.textView_calorie);
        textView_calorieAim = (TextView) rootView.findViewById(R.id.textView_calorieAim);
        textView_distance = (TextView) rootView.findViewById(R.id.textView_distance);
        textView_distanceAim = (TextView) rootView.findViewById(R.id.textView_distanceAim);
        sport_empty_layout = (RelativeLayout) rootView.findViewById(R.id.sport_empty_layout);

        progressBar_step = (ProgressBar) rootView.findViewById(R.id.progressBar_step);
        progressBar_aerobicStep = (ProgressBar) rootView.findViewById(R.id.progressBar_aerobicStep);
        progressBar_calorie = (ProgressBar) rootView.findViewById(R.id.progressBar_calorie);
        progressBar_distance = (ProgressBar) rootView.findViewById(R.id.progressBar_distance);
    }
}
