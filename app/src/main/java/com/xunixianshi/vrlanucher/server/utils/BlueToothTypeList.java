package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/26.
 */

public class BlueToothTypeList implements Serializable {
    int listType; //1 表示已配对设备  2 表示实时搜索设备
    ArrayList<BlueDeviceObj> devicelist;

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public ArrayList<BlueDeviceObj> getDevicelist() {
        return devicelist;
    }

    public void setDevicelist(ArrayList<BlueDeviceObj> devicelist) {
        this.devicelist = devicelist;
    }
}
