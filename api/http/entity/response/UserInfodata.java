package com.desay.iwan2.common.api.http.entity.response;


/**
* 个人信息
* @author hxy
*/
public class  UserInfodata {

	private String nickname;  //昵称
    private String sexCode;     //性别
    private String birthday;    //出生日期
	private String height;	//身高
	private String weight;	//体重
	private String address;	//地址
	private String portraitUrl;	//头像地址
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getSexCode() {
		return sexCode;
	}
	public void setSexCode(String sexCode) {
		this.sexCode = sexCode;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPortraitUrl() {
		return portraitUrl;
	}
	public void setPortraitUrl(String portraitUrl) {
		this.portraitUrl = portraitUrl;
	}
}