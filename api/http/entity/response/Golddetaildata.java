package com.desay.iwan2.common.api.http.entity.response;

public class Golddetaildata {
	
	/**
	 * 金币详情
	 * @author hxy
	 * */
	private String gtype;
	private String gtime;
	private String gevent;
	private int gold;
	
	
	public String getGtype() {
		return gtype;
	}
	public void setGtype(String gtype) {
		this.gtype = gtype;
	}
	public String getGtime() {
		return gtime;
	}
	public void setGtime(String gtime) {
		this.gtime = gtime;
	}
	public String getGevent() {
		return gevent;
	}
	public void setGevent(String gevent) {
		this.gevent = gevent;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
    
	
}
