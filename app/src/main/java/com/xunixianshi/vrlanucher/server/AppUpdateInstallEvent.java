package com.xunixianshi.vrlanucher.server;

/**
 * Created by Administrator on 2017/10/22.
 */

public class AppUpdateInstallEvent {
    private String mMsg;
    public AppUpdateInstallEvent(String msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
