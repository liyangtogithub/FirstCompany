package com.desay.iwan2.module.band;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.util.ToastUtil;

public class BandManageActivity extends TemplateActivity {

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
//        setContentView(R.layout.band_manage);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initUI();
    }

	private void initUI(){
		BandManageFragment bandManageFragment = new BandManageFragment();
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.layout_content, bandManageFragment);
		// transaction.addToBackStack(null);
		transaction.commit();

	}

    public static void goToActivity(Context context) {
        goToActivity(context, 0);
    }

    public static final int FLAG_OPEN_SERVICE = 1;

    public static void goToActivity(Context context, int flag) {
        switch (flag) {
            case FLAG_OPEN_SERVICE:
                MyService.start(context);
                break;
        }
        if (BleUtil.checkBleEnable(context)) {
            Intent intent = new Intent(context, BandManageActivity.class);
            context.startActivity(intent);
        } else {
            ToastUtil.shortShow(context, context.getString(R.string.band_ble_invalid));
        }
    }

    @Override
    protected void onDestroy1() throws Throwable {
//		BandManager.enableBand=false;//绑定结束
        BandManager.cancelBindMode();//绑定结束
    }

}
