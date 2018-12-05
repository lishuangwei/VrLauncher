package com.xunixianshi.vrlanucher.server;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/26.
 */

public class ReceiveObj implements Serializable {
    int type;
    String message;

    public ReceiveObj() {
    }

    public ReceiveObj(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
