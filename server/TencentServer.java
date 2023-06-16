package com.desay.iwan2.common.server;

import android.content.Context;
import android.content.SharedPreferences;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.http.entity.request.ThirdBinddata;
import com.desay.iwan2.common.api.http.entity.response.QqLoginResponse;
import com.desay.iwan2.util.SharePreferencesUtil;
import com.tencent.tauth.Tencent;
import dolphin.tools.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created by 方奕峰 on 14-12-18.
 */
public class TencentServer {
    private Context context;
    //    private OtherServer otherServer;
    public final String QQ_INFO = "qq_info";

    public TencentServer(Context context) {
        this.context = context;
//        otherServer = new OtherServer(context);
    }

    public Tencent getTencentInstance() throws IOException, XmlPullParserException, SQLException {
//        String AppId = getQqAppId(context);
        String token = getToken();
        String expiresIn = getExpiresIn() + "";
        String openId = getOpenId();
//
//        Tencent mTencent = Tencent.createInstance(AppId, context);
//        if (!StringUtil.isBlank(token) && !StringUtil.isBlank(expiresIn) && !StringUtil.isBlank(openId)) {
//            mTencent.setAccessToken(token, expiresIn);
//            mTencent.setOpenId(openId);
//        }
//
//        return mTencent;

        return getTencentInstance(openId, token, expiresIn);
    }

    public Tencent getTencentInstance(String openId, String token, String expiresIn) throws IOException, XmlPullParserException {
        String appId = getQqAppId();

        Tencent mTencent = Tencent.createInstance(appId, context);
        if (!StringUtil.isBlank(token) && !StringUtil.isBlank(expiresIn) && !StringUtil.isBlank(openId)) {
            mTencent.setAccessToken(token, expiresIn);
            mTencent.setOpenId(openId);
        }

        return mTencent;
    }

    public void save(String openId, String token, String expiresIn) {
        QqLoginResponse qqInfo = new QqLoginResponse();
        qqInfo.setAccess_token(token);
        qqInfo.setOpenid(openId);
        qqInfo.setExpires_in(expiresIn);

//        otherServer.createOrUpdate(null, Other.Type.qq, JSON.toJSONString(qqInfo));

//        SharedPreferences preferences = SharePreferencesUtil.getSharedPreferences(context);
//        preferences.edit().putString(QQ_INFO, JSON.toJSONString(qqInfo)).commit();
        save(JSON.toJSONString(qqInfo));
    }

    public void save(String info) {
        SharedPreferences preferences = SharePreferencesUtil.getSharedPreferences(context);
        preferences.edit().putString(QQ_INFO, info).commit();
    }

    public QqLoginResponse getQqInfo() {
//        Other other = otherServer.getOther(null, Other.Type.qq);

//        return other == null ? null : JSON.parseObject(other.getValue(), QqLoginResponse.class);

        String str = SharePreferencesUtil.getSharedPreferences(context).getString(QQ_INFO, null);
        if (StringUtil.isBlank(str)) {
            return null;
        } else {
            return JSON.parseObject(str, QqLoginResponse.class);
        }
    }

    public String getOpenId() {
        QqLoginResponse qqInfo = getQqInfo();
        if (qqInfo == null)
            return null;

        return qqInfo.getOpenid();
    }

    public String getToken() throws SQLException {
        QqLoginResponse qqInfo = getQqInfo();
        if (qqInfo == null)
            return null;

        return qqInfo.getAccess_token();
    }

    public String getExpiresIn() throws SQLException {
        QqLoginResponse qqInfo = getQqInfo();
        if (qqInfo == null)
            return null;

        return qqInfo.getExpires_in();
    }

    public String getQqAppId() throws IOException, XmlPullParserException {
        String appId = null;
        InputStream inputStream = context.getAssets().open("ShareSDK.xml");
        XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
        xmlPullParserFactory.setNamespaceAware(true);
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setInput(inputStream, "utf-8");
        for (int eventtType = parser.getEventType(); eventtType != XmlPullParser.END_DOCUMENT; eventtType = parser.next()) {
            switch (eventtType) {
                case XmlPullParser.START_TAG:
//                    LogUtil.i(parser.getName());
                    if ("QQ".equalsIgnoreCase(parser.getName())) {
                        appId = parser.getAttributeValue(null, "AppId");
                    }
                    break;
                case XmlPullParser.END_TAG:
//                    LogUtil.i(parser.getName());
                    break;
                default:
//                    LogUtil.i(pullParser.getName());
                    break;
            }
        }
        return appId;
    }

    public ThirdBinddata getBinddata() throws SQLException {
        ThirdBinddata binddata = null;
        QqLoginResponse qqInfo = getQqInfo();
        if (qqInfo != null) {
            binddata = new ThirdBinddata();
            try {
                binddata.setAppid(getQqAppId());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            binddata.setGtype("01");
            binddata.setOpenid(qqInfo.getOpenid());
            binddata.setTokenkey(qqInfo.getAccess_token());
        }

        return binddata;
    }
}
