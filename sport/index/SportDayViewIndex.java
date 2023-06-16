package com.desay.iwan2.module.sport.index;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.module.sport.widget.SportChartView1;

/**
 * @author 方奕峰
 */
public class SportDayViewIndex {

    public View rootView;

    public SportChartView1 chartView;

    public ChartSeries series;

    public RelativeLayout sport_statistic_empty_layout;

    public SportDayViewIndex(Activity act, ViewGroup viewGroup) {
        this.rootView = act.getLayoutInflater().inflate(R.layout.sport_day_view, viewGroup);

        chartView = (SportChartView1) rootView.findViewById(R.id.chartView1);
        chartView.init();
        series = chartView.getSeries().get(0);

        sport_statistic_empty_layout = (RelativeLayout)rootView.
                findViewById(R.id.sport_statistic_empty_layout);
    }
}
