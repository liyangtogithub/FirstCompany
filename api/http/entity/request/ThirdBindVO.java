package com.desay.iwan2.common.api.http.entity.request;

import java.util.List;

public class ThirdBindVO {
	
/**
* 接收绑定数据
* @author hxy
* */

	private String username;
	private List<ThirdBinddata> bind;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public List<ThirdBinddata> getBind() {
		return bind;
	}
	public void setBind(List<ThirdBinddata> bind) {
		this.bind = bind;
	}
	
	
}
