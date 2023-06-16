package com.desay.iwan2.module.userinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.Login;
import com.desay.iwan2.common.api.http.entity.request.Register;
import com.desay.iwan2.common.api.http.entity.response.Logindata;
import com.desay.iwan2.common.api.http.entity.response.QqLoginResponse;
import com.desay.iwan2.common.api.http.entity.response.UserInfodata;
import com.desay.iwan2.common.app.activity.TemplateActivity;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.contant.Sex;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.*;
import com.desay.iwan2.common.server.LoginInfoServer.LoginInfo;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.band.BandManageActivity;
import com.desay.iwan2.module.menu.MenuFragment;
import com.desay.iwan2.module.music.sportDB;
import com.desay.iwan2.module.start.StartActivity;
import com.desay.iwan2.module.userinfo.ImageHandle.ImageCallback;
import com.desay.iwan2.util.PwdCrypto;
import com.j256.ormlite.dao.Dao;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;
import dolphin.tools.util.ToastUtil;
import org.apache.http.Header;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class LoginFragment extends BaseFragment implements OnClickListener {
    private final int INSERT_SELF_MUSIC = 0;
    private TextView tv_title, tv_wel,tv_username,tv_email,tv_password,tv_repassword;
    LinearLayout rl_title;
    RelativeLayout layout_email,layout_repassword,layout_password;
    private ImageView iv_title,iv_email,iv_username,iv_password;
    private EditText et_username,et_password,et_repassword,et_email;
    private Button btn_reg,btn_login;
    private TemplateActivity act;
    public int status;
    private String username = "";
    private String password = "";
    private String repassword = "";
    private String emailt = "";
    LoginInfoServer loginInfoServer = null;
    public static final String MUSIC_SELF = "self_";

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = (TemplateActivity) getActivity();
        View rootView = inflater.inflate(R.layout.login, null);
        status = CommonData.WINDOW_LOGIN;
        loginInfoServer = new LoginInfoServer(act);
        initView(rootView);
        createMusicDB();

        return rootView;
    }

    private void createMusicDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handlerload.sendEmptyMessage(INSERT_SELF_MUSIC);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_title:
                if (status != CommonData.WINDOW_LOGIN) {
                    changeWindowsUI(CommonData.WINDOW_LOGIN);
                } else {
                    back();
                }
                break;
            case R.id.btn_reg:
                if (status == CommonData.WINDOW_LOGIN) {
                    changeWindowsUI(CommonData.WINDOW_REG);
                } else {
                    if (checkInputValid() && checkRegestInputValid()) {
                        try {
                            password = PwdCrypto.encrypt(password);
                            goRegister();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case R.id.btn_login:
                if (checkInputValid()) {

                    ((TemplateActivity) getActivity()).showMatteLayer(true, getString(R.string.toast_loading));
                    try {
                        password = PwdCrypto.encrypt(password);
                        goLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * 注册
     */
    private void goRegister() {
        try {
            Register param = new Register();
            param.setUsername(username);
            param.setPasswd(password);
            param.setEmail(emailt);
            Api1.register(act, param, new MyJsonHttpResponseHandler(act) {
                @Override
                public void onSuccess(Context context, String str) {
                    loginInfoServer.saveLoginInfo(username, password);
                    loginInfoServer.setIsAutoLogin(true);
                    ToastUtil.shortShow(context, getString(R.string.toast_regist_succ));
                    InfoActivity.gotoActivity(context);
                    getActivity().finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void goLogin() {
    	 try
			{
				onSuccessLogin(getActivity(),null);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
     /*   try {
           
            Login param = new Login();
            param.setUsername(username);
            param.setPasswd(password);
            Api1.login1(act, param, new MyJsonHttpResponseHandler(act) {

                @Override
                public void onFailure(final int statusCode, Header[] headers, String responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);
                    if (getActivity() != null)
                        ((TemplateActivity) getActivity()).showMatteLayer(false);
                   
                }

                @Override
                public void onFailure(Context context, String stateCode, String msg) {
                    super.onFailure(context, stateCode, msg);
                    if (getActivity() != null)
                        ((TemplateActivity) getActivity()).showMatteLayer(false);
                   
                }

                @Override
                public void onSuccess(Context context, String str) {
                   
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void onSuccessLogin(Context context1, String str) throws SQLException
	{
    	 DatabaseHelper dbHelper = DatabaseHelper.getDataBaseHelper(act);
         final Dao<User, String> userDao = dbHelper.getUserDao();
    	 loginInfoServer.setIsAutoLogin(true);
         //ToastUtil.shortShow(act, "登陆成功 ");
         //ToastUtil.shortShow(act, "正在加载数据...");
         loginInfoServer.saveLoginInfo(username, password);
         //LogUtil.i("登陆成功 str=" + str);
//         str=" {\"height\":170,}";
         try {
             List<User> userList = userDao.queryForEq(User.ID, username);
             User user = null;
             if (userList == null || userList.size() == 0) {
                 user = new User();
                 user.setId(username);
             } else {
                 user = userList.get(0);
             }
             if (isInfoBlank(str)) return;
             Logindata logindata = JSON.parseObject(str, Logindata.class);

             UserInfodata userInfo = logindata.getInfo();
             user.setIsEmpty(true);
             if (userInfo != null) {
                 user.setPortraitUrl(userInfo.getPortraitUrl());
                 user.setAddress(userInfo.getAddress());
                 user.setBirthday(userInfo.getBirthday());
                 user.setSex(StringUtil.isBlank(userInfo.getSexCode()) ? null
                         : Sex.convert(userInfo.getSexCode()));
                 String height = null;
                 if (userInfo.getHeight() != null) {
                     int dotIndex = userInfo.getHeight().indexOf(".");
                     height = dotIndex == -1 ? userInfo.getHeight() : userInfo.getHeight().substring(0, dotIndex);
                 }
                 user.setHeight(height);
                 String weight = null;
                 if (userInfo.getWeight() != null) {
                     int dotIndex = userInfo.getWeight().indexOf(".");
                     weight = dotIndex == -1 ? userInfo.getWeight() : userInfo.getWeight().substring(0, dotIndex);
                 }
                 user.setWeight(weight);
//                 loadHeadImage(user, new UserInfoServer(context));
                 if (!isEntityBlank(userInfo)) {
                     user.setIsEmpty(false);
                 }
             }
             userDao.createOrUpdate(user);


             String mac = logindata.getMac();
             if (!StringUtil.isBlank(str)) {
//                 BtDevServer btDevServer = new BtDevServer(context);
//                 btDevServer.createOrUpdate(user, mac, null, null);
             }
             next();
         } catch (Exception e) {
             e.printStackTrace();
         }
	}
    
    private boolean isInfoBlank(String str) {
        if (StringUtil.isBlank(str)) {
            InfoActivity.gotoActivity(act);
            back();
            return true;
        }
        return false;
    }

    private boolean isEntityBlank(UserInfodata entity) {
        if (StringUtil.isBlank(entity.getAddress()))
            return true;
        if (StringUtil.isBlank(entity.getBirthday()))
            return true;
        if (StringUtil.isBlank(entity.getSexCode()))
            return true;
        if (StringUtil.isBlank(entity.getHeight()))
            return true;
        if (StringUtil.isBlank(entity.getWeight()))
            return true;
        return false;
    }

    private void next() {
        act.showMatteLayer(false);
//        StartActivity.gotoActivity(act, 1);
//        back();
        UserInfoServer userInfoServer = null;
        try {
            userInfoServer = new UserInfoServer(act);
            User user = userInfoServer.getUserInfo();
            if (user == null || user.getIsEmpty() == null || user.getIsEmpty()) {
                InfoActivity.gotoActivity(act);
            } else {
                MacServer macServer = new MacServer(act);
                Other other = new OtherServer(act).getOther(user, Other.Type.isFirst);
                String isfirst = null;
                if (other != null)
                    isfirst = other.getValue();
                MainActivity.gotoActivity(act, true);
                if (other == null || isfirst == null || "".equals(isfirst)) {
                    if (macServer == null || StringUtil.isBlank(macServer.getMac())) {
                        BandManageActivity.goToActivity(act, BandManageActivity.FLAG_OPEN_SERVICE);
                    }
                }
            }
            back();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LoginFragment() {
    }

    private void loadHeadImage(final User user, final UserInfoServer userInfoServer) {
        try {
            if (user.getPortraitUrl() != null) {
                new ImageHandle().loadDrawable(user.getPortraitUrl().trim(),
                        new ImageCallback() {
                            public void imageLoaded(String imageString) {
                                if (imageString != null
                                        && !"".equals(imageString)) {
                                    user.setPortrait(imageString);
                                    userInfoServer.storeImage(user);
                                    act.sendBroadcast(new Intent(
                                            MenuFragment.BROADCAST_UPDATE_PHOTO));
                                }
                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        rl_title = (LinearLayout) view.findViewById(R.id.rl_title);
        rl_title.setVisibility(View.GONE);
        iv_title = (ImageView) view.findViewById(R.id.iv_title);
        tv_wel = (TextView) view.findViewById(R.id.tv_wel);
        tv_email = (TextView) view.findViewById(R.id.tv_email);
        tv_username = (TextView) view.findViewById(R.id.tv_username);
        tv_password = (TextView) view.findViewById(R.id.tv_password);
        tv_repassword = (TextView) view.findViewById(R.id.tv_repassword);
        et_username = (EditText) view.findViewById(R.id.et_username);
        et_password = (EditText) view.findViewById(R.id.et_password);
        et_repassword = (EditText) view.findViewById(R.id.et_repassword);
        et_email = (EditText) view.findViewById(R.id.et_email);
        btn_reg = (Button) view.findViewById(R.id.btn_reg);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        layout_email = (RelativeLayout) view.findViewById(R.id.layout_email);
        iv_email = (ImageView) view.findViewById(R.id.iv_email);
        iv_username = (ImageView) view.findViewById(R.id.iv_username);
        iv_password = (ImageView) view.findViewById(R.id.iv_password);
        layout_repassword = (RelativeLayout) view.findViewById(R.id.layout_repassword);
        layout_password = (RelativeLayout) view.findViewById(R.id.layout_password);
        btn_reg.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        rl_title.setOnClickListener(this);
        LoginInfo loginInfo = loginInfoServer.getLoginInfo();
        if (loginInfo != null) {
            String account = loginInfo.getAccount();
            // String pwd = loginInfo.getPwd();
            if (account != null /*&& pwd != null*/) {
                et_username.setText(account);
                et_password.setText("");
            }
        }
        TextWatcherLisener();
    }

    private void TextWatcherLisener(){
    	et_email.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus){
				username = et_username.getText().toString();
    			if (CommonData.isRegestName(username)){
    				iv_username.setBackgroundResource(R.drawable.login_arrow_right);
    			} else{
    				iv_username.setBackgroundResource(R.drawable.login_arrow_wrong);
    			}
    			iv_username.setVisibility(View.VISIBLE);
    			tv_username.setVisibility(View.GONE);
			}
		});
    	
    	et_email.addTextChangedListener(new TextWatcher(){
    		@Override
    		public void beforeTextChanged(CharSequence s, int start, int count,int after){
    			tv_email.setVisibility(View.GONE);
    		}
    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count){}
    		@Override
    		public void afterTextChanged(Editable s){}
    	});
    	
    	et_password.setOnFocusChangeListener(new OnFocusChangeListener(){
    		@Override
    		public void onFocusChange(View v, boolean hasFocus){
    			emailt = et_email.getText().toString();
 				if (et_email.isShown()){
 					if (CommonData.isEmail(emailt)){
 						iv_email.setBackgroundResource(R.drawable.login_arrow_right);
 					} else{
 						iv_email.setBackgroundResource(R.drawable.login_arrow_wrong);
 					}
 					iv_email.setVisibility(View.VISIBLE);
 					tv_email.setVisibility(View.GONE);
 				}
    		}
    	});
    	
    	 et_password.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,int after){
 					tv_password.setVisibility(View.GONE);
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count){}
 			@Override
 			public void afterTextChanged(Editable s){}
 		});
    	 
    	 et_repassword.addTextChangedListener(new TextWatcher(){
    		 @Override
    		 public void beforeTextChanged(CharSequence s, int start, int count,int after){
    			 password = et_password.getText().toString();
    				 /*if (CommonData.isRegestCode(password)){
    					 iv_password.setBackgroundResource(R.drawable.login_arrow_right);
    				 } else{
    					 iv_password.setBackgroundResource(R.drawable.login_arrow_wrong);
    				 }
    				 iv_password.setVisibility(View.VISIBLE);*/
    				 tv_repassword.setVisibility(View.GONE);
    		 }
    		 @Override
    		 public void onTextChanged(CharSequence s, int start, int before, int count){}
    		 @Override
    		 public void afterTextChanged(Editable s){}
    	 });
    	 
	}

	public void changeWindowsUI(int st) {
        switch (st) {
            case CommonData.WINDOW_REG:// 切换到注册界面
            	iv_email.setVisibility(View.GONE);
            	iv_username.setVisibility(View.GONE);
            	iv_password.setVisibility(View.GONE);
            	et_username.setText("");
            	et_password.setText("");
            	et_repassword.setText("");
            	et_email.setText("");
            	tv_email.setVisibility(View.VISIBLE);
            	tv_username.setVisibility(View.VISIBLE);
            	tv_password.setVisibility(View.VISIBLE);
            	tv_repassword.setVisibility(View.VISIBLE);
            	
                tv_wel.setVisibility(View.GONE);
                rl_title.setVisibility(View.VISIBLE);
                tv_title.setText(getString(R.string.user_info_register));
                et_password.setVisibility(View.VISIBLE);
                layout_repassword.setVisibility(View.VISIBLE);
                layout_email.setVisibility(View.VISIBLE);
                btn_login.setVisibility(View.GONE);
                btn_reg.setVisibility(View.VISIBLE);
                iv_title.setVisibility(View.GONE);
                status = CommonData.WINDOW_REG;
                break;
            case CommonData.WINDOW_LOGIN:// 切换到登录界面
                iv_username.setVisibility(View.GONE);
                iv_password.setVisibility(View.GONE);
                et_username.setText("");
                et_password.setText("");
                tv_username.setVisibility(View.GONE);
                tv_password.setVisibility(View.GONE);
                
                rl_title.setVisibility(View.GONE);
                tv_wel.setVisibility(View.VISIBLE);
                et_password.setVisibility(View.VISIBLE);
                layout_repassword.setVisibility(View.GONE);
                layout_email.setVisibility(View.GONE);
                btn_login.setVisibility(View.VISIBLE);
                btn_reg.setVisibility(View.VISIBLE);
                iv_title.setVisibility(View.VISIBLE);
                status = CommonData.WINDOW_LOGIN;
                break;

        }
    }

    private boolean checkInputValid() {
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();
        repassword = et_repassword.getText().toString().trim();
        emailt = et_email.getText().toString().trim();
        boolean retValue = false;

        Message msg = new Message();
        msg.what = -1;
        if ("".equals(username)) {
            msg.what = CommonData.TOAST_USERNAME_NULL;
            et_username.requestFocus();
        } else if ((et_password.isShown() && "".equals(password))
                || (et_repassword.isShown() && "".equals(repassword))) {
            msg.what = CommonData.TOAST_PASSWORD_NULL;
            if ("".equals(password))
                et_password.requestFocus();
            else
                et_repassword.requestFocus();
        } else if (et_repassword.isShown() && !password.equals(repassword)) {
            msg.what = CommonData.TOAST_PASSWORD_INVALID;
            et_password.requestFocus();
        } else if (et_email.isShown() && !CommonData.isEmail(emailt)) {
            msg.what = CommonData.TOAST_EMAIL_NULL;
            et_email.requestFocus();
        } else {
            retValue = true;
        }
        if (msg.what != -1)
            Handlerload.sendMessage(msg);
        return retValue;
    }

    private boolean checkRegestInputValid() {
        boolean retValue = false;

        Message msg = new Message();
        msg.what = -1;

        if (!CommonData.isRegestName(username)) {
            msg.what = CommonData.TOAST_USERNAME_ERROW;
            et_username.requestFocus();
        } else if (!CommonData.isRegestCode(password)) {
            msg.what = CommonData.TOAST_PASSWORD_ERROW;
            et_repassword.requestFocus();
        } else {
            retValue = true;
        }
        if (msg.what != -1)
            Handlerload.sendMessage(msg);
        return retValue;
    }

    Handler Handlerload = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CommonData.TOAST_USERNAME_NULL:
                    ToastUtil.shortShow(act, getString(R.string.toast_username_null));
                    break;
                case CommonData.TOAST_PASSWORD_NULL:
                    ToastUtil.shortShow(act, getString(R.string.toast_password_null));
                    break;
                case CommonData.TOAST_PASSWORD_INVALID:
                    ToastUtil.shortShow(act, getString(R.string.toast_password_invalid));
                    break;
                case CommonData.TOAST_EMAIL_NULL:
                    ToastUtil.shortShow(act, getString(R.string.toast_email_invalid));
                    break;
                case CommonData.TOAST_USERNAME_ERROW:
                    ToastUtil.shortShow(act, getString(R.string.toast_reg_name_invalid));
                    break;
                case CommonData.TOAST_PASSWORD_ERROW:
                    ToastUtil.shortShow(act, getString(R.string.toast_reg_word_invalid));
                    break;
                case INSERT_SELF_MUSIC:
                    final sportDB db = new sportDB(act);
                    if (!db.tableIsExist()) {
                        db.CreateTable();
                        initSleepMusic();
                    }
                    break;
            }
        }
    };

    private void initSleepMusic() {
        try {
            int sleepMusic[] = new int[]{R.raw.sleep1, R.raw.sleep2, R.raw.sleep3, R.raw.sleep4};
            String sleepArray[] = getResources().getStringArray(R.array.music_sleep_array);
            sportDB sportDb = new sportDB(act);
            for (int i = 0; i < sleepMusic.length; i++) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("_musicName", sleepArray[i]);
                values.put("_isself", i + 1);
                values.put("_musicId", (i + 1) + "");
                values.put("_time", 300000 + "");
                values.put("_singer", "<unknown>");
                values.put("_albumKey", "suiyi");
                values.put("_musicPath", MUSIC_SELF + sleepMusic[i]);
                sportDb.InsertMusic(values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
