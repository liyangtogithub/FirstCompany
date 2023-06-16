package com.desay.iwan2.module.help.index;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.widget.CommonPagerAdapter;
import dolphin.tools.util.LogUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpViewIndex implements RadioGroup.OnCheckedChangeListener {

    public View rootView;
    public ViewPager pager;
    public RadioButton[] radioBtn_indexPoints;
    public View btn_back;

    public HelpViewIndex(Context context, LayoutInflater inflater) {
        rootView = inflater.inflate(R.layout.help_fragment, null);

        ((RadioGroup) rootView.findViewById(R.id.radioGroup))
                .setOnCheckedChangeListener(this);

        radioBtn_indexPoints = new RadioButton[]{
                (RadioButton) rootView.findViewById(R.id.radioBtn_0),
                (RadioButton) rootView.findViewById(R.id.radioBtn_1),
                (RadioButton) rootView.findViewById(R.id.radioBtn_2),
                (RadioButton) rootView.findViewById(R.id.radioBtn_3)};

        ArrayList<View> pageViews = new ArrayList<View>();
        View page4 = inflater.inflate(R.layout.help_page_4, null);
        TextView p4t1 = (TextView) page4.findViewById(R.id.text1);
        String str2 = context.getString(R.string.help_4_2);
        Pattern pattern = Pattern.compile("/#");
        Matcher matcher = pattern.matcher(str2);
        ArrayList<int[]> as = new ArrayList<int[]>();
        int b1 = 0;
        int b2 = 0;
        while (matcher.find()) {
            int a1 = b1 = str2.indexOf("/#", b1 + 1);
            int a2 = b2 = str2.indexOf("#/", b2 + 1);
            as.add(new int[]{a1, a2});
        }
        str2 = str2.replaceAll("/#", "");
        str2 = str2.replaceAll("#/", "");
        SpannableString ss1 = new SpannableString(str2);
        for (int i = 0; i < as.size(); i++) {
            int[] a = as.get(i);
            ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xa2, 0x13, 0xd7)), a[0]-i*4, a[1]-2-i*4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }


//        ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xa2, 0x13, 0xd7)), b1, b2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        p4t1.setText(ss1);
        pageViews.add(page4);

        pageViews.add(inflater.inflate(R.layout.help_page_0, null));

        View page2 = inflater.inflate(R.layout.help_page_1, null);
        TextView p2t1 = (TextView) page2.findViewById(R.id.text1);
//        String str1 = "连接状态下，如果要查看手环的最新数据，也可以点击APP首页右上角的*手动同步。";
        String str1 = context.getString(R.string.help_2_5);
        SpannableString ss = new SpannableString(str1);
        ImageSpan span = new ImageSpan(context, R.drawable.main_connected);
        ss.setSpan(span, str1.indexOf('*'), str1.indexOf('*') + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        p2t1.setText(ss);
        pageViews.add(page2);

        pageViews.add(inflater.inflate(R.layout.help_page_2, null));
        btn_back = rootView.findViewById(R.id.btn_back);
        pager = (ViewPager) rootView.findViewById(R.id.viewPager);
        CommonPagerAdapter adapter = new CommonPagerAdapter(pageViews);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                radioBtn_indexPoints[position].setChecked(true);
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < radioBtn_indexPoints.length; i++) {
            if (checkedId == radioBtn_indexPoints[i].getId())
                pager.setCurrentItem(i);
        }
    }
}
