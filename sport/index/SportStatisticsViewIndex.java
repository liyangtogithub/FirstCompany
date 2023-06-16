package com.desay.iwan2.module.sport.index;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import com.desay.fitband.R;

/**
 * @author 方奕峰
 */
public class SportStatisticsViewIndex {

    public View rootView;

    public TextView textView_1, textView_2;

    public RadioGroup radioGroup;
    public RadioButton radio_day, radio_week, radio_month, radio_year;

    public TabHost tabHost;

//    public SportDayViewIndex dayViewIndex;
    public ViewPager dayView;
    public SportWeekViewIndex weekViewIndex;
    public SportMonthViewIndex monthViewIndex;
    public SportYearViewIndex yearViewIndex;

    public SportStatisticsViewIndex(Activity act, LayoutInflater inflater) {
        rootView = inflater.inflate(R.layout.sport_statistics_fragment, null);

        textView_1 = (TextView) rootView.findViewById(R.id.textView_1);
        textView_2 = (TextView) rootView.findViewById(R.id.textView_2);

        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radio_day = (RadioButton) rootView.findViewById(R.id.radio_day);
        radio_week = (RadioButton) rootView.findViewById(R.id.radio_week);
        radio_month = (RadioButton) rootView.findViewById(R.id.radio_month);
        radio_year = (RadioButton) rootView.findViewById(R.id.radio_year);

        tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("day").setIndicator("日")
                .setContent(R.id.layout_sportDay));
        tabHost.addTab(tabHost.newTabSpec("week").setIndicator("周")
                .setContent(R.id.layout_sportWeek));
        tabHost.addTab(tabHost.newTabSpec("month").setIndicator("月")
                .setContent(R.id.layout_sportMonth));
        tabHost.addTab(tabHost.newTabSpec("year").setIndicator("年")
                .setContent(R.id.layout_sportYear));

//        dayViewIndex = new SportDayViewIndex(act, rootView);
        dayView = (ViewPager) rootView.findViewById(R.id.layout_sportDay);
        weekViewIndex = new SportWeekViewIndex(act, rootView);
        monthViewIndex = new SportMonthViewIndex(act, rootView);
        yearViewIndex = new SportYearViewIndex(act, rootView);
    }
}
