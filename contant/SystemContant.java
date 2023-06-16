package com.desay.iwan2.common.contant;

import java.text.SimpleDateFormat;

/**
 * Created by 方奕峰 on 14-6-17.
 */
public class SystemContant {
    public static final SimpleDateFormat timeFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat timeFormat2 = new SimpleDateFormat("yyyy-MM-dd E");

    public static final SimpleDateFormat timeFormat3 = new SimpleDateFormat("HH:mm");

    public static final SimpleDateFormat timeFormat4 = new SimpleDateFormat("M月d");

    public static final SimpleDateFormat timeFormat5 = new SimpleDateFormat("yyyy年M月");

    public static final SimpleDateFormat timeFormat6 = new SimpleDateFormat("yyyyMMdd");

    public static final String timeFormat7Str = "yyyyMMddHHmm";
    public static final SimpleDateFormat timeFormat7 = new SimpleDateFormat(timeFormat7Str);

    public static final String timeFormat8Str = "yyyy-MM-dd HH:mm:ss.S";
    public static final SimpleDateFormat timeFormat8 = new SimpleDateFormat(timeFormat8Str);

    public static final String timeFormat9Str = "yyyyMMddHHmmss";
    public static final SimpleDateFormat timeFormat9 = new SimpleDateFormat(timeFormat9Str);

    public static final SimpleDateFormat timeFormat10 = new SimpleDateFormat("yyyy/MM/dd");

    public static final SimpleDateFormat timeFormat11 = new SimpleDateFormat("MMM");

    public static final SimpleDateFormat timeFormat12 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final String timeFormat_sql_1 = "yyyy-MM-E";

    public static final String timeFormat_sql_2 = "yyyy-M";


    public static final long millisecondInDay = 86400000;

    public static final int sleepAreaChartUnit = 1;//睡眠状态图表上的间隔
    public static final int sportMotionInterval = 15;//运动监测间隔（分）

    public static final double defaultSleepAim = 480;//默认睡眠时长目标（分钟）
    public static final int defaultSportAim = 10000;//默认运动目标（步）
    public static final int defaultAerobicsAim = 6000;//默认有氧运动目标（步）

    public static final long wristbandDefaultTime = 1262275200000L;//默认手环时间2010-01-01

    public static final int monitorDataLength = 144;

    public static final long syncBleDataInterval = 900000L;//同步手环间隔

    public static final int defaultHeightMin = 100;//身高下限
    public static final int defaultHeightMax = 250;//身高上限
    public static final int defaultWeightMin = 25;//体重下限
    public static final int defaultWeightMax = 300;//体重上限
    public static final int defaultGainMax = 85;//金币上限

    public static final int aerobicsBoundary = 600;//有氧运动界限

    public static final int syncTimeout = 10000;//同步手环数据超时时间
}
