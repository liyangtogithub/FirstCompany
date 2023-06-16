package com.desay.iwan2.common.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class BtDev extends BaseDaoEnabled<BtDev, Integer> {

    public static final String TABLE = BtDev.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";
    public static final String MAC = "mac";
    public static final String TYPE_CODE = "typeCode";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(foreign = true)
    private User user;
    @DatabaseField
    private String mac;
    @DatabaseField
    private String typeCode;
    @DatabaseField
    private String coreVersion;
    @DatabaseField
    private String nordicVersion;

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

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getCoreVersion() {
        return coreVersion;
    }

    public void setCoreVersion(String coreVersion) {
        this.coreVersion = coreVersion;
    }

    public String getNordicVersion() {
        return nordicVersion;
    }

    public void setNordicVersion(String nordicVersion) {
        this.nordicVersion = nordicVersion;
    }
}
