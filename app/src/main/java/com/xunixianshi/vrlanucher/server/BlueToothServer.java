package com.xunixianshi.vrlanucher.server;

import android.app.AlarmManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hch.viewlib.util.MLog;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.hch.viewlib.util.StringUtils;
import com.unity3d.player.UnityPlayer;
import com.xunixianshi.vrlanucher.MyApplication;
import com.xunixianshi.vrlanucher.R;
import com.xunixianshi.vrlanucher.download.DownloadUtil;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItem;
import com.xunixianshi.vrlanucher.server.utils.AppListObj;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.server.utils.BlueDeviceObj;
import com.xunixianshi.vrlanucher.server.utils.BlueToothTypeList;
import com.xunixianshi.vrlanucher.server.utils.DownloadListItemObj;
import com.xunixianshi.vrlanucher.server.utils.DownloadListObj;
import com.xunixianshi.vrlanucher.server.utils.HidInputUtil;
import com.xunixianshi.vrlanucher.server.utils.HidVideoUtil;
import com.xunixianshi.vrlanucher.server.utils.WifiDetailObj;
import com.xunixianshi.vrlanucher.tvui.adapter.AppBean;
import com.xunixianshi.vrlanucher.tvui.home.HomeActivity;
import com.xunixianshi.vrlanucher.tvui.utils.FileUtils;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.utils.VersionUtil;
import com.xunixianshi.vrlanucher.vrui.UnityPlayerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/9/26.
 */

public class BlueToothServer extends Service {
    //耳机的广播
    public static final String TAGLISTEN = "android.intent.action.HEADSET_PLUG";
    //usb线的广播
    private final static String TAGUSB = "android.hardware.usb.action.USB_STATE";
    //外设的广播
    public static final String TAGIN = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String TAGOUT = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private boolean BOOLEAN = false;

    BluetoothSPP bluetoothSPP;
    WifiManager wifiManager;
    private ArrayList<BluetoothDevice> allBlueToothList;
    Gson gson;
    int bluetoothConnectStatus = 1; // 1 表示未连接蓝牙   2 表示连接失败  3表示连接成功
    private Context mContext;
    //    private BlueToothUtil blueToothUtils;
    private AppListUtil appListUtil;
    private BluetoothAdapter mBluetoothAdapter;
    private BlueBroadcastReceiver mBroadcastReceiver;
    private BluetoothDevice mConnectDevice;
    private String HID_NAME = "";  // 连接的蓝牙设备名
    private String HID_ADDR = "";  //连接的蓝牙设备地址
    private HidInputUtil mHidInputUtil;
    private HidVideoUtil mHidVideoUtil;
    int isFirst = 0;
    private ArrayList<BluetoothDevice> scanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> tempScanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> temp1ScanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> resultBlueDeviceObjs;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> pairedBlueDeviceObjs;
    private VersionUtil versionUtil;

    private PackageManager pm;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("BlueToothServer~~~~~~~~~~~~~~~~~~~~~~~onCreate");
        mContext = this;
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        bluetoothSPP = new BluetoothSPP(mContext);
//        blueToothUtils = new BlueToothUtil(mContext);
        appListUtil = new AppListUtil(mContext);
        versionUtil = new VersionUtil(mContext);
        scanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        tempScanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        temp1ScanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        resultBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        pairedBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        allBlueToothList = new ArrayList<>();
        EventBus.getDefault().register(this);
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
        gson = builder.create();
        initBlueToothListence();
        IntentFilter filter = new IntentFilter();
        //筛选的条件
        filter.addAction(TAGIN);
        filter.addAction(TAGOUT);
        filter.addAction(TAGUSB);
        registerReceiver(receiver, filter);
        mBroadcastReceiver = new BlueBroadcastReceiver();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 4.0以上才支持HID模式
        if (Build.VERSION.SDK_INT >= 17) {
            mHidInputUtil = HidInputUtil.getInstance(this);
            mHidVideoUtil = HidVideoUtil.getInstance(this);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        this.registerReceiver(mBroadcastReceiver, intentFilter);
//        if (mConnectDevice == null) {
//            //刚进入activity可能获取不到，因为getProfileProxy是异步的，可能还没有成功。
//            mConnectDevice = mHidUtil.getConnectedDevice(HID_ADDR);
//            Log.i(TAG, "getConnected device:" + mConnectDevice);
//        }
        //开启蓝牙spp服务
        if (!bluetoothSPP.isServiceAvailable()) {
            LogUtil.d("BlueToothServer~~~~~~~~~~~~~~~~~~~~~~~开启蓝牙spp服务");
            bluetoothSPP.setupService();
            bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        //如果wifi没有开启则开启wifi
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        bluetoothSPP.getBluetoothAdapter().setName("3box_" + GetLocalMacAddress());
        versionUtil.writeVersionFile();
        closeDiscoverableTimeout();
        startDiscovery();
        //获取所有目录的视频文件
        MyApplication.localVideoList = FileUtils.getLocalAllVideo(BlueToothServer.this);
        if(MyApplication.localVideoList != null){
            MLog.d("本地视频-----："+MyApplication.localVideoList.size());
            for (DownloadItem downloadItem:MyApplication.localVideoList) {
                MLog.d("本地视频-----："+downloadItem.getName()+"--path::"+downloadItem.getUrl());
            }
        }else{
            MLog.d("~~~~~~~~~~没有找到本地视频");
        }
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "wakeLock");
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        LogUtil.d("BlueToothServer~~~~~~~~~~~~~~~~~~~~~~~onDestroy");
        if (wakeLock != null) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d("BlueToothServer~~~~~~~~~~~~~~~~~~~~~~~onStartCommand");
        //开启蓝牙spp服务
        if (!bluetoothSPP.isServiceAvailable()) {
            LogUtil.d("BlueToothServer~~~~~~~~~~~~~~~~~~~~~~~开启蓝牙spp服务");
            bluetoothSPP.setupService();
            bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initBlueToothListence() {
        if (!bluetoothSPP.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , getString(R.string.bluetooth_not_available)
                    , Toast.LENGTH_SHORT).show();
        }
        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                LogUtil.d("message" + message);
                ReceiveObj receiveObj = gson.fromJson(message, ReceiveObj.class);
                switch (receiveObj.getType()) {
                    case 1://获取用户信息
                        SimpleSharedPreferences.putString("userMessage", receiveObj.getMessage(), mContext);
                        LogUtil.d("获取用户信息" + SimpleSharedPreferences.getString("userMessage", mContext));
                        UnityPlayer.UnitySendMessage("GameState", "OnUserInfoChange", "");
                        break;
                    case 2://返回蓝牙列表
                        LogUtil.d("返回蓝牙列表");
                        //清楚蓝牙列表记录
                        startDiscovery();
                        //返回已配对蓝牙列表
                        pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            LogUtil.d("bluetoothDevices.size():" + pairedDevices.size());
                            pairedBlueDeviceObjs.clear();
                            for (BluetoothDevice device : pairedDevices) {
                                pairedBlueDeviceObjs.add(device);
                            }
                        }
                        //添加所有配对的蓝牙列表
                        allBlueToothList.clear();
                        allBlueToothList.addAll(pairedBlueDeviceObjs);
                        ArrayList<BlueDeviceObj> pairedBlueDeviceObj = new ArrayList<BlueDeviceObj>();
                        for (BluetoothDevice bluetoothDevice : pairedBlueDeviceObjs) {
                            pairedBlueDeviceObj.add(new BlueDeviceObj(bluetoothDevice.getName(), bluetoothDevice.getAddress(), bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
                        }
                        ReceiveObj pairedReceiveObj = new ReceiveObj();
                        BlueToothTypeList pairedBlueToothTypeList = new BlueToothTypeList();
                        pairedBlueToothTypeList.setListType(1);
                        pairedBlueToothTypeList.setDevicelist(pairedBlueDeviceObj);
                        String pairedBlueToothTypeListStr = gson.toJson(pairedBlueToothTypeList);
                        pairedReceiveObj.setType(2);
                        pairedReceiveObj.setMessage(pairedBlueToothTypeListStr);
                        String pairedReceiveObjStr = gson.toJson(pairedReceiveObj);
                        bluetoothSPP.send(pairedReceiveObjStr, true);
                        LogUtil.d("pairedReceiveObjStr:" + pairedReceiveObjStr);
                        //返回未配对蓝牙列表
                        ArrayList<BlueDeviceObj> blueDeviceObjs = new ArrayList<BlueDeviceObj>();
                        for (BluetoothDevice bluetoothDevice : scanBlueDeviceObjs) {
                            blueDeviceObjs.add(new BlueDeviceObj(StringUtils.isBlank(bluetoothDevice.getName()) ? bluetoothDevice.getAddress() : bluetoothDevice.getName(),
                                    bluetoothDevice.getAddress(), bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
                        }
                        allBlueToothList.addAll(scanBlueDeviceObjs);
                        ReceiveObj scanBlueReceiveObj = new ReceiveObj();
                        BlueToothTypeList blueToothTypeList = new BlueToothTypeList();
                        blueToothTypeList.setListType(2);
                        blueToothTypeList.setDevicelist(blueDeviceObjs);
                        String blueToothTypeListStr = gson.toJson(blueToothTypeList);
                        scanBlueReceiveObj.setType(2);
                        scanBlueReceiveObj.setMessage(blueToothTypeListStr);
                        String scanBlueReceiveObjStr = gson.toJson(scanBlueReceiveObj);
                        LogUtil.d("scanBlueReceiveObjStr:" + scanBlueReceiveObjStr);
                        bluetoothSPP.send(scanBlueReceiveObjStr, true);
//                        resultBlueDeviceObjs.clear();
//                        resultBlueDeviceObjs.addAll(allBlueToothList);
                        break;
                    case 3://连接指定蓝牙
                        LogUtil.d("连接指定外设蓝牙");
                        connectBlurTooth(receiveObj.getMessage());
                        break;
                    case 4://返回本机所有应用列表
                        LogUtil.d("返回本机所有应用列表");
                        ReceiveObj appListReceiveObj = new ReceiveObj();
                        AppListObj appListObj = new AppListObj();
                        ArrayList<AppDetialObj> applist = new ArrayList<AppDetialObj>();
                        applist.addAll(appListUtil.getAppList());
                        appListObj.setApplist(applist);
                        String appListObjJsonStr = gson.toJson(appListObj);
                        appListReceiveObj.setType(4);
                        appListReceiveObj.setMessage(appListObjJsonStr);
                        String appLisreceiveObjStr = gson.toJson(appListReceiveObj);
                        LogUtil.d("appLisreceiveObjStr:" + appLisreceiveObjStr);
                        bluetoothSPP.send(appLisreceiveObjStr, true);
                        break;
                    case 5://请求卸载指定apk
                        LogUtil.d("uninstall apk");
                        int uninstallRows = appListUtil.savrUninstallApp(receiveObj.getMessage());
                        ReceiveObj uninstallReceiveObj = new ReceiveObj();
                        uninstallReceiveObj.setType(5);
                        if (uninstallRows == 0) {//成功
                            uninstallReceiveObj.setMessage("0");
                        } else {//失败
                            uninstallReceiveObj.setMessage("1");
                        }
                        String uninstallReceiveObjStr = gson.toJson(uninstallReceiveObj);
                        bluetoothSPP.send(uninstallReceiveObjStr, true);
                        break;
                    case 6://请求更新指定apk类型
                        LogUtil.d("请求更新指定apk类型");
                        AppDetialObj appDetialObj = gson.fromJson(receiveObj.getMessage(), AppDetialObj.class);
                        int updataAppRows = appListUtil.updataAppType(appDetialObj);
                        ReceiveObj updataAppReceiveObj = new ReceiveObj();
                        updataAppReceiveObj.setType(6);
                        LogUtil.d("请求更新指定apk类型" + updataAppRows);
                        if (updataAppRows == 0) {//成功
                            updataAppReceiveObj.setMessage("0");
                        } else {//失败
                            updataAppReceiveObj.setMessage("1");
                        }
                        String updataAppReceiveObjStr = gson.toJson(updataAppReceiveObj);
                        bluetoothSPP.send(updataAppReceiveObjStr, true);
                        EventBus.getDefault().post(new AppUpdateInstallEvent(""));//去触发AppFragment中的initAllApp
                        break;
                    case 7://下载指定应用
                        LogUtil.d("download app " + receiveObj.getMessage());
                        DownloadListObj downloadListObj = gson.fromJson(receiveObj.getMessage(), DownloadListObj.class);
                        for (DownloadListItemObj downloadListItemObj : downloadListObj.getDownloadList()) {
                            MLog.d("" + downloadListItemObj.getAppDownloadUrl());
                            downloadListItemObj.setDownType("0");// 0 应用  1 视频   2 系统固件
                            LogUtil.d("download appPackageName:" + downloadListItemObj.getAppPackageName());
                            DownloadUtil.startDownload(mContext, downloadListItemObj);
                        }
                        break;
                    case 8://连接指定wifi
                        LogUtil.d("wifi");
                        WifiDetailObj wifiDetialObj = gson.fromJson(receiveObj.getMessage(), WifiDetailObj.class);
                        connectWifi(wifiDetialObj.getWifiName(), wifiDetialObj.getWifiPassWord(), wifiDetialObj.getWifiType());
                        break;
                    case 9://升级3box固件
                        LogUtil.d("升级3box固件");
                        DownloadListItemObj downloadListItemObj = gson.fromJson(receiveObj.getMessage(), DownloadListItemObj.class);
                        downloadListItemObj.setDownType("2");
                        DownloadUtil.startDownload(mContext, downloadListItemObj);
                        break;
                    case 10://同步3box时间
                        LogUtil.d("同步3box时间");
//                                   String defaultStr =  TimeZone.getDefault().getDisplayName();//获取默认时区
//                            LogUtil.d("defaultStr:"+defaultStr);
                        setTimeZone(receiveObj.getMessage());
                        break;
                    case 12://返回固件版本号
                        String version = versionUtil.getFrameworkVersion();
                        ReceiveObj resultObj = new ReceiveObj();
                        resultObj.setType(12);
                        resultObj.setMessage(version);
                        String objStr = gson.toJson(resultObj);
                        LogUtil.d("返回固件版本号" + objStr);
                        bluetoothSPP.send(objStr, true);
                        break;
                    case 13://返回时区
                        String defaultStr = TimeZone.getDefault().getDisplayName();//获取默认时区
                        ReceiveObj timeResultObj = new ReceiveObj();
                        timeResultObj.setType(13);
                        timeResultObj.setMessage(defaultStr);
                        String timeObjStr = gson.toJson(timeResultObj);
                        LogUtil.d("返回时区" + timeObjStr);
                        bluetoothSPP.send(timeObjStr, true);
                        break;
                }
            }
        });

        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                bluetoothConnectStatus = 1;
                LogUtil.d("Not connect");
//                textStatus.setText("Status : Not connect");
            }

            public void onDeviceConnectionFailed() {
                bluetoothConnectStatus = 2;
                LogUtil.d("Connection failed");
//                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                bluetoothConnectStatus = 3;
                LogUtil.d("Connected to " + name);
                ReceiveObj successReceiveObj = new ReceiveObj();
                successReceiveObj.setType(200);
                successReceiveObj.setMessage("");
                String successReceiveObjStr = gson.toJson(successReceiveObj);
                bluetoothSPP.send(successReceiveObjStr, true);
//                textStatus.setText("Status : Connected to " + name);
            }
        });
    }



    private boolean getBlueToothStatus() {
        boolean bluetoothStatus = false;
        switch (bluetoothConnectStatus) {
            case 1:
//                Toast.makeText(this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                bluetoothStatus = false;
                break;
            case 2:
//                Toast.makeText(this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                bluetoothStatus = false;
                break;
            case 3:
                bluetoothStatus = true;
                break;
        }
        return bluetoothStatus;
    }

    //发送蓝牙消息
//    public void sendBlueToothMessage(int type, String messgae){
//        if(getBlueToothStatus()){
//            ReceiveObj  sendReceiveObj = new ReceiveObj();
//            sendReceiveObj.setType(type);
//            sendReceiveObj.setMessage(messgae);
//            String sendMessage = gson.toJson(sendReceiveObj);
//            bluetoothSPP.send(sendMessage,true);
//        }
//    }

    //连接指定蓝牙
    private void connectBlurTooth(String blueToothAddress) {
        LogUtil.d("blueToothAddress---" + blueToothAddress);
        mConnectDevice = getAddressDevice(blueToothAddress);
        HID_NAME = mConnectDevice.getName();
        HID_ADDR = mConnectDevice.getAddress();
        LogUtil.d("getName---" + mConnectDevice.getName());
        LogUtil.d("getAddress---" + mConnectDevice.getAddress());
        mBluetoothAdapter.cancelDiscovery();
        if (mConnectDevice == null) {
            return;
        }
        int typeNum = mConnectDevice.getBluetoothClass().getMajorDeviceClass();
        switch (typeNum){
            case 1024:
                if (!mHidVideoUtil.isBonded(mConnectDevice)) {
                    mHidVideoUtil.pair(mConnectDevice);
                } else {
                    mHidVideoUtil.connect(mConnectDevice);
                }
                break;
            case 1280:
                if (!mHidInputUtil.isBonded(mConnectDevice)) {
                    mHidInputUtil.pair(mConnectDevice);
                } else {
                    mHidInputUtil.connect(mConnectDevice);
                }
                break;
        }

    }

    private BluetoothDevice getAddressDevice(String blueToothAddress) {
        BluetoothDevice thisBluetoothDevice = null;
        for (BluetoothDevice bluetoothDevice : allBlueToothList) {
            if (bluetoothDevice.getAddress().equals(blueToothAddress)) {
                thisBluetoothDevice = bluetoothDevice;
                break;
            }
        }
        return thisBluetoothDevice;
    }

    //连接wifi
    private void connectWifi(String ssid, String password, int wifiType) {
        Log.d("Cache_Log", "ssid: " + ssid + "password: " + password + "wifiType: " + wifiType);
        int netId = wifiManager.addNetwork(createWifiConfig(ssid, password, wifiType));
        boolean enable = wifiManager.enableNetwork(netId, true);
        Log.d("Cache_Log", "enable: " + enable);
        boolean reconnect = wifiManager.reconnect();
        Log.d("Cache_Log", "reconnect: " + reconnect);
    }

    //根据wifi加密类型配置wifi设置
    private WifiConfiguration createWifiConfig(String SSID, String password, int type) {
        WifiConfiguration config = null;
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig == null) continue;
                if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
                    config = existingConfig;
                    break;
                }
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    //设置时区
    public void setTimeZone(String timeZone) {
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setTimeZone(timeZone);
//        mAlarmManager.setTimeZone("Asia/Taipei");
    }

    @Subscribe
    public void onEventMainThread(ToServerEvent event) {
        ReceiveObj receiveObj = event.getMsg();
        MLog.d("蓝牙服务接收到消息" + receiveObj.getMessage());
        if (receiveObj.getType() == 11) {
            String receiveObjStr = gson.toJson(receiveObj);
            bluetoothSPP.send(receiveObjStr, true);
        }
        if (receiveObj.getType() == 3) {
            connectBlurTooth(receiveObj.getMessage());
        }
    }

    public void getScanBlueDeviceObjs(ArrayList<BluetoothDevice> bluetoothDevices) {
        MLog.d("蓝牙服务接收到消息" + bluetoothDevices.size());
        //记录当前扫描到的蓝牙列表
        scanBlueDeviceObjs.clear();
        scanBlueDeviceObjs.addAll(bluetoothDevices);

//        //删除上一次记录的蓝牙列表
//        allBlueToothList.removeAll(tempScanBlueDeviceObjs);
//        //记录上一次扫描到的蓝牙列表临时记录
//        tempScanBlueDeviceObjs.clear();
//        tempScanBlueDeviceObjs.addAll(bluetoothDevices);
//        //更新当前扫描到的所有的蓝牙列表
//        allBlueToothList.addAll(tempScanBlueDeviceObjs);
//            ArrayList<BlueDeviceObj> blueDeviceObjs = new ArrayList<BlueDeviceObj>();
//            for (BluetoothDevice bluetoothDevice : event.getMsg()) {
//                blueDeviceObjs.add(new BlueDeviceObj(StringUtils.isBlank(bluetoothDevice.getName()) ? bluetoothDevice.getAddress() : bluetoothDevice.getName(),
//                        bluetoothDevice.getAddress(), bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
//            }
//            ReceiveObj receiveObj = new ReceiveObj();
//            BlueToothTypeList blueToothTypeList = new BlueToothTypeList();
//            blueToothTypeList.setListType(2);
//            blueToothTypeList.setDevicelist(blueDeviceObjs);
//            String blueToothTypeListStr = gson.toJson(blueToothTypeList);
//            receiveObj.setType(2);
//            receiveObj.setMessage(blueToothTypeListStr);
//            String receiveObjStr = gson.toJson(receiveObj);
//            bluetoothSPP.send(receiveObjStr, true);
//            blueToothUtils.startDiscovery();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //判断外设
            if (action.equals(TAGIN)) {
                LogUtil.i("外设已经连接");
                //Toast.makeText(context, "外设已经连接", Toast.LENGTH_SHORT).show();
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> map = usbManager.getDeviceList();
                for (UsbDevice device : map.values()) {
                    if (device.getVendorId() == 11036) {
                        startActivity(new Intent(mContext, UnityPlayerActivity.class));
                    }
                }
            }
            if (action.equals(TAGOUT)) {
                LogUtil.i("外设已经移除");
                if (BOOLEAN) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    HashMap<String, UsbDevice> map = usbManager.getDeviceList();
                    startActivity(new Intent(mContext, HomeActivity.class));
                }
            }
            BOOLEAN = true;
        }

    };

    private class BlueBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive:" + action);
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 通过广播接收到了BluetoothDevice
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 防止重复添加
                    if (temp1ScanBlueDeviceObjs.indexOf(device) == -1) {
                        MLog.d("扫描到蓝牙：" + device.getName());
                        temp1ScanBlueDeviceObjs.add(device);
                        getScanBlueDeviceObjs(temp1ScanBlueDeviceObjs);
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                Log.i(TAG, "name:" + name + ",address:" + address + ",bondstate:" + device.getBondState());
                if ((address != null && address.equals(HID_ADDR)) || (name != null && name.equals(HID_NAME))) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        int typeNum = mConnectDevice.getBluetoothClass().getMajorDeviceClass();
                        switch (typeNum){
                            case 1024:
                                mHidVideoUtil.connect(device);
                                break;
                            case 1280:
                                mHidInputUtil.connect(device);
                                break;
                        }
                        // 移除已绑定的设备
                        temp1ScanBlueDeviceObjs.remove(device);
                        scanBlueDeviceObjs.remove(device);
                    }
                }
            } else if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "state=" + state + ",device=" + device);
//                if(state == BluetoothProfile.STATE_CONNECTED){
//                    Toast.makeText(MainActivity.this, R.string.connnect_success, Toast.LENGTH_LONG).show();
//                } else if(state == BluetoothProfile.STATE_DISCONNECTED){
//                    Toast.makeText(MainActivity.this, R.string.disconnected, Toast.LENGTH_LONG).show();
//                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                MLog.d("扫描完毕");
                temp1ScanBlueDeviceObjs.clear();
//                startDiscovery();
            }
        }
    }

    public void startDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            MLog.d("取消扫描");
        } else {
            // Request discover from BluetoothAdapter
            MLog.d("开始扫描");
            mBluetoothAdapter.startDiscovery();
        }
    }

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
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onEventMainThread(AppInstallEvent event) {
        final String packageName = event.getMsg();
        LogUtil.d("AppInstallEvent" + packageName);
        //如果已入库则不需要修改，  如果未入库则需要入库
        if (appListUtil.checkApkInDb(packageName)) {
            LogUtil.d("已经入库了");
        } else {
            pm = getPackageManager();
            final DownloadItem downloadItem = DownloadItem.getDownloadItemByPackageName(packageName);
            if (downloadItem != null) { // 是助手推送的应用
                try {
                    //先检查数据库是否存在  如果存在则不需要入库   不过不存在则入库
                    ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    AppBean appBean = new AppBean();
                    appBean.setPackageName(info.packageName);
                    appBean.setName(info.loadLabel(pm).toString());
                    appBean.setIcon(info.loadIcon(pm));
                    appBean.setResourceId(String.valueOf(downloadItem.getRId()));
                    appBean.setType(String.valueOf(downloadItem.getAppType()));
                    appListUtil.insterAppInfo(appBean);
                    DownloadItem.deleteByPackage(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else { // 其他途径安装的应用
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    AppBean appBean = new AppBean();
                    appBean.setPackageName(info.packageName);
                    appBean.setName(info.loadLabel(pm).toString());
                    appBean.setIcon(info.loadIcon(pm));
                    appListUtil.insterAppInfo(appBean);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe
    public void onEventMainThread(AppUninstallEvent event) {
        String packageName = event.getMsg();
        //如果存则删除 不存在也不管
        if (appListUtil.checkApkInDb(packageName)) {
            appListUtil.deleteAppByPackageName(packageName);
        }
    }

    @Subscribe
    public void onEventMainThread(CloseSelfEvent event) {
        //重启unityPlayActivity
        LogUtil.d("CloseSelfEvent");
        startActivity(new Intent(mContext, UnityPlayerActivity.class));

    }

}
