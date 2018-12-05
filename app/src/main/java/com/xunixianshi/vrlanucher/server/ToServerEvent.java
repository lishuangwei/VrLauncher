package com.xunixianshi.vrlanucher.server;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/29.
 */

public class ToServerEvent implements Serializable{
    private ReceiveObj mMsg;
    public ToServerEvent(ReceiveObj msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public ReceiveObj getMsg(){
        return mMsg;
    }
}
