package com.desay.iwan2.common.server;

import android.content.Context;
import com.desay.iwan2.common.api.http.Api1;
import com.desay.iwan2.common.api.http.entity.callback.MyJsonHttpResponseHandler;
import com.desay.iwan2.common.api.http.entity.request.CommitNc;
import com.desay.iwan2.common.db.entity.Other;

import java.sql.SQLException;

/**
 * Created by 方奕峰 on 14-9-5.
 */
public class NcServer {

    private Context context;

    public NcServer(Context context) {
        this.context = context;
    }

    public void local2Network() throws SQLException {
        final OtherServer otherServer = new OtherServer(context);
        final Other watchNc = otherServer.getOther(null, Other.Type.watchNc);
        if (watchNc != null && !watchNc.getSync()) {
            CommitNc param = new CommitNc();
            param.setUsername(watchNc.getUser().getId());
            param.setNc(watchNc.getValue());
            Api1.commitNc(context, param, new MyJsonHttpResponseHandler(context) {
                @Override
                public void onSuccess(Context context, String str) {
                    super.onSuccess(context, str);
                    watchNc.setSync(true);
                    try {
                        otherServer.update(watchNc);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
