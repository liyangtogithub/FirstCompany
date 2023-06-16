package com.desay.iwan2.module.userinfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import com.desay.iwan2.common.app.service.MyService;
import dolphin.tools.ble.BleUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;

import com.desay.fitband.R;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.contant.Sex;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.LoginInfoServer;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.UserInfoServer;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.band.BandManageActivity;
import com.desay.iwan2.module.menu.MenuFragment;
import com.desay.iwan2.module.share.SportData;
import com.desay.iwan2.module.start.StartActivity;
import com.desay.iwan2.module.userinfo.WechatCityServer.Address;

import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class InfoActivity extends TemplateActivity implements OnClickListener {
    private TextView tv_title;
    RelativeLayout relative_title;
    private TextView iv_save;
    private ImageView iv_photo;
    private TextView tv_user;
    private TextView tv_sex;
    private TextView tv_birthday;
    private EditText et_height;
    private EditText et_weight;
    private TextView tv_city;
    private TextView tv_load;
    private Context context;
    public DatePickerDialog datePickerDialog = null;
    private Calendar cal = null;
    private User info;
    UserInfoServer userInfoServer;
    LoginInfoServer loginInfoServer;
    String allData = null;
    Locale locale = null;
    WechatCityServer wechatCityServer;

    @Override
    protected void onCreate1(Bundle savedInstanceState) throws Throwable {
        super.onCreate1(savedInstanceState);
        setContentView(R.layout.login_user_info);
        context = InfoActivity.this;

        init();
        initDb();
        initEditInfo();
    }

    public void init() throws IOException {
        wechatCityServer = new WechatCityServer();
        locale = context.getResources().getConfiguration().locale;
        locale = (Locale.CHINA.equals(locale) || Locale.CHINESE.equals(locale) ||
                Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale)) ?
                Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        allData = wechatCityServer.getAllData(context, locale);

        tv_title = (TextView) findViewById(R.id.tv_title);
        relative_title = (RelativeLayout) findViewById(R.id.relative_title);
        iv_save = (TextView) findViewById(R.id.iv_save);
        tv_user = (TextView) findViewById(R.id.tv_user);
        tv_load = (TextView) findViewById(R.id.tv_load);

        tv_title.setText(getString(R.string.user_info_title));
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);
        et_height = (EditText) findViewById(R.id.et_height);
        et_weight = (EditText) findViewById(R.id.et_weight);
        tv_city = (TextView) findViewById(R.id.tv_city);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);

        relative_title.setOnClickListener(this);
        iv_save.setOnClickListener(this);
        tv_load.setOnClickListener(this);
        iv_photo.setOnClickListener(this);
        tv_sex.setOnClickListener(this);
        tv_city.setOnClickListener(this);
        tv_birthday.setOnClickListener(this);
    }

    private void initDb() {
        try {
            userInfoServer = new UserInfoServer(context);
            info = userInfoServer.getUserInfo();
            loginInfoServer = new LoginInfoServer(context);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
	public void initEditInfo() {
        cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (info != null) {
            tv_user.setText(info.getId());
            if (info.getSex() != null)
                tv_sex.setText(context.getString(info.getSex().getStringId()));
            if (info.getBirthday() != null && !"".equals(info.getBirthday())) {
                if (info.getBirthday().length() == 8) {
                    year = Integer.valueOf(info.getBirthday().substring(0, 4));
                    month = Integer.valueOf(info.getBirthday().substring(4, 6));
                    day = Integer.valueOf(info.getBirthday().substring(6));
                    tv_birthday.setText(year + "-" + info.getBirthday().substring(4, 6) + "-" + info.getBirthday().substring(6));
                }
            }
            if (info.getHeight() != null)
                et_height.setText(info.getHeight());
            if (info.getWeight() != null)
                et_weight.setText(info.getWeight());
            if (info.getAddress() != null) {
            	LogUtil.i("info.getAddress()=="+info.getAddress());
                Address address = wechatCityServer.getAddressByCode(allData, info.getAddress());
                if (address != null)
                    tv_city.setText(address.value);
                else {
                    tv_city.setText(getString(R.string.unknown_city));
                }
            }
            if (info.getPortrait() != null && !"".equals(info.getPortrait()))
                showHeadImage();
            if (info.getIsEmpty()==null || info.getIsEmpty())
            	 tv_load.setText(getString(R.string.next_step));
        } else {
            info = new User();
            tv_load.setText(getString(R.string.next_step));
            tv_user.setText(loginInfoServer.getLoginInfo().getAccount());
        }
        datePickerDialog = new DatePickerDialog(context, dateSet, year, (month-1), day);
        DatePicker datePicker = datePickerDialog.getDatePicker();  
        Calendar calendar = Calendar.getInstance();
        calendar.set(1914, Calendar.JANUARY, 1);
        datePicker.setMinDate(calendar.getTimeInMillis());  
        datePicker.setMaxDate(cal.getTimeInMillis());  
        tv_birthday.addTextChangedListener(watcher);
        et_height.addTextChangedListener(watcher);
        et_weight.addTextChangedListener(watcher);
        tv_sex.addTextChangedListener(watcher);
        tv_city.addTextChangedListener(watcher);
        tv_birthday.addTextChangedListener(watcher);
    }

    private void showHeadImage() {
        try {
            byte[] photo = Base64.decodeBase64(info.getPortrait().getBytes("UTF-8"));
            Bitmap output = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            if (output != null) {
                iv_photo.setImageBitmap(SportData.toRoundBitmap(output));
                iv_photo.setVisibility(View.VISIBLE);
            }
        } catch (UnsupportedEncodingException e) {
        }
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            iv_save.setVisibility(View.VISIBLE);
        }

    };
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_city:
                Intent i = new Intent(context, ChooseCityActivity.class);
                startActivityForResult(i, SportData.REQUESTCODE_CITY);
                break;
            case R.id.relative_title:
            	 if (info.getIsEmpty()==null || info.getIsEmpty()){
         			LoginActivity.gotoActivity(context);
         			new LoginInfoServer(context).setIsAutoLogin(false);
         		}
                finish();
                break;
            case R.id.tv_sex:
                showDialog(context, SportData.ICON_SEX);
                break;
            case R.id.iv_save:
                if (checkInputValid())
                    saveInfo();
                break;
            case R.id.iv_photo:
                showDialog(context, SportData.ICON_PHOTOSELECT);
                break;
            case R.id.tv_load:
                if (getString(R.string.next_step).equals(tv_load.getText().toString())) {
                    if (checkInputValid()) {
                        info.setIsEmpty(false);
                        saveInfo();
                        MainActivity.gotoActivity(context);
                        try
						{
							MacServer macServer = new MacServer(context);
							if (macServer == null || StringUtil.isBlank(macServer.getMac())) {
	                            BandManageActivity.goToActivity(this, BandManageActivity.FLAG_OPEN_SERVICE);
	                        }
						} catch (Exception e){
							e.printStackTrace();
						}
                        finish();
                    }
                } else {
                    loginInfoServer.setIsAutoLogin(false);
                    LoginActivity.gotoActivity(context);
                    MainActivity.closeActivity();
                    finish();
                    MyService.stop(this);
                }
                break;
            case R.id.tv_birthday:
            	if (datePickerDialog==null){
            		LogUtil.e("datePickerDialog==null");
            		return;
				}
                datePickerDialog.show();
                break;
        }
    }

    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  {  
        	 if (info.getIsEmpty()==null || info.getIsEmpty()){
      			LoginActivity.gotoActivity(context);
      			new LoginInfoServer(context).setIsAutoLogin(false);
      		}
             finish();
        }     
        return false;          
    }  
    
    private void saveInfo() {
        info.setId(tv_user.getText().toString());
        info.setSex(Sex.convert1(context, tv_sex.getText().toString()));
        info.setBirthday(tv_birthday.getText().toString().replace("-", ""));
        info.setHeight(et_height.getText().toString());
        info.setWeight(et_weight.getText().toString());

        Address address = wechatCityServer.getAddressBySuffix(allData, tv_city.getText().toString());
        if (address!=null){
        	LogUtil.i("address.code=="+address.code);
            info.setAddress(address.code);
        }
        userInfoServer.storeInfo(info);
        ToastUtil.shortShow(context, context.getString(R.string.save_success));
        sendBroadcast(new Intent(MenuFragment.BROADCAST_UPDATE_PHOTO));//update photo in menu

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String resultData;
        Bundle extras;
        LogUtil.i("requestCode==" + requestCode + "resultCode==" + resultCode + "data==" + data);
        if (SportData.REQUESTCODE_CITY == requestCode) {
            try {
                resultData = data.getExtras().getString("updatecity");
            } catch (Exception e) {
                resultData = "";
            }
            if (resultData != null && !"".equals(resultData)) {
                tv_city.setText(resultData);
            }
        } else {
            if (resultCode == SportData.REQUESTCODE_NONE)
                return;
            if (data == null)
                return;
            if (requestCode == SportData.REQUESTCODE_PHOTOGRAPH) {

                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                startPhotoZoomNew(bitmap);
            }

            if (requestCode == SportData.REQUESTCODE_PHOTOZOOM) {
                startPhotoZoomOld(data.getData());
            }
            if (requestCode == SportData.REQUESTCODE_PHOTOSAVE) {
                extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    LogUtil.i(" baos.toByteArray().length=="+baos.toByteArray().length);
                    int options = 100; 
                    //循环判断如果压缩后图片是否大于100kb,大于继续压缩          
                    while ( baos.toByteArray().length / 1024>100) { 
                             baos.reset();//重置baos即清空baos   
                             //这里压缩options%，把压缩后的数据存放到baos中   
                             photo.compress(Bitmap.CompressFormat.JPEG, options, baos);
                             options -= 10;//每次都减少10   
                          }   
                    LogUtil.i(" baos.toByteArray().length=="+baos.toByteArray().length);
                    if (info != null)
                        try {
                            info.setPortrait(new String(Base64.encodeBase64(baos.toByteArray()), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    //info.setPortrait(SportData.bytesToHexString(stream.toByteArray()));
                    if (photo != null) {
                        iv_photo.setImageBitmap(SportData.toRoundBitmap(photo));
                        iv_photo.setVisibility(View.VISIBLE);
                        iv_save.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkInputValid() {
        String height = et_height.getText().toString();
        int heightInt = 0;
        String weight = et_weight.getText().toString();
        int weightInt = 0;
        try {
            if (!"".equals(height))
                heightInt = Integer.parseInt(height);
            if (!"".equals(weight))
                weightInt = Integer.parseInt(weight);
            weightInt = Integer.parseInt(weight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sex = tv_sex.getText().toString();
        String birthday = tv_birthday.getText().toString();

        String city = tv_city.getText().toString();
        boolean retValue = false;
        Message msg = new Message();
        msg.what = -1;
        if (StringUtil.isBlank(sex)) {
            msg.what = SportData.TOAST_SEX_NULL;
            tv_sex.requestFocus();
        } else if (StringUtil.isBlank(birthday)) {
            msg.what = SportData.TOAST_BIRTHDAY_NULL;
            tv_birthday.requestFocus();
        } else if (StringUtil.isBlank(height)) {
            msg.what = SportData.TOAST_HEIGHT_NULL;
            et_height.requestFocus();
        } else if (heightInt < SystemContant.defaultHeightMin || heightInt > SystemContant.defaultHeightMax) {
            msg.what = SportData.TOAST_HEIGHT_ERROR;
            et_height.requestFocus();
        } else if (StringUtil.isBlank(weight)) {
            msg.what = SportData.TOAST_WEIGHT_NULL;
            et_weight.requestFocus();
        } else if (weightInt < SystemContant.defaultWeightMin || weightInt > SystemContant.defaultWeightMax) {
            msg.what = SportData.TOAST_WEIGHT_ERROR;
            et_height.requestFocus();
        } else if (StringUtil.isBlank(city)) {
            msg.what = SportData.TOAST_CITY_NULL;
            tv_city.requestFocus();
        } else {
            retValue = true;
        }
        if (msg.what != -1) handle.sendMessage(msg);
        return retValue;
    }

    private void showDialog(Context tcontext, final int icontype) {
        int arrayID = -1;
        int titleID = -1;
        if (icontype == SportData.ICON_SEX) {
            arrayID = R.array.array_sex;
            titleID = R.string.selectsex;
        } else if (icontype == SportData.ICON_PHOTOSELECT) {
            arrayID = R.array.array_setphoto;
            titleID = R.string.selectheadphoto;
        }
        final String[] data = tcontext.getResources().getStringArray(arrayID);
        AlertDialog.Builder builder = new AlertDialog.Builder(tcontext);
        final Dialog ad = builder.create();
        LayoutInflater inflater;
        View layout = null;
        if (!ad.isShowing()) {
            ad.show();
            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay();
            WindowManager.LayoutParams p = ad.getWindow().getAttributes();
            p.width = (int) (d.getWidth() * 0.8);
            ad.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            ad.getWindow().setAttributes(p);
            inflater = LayoutInflater.from(tcontext);
            layout = inflater.inflate(R.layout.login_dialog, null);
            ad.getWindow().setContentView(layout);
            TextView tv_content = (TextView) layout.findViewById(R.id.tv_content);
            ListView list = (ListView) layout.findViewById(R.id.list);
            tv_content.setText(tcontext.getString(titleID));
            DialogAdapter adapter = new DialogAdapter(tcontext, data, icontype);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    // TODO Auto-generated method stub
                    switch (icontype) {
                        case SportData.ICON_SEX:
                            tv_sex.setText(data[arg2]);
                            break;
                        case SportData.ICON_PHOTOSELECT:
                            setHeadPhoto(arg2);
                            break;
                    }

                    ad.dismiss();
                }
            });
        }
    }

    private void setHeadPhoto(int index) {
        Intent i = null;
        switch (index) {
            case 0:
                i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, SportData.REQUESTCODE_PHOTOGRAPH);
                break;
            case 1:
                i = new Intent(Intent.ACTION_PICK, null);
                i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        SportData.IMAGE_UNSPECIFIED);
                startActivityForResult(i, SportData.REQUESTCODE_PHOTOZOOM);
                break;
        }
    }

    public void startPhotoZoomNew(Bitmap data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.putExtra("data", data);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, SportData.REQUESTCODE_PHOTOSAVE);
    }

    public void startPhotoZoomOld(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, SportData.IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, SportData.REQUESTCODE_PHOTOSAVE);
    }

    DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            if (year < 1914 || (year >= Calendar.getInstance().get(Calendar.YEAR))) {
                ToastUtil.shortShow(context, getString(R.string.birthdayiserror));
            } else {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                tv_birthday.setText(year + "-"
                        + decimalFormat.format(monthOfYear + 1) + "-"
                        + decimalFormat.format(dayOfMonth));
            }
        }
    };

    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SportData.TOAST_SEX_NULL:
                    ToastUtil.shortShow(context, getString(R.string.sexisnull));
                    break;
                case SportData.TOAST_BIRTHDAY_NULL:
                    ToastUtil.shortShow(context, getString(R.string.birthdayisnull));
                    break;
                case SportData.TOAST_HEIGHT_NULL:
                    ToastUtil.shortShow(context, getString(R.string.heightisnull));
                    break;
                case SportData.TOAST_HEIGHT_ERROR:
                    ToastUtil.shortShow(context, getString(R.string.heigh_errow));
                    break;
                case SportData.TOAST_WEIGHT_NULL:
                    ToastUtil.shortShow(context, getString(R.string.weightisnull));
                    break;
                case SportData.TOAST_WEIGHT_ERROR:
                    ToastUtil.shortShow(context, getString(R.string.weigh_errow));
                    break;
                case SportData.TOAST_CITY_NULL:
                    ToastUtil.shortShow(context, getString(R.string.cityisnull));
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static void gotoActivity(Context packageContext) {
        Intent intent = new Intent(packageContext, InfoActivity.class);
        packageContext.startActivity(intent);
    }
}
