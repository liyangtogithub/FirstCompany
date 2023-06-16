package com.desay.iwan2.common.api.http.entity.request;

import com.desay.iwan2.common.db.entity.User;
import dolphin.tools.util.StringUtil;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitUserInfo {
    private String username;
    private String nickname;
    private int sexCode;
    private String birthday;
    private int height;
    private int weight;
    private String address;
    private String portraitUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getSexCode() {
        return sexCode;
    }

    public void setSexCode(int sexCode) {
        this.sexCode = sexCode;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
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

    public static CommitUserInfo generateFromDb(User user) {
        CommitUserInfo entity = new CommitUserInfo();
        entity.setUsername(user.getId());
        entity.setAddress(user.getAddress());
        entity.setBirthday(user.getBirthday());
        try {
            entity.setHeight(user.getHeight() == null ? null : Integer.valueOf(user.getHeight()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            entity.setHeight(user.getHeight() == null ? null : (int)((double)Double.valueOf(user.getHeight())));
        }
        try {
            entity.setWeight(user.getWeight() == null ? null : Integer.valueOf(user.getWeight()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            entity.setWeight(user.getWeight() == null ? null : (int)((double)Double.valueOf(user.getWeight())));
        }
        entity.setNickname(user.getNickname());
        entity.setSexCode(user.getSex() == null ? null : Integer.valueOf(user.getSex().getCode()));
        if (!StringUtil.isBlank(user.getPortrait())) {
            entity.setPortraitUrl(user.getPortrait());
        }
        return entity;
    }
}
