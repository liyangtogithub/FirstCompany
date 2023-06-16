package com.desay.iwan2.module.userinfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import android.os.Handler;
import android.os.Message;

public class ImageHandle
{
	String drawable = "";

	public String loadDrawable(final String imageUrl,
			final ImageCallback imageCallback)
	{
		final Handler handler = new Handler()
		{
			public void handleMessage(Message message)
			{
				imageCallback.imageLoaded((String) message.obj);
			}
		};
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					drawable = loadImageFromUrl(imageUrl);
					Message message = handler.obtainMessage(0, drawable);
					handler.sendMessage(message);
				} catch (Exception e)
				{
					e.printStackTrace();
					Message message = handler.obtainMessage(0, "");
					handler.sendMessage(message);
				}
			}
		}.start();
		return drawable;
	}

	public static String loadImageFromUrl(String url) throws Exception
	{
		URL m;
		InputStream i = null;
		String headstring = "";
		try
		{
			m = new URL(url);
			i = (InputStream) m.getContent();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1204 * 1024];
			int len = 0;
			while ((len = i.read(buffer)) != -1)
			{
				outStream.write(buffer, 0, len);
			}
			i.close();
			try
			{
				headstring = new String(Base64.encodeBase64(outStream.toByteArray()), "UTF-8");
			} catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			// headstring=SportData.bytesToHexString(outStream.toByteArray());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return headstring;
	}

	public interface ImageCallback
	{
		public void imageLoaded(String imageString);
	}

}
