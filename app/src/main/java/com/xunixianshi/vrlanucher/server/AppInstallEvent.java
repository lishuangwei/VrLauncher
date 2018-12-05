package com.xunixianshi.vrlanucher.server;

import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;

/**
 * Created by Administrator on 2017/10/22.
 */

public class AppInstallEvent {
    private String mMsg;
    public AppInstallEvent(String msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
