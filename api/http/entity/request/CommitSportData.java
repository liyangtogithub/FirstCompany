package com.desay.iwan2.common.api.http.entity.request;

import java.util.List;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSportData {
    private String username;
    private List<Day> sportdata;
    private List<ThirdBinddata> bind;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Day> getSportdata() {
        return sportdata;
    }

    public void setSportdata(List<Day> sportdata) {
        this.sportdata = sportdata;
    }

    public List<ThirdBinddata> getBind() {
        return bind;
    }

    public void setBind(List<ThirdBinddata> bind) {
        this.bind = bind;
    }

    public static class Day {
        private String gdate;
        private List<Detail> detail;

        public String getGdate() {
            return gdate;
        }

        public void setGdate(String gdate) {
            this.gdate = gdate;
        }

        public List<Detail> getDetail() {
            return detail;
        }

        public void setDetail(List<Detail> detail) {
            this.detail = detail;
        }
    }

    public static class Detail {
        private String startTime;
        private String endTime;
        private Integer sportTypeCode;
        private Integer distance;
        private Float calorie;
        private String livenCode;
        private Integer gmode;
        private Integer pace;

        public Integer getPace() {
            return pace;
        }

        public void setPace(Integer pace) {
            this.pace = pace;
        }

        public Integer getGmode() {
            return gmode;
        }

        public void setGmode(Integer gmode) {
            this.gmode = gmode;
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

        public Integer getSportTypeCode() {
            return sportTypeCode;
        }

        public void setSportTypeCode(Integer sportTypeCode) {
            this.sportTypeCode = sportTypeCode;
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
        }

        public Float getCalorie() {
            return calorie;
        }

        public void setCalorie(Float calorie) {
            this.calorie = calorie;
        }

        public String getLivenCode() {
            return livenCode;
        }

        public void setLivenCode(String livenCode) {
            this.livenCode = livenCode;
        }
    }
}
