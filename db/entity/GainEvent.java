package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class GainEvent extends BaseDaoEnabled<GainEvent, Integer> {

    public static final String TABLE = GainEvent.class.getSimpleName();
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String CN_MSG = "cnMsg";
    public static final String EN_MSG = "enMsg";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private String code;
    @DatabaseField
    private String cnMsg;
    @DatabaseField
    private String enMsg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCnMsg() {
        return cnMsg;
    }

    public void setCnMsg(String cnMsg) {
        this.cnMsg = cnMsg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }
}
