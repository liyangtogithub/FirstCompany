package com.desay.iwan2.common.db.entity;

import com.desay.iwan2.common.contant.SystemContant;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class Day extends BaseDaoEnabled<Day, Integer> {

    public static final String TABLE = Day.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String DATE = "date";
    public static final String USER_ID = "user_id";
    public static final String MAX_GAIN = "maxGain";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private User user;
    @DatabaseField()
    private Date date;
    @DatabaseField
    @Deprecated
    private Long gain;
    @DatabaseField
    @Deprecated
    private Integer totalSteps;
    @DatabaseField
    @Deprecated
    private Integer aerobicsSteps;
    @DatabaseField
    @Deprecated
    private Integer calorie;
    @DatabaseField
    @Deprecated
    private Integer distance;
    @DatabaseField
    @Deprecated
    private Integer sportGoalRate;
    @DatabaseField
    @Deprecated
    private Integer sleepDuration;
    @ForeignCollectionField
    private ForeignCollection<Sleep> sleeps;
    @ForeignCollectionField
    private ForeignCollection<Gain> gains;
    @ForeignCollectionField
    private ForeignCollection<Sport> sports;
    @DatabaseField(defaultValue = ""+ SystemContant.defaultGainMax)
    private Integer maxGain;

    public ForeignCollection<Sport> getSports() {
        return sports;
    }

    public void setSports(ForeignCollection<Sport> sports) {
        this.sports = sports;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Deprecated
    public Long getGain() {
        return gain;
    }

    @Deprecated
    public void setGain(Long gain) {
        this.gain = gain;
    }

    @Deprecated
    public Integer getTotalSteps() {
        return totalSteps;
    }

    @Deprecated
    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    @Deprecated
    public Integer getAerobicsSteps() {
        return aerobicsSteps;
    }

    @Deprecated
    public void setAerobicsSteps(Integer aerobicsSteps) {
        this.aerobicsSteps = aerobicsSteps;
    }

    @Deprecated
    public Integer getCalorie() {
        return calorie;
    }

    @Deprecated
    public void setCalorie(Integer calorie) {
        this.calorie = calorie;
    }

    @Deprecated
    public Integer getDistance() {
        return distance;
    }

    @Deprecated
    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @Deprecated
    public Integer getSportGoalRate() {
        return sportGoalRate;
    }

    @Deprecated
    public void setSportGoalRate(Integer sportGoalRate) {
        this.sportGoalRate = sportGoalRate;
    }

    @Deprecated
    public Integer getSleepDuration() {
        return sleepDuration;
    }

    @Deprecated
    public void setSleepDuration(Integer sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public ForeignCollection<Sleep> getSleeps() {
        return sleeps;
    }

    public void setSleeps(ForeignCollection<Sleep> sleeps) {
        this.sleeps = sleeps;
    }

    public ForeignCollection<Gain> getGains() {
        return gains;
    }

    public void setGains(ForeignCollection<Gain> gains) {
        this.gains = gains;
    }

    public Integer getMaxGain() {
        return maxGain;
    }

    public void setMaxGain(Integer maxGain) {
        this.maxGain = maxGain;
    }
}
