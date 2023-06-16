package com.desay.iwan2.module.userinfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonData {

	public static final int TOAST_SEX_NULL = 3;
	public static final int TOAST_BIRTHDAY_NULL = 4;
	public static final int TOAST_HEIGHT_NULL = 5;
	public static final int TOAST_WEIGHT_NULL = 6;
	public static final int TOAST_CITY_NULL = 7;
	public static final int TOAST_USERNAME_NULL = 9;
	public static final int TOAST_PASSWORD_NULL = 10;
	public static final int TOAST_PASSWORD_INVALID = 11;
	public static final int TOAST_EMAIL_NULL = 12;
	public static final int TOAST_USERNAME_ERROW = 13;
	public static final int TOAST_PASSWORD_ERROW = 14;
	public static final int WINDOW_REG = 0; // 当前是登录界面
	public static final int WINDOW_LOGIN = 1;// 当前是注册界面

	// 判断邮箱格式
	public static boolean isEmail(String strEmail) {
		String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}
	// 判断注册用户名格式
	public static boolean isRegestName(String name) {
		String strPattern = "([a-z]|[A-Z]|[0-9]|[._@])+$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(name);
		return m.matches();
	}
	// 判断注册密码格式
	public static boolean isRegestCode(String password) {
		String strPattern =  "^[a-zA-Z0-9]{6,20}";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(password);
		return m.matches();
	}





	
	
	


	
}
