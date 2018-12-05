package com.xunixianshi.vrlanucher.server.interfaces;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/26.
 */

public interface ScanBlueDeviceListInterface {
    void returnDevicesList(ArrayList<BluetoothDevice> blueDeviceObjs);
}
