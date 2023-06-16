package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;



/**
 * 根据日期返回金币数据
 * @author hxy
 * */

public class GoldListdata {

	private int total;//累计金币总
	private List<GetGolddata> every;
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<GetGolddata> getEvery() {
		return every;
	}
	public void setEvery(List<GetGolddata> every) {
		this.every = every;
	}
	
	
}
