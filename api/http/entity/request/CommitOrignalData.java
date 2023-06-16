package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitOrignalData {
    private String username;
    private String heartRates;
    private String acts;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(String heartRates) {
        this.heartRates = heartRates;
    }

    public String getActs() {
        return acts;
    }

    public void setActs(String acts) {
        this.acts = acts;
    }
}
