package com.desay.iwan2.common.api.http.entity.response;

import java.util.List;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSportData {
    private String username;
    private List sportdata;

    public static class sport{
        private String date;
        private String startTime;
        private String endTime;
        private Integer sportTypeCode;
        private Long distance;
        private Double calorie;
        private Integer livenCode;
        private Integer mode;
    }
}
