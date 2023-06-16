package com.desay.iwan2.common.server.push;

import android.content.Context;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import dolphin.tools.util.LogUtil;

import java.util.List;

/**
 * Created by jacky on 15/2/10.
 */
public class PushMsgReceiver extends FrontiaPushMessageReceiver{
    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;

        LogUtil.i(responseString);
    }

    @Override
    public void onUnbind(Context context, int i, String s) {
        LogUtil.i("onUnbind "+s);
    }

    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode + " sucessTags="
                + sucessTags + " failTags=" + failTags + " requestId="
                + requestId;
        LogUtil.i(responseString);
    }

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode + " sucessTags="
                + sucessTags + " failTags=" + failTags + " requestId="
                + requestId;

        LogUtil.i(responseString);
    }

    @Override
    public void onListTags(Context context, int errorCode,
                           List<String> tags, String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags=" + tags;
        LogUtil.i(responseString);
    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {
        String responseString = "onMessage message=" + message + " customContentString=" + customContentString;
        LogUtil.i(responseString);
    }

    @Override
    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
        String responseString = "onNotificationClicked title=" + title + " description=" + description + " customContentString=" + customContentString;
        LogUtil.i(responseString);
    }
}
