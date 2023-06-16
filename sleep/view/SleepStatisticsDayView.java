package com.desay.iwan2.module.sleep.view;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.HeartRate;
import com.desay.iwan2.common.db.entity.Sleep;
import com.desay.iwan2.common.db.entity.SleepState;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sleep.index.SleepDayViewIndex;
import com.desay.iwan2.module.sleep.server.SleepDayServer;
import dolphin.tools.util.LogUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @author 方奕峰
 */
public class SleepStatisticsDayView extends RelativeLayout implements OnClickListener {

    private SleepActivity act;

    private SleepDayServer server;
    private SleepDayViewIndex viewIndex;
    private Date date;

    private Sleep sleep;

    public SleepStatisticsDayView(SleepActivity act) {
        super(act);
        this.act = act;

        viewIndex = new SleepDayViewIndex(act, this);
        viewIndex.chartView.setOnClickListener(this);
    }

    public void initData(Date date) throws Exception {
        viewIndex.sleep_statistic_empty_layout.setVisibility(VISIBLE);
        this.date = date;

        server = new SleepDayServer(act, date);

        sleep = server.initData();
//        LogUtil.i("sleep=" + sleep +
//                (sleep == null ? "" : " ; sleep.getSleepStates()=" + sleep.getSleepStates() +
//                        (sleep.getSleepStates() == null ? "" : " ; sleep.getSleepStates().size()=" + sleep.getSleepStates().size())));
        if (sleep == null || sleep.getSleepStates() == null || sleep.getSleepStates().size() == 0) {
            return;
        } else {
            viewIndex.sleep_statistic_empty_layout.setVisibility(GONE);
        }

        LogUtil.i("sleep.getStartTime()=" + sleep.getStartTime());
        viewIndex.textView_startTime.setText(SystemContant.timeFormat3.format(sleep.getStartTime()));
        viewIndex.textView_endTime.setText(SystemContant.timeFormat3.format(sleep.getEndTime()));

        viewIndex.chartView.wakeupSeries.getPoints().clear();
        viewIndex.chartView.dreamSeries.getPoints().clear();
        viewIndex.chartView.deepSeries.getPoints().clear();
        viewIndex.chartView.shallowSeries.getPoints().clear();
        viewIndex.chartView.heartrateSeries.getPoints().clear();

        for (HeartRate heartRate : sleep.getHeartRates()) {
            viewIndex.chartView.heartrateSeries.getPoints().addDate(heartRate.getTime(), heartRate.getValue());
        }

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        for (SleepState sleepState : sleep.getSleepStates()) {
            calendar1.setTime(sleepState.getStartTime());
            calendar2.setTime(sleepState.getEndTime());
            for (; calendar1.before(calendar2); calendar1.add(Calendar.MINUTE, SystemContant.sleepAreaChartUnit)) {
                ChartPoint point = null;
                switch (sleepState.getState()) {
                    case dream:
//                        viewIndex.dayViewIndex.chartView.dreamSeries.getPoints().addDate(calendar1, 3);
                        point = new ChartPoint(calendar1.getTimeInMillis(), 3);
                        viewIndex.chartView.dreamSeries.getPoints().add(point);
                        viewIndex.chartView.wakeupSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.deepSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case shallow:
//                        viewIndex.chartView.shallowSeries.getPoints().addDate(calendar1, 2);
                        point = new ChartPoint(calendar1.getTimeInMillis(), 2);
                        viewIndex.chartView.shallowSeries.getPoints().add(point);
                        viewIndex.chartView.wakeupSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.dreamSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.deepSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case deep:
//                        viewIndex.chartView.deepSeries.getPoints().addDate(calendar1, 1);
                        point = new ChartPoint(calendar1.getTimeInMillis(), 1);
                        viewIndex.chartView.deepSeries.getPoints().add(point);
                        viewIndex.chartView.wakeupSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.dreamSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                    case wake:
                    default:
//                        viewIndex.chartView.wakeupSeries.getPoints().addDate(calendar1, 4);
                        point = new ChartPoint(calendar1.getTimeInMillis(), 4);
                        viewIndex.chartView.wakeupSeries.getPoints().add(point);
                        viewIndex.chartView.dreamSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.deepSeries.getPoints().addDate(calendar1, 0);
                        viewIndex.chartView.shallowSeries.getPoints().addDate(calendar1, 0);
                        break;
                }
                point.setTag(SystemContant.sleepAreaChartUnit);
            }
        }

        viewIndex.chartView.area.getDefaultXAxis().getScale()
                .zoomToRange(sleep.getStartTime().getTime(), sleep.getEndTime().getTime());

        viewIndex.textView_quality.setText(server.getScore());
        viewIndex.textView_deepRate.setText(server.getDeepRate());

        viewIndex.textView_shallow.setText(server.getShallowDuration());
        viewIndex.textView_deep.setText(server.getDeepDuration());
        viewIndex.textView_dream.setText(server.getDreamDuration());
        viewIndex.textView_wakeup.setText(server.getWakeupDuration());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                act.back();
                break;
            case R.id.chartView:
            case R.id.sleep_empty_layout:
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    public void dispose() {
        viewIndex.chartView.wakeupSeries.getPoints().clear();
        viewIndex.chartView.dreamSeries.getPoints().clear();
        viewIndex.chartView.deepSeries.getPoints().clear();
        viewIndex.chartView.shallowSeries.getPoints().clear();
        viewIndex.chartView.heartrateSeries.getPoints().clear();
        server = null;
        viewIndex.sleep_statistic_empty_layout.setVisibility(GONE);
    }

    public Sleep getSleep() {
        return sleep;
    }

    public SleepDayServer getServer() {
        return server;
    }
}
