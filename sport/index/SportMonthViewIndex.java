package com.desay.iwan2.module.sport.index;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sport.widget.SportChartView2;

import java.text.SimpleDateFormat;

/**
 * @author 方奕峰
 */
public class SportMonthViewIndex {

    public View rootView;

    public TextView textView_sportType;

    public SportChartView2 chartView;
    public ChartSeries series;

    public SportMonthViewIndex(Activity act, View rootView) {
        this.rootView = rootView;

        textView_sportType = (TextView) rootView.findViewById(R.id.textView_monthSportType);

        chartView = (SportChartView2) rootView.findViewById(R.id.chartView3);
        chartView.init();
        series = chartView.getSeries().get(0);

        chartView.getAreas().get(0).getDefaultXAxis().setLabelFormat(new SimpleDateFormat("dd"+act.getString(R.string.day)));
    }
}
