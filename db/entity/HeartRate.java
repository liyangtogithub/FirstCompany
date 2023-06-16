package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class HeartRate extends BaseDaoEnabled<HeartRate, Integer> {

    public static final String TABLE = HeartRate.class.getSimpleName();
    public static final String ID = "id";
//    public static final String SYNC = "sync";

    @DatabaseField(generatedId = true)
    private Integer id;
//    @DatabaseField(defaultValue = "0")
//    private Boolean sync;
    @DatabaseField(foreign = true)
    private Sleep Sleep;
    @DatabaseField
    private Date time;
    @DatabaseField
    private Integer value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

//    public Boolean getSync() {
//        return sync;
//    }
//
//    public void setSync(Boolean sync) {
//        this.sync = sync;
//    }

    public Sleep getSleep() {
        return Sleep;
    }

    public void setSleep(Sleep sleep) {
        Sleep = sleep;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
