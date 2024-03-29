package com.desay.iwan2.module.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.desay.fitband.R;
import com.desay.iwan2.module.music.AlbumImageLoader.ImageCallback;

import dolphin.tools.util.LogUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MusicLibListAdapter extends BaseAdapter
{
	private Context myCon;
	private List<Mp3Info> list;
	private int pos = -1;
	private sportDB db;
	LayoutInflater inflate_item;
	AlbumImageLoader albumImageLoader;
	private List<Mp3Info> myList = new ArrayList<Mp3Info>();
	private HashMap<Integer, Mp3Info> choseinfo = new HashMap<Integer, Mp3Info>();
	private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
	String[] sleepArray = null;
	public MusicLibListAdapter(Context con, List<Mp3Info> list,
			String[] sleepArray)
	{
		this.myCon = con;
		this.list = list;
		this.sleepArray=sleepArray;
		db = new sportDB(myCon);
		albumImageLoader = new AlbumImageLoader(myCon);
		inflate_item = LayoutInflater.from(con);
		for (int i = 0; i < list.size(); i++)
		{
			isSelected.put(i, false);
			// choseinfo.put(i, list.get(i));
		}
		myList = db.GetMusic();
		for (int j = 0; j < list.size(); j++)
		{
			for (int i = 0; i < myList.size(); i++)
			{
				if (list.get(j).getId() == myList.get(i).getId())
				{
					isSelected.put(j, true);
					choseinfo.put(j, myList.get(i));
				}
			}
		}
	}

	public int getCount()
	{
		return list.size();
	}

	public Object getItem(int position)
	{
		return position;
	}

	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 获取多选的数据
	 * 
	 * @return
	 */
	public HashMap<Integer, Mp3Info> getchoseMap()
	{
		return choseinfo;
	}

	public boolean getChecked(int pos)
	{
		if (choseinfo.containsKey(pos))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void CancelAll()
	{
		if (choseinfo != null)
		{
			isSelected.clear();
			for (int i = 0; i < list.size(); i++)
			{
				isSelected.put(i, false);
				choseinfo.remove(i);
			}
			this.notifyDataSetChanged();
		}
	}

	public void SelecteAll()
	{
		for (int i = 0; i < list.size(); i++)
		{
			isSelected.put(i, true);
			choseinfo.put(i, list.get(i));
		}
		this.notifyDataSetChanged();
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		Mp3Info m = list.get(position);
		if (convertView == null)
		{
			convertView = inflate_item.inflate(R.layout.music_libchoose_item, null);
			holder = new ViewHolder();
			holder.tv_music = (TextView) convertView.findViewById(R.id.music_title);
			holder.myCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if (m.getMusicName().length() > 24)
		{
			try
			{
				String musicTitle = bSubstring(m.getMusicName().trim(), 24);
				holder.tv_music.setText(musicTitle);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			if (position==0 )
				holder.tv_music.setText(sleepArray[0]);
			else if (position==1 ) {
				holder.tv_music.setText(sleepArray[1]);
			}
			else if (position==2 ) {
				holder.tv_music.setText(sleepArray[2]);
			}
			else if (position==3 ) {
				holder.tv_music.setText(sleepArray[3]);
			}
			else {
				holder.tv_music.setText(sleepArray[0]);
			}
		}
		holder.myCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						// isSelected.remove(position);
						if (isChecked)
						{
							LogUtil.i("isChekced:" + isChecked
									+ ",Position:" + position);
							if (!choseinfo.containsKey(position))
							{
								choseinfo.put(Integer.valueOf(position),
										list.get(position));
								isSelected.put(position, true);
							}
						}
						else
						{
							choseinfo.remove(Integer.valueOf(position));
							isSelected.put(position, false);
							db.DelMusic(list.get(Integer.valueOf(position))
									.getId());
						}
					}
				});
		holder.myCheckBox.setChecked(isSelected.get(position));

		return convertView;
	}

	public void setItemIcon(int position)
	{
		pos = position;
	}

	static class ViewHolder
	{
		TextView tv_music;
		CheckBox myCheckBox;
	}

	/**
	 * ʱ���ʽת��
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time)
	{

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 字符串裁剪
	 * 
	 * @param s
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String bSubstring(String s, int length) throws Exception
	{

		byte[] bytes = s.getBytes("Unicode");
		int n = 0; // 表示当前的字节数
		int i = 2; // 要截取的字节数，从第3个字节开始
		for (; i < bytes.length && n < length; i++)
		{
			// 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
			if (i % 2 == 1)
			{
				n++; // 在UCS2第二个字节时n加1
			}
			else
			{
				// 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
				if (bytes[i] != 0)
				{
					n++;
				}
			}
		}
		// 如果i为奇数时，处理成偶数
		if (i % 2 == 1)

		{
			// 该UCS2字符是汉字时，去掉这个截一半的汉字
			if (bytes[i - 1] != 0) i = i - 1;
			// 该UCS2字符是字母或数字，则保留该字符
			else
				i = i + 1;
		}

		return new String(bytes, 0, i, "Unicode");
	}

}
