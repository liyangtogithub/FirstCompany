package com.desay.iwan2.common.api.http.entity.response;

/**
 * Created by 方奕峰 on 14-6-17.
 */
public class ResponseEntity {
    private String stateCode;
    private String msg;
    private String data;

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
