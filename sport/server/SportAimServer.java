package com.desay.iwan2.module.sport.server;

import java.sql.SQLException;

import android.content.Context;

import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.UserInfoServer;
import dolphin.tools.util.LogUtil;

public class SportAimServer {
    private int stepAim;
    private int aerobicStepAim;
    private float calorieAim;
    private float distanceAim;


    public int getStepAim() {
        return stepAim;
    }


    public int getAerobicStepAim() {
        return aerobicStepAim;
    }


    public int getCalorieAim() {
        return (int) calorieAim;
    }


    public int getDistanceAim() {
        return (int) distanceAim;
    }


    public SportAimServer(Context context) throws SQLException {
        User user = new UserInfoServer(context).getUserInfo();
        if (user == null) return;

        this.aerobicStepAim = SystemContant.defaultAerobicsAim;

        OtherServer otherServer = new OtherServer(context);
        Other other = otherServer.getOther(user, Other.Type.sportAim);
        if (other == null || "0".equals(other.getValue())) {
            stepAim = SystemContant.defaultSportAim;
        } else {
            stepAim = Integer.valueOf(other.getValue());
        }

        int height = Integer.valueOf(user.getHeight());
        height = height == 0 ? SystemContant.defaultHeightMin : height;
        distanceAim = SportAimServer.getDistanceByStep(stepAim, height);
        double weight = Double.valueOf(user.getWeight());
        weight = weight == 0 ? SystemContant.defaultWeightMin : weight;
        calorieAim = SportAimServer.getCalorie(weight, distanceAim);

//        try {
//            InitData(context);
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


//    void InitData(Context context) throws SQLException {
//        User user = new UserInfoServer(context).getUserInfo();
//        if (user == null) return;
//
//        String height = user.getHeight();
//        String weight = user.getWeight();
//        if (height == null || weight == null) return;
//
//        OtherServer otherserver = new OtherServer(context);
//        String sportAim = otherserver.getOther(null, Other.Type.sportAim).getValue();
//        if (!sportAim.equals("")) {
//            stepAim = Integer.valueOf(sportAim);
//        }
//
//        this.distanceAim = (int) getDistanceByStep(stepAim, Integer.valueOf(height));
//        this.calorieAim = getCalorie(weight, distanceAim);
//
//    }


    /**
     * 计算走了多少米
     */
    public static float getDistanceByStep(long step, int height) {
        float dis = 0;
        dis = (float) ((double) height / 3.0 / 100.0 * step);
        return dis;
    }

    /**
     * 计算卡路里
     */
    public static float getCalorie(double weight, float dis) {
        float cal = 0;
        cal = (float) (((2.21 * weight * 0.708)
                // Distance:
                * dis // centimeters
                / 1000.0)); // centimeters/kilometer
        return cal;
    }

}
