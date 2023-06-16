package com.desay.iwan2.common.server.ble.handler;

import android.content.Context;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.request.CommitNc;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.server.NcServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.ble.OrderQueue;
import dolphin.tools.util.NetworkUtil;

import java.sql.SQLException;

/**
 * Created by 方奕峰 on 14-9-5.
 */
public class NcHandler implements Runnable {

    private Context context;
    private String nc;

    public NcHandler(Context context, String nc) {
        this.context = context;
        this.nc = nc;
    }

    @Override
    public void run() {
        try {
            OtherServer otherServer = new OtherServer(context);
            otherServer.createOrUpdate(null, Other.Type.watchNc, nc);
            if (NetworkUtil.isConnected(context)) {
                new NcServer(context).local2Network();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
