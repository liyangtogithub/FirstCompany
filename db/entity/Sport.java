package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class Sport extends BaseDaoEnabled<Sport, Integer> {

    public static final String TABLE = Sport.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String DAY = "day";
    public static final String DAY_ID = "day_id";
    public static final String TIME = "startTime";
    public static final String COUNT = "stepCount";
    public static final String DESCRIPTION = "description";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private Day day;
    @DatabaseField
    private Date startTime;
    @DatabaseField
    private Date endTime;
    @DatabaseField
    private Integer stepCount;
    @DatabaseField
    private Integer distance;
    @DatabaseField(defaultValue = "0")
    private Boolean aerobics;
    @DatabaseField(defaultValue = "0")
    private Integer typeCode;
    @DatabaseField
    private Float calorie;
    @DatabaseField(defaultValue = "1")
    private Integer mode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public Boolean getAerobics() {
        return aerobics;
    }

    public void setAerobics(Boolean aerobics) {
        this.aerobics = aerobics;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public Float getCalorie() {
        return calorie;
    }

    public void setCalorie(Float calorie) {
        this.calorie = calorie;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }
}
