package com.xunixianshi.vrlanucher.utils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/10/4.
 */

public class VersionObj implements Serializable {
    String androidVerison;
    String frameworkVersion;
    String wifiAddress;
    String blueToothAddress;
    String serialNumber;
    String launcherVersion;
    String blueToothName;

    public String getAndroidVerison() {
        return androidVerison;
    }

    public void setAndroidVerison(String androidVerison) {
        this.androidVerison = androidVerison;
    }

    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public String getWifiAddress() {
        return wifiAddress;
    }

    public void setWifiAddress(String wifiAddress) {
        this.wifiAddress = wifiAddress;
    }

    public String getBlueToothAddress() {
        return blueToothAddress;
    }

    public void setBlueToothAddress(String blueToothAddress) {
        this.blueToothAddress = blueToothAddress;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(String launcherVersion) {
        this.launcherVersion = launcherVersion;
    }

    public String getBlueToothName() {
        return blueToothName;
    }

    public void setBlueToothName(String blueToothName) {
        this.blueToothName = blueToothName;
    }
}
