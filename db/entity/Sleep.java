package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class Sleep extends BaseDaoEnabled<Sleep, Integer> {

    public static final String TABLE = Sleep.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String DAY_ID = "day_id";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String TOTAL_DURATION = "totalDuration";
    @Deprecated
    public static final String SHALLOW_DURATION = "shallowDuration";
    @Deprecated
    public static final String DEEP_DURATION = "deepDuration";
    @Deprecated
    public static final String DREAM_DURATION = "dreamDuration";
    @Deprecated
    public static final String WAKE_DURATION = "wakeDuration";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private Day day;
    @DatabaseField
    private Integer totalDuration;
//    @DatabaseField
//    @Deprecated
//    private Integer shallowDuration;
//    @DatabaseField
//    @Deprecated
//    private Integer deepDuration;
//    @DatabaseField
//    @Deprecated
//    private Integer dreamDuration;
//    @DatabaseField
//    @Deprecated
//    private Integer wakeDuration;
    @DatabaseField
    private Date startTime;
    @DatabaseField
    private Date endTime;
    @DatabaseField
    private Float score;
    @ForeignCollectionField
    private ForeignCollection<SleepState> sleepStates;
    @ForeignCollectionField
    private ForeignCollection<HeartRate> heartRates;
    @ForeignCollectionField
    private ForeignCollection<SleepMotion> sleepMotions;

    public ForeignCollection<SleepMotion> getSleepMotions() {
        return sleepMotions;
    }

    public void setSleepMotions(ForeignCollection<SleepMotion> sleepMotions) {
        this.sleepMotions = sleepMotions;
    }

    public ForeignCollection<SleepState> getSleepStates() {
        return sleepStates;
    }

    public void setSleepStates(ForeignCollection<SleepState> sleepStates) {
        this.sleepStates = sleepStates;
    }

    public ForeignCollection<HeartRate> getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(ForeignCollection<HeartRate> heartRates) {
        this.heartRates = heartRates;
    }

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

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

//    @Deprecated
//    public Integer getShallowDuration() {
//        return shallowDuration;
//    }
//
//    @Deprecated
//    public void setShallowDuration(Integer shallowDuration) {
//        this.shallowDuration = shallowDuration;
//    }
//
//    @Deprecated
//    public Integer getDeepDuration() {
//        return deepDuration;
//    }
//
//    @Deprecated
//    public void setDeepDuration(Integer deepDuration) {
//        this.deepDuration = deepDuration;
//    }
//
//    @Deprecated
//    public Integer getDreamDuration() {
//        return dreamDuration;
//    }
//
//    @Deprecated
//    public void setDreamDuration(Integer dreamDuration) {
//        this.dreamDuration = dreamDuration;
//    }
//
//    @Deprecated
//    public Integer getWakeDuration() {
//        return wakeDuration;
//    }
//
//    @Deprecated
//    public void setWakeDuration(Integer wakeDuration) {
//        this.wakeDuration = wakeDuration;
//    }

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

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
