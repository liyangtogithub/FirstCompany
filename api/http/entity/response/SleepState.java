package com.desay.iwan2.common.api.http.entity.response;

/**
 * 睡眠状态数据
 *
 * @author hxy
 */
public class SleepState {
    private String startTime;
    private String endTime;
    private String stateCode;

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
