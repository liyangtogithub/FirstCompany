package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class Login {
    private String username;
    private String passwd;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
