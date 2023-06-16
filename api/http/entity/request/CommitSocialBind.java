package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSocialBind {
    private String username;
    private String socialType;
    private String socialID;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSocialType() {
        return socialType;
    }

    public void setSocialType(String socialType) {
        this.socialType = socialType;
    }

    public String getSocialID() {
        return socialID;
    }

    public void setSocialID(String socialID) {
        this.socialID = socialID;
    }
}
