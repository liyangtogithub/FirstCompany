package com.desay.iwan2.module.sleep.index;

import android.app.Activity;
import android.view.View;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sleep.widget.SleepChartView3;
import com.desay.iwan2.module.sleep.widget.SleepChartView4;

/**
 * @author 方奕峰
 */
public class SleepYearViewIndex {

    public View rootView;


    public SleepChartView4 chartView;

    public ChartSeries series;

    public SleepYearViewIndex(Activity act, View rootView) {
        this.rootView = rootView;

        chartView = (SleepChartView4) rootView.findViewById(R.id.chartView4);
        chartView.init();
        series = chartView.getSeries().get(0);

        chartView.getAreas().get(0).getDefaultYAxis().getScale().setMinimum(0d);
    }
}
