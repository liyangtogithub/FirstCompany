package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class Gain extends BaseDaoEnabled<Gain, Integer> {

    public static final String TABLE = Gain.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String TIME = "time";
    public static final String COUNT = "count";
    public static final String DYPE_CODE = "typeCode";
    public static final String EVENT_CODE = "eventCode";
    public static final String DAY_ID = "day_id";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private Day day;
    @DatabaseField
    private String time;
    @DatabaseField
    private Integer count;
    @DatabaseField
    private String typeCode;
    @DatabaseField
    private String eventCode;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
}
