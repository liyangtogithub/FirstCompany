package com.desay.iwan2.module.sleep.index;

import android.app.Activity;
import android.view.View;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sleep.widget.SleepChartView2;
import com.desay.iwan2.module.sleep.widget.SleepChartView3;

import java.text.SimpleDateFormat;

/**
 * @author 方奕峰
 */
public class SleepMonthViewIndex {

    public View rootView;


    public SleepChartView3 chartView;

    public ChartSeries series;

    public SleepMonthViewIndex(Activity act, View rootView) {
        this.rootView = rootView;

        chartView = (SleepChartView3) rootView.findViewById(R.id.chartView3);
        chartView.init();
        series = chartView.getSeries().get(0);

        chartView.getAreas().get(0).getDefaultYAxis().getScale().setMinimum(0d);
    }
}
