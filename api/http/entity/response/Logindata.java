package com.desay.iwan2.common.api.http.entity.response;

import com.desay.iwan2.common.api.http.entity.request.ThirdBinddata;

import java.util.List;

/**
 * 返回所有设置数据
 *
 * @author hxy
 */
public class Logindata {

    private UserInfodata info;
    private ParamSetdata set;
    private AimSetdata aim;
    private String mac;
    private List<ThirdBinddata> bind;

    public UserInfodata getInfo() {
        return info;
    }

    public void setInfo(UserInfodata info) {
        this.info = info;
    }

    public ParamSetdata getSet() {
        return set;
    }

    public void setSet(ParamSetdata set) {
        this.set = set;
    }

    public AimSetdata getAim() {
        return aim;
    }

    public void setAim(AimSetdata aim) {
        this.aim = aim;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public List<ThirdBinddata> getBind() {
        return bind;
    }

    public void setBind(List<ThirdBinddata> bind) {
        this.bind = bind;
    }


}
