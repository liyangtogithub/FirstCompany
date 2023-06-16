package com.desay.iwan2.module.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.module.about.fragment.AboutFragment;
import com.desay.iwan2.module.activity.fragment.ActivityFragment;

/**
 * @author 方奕峰
 */
public class ActivityActivity extends TemplateActivity {

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable{
        super.onCreate1(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_content, new ActivityFragment()).commit();
    }

    public static void gotoActivity(Context context) {
        Intent intent = new Intent(context, ActivityActivity.class);
        context.startActivity(intent);
    }
}