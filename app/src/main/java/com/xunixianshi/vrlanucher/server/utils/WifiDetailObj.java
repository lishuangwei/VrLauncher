package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/27.
 */

public class WifiDetailObj implements Serializable {
    String wifiName;
    String wifiPassWord;
    int wifiType;

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPassWord() {
        return wifiPassWord;
    }

    public void setWifiPassWord(String wifiPassWord) {
        this.wifiPassWord = wifiPassWord;
    }

    public int getWifiType() {
        return wifiType;
    }

    public void setWifiType(int wifiType) {
        this.wifiType = wifiType;
    }
}
