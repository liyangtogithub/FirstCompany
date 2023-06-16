package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;

public class ShareGetdata
{

	/**
	 * 某天分享的金币详情
	 */
	String gtype;// 类型编码01
	String gtime;// 得金币时间（HH:mm）
	String gevent;
	int gold;    // 金币数

	public String getGtype(){
		return gtype;
	}

	public void setGtype(String gtype){
		this.gtype = gtype;
	}

	public String getGtime(){
		return gtime;
	}

	public void setGtime(String gtime){
		this.gtime = gtime;
	}

	public String getGevent(){
		return gevent;
	}

	public void setGevent(String gevent){
		this.gevent = gevent;
	}

	public int getGold(){
		return gold;
	}

	public void setGold(int gold){
		this.gold = gold;
	}

}
