package com.desay.iwan2.common.contant;

import com.desay.fitband.R;

import android.R.integer;
import android.content.Context;

/**
 * @author 方奕峰
 */
public enum Sex {

    MALE("1", R.string.sex_male), FEMALE("0", R.string.sex_female);

    private String code;
    private int stringId;

    private Sex(String code, int stringId) {
        this.code = code;
        this.stringId = stringId;
    }

    public String getCode() {
        return code;
    }

    public int getStringId() {
        return stringId;
    }

    public static Sex convert(String str) {
        if ("1".equals(str)) {
            return MALE;
        } else if ("0".equals(str)) {
            return FEMALE;
        }
        return null;
    }

    public static Sex convert1(Context context , String str) {
        if (context.getString( MALE.getStringId() ).equals(str)) {
            return MALE;
        } else if (context.getString( FEMALE.getStringId() ).equals(str)) {
            return FEMALE;
        }
        return null;
    }
}
