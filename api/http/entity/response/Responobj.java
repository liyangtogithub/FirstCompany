package com.desay.iwan2.common.api.http.entity.response;

/**
 * 返回数据格式
 * @author hxy
 * */
public class Responobj {
	
private String stateCode;	
private String msg;
private Object data ;
public String getStateCode() {
	return stateCode;
}
public void setStateCode(String stateCode) {
	this.stateCode = stateCode;
}
public String getMsg() {
	return msg;
}
public void setMsg(String msg) {
	this.msg = msg;
}
public Object getData() {
	return data;
}
public void setData(Object data) {
	this.data = data;
}


}
