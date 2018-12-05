package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/26.
 */

public class BlueToothListObj implements Serializable {
    ArrayList<BlueDeviceObj> pairedBlueDevicelist;//匹配
    ArrayList<BlueDeviceObj> scanBlueDevicelist;//未匹配

    public ArrayList<BlueDeviceObj> getPairedBlueDevicelist() {
        return pairedBlueDevicelist;
    }

    public void setPairedBlueDevicelist(ArrayList<BlueDeviceObj> pairedBlueDevicelist) {
        this.pairedBlueDevicelist = pairedBlueDevicelist;
    }

    public ArrayList<BlueDeviceObj> getScanBlueDevicelist() {
        return scanBlueDevicelist;
    }

    public void setScanBlueDevicelist(ArrayList<BlueDeviceObj> scanBlueDevicelist) {
        this.scanBlueDevicelist = scanBlueDevicelist;
    }
}
