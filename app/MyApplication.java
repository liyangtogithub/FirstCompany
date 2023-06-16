package com.desay.iwan2.common.app;

import android.app.Application;
import android.content.Context;
import com.baidu.frontia.FrontiaApplication;
import com.desay.iwan2.common.app.option.ImageLoaderConfigFactory;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author 方奕峰
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initImageLoader(getApplicationContext());

        FrontiaApplication.initFrontiaApplication(this);//TODO 百度推送

        // MyService.start(this);
        // MyServiceConnection serviceConnection = new
        // MyServiceConnection(this);
        // MyService.bind(this, serviceConnection);
    }

    public void initImageLoader(Context context) {
        ImageLoader.getInstance().init(
                ImageLoaderConfigFactory.getDefaultConfig(context));
    }
}