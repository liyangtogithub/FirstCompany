package com.desay.iwan2.module.share;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.desay.fitband.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class SportData
{
	public static final int TOAST_SEX_NULL = 3;
	public static final int TOAST_BIRTHDAY_NULL = 4;
	public static final int TOAST_HEIGHT_NULL = 5;
	public static final int TOAST_WEIGHT_NULL = 6;
	public static final int TOAST_CITY_NULL = 7;
	public static final int TOAST_HEIGHT_ERROR = 8;
	public static final int TOAST_WEIGHT_ERROR = 9;
	public static final int NO_NET = 2;
	public static final int LOAD_OK = 3;
	public static final int LOAD_FAIL = 4;
	public static final int OPEN_THREAD = 5;
	public static final int SEND_FAIL = 6;
	public static final int REG_SEND = 7;
	public static final int REG_OK = 8;
	public static final int TOAST_USERNAME_NULL = 9;
	public static final int TOAST_PASSWORD_NULL = 10;
	public static final int TOAST_PASSWORD_INVALID = 11;
	public static final int TOAST_EMAIL_NULL = 12;
	public static final int EMAIL_SEND = 13;
	public static final int EMAIL_OK = 14;
	public static final int ANIMATION_END = 15;
	public static final int THRID_LOAD = 16;
	public static final int THRID_LOAD_FAIL = 17;
	public static final int SEND_OK = 4;

	public static int LISTENTERTIME = 20;
	public static double SPACETIME = 1.0 / 3;
	public static double HEART_SPACETIME = 1.0;
	public static int COLLECTTIME = 20;
	public static double max_walk = 8;
	public static double max_run = 16;
	public static double max_drive = 35;
	public static final int WINDOW_REG = 0; // 当前是登录界面
	public static final int WINDOW_LOGIN = 1;// 当前是注册界面
	public static final int WINDOW_EMAIL = 2;// 当前是找回密码界面


	public static final int ICON_SPORTTYPE = 0;
	public static final int ICON_SEX = 1;
	public static final int ICON_PHOTOSELECT = 2;
	public static final int ICON_SLIDEPAGE = 3;
	public static final int ICON_MEDAL = 4;
	public static final int ICON_NOMEDAL = 5;

	public static final int REQUESTCODE_NONE = 0;
	public static final int REQUESTCODE_CITY = 1;
	public static final int REQUESTCODE_HEADPHOTO = 2;
	public static final int REQUESTCODE_PHOTOGRAPH = 3;
	public static final int REQUESTCODE_PHOTOZOOM = 4;
	public static final int REQUESTCODE_PHOTOSAVE = 5;
	public static final String IMAGE_UNSPECIFIED = "image/*";

	public static final String LOAD = "load";
	public static final String UPLOAD = "upload";
	public static final String UNUPLOAD = "unupload";

	public static final int LOADSTATUS_LOGIN = 0;
	public static final int LOADSTATUS_LOGINOUT = 1;
	public static final int LOADSTATUS_SYNC = 2;
	public static final int LOADSTATUS_SYNCFAIL = 3;
	public static int msgnum = 0;

	public static String bytesToHexString(byte[] src)
	{
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0)
		{
			return null;
		}
		for (int i = 0; i < src.length; i++)
		{
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2)
			{
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString)
	{
		if (hexString == null || "".equals(hexString))
		{
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++)
		{
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c)
	{
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static Drawable getIconDrawable(Context context, int position,
			int type)
	{
		int resID = 0;
		switch (type)
		{
			case ICON_SEX:
				resID = R.array.icons_sex;
				break;
			case ICON_PHOTOSELECT:
				resID = R.array.icons_photoselect;
				break;
		}
		TypedArray icons = context.getResources().obtainTypedArray(resID);
		Drawable drawable = icons.getDrawable(position);
		icons.recycle();
		return drawable;
	}

	public static Bitmap drawableToBitmap(Drawable drawable)
	{

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	

	public static Bitmap toRoundBitmap(Bitmap bitmap)
	{
		// 创建一个和原始图片一样大小位图
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height)
		{
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else
		{
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	public static void sendBroadCast(Context context, String action)
	{
		Intent scanIntent = new Intent();
		scanIntent.setAction(action);
		context.sendBroadcast(scanIntent);
	}

}
