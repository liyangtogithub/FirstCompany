package com.desay.iwan2.module.userinfo;

import com.desay.fitband.R;
import com.desay.iwan2.module.share.SportData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CityAdapter extends BaseAdapter
{
	private String[] list;
	private LayoutInflater mInflater;
	
	
	public CityAdapter(Context context,String[] list) {
		mInflater = LayoutInflater.from(context);
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.length;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null)
		{		
			convertView = mInflater.inflate(R.layout.login_item_dialog, null);
			holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		    holder.content.setText(list[position]); 
		return convertView;
	}

	public class ViewHolder {
		TextView content;
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}

}
