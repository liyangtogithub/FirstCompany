package com.desay.iwan2.module.music;

import com.desay.fitband.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MusicSetAdapter extends BaseAdapter
{
	private LayoutInflater mInflater = null;
	Context context = null;
	String[] dataArray = null;
	int musicTimeIndex = 0;
	 MusicSetAdapter(Context context,String[] dataArray)
	{
		this.context = context;
		this.dataArray = dataArray;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount()
	{
		return dataArray.length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder1 holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder1();
			convertView = mInflater.inflate(R.layout.music_set_item, null);
			holder.tv_set_time = (TextView) convertView.findViewById(R.id.tv_set_time);
			holder.iv_set_check = (ImageView) convertView.findViewById(R.id.iv_set_check);
			holder.iv_set_nocheck = (ImageView) convertView.findViewById(R.id.iv_set_nocheck);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder1) convertView.getTag();
		}
		holder.tv_set_time.setText((String) dataArray[ position]);
		if ( musicTimeIndex == position)
		{
			holder.iv_set_check.setVisibility(View.VISIBLE);
		}else {
			holder.iv_set_check.setVisibility(View.GONE);
		}
		return convertView;
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	static class ViewHolder1
	{
		public TextView  tv_set_time;
		public ImageView iv_set_check;
		public ImageView iv_set_nocheck;
	}

	public void setMusicIndex(int musicTimeIndex)
	{
		this.musicTimeIndex = musicTimeIndex;
	}
}