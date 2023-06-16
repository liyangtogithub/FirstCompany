package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class Other extends BaseDaoEnabled<Other, Integer> {

    public static final String TABLE = Other.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String USER_ID = "user_id";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private User user;
    @DatabaseField
    private String value;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private Type type;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        sleepAim, sportAim, treasure, alarm, alarmToBand,alarmToBand2, caller, sedentary, sedentaryToBand,music, currentGain,
        gainEventVer, isFirst, netNodicVer, netEf32Ver, netNodicExpain,netEf32Expain,watchNc, tempTotalStep,handsUp,SlpTime,
        blueNoticeVer,coreNoticeVer,crcNodicValue,crcEf32Value,qq
    }

}
