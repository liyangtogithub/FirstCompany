package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;


/**
 * 根据日期返回运动数据
 *
 * @author hxy
 */

public class SportListdata {

    private String startTime;
    private String endTime;
    private int sportTypeCode;
    private int distance;
    private float calorie;
    private String livenCode;
    private int gmode;
    private int pace;
    private List<Location> location;

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

    public int getSportTypeCode() {
        return sportTypeCode;
    }

    public void setSportTypeCode(int sportTypeCode) {
        this.sportTypeCode = sportTypeCode;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public float getCalorie() {
        return calorie;
    }

    public void setCalorie(float calorie) {
        this.calorie = calorie;
    }

    public String getLivenCode() {
        return livenCode;
    }

    public void setLivenCode(String livenCode) {
        this.livenCode = livenCode;
    }

    public int getGmode() {
        return gmode;
    }

    public void setGmode(int gmode) {
        this.gmode = gmode;
    }

    public int getPace() {
        return pace;
    }

    public void setPace(int pace) {
        this.pace = pace;
    }

    public List<Location> getLocation() {
        return location;
    }

    public void setLocation(List<Location> location) {
        this.location = location;
    }

}
