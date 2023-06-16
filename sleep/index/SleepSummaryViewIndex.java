package com.desay.iwan2.module.sleep.index;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.artfulbits.aiCharts.ChartView;
import com.desay.fitband.R;
import com.desay.iwan2.module.sleep.adapter.SleepSummaryAdapter;

/**
 * @author 方奕峰
 */
public class SleepSummaryViewIndex {

    public View rootView;

    public View btn_toLandscape;
    public Button btn_back;
    public TextView textView_duration, textView_quality,textView_startTime,textView_endTime;
    public ListView listView;
    public SleepSummaryAdapter adapter;
    public ChartView chartView;
    public RelativeLayout sleep_empty_layout;
    
    // public CircleProgressDialog progressDialog;

    public SleepSummaryViewIndex(Activity act, ViewGroup viewGroup) {
        rootView = act.getLayoutInflater().inflate(R.layout.sleep_summary_view, viewGroup);

        btn_back = (Button) rootView.findViewById(R.id.btn_back);
        btn_toLandscape = rootView.findViewById(R.id.btn_toLandscape);
//        textView_title = (TextView) rootView.findViewById(R.id.textView_title);
        textView_duration = (TextView) rootView.findViewById(R.id.textView_duration);
        textView_quality = (TextView) rootView.findViewById(R.id.textView_quality);
        textView_startTime = (TextView) rootView.findViewById(R.id.textView_startTime);
        textView_endTime = (TextView) rootView.findViewById(R.id.textView_endTime);
        sleep_empty_layout = (RelativeLayout) rootView.findViewById(R.id.sleep_empty_layout);

        listView = (ListView) rootView.findViewById(R.id.listView);
        adapter = new SleepSummaryAdapter(act);
        listView.setAdapter(adapter);

        chartView = (ChartView) rootView.findViewById(R.id.chartView);
    }
}
