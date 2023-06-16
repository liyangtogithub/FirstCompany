package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class LoadVersion {
    private String gtype;
    private Integer goldVer;

    public String getGtype() {
        return gtype;
    }

    public void setGtype(String gtype) {
        this.gtype = gtype;
    }

    public Integer getGoldVer() {
        return goldVer;
    }

    public void setGoldVer(Integer goldVer) {
        this.goldVer = goldVer;
    }
}
