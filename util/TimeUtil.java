package com.desay.iwan2.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    public static String formatTimeString(int minute) {
        final DecimalFormat decimalFormat2 = new DecimalFormat("00");
        int h = minute / 60;
        int m = minute % 60;
        return decimalFormat2.format(h) + "h" + decimalFormat2.format(m) + "'";
    }

    public static Date[] getWeekDateRange(Calendar calendar, int offset) {
        Date[] result = new Date[2];
        // Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, offset);

        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        calendar.add(Calendar.DATE, -w);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        result[0] = calendar.getTime();
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.MILLISECOND, -1);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
        result[1] = calendar.getTime();

        return result;
    }

    public static Date[] getMonthDateRange(Calendar calendar, int offset) {
        Date[] result = new Date[2];
        // Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, offset);

        result[0] = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
        result[1] = calendar.getTime();

        return result;
    }

    public static Date[] getYearDateRange(Calendar calendar, int offset) {
        Date[] result = new Date[2];
        // Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.YEAR, offset);

        result[0] = calendar.getTime();

        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MILLISECOND, -1);
//        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
//        calendar.set(Calendar.DAY_OF_MONTH, 31);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
        result[1] = calendar.getTime();

        return result;
    }

    public static String getDateString(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String monthString = month + "";
        String dayString = day + "";
        if (month < 10)
            monthString = "0" + monthString;
        if (day < 10)
            dayString = "0" + dayString;
        return calendar.get(Calendar.YEAR) + "/" + monthString
                + "/" + dayString;
    }

    public static Calendar getDateStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar;
    }

//    public enum DateRangeUnit {
//        WEEK, MONTH, YEAR;
//    }
//
//    public static String format(DateRangeUnit dateRangeUnit, Date[] dateRange) {
//        Date startTime = dateRange[0];
//        Date endTime = dateRange[1];
//        String result = null;
//        switch (dateRangeUnit) {
//            case YEAR:
//                SimpleDateFormat formatY = new SimpleDateFormat("yyyy");
//                result = formatY.format(startTime);
//                break;
//            case MONTH:
//                SimpleDateFormat formatM = new SimpleDateFormat("yyyy/MM");
//                result = formatM.format(startTime);
//                break;
//            case WEEK:
//            default:
//                String startTimeStr = null;
//                SimpleDateFormat formatW = new SimpleDateFormat("yyyy/MM/dd");
//                startTimeStr = formatW.format(startTime);
//                String endTimeStr = null;
//                if (startTime.getYear() == endTime.getYear()) {
//                    formatW = new SimpleDateFormat("MM/dd");
//                }
//                endTimeStr = formatW.format(endTime);
//                result = startTimeStr + "~" + endTimeStr;
//                break;
//        }
//        return result;
//    }
//
//    public enum TimeRangeUnit {
//        DAY, MONTH
//    }
//
//    public static String format(TimeRangeUnit dateRangeUnit,
//                                Date date) {
//        String result = null;
//        switch (dateRangeUnit) {
//            case MONTH:
//                SimpleDateFormat formatM = new SimpleDateFormat("yyyy年MM月");
//                result = formatM.format(date);
//                break;
//            case DAY:
//            default:
//                SimpleDateFormat formatD = new SimpleDateFormat("yyyy年MM月dd日");
//                result = formatD.format(date);
//                break;
//        }
//        return result;
//    }
}
