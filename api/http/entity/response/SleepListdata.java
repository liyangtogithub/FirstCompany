package com.desay.iwan2.common.api.http.entity.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据日期返回睡眠数据
 *
 * @author hxy
 */
public class SleepListdata extends GetGolddata {

//    private String gdate;
    private String sleepTime;
    private Integer gtime;
    private String wakeupTime;
    //    private Integer wakeup;
    private float quantity;
//    private Integer dayGold;
    private List<SleepState> sleepState = new ArrayList<SleepState>();
    private List<HeartEntity> heartRates = new ArrayList<HeartEntity>();
//    private List<Golddetaildata> detail;


//    public String getGdate() {
//        return gdate;
//    }
//
//    public void setGdate(String gdate) {
//        this.gdate = gdate;
//    }

    public String getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }

    public Integer getGtime() {
        return gtime;
    }

    public void setGtime(Integer gtime) {
        this.gtime = gtime;
    }

    public String getWakeupTime() {
        return wakeupTime;
    }

    public void setWakeupTime(String wakeupTime) {
        this.wakeupTime = wakeupTime;
    }

//    public Integer getWakeup() {
//        return wakeup;
//    }
//
//    public void setWakeup(Integer wakeup) {
//        this.wakeup = wakeup;
//    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public List<SleepState> getSleepState() {
        return sleepState;
    }

    public void setSleepState(List<SleepState> sleepState) {
        this.sleepState = sleepState;
    }
//
//    public List<Golddetaildata> getDetail() {
//        return detail;
//    }

    public List<HeartEntity> getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(List<HeartEntity> heartRates) {
        this.heartRates = heartRates;
    }
//
//    public void setDetail(List<Golddetaildata> detail) {
//        this.detail = detail;
//    }

//    public Integer getDayGold() {
//        return dayGold;
//    }
//
//    public void setDayGold(Integer dayGold) {
//        this.dayGold = dayGold;
//    }
}
