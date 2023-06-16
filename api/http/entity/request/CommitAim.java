package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitAim {
    private String username;
    private String sleepAim;
    private String sportAim;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSleepAim() {
        return sleepAim;
    }

    public void setSleepAim(String sleepAim) {
        this.sleepAim = sleepAim;
    }

    public String getSportAim() {
        return sportAim;
    }

    public void setSportAim(String sportAim) {
        this.sportAim = sportAim;
    }
}
