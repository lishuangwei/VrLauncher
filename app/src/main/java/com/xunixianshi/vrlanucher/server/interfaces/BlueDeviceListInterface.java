package com.xunixianshi.vrlanucher.server.interfaces;

import android.bluetooth.BluetoothDevice;

import com.xunixianshi.vrlanucher.server.utils.BlueDeviceObj;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/26.
 */

public interface BlueDeviceListInterface {
    void returnDevicesList(ArrayList<BluetoothDevice> blueDeviceObjs);
}
