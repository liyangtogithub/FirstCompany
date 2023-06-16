package com.desay.iwan2.module.sport.fragment;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TextView;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.DayServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sport.SportActivity;
import com.desay.iwan2.module.sport.index.SportStatisticsViewIndex;
import com.desay.iwan2.module.sport.server.SportMonthServer;
import com.desay.iwan2.module.sport.server.SportMonthServer.SportPoint;
import com.desay.iwan2.module.sport.server.SportWeekServer;
import com.desay.iwan2.module.sport.server.SportYearServer;

import com.desay.iwan2.module.sport.view.SportStatisticsDayView;
import com.desay.iwan2.module.sport.widget.SportChartView2;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author 方奕峰
 */
public class SportStatisticsFragment extends BaseFragment implements OnClickListener, CompoundButton.OnCheckedChangeListener,
        TabHost.OnTabChangeListener {

    private Activity act;
    private LayoutInflater inflater;

    private SportStatisticsViewIndex viewIndex;

    private SportWeekServer weekServer;
    private SportMonthServer monthServer;
    private SportYearServer yearServer;
    private Long sprotAim;

    private boolean isFirst;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = getActivity();
        this.inflater = inflater;
        isFirst = true;

        Date date = (Date) ((SportActivity) act).bundle.getSerializable(SportActivity.KEY);
        try {
            weekServer = new SportWeekServer(act, date);
            monthServer = new SportMonthServer(act, date);
            yearServer = new SportYearServer(act, date);

            OtherServer os = new OtherServer(getActivity());
            Other sleepAim = os.getOther(null, Other.Type.sportAim);
            if (sleepAim == null) {
                sprotAim = (long) SystemContant.defaultSportAim;
            } else {
                String strSprotAim = sleepAim.getValue();
                sprotAim = Long.valueOf(strSprotAim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        viewIndex = new SportStatisticsViewIndex(act, inflater);
        viewIndex.radio_day.setOnCheckedChangeListener(this);
        viewIndex.radio_week.setOnCheckedChangeListener(this);
        viewIndex.radio_month.setOnCheckedChangeListener(this);
        viewIndex.radio_year.setOnCheckedChangeListener(this);
        viewIndex.radio_day.setChecked(true);

        initDayView();

        viewIndex.tabHost.setOnTabChangedListener(this);

        return viewIndex.rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                back();
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        try {
            if ("day".equals(tabId)) {
                updateTitleBar(viewIndex.dayView.getCurrentItem());
            } else if ("week".equals(tabId)) {
                showWeek();
            } else if ("month".equals(tabId)) {
                showMonth();
            } else if ("year".equals(tabId)) {
                showYear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.radio_day:
                    viewIndex.radio_day.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    viewIndex.radio_week.getPaint().setFlags(0);
                    viewIndex.radio_month.getPaint().setFlags(0);
                    viewIndex.radio_year.getPaint().setFlags(0);
                    viewIndex.tabHost.setCurrentTabByTag("day");
                    break;
                case R.id.radio_week:
                    viewIndex.radio_day.getPaint().setFlags(0);
                    viewIndex.radio_week.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    viewIndex.radio_month.getPaint().setFlags(0);
                    viewIndex.radio_year.getPaint().setFlags(0);
                    viewIndex.tabHost.setCurrentTabByTag("week");
                    break;
                case R.id.radio_month:
                    viewIndex.radio_day.getPaint().setFlags(0);
                    viewIndex.radio_week.getPaint().setFlags(0);
                    viewIndex.radio_month.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    viewIndex.radio_year.getPaint().setFlags(0);
                    viewIndex.tabHost.setCurrentTabByTag("month");
                    break;
                case R.id.radio_year:
                    viewIndex.radio_day.getPaint().setFlags(0);
                    viewIndex.radio_week.getPaint().setFlags(0);
                    viewIndex.radio_month.getPaint().setFlags(0);
                    viewIndex.radio_year.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    viewIndex.tabHost.setCurrentTabByTag("year");
                    break;
            }
        }
    }

    ///////////////////////////////
    private Date[] dateRange;

    private void initDayView() {
        Date date = (Date) ((SportActivity) act).bundle.getSerializable(SleepActivity.KEY);
        try {
            dateRange = new DayServer(act).getRange(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dateRange[0] == null || date.before(dateRange[0])) {
            dateRange[0] = date;
        } else if (dateRange[1] == null || date.after(dateRange[1])) {
            dateRange[1] = date;
        }
        int day = (int) ((dateRange[1].getTime() - dateRange[0].getTime()) / SystemContant.millisecondInDay) + 1;
        DayViewPagerAdapter pagerAdapter = new DayViewPagerAdapter(day);
        viewIndex.dayView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
//                for (int i = 0; i < viewIndex.dayView.getChildCount(); i++) {
//                    SportStatisticsDayView v = (SportStatisticsDayView) viewIndex.dayView.getChildAt(i);
//                    if (position == (Integer) v.getTag()) {
//                        viewIndex.textView_1.setText(v.getServer().getText1());
//                        viewIndex.textView_2.setText(v.getServer().getTotalStep());
//                    }
//                }
                updateTitleBar(position);
                super.onPageSelected(position);
            }
        });
        viewIndex.dayView.setAdapter(pagerAdapter);
        int index = (int) ((date.getTime() - dateRange[0].getTime()) / SystemContant.millisecondInDay);
        viewIndex.dayView.setCurrentItem(index);
        updateTitleBar(index);
    }

    private void updateTitleBar(int index) {
        for (int i = 0; i < viewIndex.dayView.getChildCount(); i++) {
            SportStatisticsDayView v = (SportStatisticsDayView) viewIndex.dayView.getChildAt(i);
            if (index == (Integer) v.getTag()) {
                viewIndex.textView_1.setText(v.getServer().getText1());
                viewIndex.textView_2.setText(v.getServer().getTotalStep());
            }
        }
    }

    private class DayViewPagerAdapter extends PagerAdapter {

        public int size;

        public DayViewPagerAdapter(int size) {
            this.size = size;
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SportStatisticsDayView v = new SportStatisticsDayView((SportActivity) act);
            v.setTag(position);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateRange[0]);
            calendar.add(Calendar.DATE, position);
            try {
                v.initData(calendar.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

            container.addView(v);

            if (isFirst) {
                isFirst = false;
                viewIndex.textView_1.setText(v.getServer().getText1());
                viewIndex.textView_2.setText(v.getServer().getTotalStep());
            }

            return v;
        }
    }

    private void setSportTypeShow(TextView textView, int step) {
        String str = null;
        if (step < 6000) {
            str = act.getString(R.string.SportTypeSedentary);
        } else if (step < 10000) {
            str = act.getString(R.string.SportTypeActivity);
        } else {
            str = act.getString(R.string.SportTypeAthletic);
        }
        textView.setText(str);
    }

//    private DecimalFormat decimalFormat = new DecimalFormat("0");

    private void showWeek() throws SQLException {
        List<SportPoint> listSP = weekServer.init();
        viewIndex.weekViewIndex.chartView.setCameraListener(new SportChartView2.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM" + " d");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay)) +
                        " ~ " +
                        timeFormat.format(new Date((long) r)));
                int avgStep = (int) (sum / pcount);
                viewIndex.textView_2.setText(act.getString(R.string.SportAvgStepLabel1) + avgStep);
                setSportTypeShow(viewIndex.weekViewIndex.textView_sportType, avgStep);
            }
        });

//        viewIndex.textView_1.setText(weekServer.getText1());
//        viewIndex.textView_2.setText(weekServer.getAvgStep());
        viewIndex.weekViewIndex.textView_sportType.setText(weekServer.getSportType());

        ChartSeries series = viewIndex.weekViewIndex.series;

        Calendar cl1 = Calendar.getInstance();
        SportPoint lastSportPoint = null;
        if (listSP.size() > 0) {
            cl1.setTime(listSP.get(0).date);
            cl1.set(Calendar.DAY_OF_MONTH, 1);

            lastSportPoint = listSP.get(listSP.size() - 1);
            Calendar cl2 = Calendar.getInstance();
            cl2.setTime(lastSportPoint.date);
            cl2.set(Calendar.DAY_OF_MONTH, 1);
            cl2.add(Calendar.MONTH, -15);
            if (cl2.before(cl1)) {
                cl1 = cl2;
            }
        } else {
            cl1.setTime(weekServer.rangeCalendars[0].getTime());
        }
        for (int i = 0; cl1.getTime().compareTo(lastSportPoint == null ? weekServer.rangeCalendars[1].getTime() : lastSportPoint.date) <= 0; cl1.add(Calendar.DATE, 1)) {
            SportPoint sportPoint = listSP.get(i);
            if (cl1.getTime().equals(sportPoint.date)) {
//                int a = sportPoint.step;
                int a = sportPoint.step < 0 ? 0 : sportPoint.step;
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), a);
                point.setTag(sportPoint);
                if (a >= sprotAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);
                if (i < listSP.size() - 1)
                    ++i;
            } else {
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), 0d);
                series.getPoints().add(point);
            }
        }

        viewIndex.weekViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
                .getScale().setZoom(weekServer.rangeCalendars[1].getTimeInMillis(), 7d * 1000 * 60 * 60 * 24);
    }

    private void showMonth() throws SQLException {
        List<SportPoint> listSP = monthServer.init();
        viewIndex.monthViewIndex.chartView.setCameraListener(new SportChartView2.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM" + " d");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay)) +
                        " ~ " +
                        timeFormat.format(new Date((long) r)));
                int avgStep = (int) (sum / pcount);
                viewIndex.textView_2.setText(act.getString(R.string.SportAvgStepLabel1) + avgStep);
                setSportTypeShow(viewIndex.monthViewIndex.textView_sportType, avgStep);
            }
        });
//        viewIndex.textView_1.setText(monthServer.getText1());

//        viewIndex.textView_2.setText(monthServer.getAvgStep());

        viewIndex.monthViewIndex.textView_sportType.setText(monthServer.getSportType());

        ChartSeries series = viewIndex.monthViewIndex.series;

        Calendar cl1 = Calendar.getInstance();
        SportPoint lastSportPoint = null;
        if (listSP.size() > 0) {
            cl1.setTime(listSP.get(0).date);
            cl1.set(Calendar.DAY_OF_MONTH, 1);

            lastSportPoint = listSP.get(listSP.size() - 1);
            Calendar cl2 = Calendar.getInstance();
            cl2.setTime(lastSportPoint.date);
            cl2.set(Calendar.DAY_OF_MONTH, 1);
            cl2.add(Calendar.MONTH, -15);
            if (cl2.before(cl1)) {
                cl1 = cl2;
            }
        } else {
            cl1.setTime(monthServer.rangeCalendars[0].getTime());
        }
        for (int i = 0; cl1.getTime().compareTo(lastSportPoint == null ? monthServer.rangeCalendars[1].getTime() : lastSportPoint.date) <= 0; cl1.add(Calendar.DATE, 1)) {
            SportPoint sportPoint = listSP.get(i);
            if (cl1.getTime().compareTo(sportPoint.date) == 0) {
//                int a = sportPoint.step;
                int a = sportPoint.step < 0 ? 0 : sportPoint.step;
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), a);
                point.setTag(sportPoint);
                if (a > sprotAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);
                if (i < listSP.size() - 1)
                    ++i;
            } else {
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), 0d);
                series.getPoints().add(point);
            }
        }

        viewIndex.monthViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
                .getScale().setZoom(monthServer.rangeCalendars[1].getTimeInMillis(), 30d * 1000 * 60 * 60 * 24);
    }

    private void showYear() throws SQLException {
        List<SportPoint> listSP = yearServer.init();
        viewIndex.yearViewIndex.chartView.setCameraListener(new SportChartView2.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy" + act.getString(R.string.year) + " MMM");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay * 31L)) +
                        " ~ " +
                        timeFormat.format(new Date((long) r)));
                int avgStep = (int) (sum / pcount);
                viewIndex.textView_2.setText(act.getString(R.string.SportAvgStepLabel1) + avgStep);
                setSportTypeShow(viewIndex.yearViewIndex.textView_sportType, avgStep);
            }
        });

        viewIndex.yearViewIndex.textView_sportType.setText(yearServer.getSportType());

        ChartSeries series = viewIndex.yearViewIndex.series;

        Calendar cl1 = Calendar.getInstance();
        SportPoint lastSportPoint = null;
        if (listSP.size() > 0) {
            cl1.setTime(listSP.get(0).date);
            cl1.set(Calendar.DAY_OF_MONTH, 1);

            lastSportPoint = listSP.get(listSP.size() - 1);
            Calendar cl2 = Calendar.getInstance();
            cl2.setTime(lastSportPoint.date);
            cl2.set(Calendar.DAY_OF_MONTH, 1);
            cl2.add(Calendar.MONTH, -15);
            if (cl2.before(cl1)) {
                cl1 = cl2;
            }
        } else {
            cl1.setTime(yearServer.rangeCalendars[0].getTime());
        }
        Calendar cl3 = Calendar.getInstance();
        for (int i = 0; cl1.getTime().compareTo(lastSportPoint == null ? yearServer.rangeCalendars[1].getTime() : lastSportPoint.date) <= 0; cl1.add(Calendar.MONTH, 1)) {
            SportPoint sportPoint = listSP.get(i);
            cl3.setTime(sportPoint.date);
            cl3.set(Calendar.DAY_OF_MONTH, 1);
            if (cl3.getTimeInMillis() == cl1.getTimeInMillis()) {
                int a = sportPoint.step < 0 ? 0 : sportPoint.step;
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), a);
                point.setTag(sportPoint);
                if (a > sprotAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);

                if (i < listSP.size() - 1)
                    ++i;
            } else {
                ChartPoint point = new ChartPoint(cl1.getTimeInMillis(), 0d);
                series.getPoints().add(point);
            }
        }

        viewIndex.yearViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
                .getScale().setZoom(yearServer.rangeCalendars[1].getTimeInMillis(), cl1.getMaximum(Calendar.DAY_OF_YEAR) * 1000d * 60 * 60 * 24);
    }
}
