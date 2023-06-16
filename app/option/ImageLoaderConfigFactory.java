package com.desay.iwan2.common.app.option;

import android.content.Context;
import android.graphics.Bitmap;
import com.desay.fitband.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * 
 * @author 方奕峰
 *
 */
public class ImageLoaderConfigFactory {

	private static ImageLoaderConfiguration defaultImageLoaderConfiguration;

	public synchronized static final ImageLoaderConfiguration getDefaultConfig(
			Context context) {
		if (defaultImageLoaderConfiguration == null) {
			defaultImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
					context)
					.discCache(
							new UnlimitedDiscCache(
									StorageUtils
											.getOwnCacheDirectory(
													context,
													context.getString(R.string.app_name)
															+ "/cache/img")))
					.defaultDisplayImageOptions(
							new DisplayImageOptions.Builder()
									// .showStubImage(R.drawable.ic_stub)
									// .showImageForEmptyUri(R.drawable.ic_empty)
									// .showImageOnFail(R.drawable.ic_error)
									.cacheInMemory()
									.cacheOnDisc()
									.imageScaleType(
											ImageScaleType.EXACTLY_STRETCHED)
									.bitmapConfig(Bitmap.Config.ARGB_8888)
									.displayer(new FadeInBitmapDisplayer(300))
									.resetViewBeforeLoading().build()).build();
		}
		return defaultImageLoaderConfiguration;
	}
}
