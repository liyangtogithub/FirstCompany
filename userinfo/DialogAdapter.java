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

public class DialogAdapter extends BaseAdapter
{
	private String[] list;
	private Context context;
	private LayoutInflater mInflater;
	private int icontype;//对话框类型
	
	
	public DialogAdapter(Context tcontext,String[] tlist,int icon_type) {
		context = tcontext;
		mInflater = LayoutInflater.from(context);
		list = tlist;
		icontype = icon_type;
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
			holder.icon =  (ImageView) convertView.findViewById(R.id.iv_icon);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		    holder.content.setText(list[position]); 
		    holder.icon.setImageDrawable(SportData.getIconDrawable(context,position,icontype));
		return convertView;
	}

	public class ViewHolder {
		TextView content;
		ImageView icon;
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}

}
