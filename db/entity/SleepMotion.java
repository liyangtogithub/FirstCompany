package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class SleepMotion extends BaseDaoEnabled<SleepMotion, Integer> {

    public static final String TABLE = SleepMotion.class.getSimpleName();
    public static final String ID = "id";

    @DatabaseField(generatedId = true)
    private Integer id;
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
