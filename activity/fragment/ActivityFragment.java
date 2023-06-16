package com.desay.iwan2.module.activity.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.UserInfoServer;

/**
 * QQ SDK
 *
 * @author 方奕峰
 */
public class ActivityFragment extends BaseFragment implements View.OnClickListener {

    private Activity act;

    private WebView webview;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = getActivity();

        User user = new UserInfoServer(act).getUserInfo();
        String username = "";
        if (user != null) {
            username = user.getId();
        }

        View rootView = inflater.inflate(R.layout.activity_fragment, null);
        rootView.findViewById(R.id.btn_back).setOnClickListener(this);
        webview = (WebView) rootView.findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://59.33.252.109:4411/social/home.html?username=" + username);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ("http://www.baidu.com".equalsIgnoreCase(url)){//TODO关闭活动界面
                    act.finish();
                }else {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                back();
                break;
            default:
                break;
        }
    }
}
