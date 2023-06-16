package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class TempData extends BaseDaoEnabled<TempData, Integer> {

    public static final String TABLE = TempData.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private User user;
    @DatabaseField
    private String generateTime;
    @DatabaseField
    private String typeCode;
}
