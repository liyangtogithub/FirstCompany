package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;

/**
 * 返回升级信息
 *
 * @author hxy
 */

public class Upgradedata {

    private List<Devicedata> deviceVer;
    private int protocolType;
    private int goldVer;
    private List<GoldRuledata> goldRule;

    public List<Devicedata> getDeviceVer() {
        return deviceVer;
    }

    public void setDeviceVer(List<Devicedata> deviceVer) {
        this.deviceVer = deviceVer;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public int getGoldVer() {
        return goldVer;
    }

    public void setGoldVer(int goldVer) {
        this.goldVer = goldVer;
    }

    public List<GoldRuledata> getGoldRule() {
        return goldRule;
    }

    public void setGoldRule(List<GoldRuledata> goldRule) {
        this.goldRule = goldRule;
    }


}
