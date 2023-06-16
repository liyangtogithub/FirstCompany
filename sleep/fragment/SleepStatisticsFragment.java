package com.desay.iwan2.module.sleep.fragment;

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
import com.artfulbits.aiCharts.Base.ChartAxisScale;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.server.DayServer;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sleep.index.SleepStatisticsViewIndex;
import com.desay.iwan2.module.sleep.server.SleepMonthServer;
import com.desay.iwan2.module.sleep.server.SleepWeekServer;
import com.desay.iwan2.module.sleep.server.SleepYearServer;
import com.desay.iwan2.module.sleep.view.SleepStatisticsDayView;
import com.desay.iwan2.module.sleep.widget.SleepChartView2;
import com.desay.iwan2.module.sleep.widget.SleepChartView3;
import com.desay.iwan2.module.sleep.widget.SleepChartView4;
import com.desay.iwan2.util.TimeUtil;
import dolphin.tools.util.LogUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * @author 方奕峰
 */
public class SleepStatisticsFragment extends BaseFragment implements OnClickListener, CompoundButton.OnCheckedChangeListener,
        TabHost.OnTabChangeListener {

    private Activity act;
    private LayoutInflater inflater;

    private SleepStatisticsViewIndex viewIndex;
    private SleepWeekServer weekServer;
    private SleepMonthServer monthServer;
    private SleepYearServer yearServer;
    private boolean isFirst;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = getActivity();
        this.inflater = inflater;
        isFirst = true;

        Date date = (Date) ((SleepActivity) act).bundle.getSerializable(SleepActivity.KEY);


        try {
            weekServer = new SleepWeekServer(act, date);
            monthServer = new SleepMonthServer(act, date);
            yearServer = new SleepYearServer(act, date);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        viewIndex = new SleepStatisticsViewIndex(act, inflater);
        viewIndex.radio_day.setOnCheckedChangeListener(this);
        viewIndex.radio_day.setChecked(true);
        viewIndex.radio_week.setOnCheckedChangeListener(this);
        viewIndex.radio_month.setOnCheckedChangeListener(this);
        viewIndex.radio_year.setOnCheckedChangeListener(this);

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
        } catch (Exception e) {
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
        Date date = (Date) ((SleepActivity) act).bundle.getSerializable(SleepActivity.KEY);
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
//                    SleepStatisticsDayView v = (SleepStatisticsDayView) viewIndex.dayView.getChildAt(i);
//                    if (position == (Integer) v.getTag()) {
//                        viewIndex.textView_1.setText(v.getServer().getText1());
//                        viewIndex.textView_2.setText(v.getServer().getTotalDuration());
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
            SleepStatisticsDayView v = (SleepStatisticsDayView) viewIndex.dayView.getChildAt(i);
            if (index == (Integer) v.getTag()) {
                viewIndex.textView_1.setText(v.getServer().getText1());
                viewIndex.textView_2.setText(v.getServer().getTotalDuration());
            }
        }
    }

    private class DayViewPagerAdapter extends PagerAdapter {
        Vector<SleepStatisticsDayView> l = new Vector<SleepStatisticsDayView>();

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
            SleepStatisticsDayView v = (SleepStatisticsDayView) object;
            container.removeView(v);
            v.dispose();
            l.add(v);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SleepStatisticsDayView v = null;
            if (l.size() > 0) {
                v = l.get(l.size() - 1);
                l.remove(v);
            } else {
                v = new SleepStatisticsDayView((SleepActivity) act);
            }
            v.setTag(position);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateRange[0]);
            calendar.add(Calendar.DATE, position);
            try {
//                LogUtil.i("1111111111111111111111111");
                v.initData(calendar.getTime());
//                LogUtil.i("222222222222222222222222");
            } catch (Exception e) {
                e.printStackTrace();
//                LogUtil.i("3333333333;"+e.getMessage()+" ; date="+SystemContant.timeFormat10.format(calendar.getTime()));
            }

            container.addView(v);

            if (isFirst) {
                isFirst = false;
                viewIndex.textView_1.setText(v.getServer().getText1());
                viewIndex.textView_2.setText(v.getServer().getTotalDuration());
            }

            return v;
        }
    }

    private void showWeek() throws Exception {
        List<SleepWeekServer.SleepDuration> sleepDurationList = weekServer.initData();
        viewIndex.weekViewIndex.chartView.setCameraListener(new SleepChartView2.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM" + " d");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay))
                        + " ~ "
                        + timeFormat.format(new Date((long) r)));
                viewIndex.textView_2.setText(act.getString(R.string.SleepAvgTime) + TimeUtil.formatTimeString((int) (sum * 60 / pcount)));
            }
        });

        ChartSeries series = viewIndex.weekViewIndex.series;

        series.getPoints().clear();

//        for (SleepWeekServer.SleepDuration sleepDuration : sleepDurationList) {
//            double a = sleepDuration.duration / 60d;
//            ChartPoint point = new ChartPoint(sleepDuration.date.getTime(), a);
//            if (a > weekServer.sleepAim) {
//                point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
//            }
//            series.getPoints().add(point);
//        }

        Calendar cal1 = Calendar.getInstance();
        SleepWeekServer.SleepDuration sleepDuration2 = null;
        if (sleepDurationList.size() > 0) {
            SleepWeekServer.SleepDuration sleepDuration1 = sleepDurationList.get(0);
            cal1.setTime(sleepDuration1.date);

            sleepDuration2 = sleepDurationList.get(sleepDurationList.size() - 1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(sleepDuration2.date);
            cal2.add(Calendar.DATE, -10);
            if (cal2.before(cal1)) {
                cal1 = cal2;
            }
        } else {
            cal1.setTime(weekServer.rangeCalendars[0].getTime());
        }
        Calendar cal3 = Calendar.getInstance();
        int i = 0;
//        int j=0,k=0;
        for (; cal1.getTime().compareTo(sleepDuration2 == null ? weekServer.rangeCalendars[1].getTime() : sleepDuration2.date) <= 0; cal1.add(Calendar.DATE, 1)) {
            cal3.setTime(sleepDurationList.get(i).date);
//            LogUtil.i("j="+j++);
//            LogUtil.i("cal1="+SystemContant.timeFormat8.format(cal1.getTime())+";cal3="+SystemContant.timeFormat8.format(cal3.getTime()));
            if (cal3.compareTo(cal1) == 0) {
                double a = sleepDurationList.get(i).duration / 60d;
                ChartPoint point = new ChartPoint(cal3.getTimeInMillis(), a);
                point.setTag(sleepDurationList.get(i));
                if (a >= weekServer.sleepAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);
                i++;
            } else {
                ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), 0d);
                point.setTag(false);
                series.getPoints().add(point);
//                LogUtil.i("k="+k++);
            }
        }

        viewIndex.weekViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
                .getScale().setZoom(weekServer.rangeCalendars[1].getTimeInMillis(), 7 * SystemContant.millisecondInDay);
    }

    private void showMonth() throws SQLException {
        List<SleepWeekServer.SleepDuration> sleepDurationList = monthServer.initData();
        viewIndex.monthViewIndex.chartView.setCameraListener(new SleepChartView3.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM" + " d");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay))
                        + " ~ "
                        + timeFormat.format(new Date((long) r)));
                viewIndex.textView_2.setText(act.getString(R.string.SleepAvgTime) + TimeUtil.formatTimeString((int) (sum * 60 / pcount)));
            }
        });

        ChartSeries series = viewIndex.monthViewIndex.series;

        series.getPoints().clear();
//        for (SleepWeekServer.SleepDuration sleepDuration : sleepDurationList) {
//            double a = sleepDuration.duration / 60d;
//            ChartPoint point = new ChartPoint(sleepDuration.date.getTime(), a);
//            if (a > monthServer.sleepAim) {
//                point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
//            }
//            series.getPoints().add(point);
//        }

        Calendar cal1 = Calendar.getInstance();
        SleepWeekServer.SleepDuration sleepDuration2 = null;
        if (sleepDurationList.size() > 0) {
            SleepWeekServer.SleepDuration sleepDuration1 = sleepDurationList.get(0);
            cal1.setTime(sleepDuration1.date);

            sleepDuration2 = sleepDurationList.get(sleepDurationList.size() - 1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(sleepDuration2.date);
            cal2.add(Calendar.DATE, -35);
            if (cal2.before(cal1)) {
                cal1 = cal2;
            }
        } else {
            cal1.setTime(monthServer.rangeCalendars[0].getTime());
        }
        Calendar cal3 = Calendar.getInstance();
        int i = 0;
//        int j=0,k=0;
        for (; cal1.getTime().compareTo(sleepDuration2 == null ? monthServer.rangeCalendars[1].getTime() : sleepDuration2.date) <= 0; cal1.add(Calendar.DATE, 1)) {
            cal3.setTime(sleepDurationList.get(i).date);
//            LogUtil.i("j="+j++);
//            LogUtil.i("cal1="+SystemContant.timeFormat8.format(cal1.getTime())+";cal3="+SystemContant.timeFormat8.format(cal3.getTime()));
            if (cal3.compareTo(cal1) == 0) {
                double a = sleepDurationList.get(i).duration / 60d;
                ChartPoint point = new ChartPoint(cal3.getTimeInMillis(), a);
                point.setTag(sleepDurationList.get(i));
                if (a >= monthServer.sleepAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);
                i++;
            } else {
                ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), 0d);
                point.setTag(false);
                series.getPoints().add(point);
//                LogUtil.i("k="+k++);
            }
        }
        viewIndex.monthViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
                .getScale().setZoom(monthServer.rangeCalendars[1].getTimeInMillis(), 30d * SystemContant.millisecondInDay);
    }

    private void showYear() throws SQLException {
        List<SleepYearServer.SleepDuration> sleepDurationList = yearServer.initData();
        viewIndex.yearViewIndex.chartView.setCameraListener(new SleepChartView4.CameraListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy" + act.getString(R.string.year) + " MMM");

            @Override
            public void onShow(double l, double r, double sum, int pcount) {
                viewIndex.textView_1.setText(timeFormat.format(new Date((long) l + SystemContant.millisecondInDay * 31L))
                        + " ~ "
                        + timeFormat.format(new Date((long) r)));
                viewIndex.textView_2.setText(act.getString(R.string.SleepAvgTime) + TimeUtil.formatTimeString((int) (sum * 60 / pcount)));
            }
        });

        ChartSeries series = viewIndex.yearViewIndex.series;

        series.getPoints().clear();
//        for (SleepYearServer.SleepDuration sleepDuration : sleepDurationList) {
//            double a = sleepDuration.duration / 60d;
//            ChartPoint point = new ChartPoint(sleepDuration.date.getTime(), a);
//            if (a > yearServer.sleepAim) {
//                point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
//            }
//            series.getPoints().add(point);
//        }

        Calendar cal1 = Calendar.getInstance();
        SleepYearServer.SleepDuration sleepDuration2 = null;
        if (sleepDurationList.size() > 0) {
            SleepYearServer.SleepDuration sleepDuration1 = sleepDurationList.get(0);
            cal1.setTime(sleepDuration1.date);
            cal1.set(Calendar.DAY_OF_MONTH, 1);

            sleepDuration2 = sleepDurationList.get(sleepDurationList.size() - 1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(sleepDuration2.date);
            cal2.set(Calendar.DAY_OF_MONTH, 1);
            cal2.add(Calendar.MONTH, -15);
            if (cal2.before(cal1)) {
                cal1 = cal2;
            }
        } else {
            cal1.setTime(yearServer.rangeCalendars[0].getTime());
        }
        Calendar cal3 = Calendar.getInstance();
        int i = 0;
        for (; cal1.getTime().compareTo(sleepDuration2 == null ? yearServer.rangeCalendars[1].getTime() : sleepDuration2.date) <= 0; cal1.add(Calendar.MONTH, 1)) {
            cal3.setTime(sleepDurationList.get(i).date);
            cal3.set(Calendar.DAY_OF_MONTH, 1);
            if (cal3.compareTo(cal1) == 0) {
                SleepYearServer.SleepDuration sleepDuration = sleepDurationList.get(i);
                double a = sleepDuration.duration / 60d;
                ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), a);
                point.setTag(sleepDuration);
                if (a >= yearServer.sleepAim) {
                    point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
                }
                series.getPoints().add(point);
                i++;
            } else {
                ChartPoint point = new ChartPoint(cal1.getTimeInMillis(), 0d);
                point.setTag(false);
                series.getPoints().add(point);
            }
        }

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(yearServer.rangeCalendars[1].getTime());
//        Random random = new Random();
//        series.getPoints().clear();
//        for (; calendar.before(yearServer.rangeCalendars[0]); calendar.add(Calendar.MONTH, 1)) {
//            if (random.nextInt(10) > 1) continue;
//            int a = random.nextInt(8) + 3;
//            ChartPoint point = new ChartPoint(calendar.getTimeInMillis(), a);
//            if (a > 7) {
//                point.setBackDrawable(getResources().getDrawable(R.drawable.rectangle_pommel_2));
//            }
//            series.getPoints().add(point);
//        }

//        LogUtil.i("最小日期=" + SystemContant.timeFormat1.format(yearServer.rangeCalendars[1].getTime()));
        ChartAxisScale scale = viewIndex.yearViewIndex.chartView.getAreas().get(0).getDefaultXAxis().getScale();
        scale.setZoom(yearServer.rangeCalendars[1].getTimeInMillis(), 365L * SystemContant.millisecondInDay);
//        scale.zoomToRange(yearServer.rangeCalendars[1].getTimeInMillis(), yearServer.rangeCalendars[0].getTimeInMillis());

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(yearServer.rangeCalendars[1].getTime());
//        calendar.add(Calendar.DATE,365);
//        viewIndex.yearViewIndex.chartView.getAreas().get(0).getDefaultXAxis()
//                .getScale().setDateRange(yearServer.rangeCalendars[1],calendar);
    }
}
