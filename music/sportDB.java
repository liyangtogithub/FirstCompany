package com.desay.iwan2.module.music;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dolphin.tools.util.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class sportDB
{
	private SQLiteDatabase database;
	private String TAG = "sportDB";
	private Context context;
	Calendar calendarMinite = Calendar.getInstance();
	/* db name */
	private static final String databaseFilename = "sportDB.db";

	/* table name */
	public static final String TABLE_USERINFO = "UserInfo";
	public static final String TABLE_SPORTRECORD = "SportRecord";
	public static final String TABLE_SPORTROUTE = "SportRoute";
	public static final String TABLE_SPORTACHIEVE = "SportAchieve";
	public static final String TABLE_ACTIONMSG = "ActionMsg";
	public static final String TABLE_SPORTHEART = "SportHeart";
	public static final String TABLE_SPORTHANDLOOP = "sportHandLoop";
	public static final String TABLE_IWANMAC = "IwanMac";
	/* Table ID */
	public static final String ID_ID = "_id";
	public static final String ID_USERNAME = "_username";
	public static final String ID_IFUPLOAD = "_ifupload";
	/* UserInfo */
	public static final String ID_SHOWNAME = "_showname";
	public static final String ID_PASSWORD = "_password";
	public static final String ID_SEX = "_sex";
	public static final String ID_AGE = "_age";
	public static final String ID_HEIGHT = "_height";
	public static final String ID_WEIGHT = "_weight";
	public static final String ID_CITY = "_city";
	public static final String ID_HEADPHOTO = "_headphoto";
	/* SportRecord */
	public static final String ID_STARTTIME = "_starttime";
	public static final String ID_ENDTIME = "_endtime";
	public static final String ID_SPORTTYPE = "_sporttype";
	public static final String ID_DISTANCE = "_distance";
	public static final String ID_DURATIONTIME = "_durationtime";
	public static final String ID_CALORIE = "_calorie";
	public static final String ID_AVERAGE_SPEED = "_avgspeed";
	public static final String ID_MODE = "_mode";
	public static final String ID_FOOTNUM = "_footNum";
	/* SportRoute */
	public static final String ID_LONGITUDE = "_longitude";
	public static final String ID_LATITUDE = "_latitude";
	public static final String ID_INSTANT_SPEED = "_speed";
	/* SportAchieve */
	public static final String ID_CLASSIFY = "_classify";
	public static final String ID_ACHIEVETYPE = "_achievetype";
	public static final String ID_ACHIEVENAME = "_achievename";
	public static final String ID_ACHIEVERECORD = "_achieverecord";
	/* ActionMsg */
	public static final String ID_ACTIONSTATUS = "_status";
	public static final String ID_ACTIONTIME = "_actiontime";
	public static final String ID_ACTIONNAME = "_fname";
	/* SportHeart */
	public static final String ID_HEART = "_heart";

	/* IwanMac */
	public static final String ID_MAC = "_mac";
	public static final String ID_SYNTIME = "_syntime";
	public static final int TYPE_FASTEST = 0;
	public static final int TYPE_FARTHEST = 1;
	public static final int TYPE_LONGEST = 2;

	int medalName = -1;// 运动停止后，获得奖章的名字

	public sportDB(Context tcontext)
	{
		context = tcontext;
	}

	// //////////////////

	public void openOrCreateDB()
	{
		// TODO Auto-generated method stub
		if (database == null || !database.isOpen())
		{
			// String
			// DATABASE_PATH=android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
			// + "/wantsport/";
			// String filename = DATABASE_PATH + "/" + databaseFilename;
			database = context.openOrCreateDatabase(databaseFilename,
					Context.MODE_PRIVATE, null);
		}
	}

	public void closeDB()
	{
		// TODO Auto-generated method stub
		if (database != null && database.isOpen())
			database.close();
		database = null;
	}

	public boolean tableIsExist()
	{

		boolean result = false;
		Cursor cursor = null;
		try
		{
			openOrCreateDB();
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ "UserInfo" + "'; ";
			cursor = database.rawQuery(sql, null);
			if (cursor.moveToNext())
			{
				int count = cursor.getInt(0);
				if (count > 0)
				{
					result = true;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (cursor != null)
				cursor.close();
			closeDB();
		}
		return result;
	}

	public void CreateTable()
	{
		try
		{
			openOrCreateDB();

			database.execSQL("CREATE TABLE if not exists SportMusic ("
					+ "_id INTEGER PRIMARY KEY , "
					+ "_singer text, "
					+ "_musicId text," 
					+ "_time text, " 
					+ "_musicName text, "
					+ "_musicPath text, " 
					+ "_isself INTEGER, "
					+ "_albumKey text);");
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			closeDB();
		}
	}

	public void InsertMusic(HashMap<String, Object> ChangeValue)
	{
		Cursor c = null;
		try
		{
			openOrCreateDB();
			int ID = 0;
			ContentValues newValues = new ContentValues();
			Iterator iter = ChangeValue.entrySet().iterator();
			while (iter.hasNext())
			{
				Map.Entry entry = (java.util.Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if ("_singer".equals(key))
				{
					newValues.put("_singer", (String) val);
				} else if ("_time".equals(key))
				{
					newValues.put("_time", (String) val);
				} 
				else if ("_isself".equals(key))
				{
					newValues.put("_isself", (Integer) val);
				}
				else if ("_musicName".equals(key))
				{
					newValues.put("_musicName", (String) val);
				} else if ("_musicPath".equals(key))
				{
					newValues.put("_musicPath", (String) val);
				} else if ("_albumKey".equals(key))
				{
					newValues.put("_albumKey", (String) val);
				} else if ("_musicId".equals(key))
				{
					newValues.put("_musicId", (String) val);
					ID = Integer.parseInt((String) val);
				}
			}
			c = database.query("SportMusic", null, "_musicId=?",
					new String[] { ID + "" }, null, null, null);
			if (c.getCount() == 0)
			{
				database.insert("SportMusic", null, newValues);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (c != null)
				c.close();
			closeDB();
		}

	}

	public void DelMusic(int musicId)
	{
		try
		{
			database.delete("SportMusic", "_musicId=?", new String[] { musicId
					+ "" });
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
		}
	}

	public List<Mp3Info> GetMusic()
	{

		Cursor c = null;
		List<Mp3Info> list = new ArrayList<Mp3Info>();
		try
		{
			openOrCreateDB();
			c = database
					.query("SportMusic", null, null, null, null, null, null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++)
			{
				Mp3Info m = new Mp3Info();
				m.setId(Integer.parseInt(c.getString(c
						.getColumnIndex("_musicId"))));
				m.setAlbumkey(c.getString(c.getColumnIndex("_albumKey")));
				m.setMusicName(c.getString(c.getColumnIndex("_musicName")));
				m.setMusicPath(c.getString(c.getColumnIndex("_musicPath")));
				m.setSinger(c.getString(c.getColumnIndex("_singer")));
				m.setTime(Integer.parseInt(c.getString(c
						.getColumnIndex("_time"))));
				list.add(m);
				c.moveToNext();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (c != null)
				c.close();
			closeDB();
		}
		return list;
	}
	public List<Mp3Info> getSelfMusic()
	{
		
		Cursor c = null;
		List<Mp3Info> list = null;
		try
		{
			openOrCreateDB();
			c = database
					.query("SportMusic", null, "_isself!=?", new String[] { "0" }, null,
							null, null);
			if (c!=null&&c.getCount()>0)
			{
				list = new ArrayList<Mp3Info>();
				c.moveToFirst();
				for (int i = 0; i < c.getCount(); i++)
				{
					Mp3Info m = new Mp3Info();
					m.setMusicName(c.getString(c.getColumnIndex("_musicName")));
					LogUtil.i("getSelfMusic()  _musicName=="+c.getString(c.getColumnIndex("_musicName")));
					list.add(m);
					c.moveToNext();
				}
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (c != null)
				c.close();
			closeDB();
		}
		return list;
	}

}
