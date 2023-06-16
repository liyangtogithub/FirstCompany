package com.desay.iwan2.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 方奕峰 on 14-7-18.
 */
public class SharePreferencesUtil {
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
    }
}
