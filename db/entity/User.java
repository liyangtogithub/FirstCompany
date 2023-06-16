package com.desay.iwan2.common.db.entity;

import com.desay.iwan2.common.contant.Sex;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author 方奕峰
 */
@DatabaseTable
public class User extends BaseDaoEnabled<User, String> {

    public static final String TABLE = User.class.getSimpleName();
    public static final String ID = "id";
    public static final String SYNC = "sync";

    @DatabaseField(id = true)
    private String id;
    @DatabaseField(defaultValue = "0")
    private Boolean sync;
    @DatabaseField(defaultValue = "1")
    private Boolean isEmpty;
    @DatabaseField
    private String nickname;
    @DatabaseField
    private String birthday;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private Sex sex;
    @DatabaseField
    private String address;
    @DatabaseField
    private String height;
    @DatabaseField
    private String weight;
    @DatabaseField
    private String portrait;
    @DatabaseField
    private String portraitUrl;
    @ForeignCollectionField
    private ForeignCollection<Day> days;
    @ForeignCollectionField
    private ForeignCollection<Other> others;
    @ForeignCollectionField
    private ForeignCollection<TempData> tempDatas;
    @ForeignCollectionField
    private ForeignCollection<BtDev> btDevs;
    
    public Boolean getIsEmpty()
	{
		return isEmpty;
	}

	public void setIsEmpty(Boolean isEmpty)
	{
		this.isEmpty = isEmpty;
	}

	public ForeignCollection<Other> getOthers() {
        return others;
    }

    public void setOthers(ForeignCollection<Other> others) {
        this.others = others;
    }

    public ForeignCollection<TempData> getTempDatas() {
        return tempDatas;
    }

    public void setTempDatas(ForeignCollection<TempData> tempDatas) {
        this.tempDatas = tempDatas;
    }

    public ForeignCollection<BtDev> getBtDevs() {
        return btDevs;
    }

    public void setBtDevs(ForeignCollection<BtDev> btDevs) {
        this.btDevs = btDevs;
    }

    public ForeignCollection<Day> getDays() {
        return days;
    }

    public void setDays(ForeignCollection<Day> days) {
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }
}
