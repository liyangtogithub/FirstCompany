package com.desay.iwan2.module.sleep.view;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.SleepState;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sleep.index.SleepSummaryViewIndex;
import com.desay.iwan2.module.sleep.server.SleepSummaryServer;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 方奕峰
 */
public class SleepSummaryView extends RelativeLayout implements OnClickListener {

    private SleepActivity act;

    private SleepSummaryServer server;
    private SleepSummaryViewIndex viewIndex;
    public Date date;

    public SleepSummaryView(SleepActivity act) {
        super(act);
        this.act = act;

        viewIndex = new SleepSummaryViewIndex(act, this);
        viewIndex.btn_back.setOnClickListener(this);
        viewIndex.chartView.setOnClickListener(this);
        viewIndex.btn_toLandscape.setOnClickListener(this);
        viewIndex.sleep_empty_layout.setOnClickListener(this);
    }

    public void initData(Date date) throws Exception {
        viewIndex.sleep_empty_layout.setVisibility(View.VISIBLE);
        this.date = date;

        try {
            server = new SleepSummaryServer(act, date);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        viewIndex.btn_back.setText(server.getTitle());
        viewIndex.adapter.setDatas(server.getSleepRecordList());

        ChartSeries shallowSeries = viewIndex.chartView.getSeries().get("shallow");
        shallowSeries.getPoints().clear();
        ChartSeries deepSeries = viewIndex.chartView.getSeries().get("deep");
        deepSeries.getPoints().clear();
        ChartSeries dreamSeries = viewIndex.chartView.getSeries().get("dream");
        dreamSeries.getPoints().clear();
        ChartSeries wakeupSeries = viewIndex.chartView.getSeries().get("wakeup");
        wakeupSeries.getPoints().clear();

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        if (server.mainSleep == null || server.mainSleep.getSleepStates() == null || server.mainSleep.getSleepStates().size() == 0) {
            return;
        } else {
            viewIndex.sleep_empty_layout.setVisibility(View.GONE);
        }
        for (SleepState sleepState : server.mainSleep.getSleepStates()) {
            calendar1.setTime(sleepState.getStartTime());
            calendar2.setTime(sleepState.getEndTime());
            for (; calendar1.before(calendar2); calendar1.add(Calendar.MINUTE, SystemContant.sleepAreaChartUnit))
                switch (sleepState.getState()) {
                    case dream:
                        dreamSeries.getPoints().addDate(calendar1, 3);
                        wakeupSeries.getPoints().addDate(calendar1, 0);
                        deepSeries.getPoints().addDate(calendar1, 0);
                        shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case shallow:
                        shallowSeries.getPoints().addDate(calendar1, 2);
                        wakeupSeries.getPoints().addDate(calendar1, 0);
                        dreamSeries.getPoints().addDate(calendar1, 0);
                        deepSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case deep:
                        deepSeries.getPoints().addDate(calendar1, 1);
                        wakeupSeries.getPoints().addDate(calendar1, 0);
                        dreamSeries.getPoints().addDate(calendar1, 0);
                        shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case wake:
                    default:
                        wakeupSeries.getPoints().addDate(calendar1, 4);
                        dreamSeries.getPoints().addDate(calendar1, 0);
                        deepSeries.getPoints().addDate(calendar1, 0);
                        shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                }
        }

        viewIndex.chartView.getAreas().get(0).getDefaultXAxis().getScale()
                .zoomToRange(server.mainSleep.getStartTime().getTime(), server.mainSleep.getEndTime().getTime());

        viewIndex.textView_startTime.setText(server.getMainSleepStartTime());
        viewIndex.textView_endTime.setText(server.getMainSleepEndTime());

//        calendar.add(Calendar.HOUR_OF_DAY, 1);
//        deepSeries.getPoints().addDate(calendar, 0);
//        wakeupSeries.getPoints().addDate(calendar, 0);
//        dreamSeries.getPoints().addDate(calendar, 0);
//        shallowSeries.getPoints().addDate(calendar, 0);

        viewIndex.textView_duration.setText(server.getMainSleepDuration());
        viewIndex.textView_quality.setText(server.getMainSleepScore());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                act.back();
                break;
            case R.id.chartView:
            case R.id.btn_toLandscape:
            case R.id.sleep_empty_layout:
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    public void dispose() {
        ChartSeries shallowSeries = viewIndex.chartView.getSeries().get("shallow");
        shallowSeries.getPoints().clear();
        ChartSeries deepSeries = viewIndex.chartView.getSeries().get("deep");
        deepSeries.getPoints().clear();
        ChartSeries dreamSeries = viewIndex.chartView.getSeries().get("dream");
        dreamSeries.getPoints().clear();
        ChartSeries wakeupSeries = viewIndex.chartView.getSeries().get("wakeup");
        wakeupSeries.getPoints().clear();
        server = null;
        viewIndex.sleep_empty_layout.setVisibility(View.GONE);
    }
}
