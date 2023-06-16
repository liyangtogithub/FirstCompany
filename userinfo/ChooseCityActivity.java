package com.desay.iwan2.module.userinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ChooseCityActivity extends TemplateActivity implements OnClickListener {
    private TextView tv_title;
    RelativeLayout relative_title;
    ListView country_listview;
    ListView province_listview;
    ListView city_listview;
    Context context;
    Locale locale = null;
    String[] countryTitleArray = null;
    String[] provinceTitleArray = null;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
		setContentView(R.layout.login_setcity);
        context = ChooseCityActivity.this;
        locale = context.getResources().getConfiguration().locale;
        locale = (Locale.CHINA.equals(locale) || Locale.CHINESE.equals(locale) ||
                Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale)) ?
                Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        initView();
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        relative_title = (RelativeLayout) findViewById(R.id.relative_title);
        country_listview = (ListView) findViewById(R.id.country_listview);
        province_listview = (ListView) findViewById(R.id.province_listview);
        city_listview = (ListView) findViewById(R.id.city_listview);

        tv_title.setText(getString(R.string.choose_city_str));
        relative_title.setOnClickListener(this);
        CityAdapter cityAdapter = new CityAdapter(context, getCountrydata());
        country_listview.setAdapter(cityAdapter);
        initListListenner();
    }

    private void initListListenner() {
        country_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                String[] provinceArray = getSubdata(countryTitleArray[position]);
                if (provinceArray != null) {
                    CityAdapter cityAdapter = new CityAdapter(context, provinceArray);
                    province_listview.setAdapter(cityAdapter);
                    country_listview.setVisibility(View.GONE);
                    province_listview.setVisibility(View.VISIBLE);
                } else {
                    getBackCity(view);
                }
            }
        });

        province_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                String[] cityArray = getSubdata(provinceTitleArray[position]);
                if (cityArray != null) {
                    CityAdapter cityAdapter = new CityAdapter(context, cityArray);
                    city_listview.setAdapter(cityAdapter);
                    province_listview.setVisibility(View.GONE);
                    city_listview.setVisibility(View.VISIBLE);
                } else {
                    getBackCity(view);
                }
            }
        });

        city_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                getBackCity(view);
            }
        });
    }

    private void getBackCity(View view) {
        TextView city_textview = (TextView) view.findViewById(R.id.tv_content);
        String cityName = city_textview.getText().toString();
        Intent intent = getIntent();
        intent.putExtra("updatecity", cityName);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            changeVisible();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_title:
                changeVisible();
                break;
        }
    }

    private void changeVisible() {
        if (country_listview.getVisibility() == View.VISIBLE) {
            finish();
        } else if (province_listview.getVisibility() == View.VISIBLE) {
            province_listview.setVisibility(View.GONE);
            country_listview.setVisibility(View.VISIBLE);
        } else if (city_listview.getVisibility() == View.VISIBLE) {
            city_listview.setVisibility(View.GONE);
            province_listview.setVisibility(View.VISIBLE);
        }
    }

    private String[] getCountrydata() {
        WechatCityServer wechatCityServer = new WechatCityServer();
        String allData = null;
        try {
            allData = wechatCityServer.getAllData(context, locale);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> countryList = wechatCityServer.getRoot(allData);
        countryTitleArray = new String[countryList.size()];
        String[] countryArray = new String[countryList.size()];
        String tempArray[] = null;
        for (int i = 0; i < countryList.size(); i++) {
            tempArray = countryList.get(i).split("\\|");
            countryTitleArray[i] = tempArray[0];
            countryArray[i] = tempArray[1];
        }
        return countryArray;
    }

    private String[] getSubdata(String title) {
        WechatCityServer wechatCityServer = new WechatCityServer();
        String allData = null;
        try {
            allData = wechatCityServer.getAllData(context, locale);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> provinceList = wechatCityServer.getSub(allData, title);
        int size = provinceList.size();
        if (size == 0)
            return null;

        provinceTitleArray = new String[size];
        String[] provinceArray = new String[size];
        String tempArray[] = null;
        for (int i = 0; i < provinceList.size(); i++) {
            tempArray = provinceList.get(i).split("\\|");
            provinceTitleArray[i] = tempArray[0];
            provinceArray[i] = tempArray[1];
        }
        return provinceArray;
    }

}
