package com.desay.iwan2.common.app.widget;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * PagerAdapter
 * 
 * @author 方奕峰
 * 
 */
public class CommonPagerAdapter extends PagerAdapter {

	private ArrayList<View> pageViews;

	public CommonPagerAdapter(ArrayList<View> pageViews) {
		this.pageViews = pageViews;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return pageViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		((ViewPager) container).removeView(pageViews.get(position));
		// super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View v = pageViews.get(position);
		((ViewPager) container).addView(v);
		return v;
	}
}
