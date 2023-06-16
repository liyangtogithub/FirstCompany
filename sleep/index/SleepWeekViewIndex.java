package com.desay.iwan2.module.sleep.index;

import android.app.Activity;
import android.view.View;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sleep.widget.SleepChartView2;

import java.text.SimpleDateFormat;

/**
 * @author 方奕峰
 */
public class SleepWeekViewIndex {

    public View rootView;


    public SleepChartView2 chartView;

    public ChartSeries series;

    public SleepWeekViewIndex(Activity act, View rootView) {
        this.rootView = rootView;

        chartView = (SleepChartView2) rootView.findViewById(R.id.chartView2);
        chartView.init();
        series = chartView.getSeries().get(0);

        chartView.getAreas().get(0).getDefaultXAxis().setLabelFormat(new SimpleDateFormat("E"));
    }
}
