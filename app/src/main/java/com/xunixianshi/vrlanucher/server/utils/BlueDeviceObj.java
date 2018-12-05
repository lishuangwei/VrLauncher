package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/26.
 */

public class BlueDeviceObj implements Serializable {
    String deviceName;//设备名称
    String deviceAddress;//设备mac地址
    int deviceType;//设备类型

    public BlueDeviceObj(String deviceName, String deviceAddress,int deviceType) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
