package com.desay.iwan2.common.api.http;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.http.entity.request.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import dolphin.tools.util.CompressUtil;
import dolphin.tools.util.LogUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

/**
 * 方奕峰
 */
public class Api1 {
    private static AsyncHttpClient httpClient;

    public static synchronized AsyncHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new AsyncHttpClient();
            httpClient.setTimeout(180000);
        }
        return httpClient;
    }

    private Api1() {
    }

    public static void executeCommon(String url,
                                     AsyncHttpResponseHandler responseHandler) {
        getHttpClient().post(url, responseHandler);
    }

    public static void executeCommon(Context context, String url, String strParam,
                                     AsyncHttpResponseHandler responseHandler) {
//        LogUtil.i(strParam);
//        byte[] gzip = CompressUtil.gZip(StringEscapeUtils
//                .escapeJavaScript(strParam).getBytes());
//        String str = new String(Base64.encodeBase64(gzip));
        String str = strParam;
        StringEntity entity = null;
        try {
            LogUtil.i("发送数据2(http)");
            LogUtil.i1(str);
            entity = new StringEntity(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.e(e.getMessage());
        }

        getHttpClient().post(context, url, entity, null, responseHandler);
    }

    public static void executeCommon(Context context, String url, Object obj,
                                     AsyncHttpResponseHandler responseHandler) {
        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setSysdata(RequestEntity.generateSysData(context));
//        requestEntity.setRqdata(obj);
        String str = JSON.toJSONString(obj);
        LogUtil.i("发送数据1(http),url=" + url);
        LogUtil.i1(str);
        requestEntity.setRqdata(new String(Base64.encodeBase64(CompressUtil.gZip(StringEscapeUtils
                .escapeJavaScript(str).getBytes()))));
//        requestEntity.setRqdata(Base64.encodeBase64String(CompressUtil.gZip(StringEscapeUtils
//                .escapeJavaScript(str).getBytes())));
        executeCommon(context, url, JSON.toJSONString(requestEntity), responseHandler);
    }

    /////////////////////////////////////////////////

    //    private static final String ADDRESS = "http://10.5.8.19:8000";
//    private static final String ADDRESS = "http://apis.iwan2.upcoder.net";
//    private static final String ADDRESS = "http://59.33.252.107:8000";
//    private static final String ADDRESS_0 = "http://10.5.8.70:2978";
    private static final String ADDRESS_0 = "http://59.33.252.109:2978";
    private static final String ADDRESS_1 = "http://port.desay.com:7999";
//    private static final String ADDRESS_1 = "http://121.15.245.100:7999";
    private static final String SERVICE = "/Service";
//    private static final String SERVER = ADDRESS_1 + SERVICE;

//    /**
//     * 检查更新
//     *
//     * @param responseHandler
//     */
//    public static void test(AsyncHttpResponseHandler responseHandler) {
//        executeCommon("http://10.5.8.66:804/upgradeInfo",
//                responseHandler);
//    }

//    public static void executeUrl(Context context, String url) {
//        Uri uri = Uri.parse("http://care.desay.com/apk_update/SleepAndHealth.apk");
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        context.startActivity(intent);
//    }

    private static String getServerChannel(Context context) {
        String serverUrl = null;
        String customType = context.getString(R.string.customer_type_code);
        if ("10".equals(customType)) {
            serverUrl = ADDRESS_0 + SERVICE;
        } else {
            serverUrl = ADDRESS_1 + SERVICE;
        }
        return serverUrl;
    }

    /**
     * 注册
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void register(Context context, Register param,
                                AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/userinfoDatas/register", param,
                responseHandler);
    }

    /**
     * 登录
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void login(Context context, Login param,
                             AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/userinfoDatas/login", param,
                responseHandler);
    }

    /**
     * 登录
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void login1(Context context, Login param,
                             AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/userinfoDatas/loginAll", param,
                responseHandler);
    }

    /**
     * 修改用户信息
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitUserInfo(Context context, CommitUserInfo param,
                                      AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/userinfoDatas/info", param,
                responseHandler);
    }

    /**
     * 提交睡眠原始数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitOrignalData(Context context, CommitOrignalData param,
                                         AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/sleepDatas/uploadBaseSleep", param,
                responseHandler);
    }

    /**
     * 提交睡眠数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitSleepData(Context context, CommitSleep param,
                                       AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/sleepDatas/uploadBaseSleep", param,
                responseHandler);
    }

    /**
     * 加载睡眠数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadSleepData(Context context, LoadSleepData param,
                                     AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/sleepDatas/downloadSleep", param,
                responseHandler);
    }

    /**
     * 提交运动数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitSportData(Context context, CommitSportData param,
                                       AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/sportsDatas/upload", param,
                responseHandler);
    }

    /**
     * 加载运动数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadSportData(Context context, LoadSportData param,
                                     AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/sportsDatas/download", param,
                responseHandler);
    }

    /**
     * 提交MAC
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitMac(Context context, CommitMac param,
                                 AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/uploadMAC", param,
                responseHandler);
    }

    /**
     * 加载MAC
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadMac(Context context, Common param,
                               AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/downloadMAC", param,
                responseHandler);
    }

    /**
     * 提交设置
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitSetting(Context context, CommitSetting param,
                                     AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/uploadSET", param,
                responseHandler);
    }

    /**
     * 加载设置
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadSetting(Context context, Common param,
                                   AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/downloadSET", param,
                responseHandler);
    }

    /**
     * 提交目标设置
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitAim(Context context, CommitAim param,
                                 AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/uploadAIM", param,
                responseHandler);
    }

    /**
     * 加载目标设置
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadAim(Context context, Common param,
                               AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/downloadAIM", param,
                responseHandler);
    }

    /**
     * 提交第三方绑定
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitSocialBind(Context context, CommitSocialBind param,
                                        AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/goldDatas/bind", param,
                responseHandler);
    }

    /**
     * 提交第三方分享
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void commitSocialShare(Context context, CommitSocialShare param,
                                         AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/goldDatas/share", param,
                responseHandler);
    }

    /**
     * 加载金币
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadCoin(Context context, LoadCoin param,
                                AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/goldDatas/gain", param,
                responseHandler);
    }

    /**
     * 加载业务数据
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadBizData(Context context, LoadBizData param,
                                   AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/othersDatas/downloadAll", param,
                responseHandler);
    }

    /**
     * 加载版本
     *
     * @param context
     * @param param
     * @param responseHandler
     * @throws UnsupportedEncodingException
     */
    public static void loadVersion(Context context, LoadVersion param,
                                   AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/othersDatas/version", param,
                responseHandler);
    }

    /**
     * 提交NC号
     *
     * @param context
     * @param param
     * @param responseHandler
     */
    public static void commitNc(Context context, CommitNc param,
                                AsyncHttpResponseHandler responseHandler) {
        executeCommon(context, getServerChannel(context) + "/setDatas/uploadNC", param,
                responseHandler);
    }
}
