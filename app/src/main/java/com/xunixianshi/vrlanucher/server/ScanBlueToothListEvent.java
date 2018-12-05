package com.xunixianshi.vrlanucher.server;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/11.
 */

public class ScanBlueToothListEvent {
    private ArrayList<BluetoothDevice> mMsg;
    public ScanBlueToothListEvent(ArrayList<BluetoothDevice> msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public ArrayList<BluetoothDevice> getMsg(){
        return mMsg;
    }
}
