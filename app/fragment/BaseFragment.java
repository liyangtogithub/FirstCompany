package com.desay.iwan2.common.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dolphin.tools.util.ToastUtil;

/**
 * Created by 方奕峰 on 14-5-29.
 */
public class BaseFragment extends Fragment {

    public void back() {
        Activity act = getActivity();
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_BACK);
        act.dispatchKeyEvent(event);
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
        act.dispatchKeyEvent(event);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        try {
            return onCreateView1(inflater, container, savedInstanceState);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            ToastUtil.shortShow(getActivity(), "error_01");
            back();
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    protected View onCreateView1(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) throws Throwable {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public final void onDestroyView() {
        try {
            onDestroyView1();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onDestroyView();
    }

    protected void onDestroyView1() throws Throwable{

    }
}
