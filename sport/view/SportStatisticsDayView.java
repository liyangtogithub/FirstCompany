package com.desay.iwan2.module.sport.view;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sport.SportActivity;
import com.desay.iwan2.module.sport.index.SportDayViewIndex;
import com.desay.iwan2.module.sport.server.SportDayServer;
import com.desay.iwan2.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author 方奕峰
 */
public class SportStatisticsDayView extends RelativeLayout implements OnClickListener {

    private SportActivity act;

    private SportDayServer server;
    private SportDayViewIndex viewIndex;
    private Date date;

    public SportStatisticsDayView(SportActivity act) {
        super(act);
        this.act = act;

        viewIndex = new SportDayViewIndex(act, this);
        viewIndex.chartView.setOnClickListener(this);
    }

    public void initData(Date date) throws Exception {
        viewIndex.sport_statistic_empty_layout.setVisibility(VISIBLE);
        this.date = date;

        server = new SportDayServer(act, date);
        server.init();

        List<Sport> listSP = server.getSport();
        if (listSP.size() == 0) {
            return;
        }else {
            viewIndex.sport_statistic_empty_layout.setVisibility(GONE);
        }

        viewIndex.series.getPoints().clear();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(server.date);
        TimeUtil.getDateStart(cal1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(server.date);
        TimeUtil.getDateStart(cal2);
        cal2.add(Calendar.DATE, 1);
        cal2.add(Calendar.MILLISECOND, -1);

        int interval = SystemContant.sportMotionInterval * 60 * 1000;
        for (; cal1.getTime().compareTo(cal2.getTime()) <= 0; cal1.add(Calendar.MINUTE, SystemContant.sportMotionInterval)) {
            boolean b = true;
            for (Sport sport : listSP) {
                if (cal1.getTimeInMillis() <= sport.getStartTime().getTime() &&
                        cal1.getTimeInMillis() + interval > sport.getStartTime().getTime()) {
//                    int a = sport.getStepCount();
                    int a = sport.getStepCount() < 0 ? 0 : sport.getStepCount();
                    ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), a);
                    point.setTag(sport);
//                    if (sport.getAerobics()) {
                    if (a > SystemContant.aerobicsBoundary) {
                        sport.setAerobics(true);
                        point.setBackColor(Color.rgb(0x1a, 0xff, 0xd7));
                    } else {
                        sport.setAerobics(false);
                    }
                    viewIndex.series.getPoints().add(point);
                    b = false;
                }
            }
            if (b) {
                viewIndex.series.getPoints().addDate(cal1.getTimeInMillis(), 0);
            }
        }
//        calendar.setTime(dayServer.date);
//        viewIndex.dayViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
//                .getScale().setZoom(calendar.getTimeInMillis(),SystemContant.millisecondInDay);

        viewIndex.chartView.setTodayDate(server.date);
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

    public SportDayServer getServer() {
        return server;
    }
}
