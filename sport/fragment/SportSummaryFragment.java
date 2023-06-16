package com.desay.iwan2.module.sport.fragment;

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
import com.desay.iwan2.module.sport.SportActivity;
import com.desay.iwan2.module.sport.server.SportAimServer;
import com.desay.iwan2.module.sport.view.SportSummaryView;
import dolphin.tools.util.LogUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 方奕峰
 */
public class SportSummaryFragment extends BaseFragment {

    private Activity act;

    private ViewPager viewPager;
    private SleepSummaryPagerAdapter pagerAdapter;
    private Date[] dateRange;

	private SportAimServer aimServer;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable{
        act = getActivity();

        Date date = (Date) ((SportActivity) act).bundle.getSerializable(SportActivity.KEY);
        //Calendar cl = Calendar.getInstance();
        //cl.set(2014, 5, 24, 0, 0);
        //date = cl.getTime();

        View view = inflater.inflate(R.layout.sport_summary_fragment, null);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateRange[0]);
                calendar.add(Calendar.DATE, position);
                ((SportActivity) act).setDate(calendar.getTime());
                super.onPageSelected(position);
            }
        });

        try {
            aimServer = new SportAimServer(getActivity());
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
        pagerAdapter = new SleepSummaryPagerAdapter(day);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem((int) ((date.getTime() - dateRange[0].getTime()) / SystemContant.millisecondInDay));

        return view;
    }

    private class SleepSummaryPagerAdapter extends PagerAdapter {

        public int size;

        public SleepSummaryPagerAdapter(int size) {
            this.size = size;
            LogUtil.i("size=" + size);
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
//            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            SportSummaryView v = new SportSummaryView((SportActivity) act,aimServer);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateRange[0]);
            calendar.add(Calendar.DATE, position);
            try {
                v.initData(calendar.getTime());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            container.addView(v);

            return v;
        }
    }

}
