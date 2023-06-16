package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;


public class GetGolddata {

    /**
     * 某天金币汇总详情
     *
     * @author hxy
     */
    private String gdate;  //日期
    @Deprecated
    private int count;   //当天金币数
    private List<Golddetaildata> detail;  //当天金币详情
    private int dayGold;    //当天金币上限

    public String getGdate() {
        return gdate;
    }

    public void setGdate(String gdate) {
        this.gdate = gdate;
    }

    @Deprecated
    public void setCount(int count) {
        this.count = count;
    }

    @Deprecated
    public long getCount() {
        return count;
    }

    public List<Golddetaildata> getDetail() {
        return detail;
    }

    public void setDetail(List<Golddetaildata> detail) {
        this.detail = detail;
    }

    public int getDayGold() {
        return dayGold;
    }

    public void setDayGold(int dayGold) {
        this.dayGold = dayGold;
    }
}
