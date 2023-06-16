package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class SleepState extends BaseDaoEnabled<SleepState, Integer> {

    public static final String TABLE = SleepState.class.getSimpleName();
    public static final String ID = "id";
    public static final String SLEEP_ID = "sleep_id";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String STATE = "state";
//    public static final String SYNC = "sync";

    @DatabaseField(generatedId = true)
    private Integer id;
    //    @DatabaseField(defaultValue = "0")
//    private Boolean sync;
    @DatabaseField(foreign = true)
    private Sleep Sleep;
    @DatabaseField
    private Date startTime;
    @DatabaseField
    private Date endTime;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private State state;

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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        deep, shallow, dream, wake, insleep;

        public static State code2State(String code) {
            if ("3".equals(code)) {
                return deep;
            } else if ("2".equals(code)) {
                return shallow;
            } else if ("1".equals(code)) {
                return dream;
            } else if ("0".equals(code)) {
                return wake;
            }
            return insleep;
        }

        public static String state2Code(State state) {
            switch (state) {
                case deep:
                    return "3";
                case shallow:
                    return "2";
                case dream:
                    return "1";
                case wake:
                    return "0";
                case insleep:
                default:
                    return "4";
            }
        }
    }

}
