package com.desay.iwan2.common.api.http.entity.response;

public class Devicedata {

    /**
     * 版本信息
     *
     * @author hxy
     */
    private String appname;
    private String appurl;
    private String gtype;
    private String md5;
    private int Ver;
    private String explains;
    private String explainsEN;

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getAppurl() {
        return appurl;
    }

    public void setAppurl(String appurl) {
        this.appurl = appurl;
    }

    public String getGtype() {
        return gtype;
    }

    public void setGtype(String gtype) {
        this.gtype = gtype;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getVer() {
        return Ver;
    }

    public void setVer(int ver) {
        Ver = ver;
    }

    public String getExplains() {
        return explains;
    }

    public void setExplains(String explains) {
        this.explains = explains;
    }

    public String getExplainsEN() {
        return explainsEN;
    }

    public void setExplainsEN(String explainsEN) {
        this.explainsEN = explainsEN;
    }
}
