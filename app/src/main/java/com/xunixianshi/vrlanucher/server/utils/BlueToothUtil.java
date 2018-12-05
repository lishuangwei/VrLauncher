package com.xunixianshi.vrlanucher.server.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hch.viewlib.util.MLog;
import com.xunixianshi.vrlanucher.server.ScanBlueToothListEvent;
import com.xunixianshi.vrlanucher.server.interfaces.BlueDeviceListInterface;
import com.xunixianshi.vrlanucher.server.interfaces.ScanBlueDeviceListInterface;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Administrator on 2017/9/26.
 */

public class BlueToothUtil {
    private Context mContext;
    private BluetoothAdapter mBtAdapter;
    private ArrayList<BluetoothDevice> scanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> pairedBlueDeviceObjs;
    private Set<BluetoothDevice> pairedDevices;
    private ScanBlueDeviceListInterface scanBlueDeviceListInterface;
    private boolean isSend;

    public BlueToothUtil(Context context) {
        this.mContext = context;
        scanBlueDeviceObjs = new ArrayList<>();
        pairedBlueDeviceObjs = new ArrayList<>();

        // Register for broadcasts when a device is discovered
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, intentFilter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBtAdapter.isEnabled()){
            mBtAdapter.enable();
        }

        // Get a set of currently paired devices
        pairedDevices = mBtAdapter.getBondedDevices();
    }

    // 获取已配对设备
    public void getPairedBlueToothList(BlueDeviceListInterface blueDeviceListInterface) {
// If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            LogUtil.d("bluetoothDevices.size():"+pairedDevices.size());
            pairedBlueDeviceObjs.clear();
            for (BluetoothDevice device : pairedDevices) {
                pairedBlueDeviceObjs.add(device);
            }
        }
        blueDeviceListInterface.returnDevicesList(pairedBlueDeviceObjs);
    }

    // 获取周边扫描设备
    public void getScanBlueToothList(ScanBlueDeviceListInterface blueDeviceListInterface) {
        isSend = false;
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
        this.scanBlueDeviceListInterface = blueDeviceListInterface;
    }

    public void startDiscovery(){
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            MLog.d("取消扫描");
        }
        // Request discover from BluetoothAdapter
        MLog.d("开始扫描");
        mBtAdapter.startDiscovery();
    }

    int i = 0;
    //接收到蓝牙广播
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 防止重复添加
                    if (scanBlueDeviceObjs.indexOf(device) == -1) {
                        MLog.d("扫描到蓝牙："+device.getName());
                        scanBlueDeviceObjs.add(device);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                MLog.d("扫描完毕");
                EventBus.getDefault().post(new ScanBlueToothListEvent(scanBlueDeviceObjs));
                scanBlueDeviceObjs.clear();
                startDiscovery();
            }
        }
    };

    //获取本机器蓝牙地址
    public String GetLocalMacAddress() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Field field = bluetoothAdapter.getClass().getDeclaredField("mService");
            // 参数值为true，禁用访问控制检查
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }
            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            Object address = method.invoke(bluetoothManagerService);
            if (address != null && address instanceof String) {

                return (String) address;
            } else {
                return null;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //关闭设置蓝牙可见时间限制
    public void closeDiscoverableTimeout() {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
