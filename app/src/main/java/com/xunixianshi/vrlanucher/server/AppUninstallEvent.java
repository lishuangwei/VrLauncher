package com.xunixianshi.vrlanucher.server;

/**
 * Created by Administrator on 2017/10/22.
 */

public class AppUninstallEvent {
    private String mMsg;
    public AppUninstallEvent(String msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
