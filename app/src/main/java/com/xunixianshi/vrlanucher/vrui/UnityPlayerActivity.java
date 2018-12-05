package com.xunixianshi.vrlanucher.vrui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hch.utils.MLog;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.hch.viewlib.util.StringUtils;
import com.unity3d.player.*;
import com.xunixianshi.vrlanucher.MyApplication;
import com.xunixianshi.vrlanucher.download.DownloadUtil;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItem;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItemList;
import com.xunixianshi.vrlanucher.server.ReceiveObj;
import com.xunixianshi.vrlanucher.server.ToServerEvent;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.server.utils.DownloadListItemObj;
import com.xunixianshi.vrlanucher.server.utils.VideoListObj;
import com.xunixianshi.vrlanucher.tvui.home.HomeActivity;
import com.xunixianshi.vrlanucher.tvui.utils.FileUtils;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.utils.AppListJson;
import com.xunixianshi.vrlanucher.utils.VersionUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
//import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cwm.android_plugin_lib.CwmController;
import cwm.android_plugin_lib.CwmManager;
import cwm.android_plugin_lib.DeviceHandler;


public class UnityPlayerActivity extends Activity {
    private static final String BLUETOOTH = "yc_bluetooth";
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    WifiManager wifiManager;
    AppListUtil applistUtil;
    AudioManager audioManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    private VersionUtil versionUtil;
    Gson gson;

    // 手柄相关start
    CwmManager mCwmManager;
    BluetoothAdapter mBluetoothAdapter;
    // Stops scanning after 30 seconds.
    private static final long SCAN_PERIOD = 30000;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private AppListUtil appListUtil;
    private List<BluetoothDevice> mScanDayDreamDeviceList = new ArrayList<BluetoothDevice>();
    // 是否扫描手柄
    private boolean isScanBluetoothController = true;

    public class MyCwmController
    {
        MyCwmController(DeviceHandler dh)
        {
            this.dh = dh;
            connect = false;
        }

        public float[] rotation = {0,0,0,1};
        public boolean[] buttons = {false, false, false, false, false};
        public DeviceHandler dh;
        public boolean connect = false;
    }

    Map<String, MyCwmController> cwmControllerMap = new HashMap<String, MyCwmController>();

    // 手柄相关end

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy
        mUnityPlayer = new UnityPlayer(this);
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
        gson = builder.create();
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
        applistUtil = new AppListUtil(this);
        versionUtil = new VersionUtil(this);
        audioManager = (AudioManager) UnityPlayerActivity.this.getSystemService(Context.AUDIO_SERVICE);
//        UnityPlayer.UnitySendMessage("PlaformManager", "OnTest", "test");
//        Log.i("TEST", "test");
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "wakeLock");
        wakeLock.acquire();
        listenNearSensor();



        // 蓝牙
        // Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(BLUETOOTH, "no support lebluetooth---------------------------");
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(BLUETOOTH, "bluetooth is closed---------------------------");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            } else {
                // 初始化本地应用列表
                appListUtil = new AppListUtil(UnityPlayerActivity.this);
                appListUtil.initAppListToDb();
                versionUtil.writeVersionFile();
            }
//            int permissionCheck = 0;
//            permissionCheck = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
//            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                //注册权限
//                this.requestPermissions(
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION},
//                        1001); //Any number
//            } else {//已获得过权限
//                //进行蓝牙设备搜索操作
//            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                // 初始化本地应用列表
                appListUtil = new AppListUtil(UnityPlayerActivity.this);
                appListUtil.initAppListToDb();
                versionUtil.writeVersionFile();
            } else {
                //用户不同意

            }
        }
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TryConnect(device);
                        }
                    });
                }
            };
    private void scanLeDevice(final boolean enable) {
        if (!isScanBluetoothController)
            return;
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

//                    // 判断是否需要再次循环
//                    boolean nextScan = true;
//                    for(MyCwmController cc : cwmControllerMap.values())
//                    {
//                        if (cc.connect)
//                        {
//                            nextScan = false;
//                        }
//                    }
//                    if (nextScan)
//                    {
//                        Log.i(BLUETOOTH, "scan again---------------------------");
//                        scanLeDevice(true);
//                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            boolean scanstatus = mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (!scanstatus)
            {
                Log.i(BLUETOOTH, "mBluetoothAdapter.startLeScan return false--------------------------");
            }
            Log.i(BLUETOOTH, "scan --------------------------- true");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    public void TryConnect(BluetoothDevice device)
    {
        //        Log.i(BLUETOOTH, "scaned bluetooth device is" + device);
//        Log.i(BLUETOOTH, "scaned bluetooth：" + device.getName() + "    mac=" + device.getAddress());
        String address = device.getAddress();

        if (address.contains("ED:50:5A") || (device.getName() != null && device.getName().equals("Daydream controller")))
        {

            if (mScanDayDreamDeviceList.indexOf(device) != -1)
            {
                Log.i(BLUETOOTH, "the device already existed"+"  listsize="+mScanDayDreamDeviceList.size());
                return;
            }


            mScanDayDreamDeviceList.add(device);
            Log.i(BLUETOOTH, "try connect the device");
            DeviceHandler deviceHandler = mCwmManager.CwmBleConnectTo(address);
            cwmControllerMap.put(address, new MyCwmController(deviceHandler));
        }
    }
    // 蓝牙遥控器回调
    public CwmManager.RemoteServiceListener mCwmCallBack = new CwmManager.RemoteServiceListener() {
        @Override
        public void onConnected(String s)
        {
            Log.i(BLUETOOTH, "day dream connected -----------------------callback");
            UnityPlayer.UnitySendMessage("PlaformManager", "ReBindController", "");
            UnityPlayerActivity.this.scanLeDevice(false);
            if (cwmControllerMap.containsKey(s))
                cwmControllerMap.get(s).connect = true;
        }

        @Override
        public void onDisconnected(String s)
        {
            Log.i(BLUETOOTH, "day dream disconnected ------------------------callback");
//            UnityPlayer.UnitySendMessage("PlaformManager", "UnBindController", "");
//            UnityPlayerActivity.this.mScanDayDreamDeviceList.clear();
//
//            cwmControllerMap.remove(s);
//            UnityPlayerActivity.this.scanLeDevice(true);

            cwmControllerMap.clear();

            mScanDayDreamDeviceList.clear();
        }

        @Override
        public void onServiceDiscovery(String s, String s1)
        {
            Log.i(BLUETOOTH, "-----------------------------------------------onServiceDiscovery");
        }

        @Override
        public void onDataArrival(ArrayList<cwm.android_plugin_lib.CwmController> arrayList)
        {
            if (!arrayList.isEmpty())
            {
                // 0: touchpadButton 1:homeButton(下面的按键) 2:appButton（上面的按键） 3: vol+ 4 vol-
                int action = 0;
                for (CwmController cc : arrayList)
                {
                    if (cwmControllerMap.containsKey(cc.getMac()))
                    {
                        MyCwmController item = cwmControllerMap.get(cc.getMac());
                        System.arraycopy(cc.getRotationvector(), 0, item.rotation, 0, 4);
                        System.arraycopy(cc.getButtons(), 0, item.buttons, 0, 5);
                         Log.i(BLUETOOTH, cc.getMac()+"===0:"+item.buttons[0]+"   1:"+item.buttons[1]+"   2:"+item.buttons[2]+"   3:"+item.buttons[3]+"   4:"+item.buttons[4]);
                        if (item.buttons[1])
                        {
                            Log.i(BLUETOOTH, cc.getMac() + "-----------------------------------------------recenter");
                            reCenter(cc.getMac());
                        }

                        if (item.buttons[2] && item.buttons[4])
                        {
                            action = -1;
                            break;
                        }
                    }
                }
                if (action < 0)
                {
                    unBindController();
                }
            }
        }

        @Override
        public void onNotSupport(String s)
        {
            Log.i(BLUETOOTH, "-----------------------------------------------onNotSupport");
        }

        @Override
        public void onUnknownProblem(String s)
        {
            Log.i(BLUETOOTH, "-----------------------------------------------onUnknownProblem");
        }
    };

    public void listenNearSensor() {
             /*获取系统服务（SENSOR_SERVICE）返回一个SensorManager对象*/
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         /*通过SensorManager获取相应的（接近传感器）Sensor类型对象*/
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(mSensorEventListener, mSensor
                , SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*声明一个SensorEventListener对象用于侦听Sensor事件，并重载onSensorChanged方法*/
    private final SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float value = event.values[SensorManager.DATA_X];
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//                Log.d("Unity", "its[0]:::" + value);
                if (value == 0.0) {// 贴近手机
                    wakeLock.acquire();
                    UnityPlayer.UnitySendMessage("GameState", "OnSensorOpen", "");
                    Log.d("UnityAndroid", "open");
                } else {// 远离手机
                    if (wakeLock != null) {
                        wakeLock.release();
                    }
                    UnityPlayer.UnitySendMessage("GameState", "OnSensorClose", "");
                    Log.d("UnityAndroid", "close");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override
    protected void onDestroy() {
        mUnityPlayer.quit();
//        pauseAllDown();
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        //注销wifi广播
        unregisterReceiver(rssiReceiver);
//        unregisterReceiver(powerConnectionReceiver);
        unregisterReceiver(bluetoothStatusReceiver);

        unBindController();

        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override
    protected void onResume() {
        //注册wifi广播
        IntentFilter wififilter = new IntentFilter();
        wififilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        wififilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(rssiReceiver, wififilter);
//        // 注册电量广播
//        IntentFilter batteryfilter = new IntentFilter();
//        batteryfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        registerReceiver(powerConnectionReceiver, batteryfilter);
        // 注册蓝牙广播
        IntentFilter bluetoothfilter = new IntentFilter();
        bluetoothfilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothfilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothStatusReceiver, bluetoothfilter);


        Log.i("life", "--------------------------------------------onResume");

        reBindController();

        super.onResume();
        mUnityPlayer.resume();
    }

    // Low Memory Unity
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.d("audioManager:"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        LogUtil.d("keycode-------------"+keyCode+", keyevent="+event.toString());
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                int currentDownVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float currentDownVolumeFloat = currentDownVolume/14.0f;
                UnityPlayer.UnitySendMessage("PlayerManager", "OnVolumeSub", currentDownVolumeFloat+"");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                int currentUpVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float currentUpVolumeFloat = currentUpVolume/14.0f;
                UnityPlayer.UnitySendMessage("PlayerManager", "OnVolumeAdd", currentUpVolumeFloat+"");
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                break;
        }
        return mUnityPlayer.injectEvent(event);
    }
    //获取当前系统音量
    public float getCurrentVolume(){
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/14.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    /*API12*/
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }


    //    wifi相关 start
    public BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
        private int obtainWifiInfo() {
            // Wifi的连接速度及信号强度：
            int strength = 0;
            wifiManager = (WifiManager) UnityPlayerActivity.this.getApplicationContext().getSystemService(WIFI_SERVICE);
            // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info.getBSSID() != null) {
                // 链接信号强度，5为获取的信号强度值在5以内
                strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                // 链接速度
                int speed = info.getLinkSpeed();
                // 链接速度单位
                String units = WifiInfo.LINK_SPEED_UNITS;
                // Wifi源名称
                String ssid = info.getSSID();
            }
            //        return info.toString();
            return strength;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int s = obtainWifiInfo();
            UnityPlayer.UnitySendMessage("Wifi", "OnChange", s + "");
        }
    };
    //    wifi相关 end


    // 电量相关 start
//    public BroadcastReceiver powerConnectionReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//                    status == BatteryManager.BATTERY_STATUS_FULL;
//
//            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
////            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
////            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
//
//            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//
//            float batteryPct = level / (float)scale;
//
//            // 充电状态+电量百分比
//            UnityPlayer.UnitySendMessage("Battery", "OnChange", chargePlug+"+"+batteryPct);
//        }
//    };
    // 电量相关 end

    // 蓝牙相关 start
    private List<BluetoothDevice> tempScanBlueDeviceObjs = new ArrayList<>();
    private BroadcastReceiver bluetoothStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();
            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.i(BLUETOOTH, keyName + ">>>" + String.valueOf(b.get(keyName)));
            }
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:

                            break;
                        case BluetoothAdapter.STATE_ON:
                            UnityPlayer.UnitySendMessage(BLUETOOTH, "OnChange", "1");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            UnityPlayer.UnitySendMessage(BLUETOOTH, "OnChange", "0");
                            break;
                    }
                    break;
               /* case BluetoothDevice.ACTION_FOUND:
                    Log.i(BLUETOOTH, "搜索到设备");
                    // 通过广播接收到了BluetoothDevice
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                    {
                        // 防止重复添加
                        if (tempScanBlueDeviceObjs.indexOf(device) == -1)
                        {
                            Log.d(BLUETOOTH, "扫描到蓝牙：" + device.getName() + "    mac=" + device.getAddress());
                            tempScanBlueDeviceObjs.add(device);
                            TryConnect(device);

                        }
                    }
                    break;*/
            }
        }
    };
    // 蓝牙相关 end

    //获取用户信息
    public String getUserMessage() {
        MLog.d("getUserMessage:"+SimpleSharedPreferences.getString("userMessage",UnityPlayerActivity.this));
        return SimpleSharedPreferences.getString("userMessage",UnityPlayerActivity.this);
    }
    //清楚用户信息
    public void clearUserMessage(){
        MLog.d("clearUserMessage");
        SimpleSharedPreferences.putString("userMessage","",UnityPlayerActivity.this);
//        EventBus.getDefault().post(new CloseSelfEvent(""));//去触发AppFragment中的initAllApp
//        UnityPlayerActivity.this.finish();
//        restartApp();
    }

    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        System.exit(0);

        //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //获取mac地址
    public String getLocalMacAddressFromWifiInfo() {
        return versionUtil.getWifiAddress();
    }

    //获取蓝牙地址
    public String getBlueToothAddress() {
        return versionUtil.GetLocalMacAddress();
    }

    //获取固件版本
    public String getFrameworkVersion() {
        String messaage = null;
        try {
            Method method = Build.class.getDeclaredMethod("getString", String.class);
            method.setAccessible(true);
            messaage = (String) method.invoke(new Build(), "persist.3box.fw.version");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return messaage;
    }

    //购买指定应用
    public void buyAppVideo(String message){
        MLog.d("Android buyAppVideo"+message);
        gson = new Gson();
        ReceiveObj receiveObj = new ReceiveObj();
        receiveObj.setType(11);
        receiveObj.setMessage(message);
        EventBus.getDefault().post( new ToServerEvent(receiveObj));
    }
    //下载指定视频
    public void downLoadVideo(String downloadInfo)
    {
        Log.d("UnityAndroid", "downloadVideo"+downloadInfo);
        //downloadInfo转成object
        DownloadListItemObj downloadObj = gson.fromJson(downloadInfo,DownloadListItemObj.class);
        if(downloadObj != null){
            DownloadItem item = DownloadItem.getDownloadItemById(downloadObj.getAppResourceId()+"");
            if(item == null){
                DownloadUtil.startDownload(getApplicationContext(),downloadObj);
            }
        }
//        downTest();//测试用的
    }

    //恢复下载指定视频
    public void reloadVideo(String url){
        if(!StringUtils.isBlank(url)) {
            //先更新数据库状态  在启动恢复下载
            MLog.d("恢复下载url:"+url);
            DownloadItem.updateStatus(url,DownloadItem.DOWNLOAING);
            DownloadUtil.reDownload(getApplicationContext(), url);
        }
    }

    //获取当前下载列表进度
    public String getDownLoadInfo(){
        DownloadItemList downloadItemListBean = new DownloadItemList();
        List<DownloadItem> list = new ArrayList<>();
        if(DownloadItem.findDownloadItem() != null && DownloadItem.findDownloadItem().size() > 0){
            list.addAll(DownloadItem.findDownloadItem());
        }
        if(MyApplication.localVideoList != null && MyApplication.localVideoList.size() > 0){
            list.addAll(MyApplication.localVideoList);

        }
        for(AppDetialObj appDetialObj : applistUtil.getVrAppList()){
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setType(0);
            downloadItem.setAppType(Integer.parseInt(appDetialObj.getAppType()));
            downloadItem.setState(2);
            downloadItem.setName(appDetialObj.getAppName());
            downloadItem.setIcon(appDetialObj.getAppIcon());
            downloadItem.setUrl(appDetialObj.getAppPackageName());
            downloadItem.setProcess(1.0f);
            downloadItem.setVideoType(0);
            list.add(downloadItem);
        }
        downloadItemListBean.setList(list);

        String downLoadInfoList = gson.toJson(downloadItemListBean);
        LogUtil.d("获取所有下载列表和应用列表:"+downLoadInfoList);
        return downLoadInfoList;
    }

    private void pauseAllDown(){
        DownloadItemList downloadItemListBean = new DownloadItemList();
        for(DownloadItem downloadItem : DownloadItem.findDownloadItem()){
            if(downloadItem.getState() == 0){
                DownloadItem.updateStatus(downloadItem.getUrl(),DownloadItem.PAUSE);
                DownloadUtil.pauseDownload(getApplicationContext(),downloadItem.getUrl());
            }
        }
    }

    //获取vr应用列表
    public String getApplist(){
        LogUtil.d("获取所有vr应用列表====getApplist");
        List<AppDetialObj> AppDetialObjs = applistUtil.getVrAppList();
        AppListJson appListJson = new AppListJson();
        appListJson.setVrAppList(AppDetialObjs);
        return gson.toJson(appListJson);
    }

    //暂停下载应用
    public void pauseDownload(String url)
    {
        Log.d("UnityAndroid", "PausedDownload");
        if(!StringUtils.isBlank(url)){
            //先更新数据库状态  再暂停下载
            MLog.d("url:"+url);
            DownloadItem.updateStatus(url,DownloadItem.PAUSE);
            DownloadUtil.pauseDownload(getApplicationContext(),url);
        }
    }

    // 打开应用
    public void openApp(String name)
    {
        Log.d("UnityAndroid", "openApp");
        applistUtil.openApk(name);
    }

    // 卸载应用
    public void uninstallApp(String name)
    {
        Log.d("UnityAndroid", "uninstallApp");
        applistUtil.savrUninstallApp(name);
    }

    // 检测应用
    public boolean checkApp(String name) {return applistUtil.checkApkInDb(name);}

    // 修改应用类型
    public void updataAppType(String packageName,String appType)
    {
        Log.d("UnityAndroid", "updateAppType");
        applistUtil.updataAppTypeByPackageName(packageName,appType);
    }


    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-07 : 09:34:22
     * 获取本地所有目录的视频文件
     * 排除/sdcard/3box/download目录
     **/
    public String getAllVideo(){
        List<String> videoList = FileUtils.getAllVideo();
        VideoListObj videoListObj = new VideoListObj();
        videoListObj.setList(videoList);
        return gson.toJson(videoListObj);
    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-07 : 09:34:22
     * 更新本地所有目录的视频文件
     **/
    public void updateLocalVideo(){
        if(MyApplication.localVideoList != null && MyApplication.localVideoList.size() > 0){
            MyApplication.localVideoList.clear();
            FileUtils.getLocalAllVideo(UnityPlayerActivity.this);
        }
    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * 删除指定路径视频
     **/
    public  void deleteVideo(String filePath){
        Log.d("UnityAndroid", "deleteVideo:"+filePath);
        if(FileUtils.deleteFileByPath(filePath)){
            //删除localVideoList
            if(MyApplication.localVideoList != null
                    && MyApplication.localVideoList.size() > 0){
                Iterator<DownloadItem> it = MyApplication.localVideoList.iterator();
                while(it.hasNext()) {
                    if(filePath.equals(it.next().getUrl())) {
                        it.remove();
                    }
                }
            }
            DownloadUtil.deleteDBByUrl(filePath);
        }
    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * 删除正在下载的
     **/
    public  void deleteDownLoad(String url){
        Log.d("UnityAndroid", "deleteDownload");
        DownloadUtil.deleteServiceDownload(UnityPlayerActivity.this,url,true);
    }

    private void downTest(){
        DownloadListItemObj downloadObj = new DownloadListItemObj();
        downloadObj.setAppName("手机资源");
//        downloadObj.setAppDownloadUrl("http://xnxs.test2.vrshow.com/copyo_1bscbpqk814gomq7rt316pl1q4m9.mp4?sign=9b66935b073e6eb6a77f94bb108de52b");
        downloadObj.setAppDownloadUrl("http://file.mydrivers.com/DGSetup_1366B.exe");
        downloadObj.setAppResourceId("111");
        downloadObj.setApkType("1");
        downloadObj.setDownType("1");
        //下载应用
        if(downloadObj != null){
            DownloadUtil.startDownload(UnityPlayerActivity.this,downloadObj);
        }
    }

    // 获取蓝牙遥控信息
    public float[] getRotation()
    {
        float[] rotation = {0, 0, 0, 1};
        for (MyCwmController item : cwmControllerMap.values())
        {
            rotation = item.rotation;
            break;
        }
        return rotation;
    }

    public boolean[] getButtons()
    {
        boolean[] buttons = {false, false, false, false, false};
        for (MyCwmController item : cwmControllerMap.values())
        {
            buttons = item.buttons;
            break;
        }
        Log.i("sb", "b0="+buttons[0]+"   b1="+buttons[1]+"   b2="+buttons[2]+"   b3="+buttons[3]+"   b4="+buttons[4]);
        return buttons;
    }

    public void reCenter(String mac)
    {
        mCwmManager.CwmReCenter(cwmControllerMap.get(mac).dh, 1);
        Log.d(BLUETOOTH, "recenter success!!!!!!!!");
    }

    public void reBindController()
    {
        Log.d(BLUETOOTH, "unity rebind");
        // 蓝牙手柄初始化
        mCwmManager = new CwmManager(this, mCwmCallBack);
        scanLeDevice(true);
    }
    public void unBindController()
    {
        Log.d(BLUETOOTH, "unity unbind");
        scanLeDevice(false);
        for (MyCwmController item : cwmControllerMap.values())
        {
            mCwmManager.CwmDisconnectBleDevice(item.dh);
        }
        cwmControllerMap.clear();

        mScanDayDreamDeviceList.clear();
        mCwmManager.CwmReleaseResource();

        // 这里都释放资源了所以必然解绑定，不依赖于回调，但是连接成功还是依赖回调
        UnityPlayer.UnitySendMessage("PlaformManager", "UnBindController", "");

        Log.d(BLUETOOTH, "unity unbind");
    }
}
