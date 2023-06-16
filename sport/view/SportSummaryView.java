package com.desay.iwan2.module.sport.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.http.entity.response.SportListdata;
import com.desay.iwan2.common.api.http.entity.response.CommitSportData.sport;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Sport;
import com.desay.iwan2.module.sport.SportActivity;
import com.desay.iwan2.module.sport.index.SportSummaryViewIndex;
import com.desay.iwan2.module.sport.server.SportAimServer;
import com.desay.iwan2.module.sport.server.SportSummaryServer;

import com.desay.iwan2.util.TimeUtil;
import dolphin.tools.util.LogUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author 方奕峰
 */
public class SportSummaryView extends RelativeLayout implements OnClickListener {

    private SportActivity act;

    private SportSummaryServer server;
    private SportSummaryViewIndex viewIndex;

    public Date date;

    private SportAimServer aimServer;

    public SportSummaryView(SportActivity act, SportAimServer aimServer) {
        super(act);
        this.act = act;

        viewIndex = new SportSummaryViewIndex(act, this);
        viewIndex.btn_back.setOnClickListener(this);
        viewIndex.chartView.setOnClickListener(this);
        viewIndex.btn_toLandscape.setOnClickListener(this);
        viewIndex.sport_empty_layout.setOnClickListener(this);
        this.aimServer = aimServer;
    }

    public View initData(Date date) throws SQLException {
        viewIndex.sport_empty_layout.setVisibility(VISIBLE);
        this.date = date;

        server = new SportSummaryServer(getContext(), date);
        server.init();
        server.aerobicStepAim = aimServer.getAerobicStepAim();
        server.stepAim = aimServer.getStepAim();
        server.calorieAim = aimServer.getCalorieAim();
        server.distanceAim = aimServer.getDistanceAim();

        //viewIndex.textView_title.setText(server.getTitle());

        ChartSeries series = viewIndex.chartView.getSeries().get(0);
        List<Sport> sportList = server.getSportList();

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
            for (Sport sport : sportList) {
                if (cal1.getTimeInMillis() <= sport.getStartTime().getTime() &&
                        cal1.getTimeInMillis() + interval > sport.getStartTime().getTime()) {
                    int a = sport.getStepCount();
                    ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), a);
                    point.setTag(sport);
//                    if (sport.getAerobics()) {
                    if (a > SystemContant.aerobicsBoundary) {
                        point.setBackColor(Color.rgb(0x1a, 0xff, 0xd7));
                    }
                    series.getPoints().add(point);
                    b = false;
                }
            }
            if (b) {
                series.getPoints().addDate(cal1.getTimeInMillis(), 0);
            }
        }

//        viewIndex.chartView.getAreas().get(0).getDefaultXAxis()
//        .getScale().setZoom(0,SystemContant.millisecondInDay);

        viewIndex.btn_back.setText(server.getTitle());
        if (server.getStep().equals("0")) {
        } else {
            viewIndex.sport_empty_layout.setVisibility(GONE);
            viewIndex.textView_startTime.setText(SystemContant.timeFormat3.format(server.getStartDate()));
            viewIndex.textView_endTime.setText(SystemContant.timeFormat3.format(server.getEndDate()));
            viewIndex.textView_aimRate.setText(String.valueOf(server.getStepRate()) + "%");
            viewIndex.textView_step.setText(server.getStep());
            viewIndex.textView_stepAim.setText(String.valueOf(aimServer.getStepAim()));
            viewIndex.progressBar_step.setProgress(server.getStepRate());
            viewIndex.textView_aerobicStep.setText(server.getAerobicStep());
            viewIndex.textView_aerobicStepAim.setText(String.valueOf(aimServer.getAerobicStepAim()));
            viewIndex.progressBar_aerobicStep.setProgress(server.getAerobicStepRate());
            viewIndex.textView_calorie.setText(server.getCalorie());
            viewIndex.textView_calorieAim.setText(String.valueOf(server.getCalorieAim()));
            viewIndex.progressBar_calorie.setProgress(server.getCalorieRate());
            viewIndex.textView_distance.setText(server.getDistance());
            viewIndex.textView_distanceAim.setText(String.valueOf(server.getDistanceAim()));
            viewIndex.progressBar_distance.setProgress(server.getDistanceRate());
        }


        return viewIndex.rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                act.back();
                break;
            case R.id.chartView:
            case R.id.btn_toLandscape:
            case R.id.sport_empty_layout:
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

}
