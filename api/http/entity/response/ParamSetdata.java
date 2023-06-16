package com.desay.iwan2.common.api.http.entity.response;

public class ParamSetdata {
	
/**
* 返回手环设置数据
* @author hxy
* */

	private String alarm;
	private String caller;
	private String sedentary;
	private String music;
	private String sleep;
	
	public String getAlarm() {
		return alarm;
	}
	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}
	public String getCaller() {
		return caller;
	}
	public void setCaller(String caller) {
		this.caller = caller;
	}
	public String getSedentary() {
		return sedentary;
	}
	public void setSedentary(String sedentary) {
		this.sedentary = sedentary;
	}
	public String getMusic() {
		return music;
	}
	public void setMusic(String music) {
		this.music = music;
	}
	public String getSleep(){
		return sleep;
	}
	public void setSleep(String sleep){
		this.sleep = sleep;
	}
}
