package com.desay.iwan2.common.api.http.entity.callback;

import android.app.Activity;
import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.entity.request.RequestEntity;
import com.desay.iwan2.common.api.http.entity.response.ResponseEntity;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import dolphin.tools.util.CompressUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by 方奕峰 on 14-6-17.
 */
public class MyJsonHttpResponseHandler extends TextHttpResponseHandler {
    private Context context;

    public MyJsonHttpResponseHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseBody) {
        try {
            LogUtil.i1("接收数据(http) url=" + getRequestURI());
            if (!StringUtil.isBlank(responseBody)) {
                String result = new String(StringEscapeUtils
                        .unescapeJavaScript(new String(CompressUtil
                                .unGZip(Base64.decodeBase64(responseBody.getBytes())))));
                LogUtil.i1(result);
                ResponseEntity responseEntity = JSON.parseObject(result, ResponseEntity.class);
                if ("001".equals(responseEntity.getStateCode())) {
                    onSuccess(context, responseEntity.getData());
                } else {
                    onFailure(context, responseEntity.getStateCode(), responseEntity.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onSuccess(statusCode, headers, responseBody);
    }

    @Override
    public void onFailure(final int statusCode, Header[] headers, String responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);
        LogUtil.i1("接收数据(http) url=" + getRequestURI());
        if (error == null || error.getMessage() == null) {
            LogUtil.i1("http exception");
        } else
            LogUtil.i1(error.getMessage());

        if (context instanceof Activity) {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.shortShow(context, "网络请求失败，code=" + statusCode);
                    }
                });
            }
        }
    }

    public void onSuccess(Context context, String str) {

    }

    public void onFailure(final Context context, String stateCode, final String msg) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.shortShow(context, msg);
                }
            });
        }
    }
}
