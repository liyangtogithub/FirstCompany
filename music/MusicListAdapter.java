package com.desay.iwan2.module.music;

import java.util.List;

import com.desay.fitband.R;

import dolphin.tools.util.LogUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {

	private Context myCon;
	String[] sleepNameArray = null;
	String[] sleepNoticeArray = null;
	LayoutInflater inflate_item;
	public MusicListAdapter(Context con, String sleepNameArray[]) {	
		this.sleepNameArray=sleepNameArray;
		this.myCon=con;
		sleepNoticeArray = con.getResources().getStringArray(R.array.music_notice_array);
}

	public int getCount() {
		return sleepNameArray.length;
	}
	
	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		if(convertView==null){
			holder=new ViewHolder();
			convertView =  LayoutInflater.from(myCon).inflate(R.layout.music_list_item,null);		
		holder.music_name = (TextView) convertView.findViewById(R.id.music_name);
		holder.music_notice = (TextView) convertView.findViewById(R.id.music_notice);
		
		convertView.setTag(holder);}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		if (position==0 ){
			holder.music_name.setText(sleepNameArray[0]);
		    holder.music_notice.setText(sleepNoticeArray[0]);
		}
		else if (position==1 ) {
			holder.music_name.setText(sleepNameArray[1]);
			holder.music_notice.setText(sleepNoticeArray[1]);
		}
		else if (position==2 ) {
			holder.music_name.setText(sleepNameArray[2]);
			holder.music_notice.setText(sleepNoticeArray[2]);
		}
		else if (position==3 ) {
			holder.music_name.setText(sleepNameArray[3]);
			holder.music_notice.setText(sleepNoticeArray[3]);
		}
		else {
			holder.music_name.setText(sleepNameArray[0]);
			holder.music_notice.setText(sleepNoticeArray[0]);
		}
		convertView.setBackgroundResource(R.drawable.music_list_rect_selector);
		
		return convertView;
	}
	
	public static class ViewHolder{
		TextView music_name;
		TextView music_notice;
		//ImageView img;
	}
}
