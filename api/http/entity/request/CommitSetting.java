package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSetting {
    private String username;
    private String alarm;
    private String caller;
    private String sedentary;
    private String music;
    private String sleep;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
