package com.desay.iwan2.common.api.http.entity.request;

public class ThirdBinddata {
	
/**
* 返回绑定数据
* @author hxy
* */

	private String gtype;
	private String openid;
	private String tokenkey;
	private String appid;
//	private String gstatus;
	public String getGtype() {
		return gtype;
	}
	public void setGtype(String gtype) {
		this.gtype = gtype;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getTokenkey() {
		return tokenkey;
	}
	public void setTokenkey(String tokenkey) {
		this.tokenkey = tokenkey;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
//	public String getGstatus() {
//		return gstatus;
//	}
//	public void setGstatus(String gstatus) {
//		this.gstatus = gstatus;
//	}
	
}
