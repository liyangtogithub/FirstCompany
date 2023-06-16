package com.desay.iwan2.common.api.http.entity.request;

import com.desay.iwan2.common.api.http.entity.response.HeartEntity;

import java.util.List;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSleep {
    private String username;
    private List<Record> baseSleep;
    private List<ThirdBinddata> bind;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Record> getBaseSleep() {
        return baseSleep;
    }

    public void setBaseSleep(List<Record> baseSleep) {
        this.baseSleep = baseSleep;
    }

    public List<ThirdBinddata> getBind() {
        return bind;
    }

    public void setBind(List<ThirdBinddata> bind) {
        this.bind = bind;
    }

    public static class Record {
        private String gtime;
        private List<HeartRate> heartRates;
        private List<Motion> acts;

        public String getGtime() {
            return gtime;
        }

        public void setGtime(String gtime) {
            this.gtime = gtime;
        }

        public List<HeartRate> getHeartRates() {
            return heartRates;
        }

        public void setHeartRates(List<HeartRate> heartRates) {
            this.heartRates = heartRates;
        }

        public List<Motion> getActs() {
            return acts;
        }

        public void setActs(List<Motion> acts) {
            this.acts = acts;
        }
    }

    public static class HeartRate extends HeartEntity {
    }

    public static class Motion extends HeartRate {
    }
}
