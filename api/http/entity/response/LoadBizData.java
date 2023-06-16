package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;

/**
 * Created by 方奕峰 on 14-7-15.
 */
public class LoadBizData {
    private List<SleepListdata> sleeps;
    private List<SportListdata> sports;
    private GoldListdata golds;

    public List<SleepListdata> getSleeps() {
        return sleeps;
    }

    public void setSleeps(List<SleepListdata> sleeps) {
        this.sleeps = sleeps;
    }

    public List<SportListdata> getSports() {
        return sports;
    }

    public void setSports(List<SportListdata> sports) {
        this.sports = sports;
    }

    public GoldListdata getGolds() {
        return golds;
    }

    public void setGolds(GoldListdata golds) {
        this.golds = golds;
    }
}
