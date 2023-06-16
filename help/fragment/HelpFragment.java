package com.desay.iwan2.module.help.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.db.entity.BtDev;
import com.desay.iwan2.common.server.BtDevServer;
import com.desay.iwan2.module.help.index.HelpViewIndex;
import dolphin.tools.util.AppUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;

/**
 * @author 方奕峰
 */
public class HelpFragment extends BaseFragment implements View.OnClickListener {

    private Activity act;
    private HelpViewIndex viewIndex;

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = getActivity();

        viewIndex = new HelpViewIndex(act,inflater);

        viewIndex.btn_back.setOnClickListener(this);

        return viewIndex.rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                back();
                break;
        }
    }
}
