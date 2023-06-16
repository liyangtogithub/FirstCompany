package com.desay.iwan2.module.help;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.module.help.fragment.HelpFragment;


public class HelpActivity extends TemplateActivity {

    @Override
    public void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_content, new HelpFragment()).commit();
    }

    public static void gotoActivity(Context context) {
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
    }
}
