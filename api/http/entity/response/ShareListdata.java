package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;



/**
 * 返回分享后金币的数据
 * */

public class ShareListdata {

	private String gdate;//得金币时间（yyyyMMdd）
	private List<ShareGetdata> detail;//详情数组[]
	
	public String getGdate() {
		return gdate;
	}
	public void setGdate(String gdate) {
		this.gdate = gdate;
	}
	
	public List<ShareGetdata> getDetail() {
		return detail;
	}
	public void setDetail(List<ShareGetdata> detail) {
		this.detail = detail;
	}
}
