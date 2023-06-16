package com.desay.iwan2.module.sleep.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.server.DayServer;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sleep.view.SleepSummaryView;
import dolphin.tools.util.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * @author 方奕峰
 */
public class SleepSummaryFragment extends BaseFragment {

    private Activity act;

    private ViewPager viewPager;
    private SleepSummaryPagerAdapter pagerAdapter;
    private Date[] dateRange;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
//        LogUtil.i("1111111111111111111111a="+System.currentTimeMillis());
        act = getActivity();

        Date date = (Date) ((SleepActivity) act).bundle.getSerializable(SleepActivity.KEY);

        View view = inflater.inflate(R.layout.sleep_summary_fragment, null);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateRange[0]);
                calendar.add(Calendar.DATE, position);
                ((SleepActivity) act).setDate(calendar.getTime());
                super.onPageSelected(position);
            }
        });
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        Date date1 = calendar.getTime();
//        calendar.add(Calendar.YEAR, -1);
//        Date date0 = calendar.getTime();
//        dateRange = new Date[]{date0, date1};
        try {
            dateRange = new DayServer(act).getRange(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        LogUtil.i("1111111111111111111111b="+System.currentTimeMillis());
        if (dateRange[0] == null || date.before(dateRange[0])) {
            dateRange[0] = date;
        } else if (dateRange[1] == null || date.after(dateRange[1])) {
            dateRange[1] = date;
        }
        int day = (int) ((dateRange[1].getTime() - dateRange[0].getTime()) / SystemContant.millisecondInDay) + 1;
        pagerAdapter = new SleepSummaryPagerAdapter(day);
//        LogUtil.i("1111111111111111111111c="+System.currentTimeMillis());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem((int) ((date.getTime() - dateRange[0].getTime()) / SystemContant.millisecondInDay));
//        LogUtil.i("1111111111111111111111d="+System.currentTimeMillis());

        return view;
    }

    private class SleepSummaryPagerAdapter extends PagerAdapter {
        Vector<SleepSummaryView> l = new Vector<SleepSummaryView>();

        public int size;

        public SleepSummaryPagerAdapter(int size) {
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
//            container.removeViewAt(position);

            SleepSummaryView v = (SleepSummaryView) object;
            container.removeView(v);
            v.dispose();
            l.add(v);
//            v.clearFocus();

//            container.removeView((View) object);

//            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LogUtil.i("instantiateItem,position=" + position);

//            LogUtil.i("1111111111111111111111e="+System.currentTimeMillis());
            SleepSummaryView v =null;
            if (l.size() > 0) {
                v = l.get(l.size()-1);
                l.remove(v);
            }else {
                v = new SleepSummaryView((SleepActivity) act);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateRange[0]);
            calendar.add(Calendar.DATE, position);
//            LogUtil.i("1111111111111111111111f="+System.currentTimeMillis());
            try {
                v.initData(calendar.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            LogUtil.i("1111111111111111111111g="+System.currentTimeMillis());
            container.addView(v);
//            LogUtil.i("1111111111111111111111h="+System.currentTimeMillis());

            return v;
        }
    }
}
