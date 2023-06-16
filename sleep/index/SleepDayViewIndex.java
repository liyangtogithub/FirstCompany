package com.desay.iwan2.module.sleep.index;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.artfulbits.aiCharts.Base.ChartAxis;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sleep.widget.SleepChartView1;

/**
 * @author 方奕峰
 */
public class SleepDayViewIndex {

    public View rootView;

    public TextView textView_quality, textView_deepRate,
            textView_startTime, textView_endTime,
            textView_shallow, textView_deep, textView_dream, textView_wakeup;

    public SleepChartView1 chartView;

    public RelativeLayout sleep_statistic_empty_layout;

//    public ChartSeries shallowSeries, deepSeries, dreamSeries, wakeupSeries, heartrateSeries;

    public SleepDayViewIndex(Activity act, ViewGroup viewGroup) {
        rootView = act.getLayoutInflater().inflate(R.layout.sleep_day_view, viewGroup);

        textView_quality = (TextView) rootView.findViewById(R.id.textView_quality);
        textView_deepRate = (TextView) rootView.findViewById(R.id.textView_deepRate);
        textView_startTime = (TextView) rootView.findViewById(R.id.textView_startTime);
        textView_endTime = (TextView) rootView.findViewById(R.id.textView_endTime);
        textView_shallow = (TextView) rootView.findViewById(R.id.textView_shallow);
        textView_deep = (TextView) rootView.findViewById(R.id.textView_deep);
        textView_dream = (TextView) rootView.findViewById(R.id.textView_dream);
        textView_wakeup = (TextView) rootView.findViewById(R.id.textView_wakeup);


        chartView = (SleepChartView1) rootView.findViewById(R.id.chartView1);
        chartView.init();
//        shallowSeries = chartView.getSeries().get("shallow");
//        deepSeries = chartView.getSeries().get("deep");
//        dreamSeries = chartView.getSeries().get("dream");
//        wakeupSeries = chartView.getSeries().get("wakeup");
//        heartrateSeries = chartView.getSeries().get("heartrate");

        ChartAxis heartrateAxis = new ChartAxis(ChartAxis.Position.Right);
        heartrateAxis.setVisible(false);
        heartrateAxis.setGridVisible(false);
        chartView.getAreas().get(0).getAxes().add(heartrateAxis);
        chartView.heartrateSeries.setYAxis(heartrateAxis);

        sleep_statistic_empty_layout = (RelativeLayout)rootView.
                findViewById(R.id.sleep_statistic_empty_layout);
    }
}
