package com.xunixianshi.vrlanucher.server;

import android.app.AlarmManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hch.utils.MLog;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.hch.viewlib.util.StringUtils;
import com.unity3d.player.UnityPlayer;
import com.xunixianshi.vrlanucher.download.DownloadUtil;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.server.utils.AppListObj;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.server.utils.BlueDeviceObj;
import com.xunixianshi.vrlanucher.server.utils.BlueToothListObj;
import com.xunixianshi.vrlanucher.server.utils.BlueToothTypeList;
import com.xunixianshi.vrlanucher.server.utils.DownloadListItemObj;
import com.xunixianshi.vrlanucher.server.utils.DownloadListObj;
import com.xunixianshi.vrlanucher.server.utils.HidInputUtil;
import com.xunixianshi.vrlanucher.server.utils.HidVideoUtil;
import com.xunixianshi.vrlanucher.server.utils.WifiDetailObj;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.utils.HexUtils;
import com.xunixianshi.vrlanucher.utils.VersionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserService extends Service {

    private static final String TAG = AdvertiserService.class.getSimpleName();
    private boolean connentResult;

    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid WIFI_NAME_UUID = ParcelUuid
            .fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid WIFI_PW_UUID = ParcelUuid
            .fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid WIFI_TYPE_UUID = ParcelUuid
            .fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid WIFI_IP_UUID = ParcelUuid
            .fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    private StringBuffer mReceiveData;

    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private String wifiName = "";
    private String wifiPw = "";

    private WifiManager mWifiManager;
    private NetworkConnectChangedReceiver receiver;
    private BluetoothGattCharacteristic wifiTypeCharacteristic;
    private BluetoothGattCharacteristic wifiIPCharacteristic;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    public static boolean running = false;

    public static final String ADVERTISING_FAILED =
            "com.example.android.bluetoothadvertisements.advertising_failed";
    public static final String START_SOCKET =
            "com.example.android.bluetoothadvertisements.start_socket";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    public static final int ADVERTISING_TIMED_OUT = 6;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private AdvertiseCallback mAdvertiseCallback;

    private Handler mHandler;

    private Runnable timeoutRunnable;

    private ServerSocket m_serverSocket = null;
    private int nServerPort = 8888;
    public Socket socketClient;
    private Gson gson;
//    private BlueToothUtil blueToothUtils;
    private AppListUtil appListUtil;
    private Context mContext;
    private WifiManager wifiManager;
    private BluetoothSPP bluetoothSPP;
    int isFirst = 0;
    private ArrayList<BluetoothDevice> scanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> tempScanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> temp1ScanBlueDeviceObjs;
    private ArrayList<BluetoothDevice> resultBlueDeviceObjs;
    private ArrayList<BluetoothDevice> allBlueToothList;
    private BluetoothDevice mConnectDevice;
    private String HID_NAME = "";  // 连接的蓝牙设备名
    private String HID_ADDR = "";  //连接的蓝牙设备地址
    private HidInputUtil mHidInputUtil;
    private HidVideoUtil mHidVideoUtil;
    private BlueBroadcastReceiver mBroadcastReceiver;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> pairedBlueDeviceObjs;

    /**
     * Length of time to allow advertising before automatically shutting off. (10 minutes)
     */
    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    @Override
    public void onCreate() {
        running = true;
//        initialize();
//        startAdvertising();
//        setTimeout();
        super.onCreate();
        LogUtil.d("AdvertiserService~~~~~~~~~~~~~~~~~~~~~~~onCreate");
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
        gson = builder.create();
        mContext = this;
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        bluetoothSPP = new BluetoothSPP(mContext);
//        blueToothUtils = new BlueToothUtil(mContext);
        appListUtil = new AppListUtil(mContext);
        scanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        pairedBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        tempScanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        temp1ScanBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        resultBlueDeviceObjs = new ArrayList<BluetoothDevice>();
        allBlueToothList = new ArrayList<>();
        EventBus.getDefault().register(this);
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
            bluetoothSPP.setupService();
            bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        initialize();
        startAdvertising();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        running = false;
        stopAdvertising();
        mHandler.removeCallbacks(timeoutRunnable);
        stopForeground(true);
        super.onDestroy();
        if (null != receiver) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    mGattServer = mBluetoothManager.openGattServer(this, mCallBack);
//                    mBluetoothAdapter.setName("3box_"+BluetoothAdapter.getDefaultAdapter().getAddress());
                } else {
//                    Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
                }
            } else {
//                Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
            }
        }

        if (null == mWifiManager) {
            mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        }

        if (null == receiver) {
            receiver = new NetworkConnectChangedReceiver();
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, mIntentFilter);
        }
    }

    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private void setTimeout() {
        mHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "AdvertiserService has reached timeout of " + TIMEOUT + " milliseconds, stopping advertising.");
                sendFailureIntent(ADVERTISING_TIMED_OUT);
                stopSelf();
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }

    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {
//        goForeground();
        stopAdvertising();
        Log.d(TAG, "Service: Starting Advertising");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                        mAdvertiseCallback);

                BluetoothGattService service = new BluetoothGattService(Service_UUID.getUuid(), BluetoothGattService
                        .SERVICE_TYPE_PRIMARY);

                BluetoothGattCharacteristic name_characteristic = new BluetoothGattCharacteristic(WIFI_NAME_UUID.getUuid
                        (), 0x0A, 0x11);
                service.addCharacteristic(name_characteristic);

                BluetoothGattCharacteristic pw_characteristic = new BluetoothGattCharacteristic(WIFI_PW_UUID.getUuid
                        (), 0x0B, 0x11);
                service.addCharacteristic(pw_characteristic);

                wifiTypeCharacteristic = new BluetoothGattCharacteristic(WIFI_TYPE_UUID.getUuid(),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PERMISSION_READ);
                service.addCharacteristic(wifiTypeCharacteristic);

                wifiIPCharacteristic = new BluetoothGattCharacteristic(WIFI_IP_UUID.getUuid(),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PERMISSION_READ);
                service.addCharacteristic(wifiIPCharacteristic);


//                //add a read characteristic.
//                // 当是ios设备连接过来时，需添加BluetoothGattCharacteristic.PROPERTY_INDICATE或者notify进行兼容。
//                BluetoothGattCharacteristic mCharacteristicRead = new BluetoothGattCharacteristic(WIFI_UUID.getUuid()
//                , BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_INDICATE
//                , BluetoothGattCharacteristic.PERMISSION_READ);
//                //add a descriptor
//                BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(WIFI_UUID.getUuid
//                        (), BluetoothGattCharacteristic.PERMISSION_WRITE);
//                mCharacteristicRead.addDescriptor(descriptor);
//                service.addCharacteristic(mCharacteristicRead);
//
//                BluetoothGattCharacteristic write = new BluetoothGattCharacteristic(WIFI_UUID.getUuid(),
//                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_INDICATE,
//                BluetoothGattCharacteristic.PERMISSION_WRITE);
//                service.addCharacteristic(write);


                mGattServer.addService(service);
            }
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     *
     * Callers should call stopForeground(true) when background work is complete.
     */
//    private void goForeground() {
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//            notificationIntent, 0);
//        Notification n = new Notification.Builder(this)
//            .setContentTitle("Advertising device via Bluetooth")
//            .setContentText("This device is discoverable to others nearby.")
//            .setSmallIcon(R.drawable.ic_launcher)
//            .setContentIntent(pendingIntent)
//            .build();
//        startForeground(FOREGROUND_NOTIFICATION_ID, n);
//    }

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising");
        if (null != mBluetoothLeAdvertiser && null != mAdvertiseCallback) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Service_UUID);
        dataBuilder.setIncludeDeviceName(true);

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            sendFailureIntent(errorCode);
            stopSelf();

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode) {
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }

    private void sendSocketIntent() {
//        Intent failureIntent = new Intent();
//        failureIntent.setAction(START_SOCKET);
//        sendBroadcast(failureIntent);
        try {
            if (null == m_serverSocket) {
                m_serverSocket = new ServerSocket(nServerPort);
                // Start a server thread to do socket-accept tasks
            }
            new MyServerThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private BluetoothGattServerCallback mCallBack = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic
                characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            if (characteristic != null && characteristic.getUuid() != null) {
                prarseData(value, characteristic);
            }
            bluetoothDevice = device;
        }

    };


    /**
     * 解析数据
     *
     * @param value
     */
    private void prarseData(byte[] value, BluetoothGattCharacteristic characteristic) {
        if (null == mReceiveData) {
            mReceiveData = new StringBuffer();
        }
        if (value == null || value.length <= 0) {
            return;
        }
        String hexStr = HexUtils.bytesToHexString(value);
        if (TextUtils.isEmpty(hexStr)) {
            return;
        }
        String str = HexUtils.hexStr2Str(hexStr.toLowerCase());
        if (WIFI_NAME_UUID.getUuid().equals(characteristic.getUuid())) {
            wifiName = str;
        } else if (WIFI_PW_UUID.getUuid().equals(characteristic.getUuid())) {
            wifiPw = str;
        }
        Log.d(TAG, "prarseData: " + str);

        if (!TextUtils.isEmpty(wifiName) && !TextUtils.isEmpty(wifiPw)) {

            if(!mWifiManager.isWifiEnabled()){
                mWifiManager.setWifiEnabled(true);
            }
            int netId = mWifiManager.addNetwork(createWifiConfig(wifiName, wifiPw, WIFICIPHER_WPA));
//            mWifiManager.enableNetwork(netId, true);
//            boolean reconnect = mWifiManager.reconnect();
//            Log.d(TAG, "reconnect: " + reconnect);

            //wifi 断开重新连接
            List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.replace("\"", "").equals(wifiName)) {
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    boolean reconnect = mWifiManager.reconnect();
                    Log.d(TAG, "reconnect: " + reconnect);
                    break;
                }
            }

            wifiName = "";
            wifiPw = "";
        }

    }

    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            //则清除旧有配置
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        //不需要密码的场景
        if (type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs == null) {
            return null;
        }

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }

    public class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                if (null == mGattServer || null == bluetoothDevice) {
                    return;
                }
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;//当然，这边可以更精确的确定状态
                    Log.d(TAG, state.toString());
                    if (isConnected) {
                        Log.d(TAG, "返回Wi-Fi连接状态：connected");
                        if (!connentResult && isConnected) {
                            connentResult = isConnected;
                            setWifiIpValue();
                            sendSocketIntent();
                            setWifiTypeValue("5000".getBytes(Charset.forName("UTF-8")));//连接成功
                        }
                    } else if (state == NetworkInfo.State.CONNECTING) {
                        connentResult = false;
                        Log.d(TAG, "返回Wi-Fi连接状态：connecting");
                    } else if (state == NetworkInfo.State.DISCONNECTING) {
                        connentResult = false;
                        Log.d(TAG, "返回Wi-Fi连接状态：disconnecting");
                    } else if (state == NetworkInfo.State.UNKNOWN) {
                        connentResult = false;
                        Log.d(TAG, "返回Wi-Fi连接状态：unknown");
                    } else if (state == NetworkInfo.State.SUSPENDED) {
                        connentResult = false;
                        Log.d(TAG, "返回Wi-Fi连接状态：suspended");
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        connentResult = false;
                        Log.d(TAG, "返回Wi-Fi连接状态：disconnected");
//                        setWifiTypeValue("5001".getBytes(Charset.forName("UTF-8")));//连接失败或未连接
                    }

                }
            }
        }
    }


    //给手机发送数据
    private void setWifiTypeValue(byte[] value) {
        wifiTypeCharacteristic.setValue(value);
        mGattServer.notifyCharacteristicChanged(bluetoothDevice, wifiTypeCharacteristic, false);
    }

    private void setWifiIpValue() {
        int i = mWifiManager.getConnectionInfo().getIpAddress();
        Log.d(TAG, "setWifiIpValue: i = " + i);
        if (i > 0) {
            String ipAddress = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                    + "." + (i >> 24 & 0xFF);
            Log.d(TAG, "setWifiIpValue: ipAddress = " + ipAddress);
            wifiIPCharacteristic.setValue(ipAddress.getBytes(Charset.forName("UTF-8")));
            mGattServer.notifyCharacteristicChanged(bluetoothDevice, wifiIPCharacteristic, false);
        }
    }

    public class MyServerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    // Wait for new client connection
                    socketClient = m_serverSocket.accept();
                    LogUtil.d("~~~~~~~~~~~开启socket");
                    // Read input from client socket
                    InputStream is = socketClient.getInputStream();
                    DataInputStream dis = new DataInputStream(is);

                    while (!socketClient.isClosed()) {
                        String sLine;
                        sLine = dis.readLine();

                        if (!TextUtils.isEmpty(sLine)) {
                            System.out.println("读取到的内容" + sLine);
                            checkDate(sLine);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Stop loop when server socket is closed
                if (m_serverSocket.isClosed()) {
                    break;
                }
            }
        }
    }

    private void checkDate(String sLine) {
        if(!sLine.contains("{") || !sLine.contains("}")){
            return;
        }
        ReceiveObj receiveObj = gson.fromJson(sLine, ReceiveObj.class);
        LogUtil.d("shuju", receiveObj.getType() + "");
        if (null != receiveObj) {
            switch (receiveObj.getType()) {
                case 1://获取用户信息
                    LogUtil.d("获取用户信息");
                    SimpleSharedPreferences.putString("userMessage", receiveObj.getMessage(), mContext);
                    UnityPlayer.UnitySendMessage("GameState", "OnUserInfoChange", "");
                    break;
                case 2://返回蓝牙列表
                    startDiscovery();
                    LogUtil.d("返回蓝牙列表");
                    //返回已配对蓝牙列表
                    pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        LogUtil.d("bluetoothDevices.size():" + pairedDevices.size());
                        pairedBlueDeviceObjs.clear();
                        for (BluetoothDevice device : pairedDevices) {
                            pairedBlueDeviceObjs.add(device);
                        }
                    }
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
//                    sendServerSockerMessage(pairedReceiveObjStr);
                    //返回未配对蓝牙列表
                    ArrayList<BluetoothDevice> currentScanBlueDeviceObjs = new ArrayList<>();
                    currentScanBlueDeviceObjs.addAll(scanBlueDeviceObjs);
                    ArrayList<BlueDeviceObj> blueDeviceObjs = new ArrayList<BlueDeviceObj>();
                    for (BluetoothDevice bluetoothDevice : currentScanBlueDeviceObjs) {
                        blueDeviceObjs.add(new BlueDeviceObj(StringUtils.isBlank(bluetoothDevice.getName()) ? bluetoothDevice.getAddress() : bluetoothDevice.getName(),
                                bluetoothDevice.getAddress(), bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
                    }
                    ReceiveObj scanBlueReceiveObj = new ReceiveObj();
                    BlueToothTypeList blueToothTypeList = new BlueToothTypeList();
                    blueToothTypeList.setListType(2);
                    blueToothTypeList.setDevicelist(blueDeviceObjs);
                    String blueToothTypeListStr = gson.toJson(blueToothTypeList);
                    scanBlueReceiveObj.setType(2);
                    scanBlueReceiveObj.setMessage(blueToothTypeListStr);
                    String scanBlueReceiveObjStr = gson.toJson(scanBlueReceiveObj);
//                    sendServerSockerMessage(scanBlueReceiveObjStr);

                    allBlueToothList.clear();
                    allBlueToothList.addAll(pairedBlueDeviceObjs);
                    allBlueToothList.addAll(currentScanBlueDeviceObjs);
                    resultBlueDeviceObjs.clear();
                    resultBlueDeviceObjs.addAll(allBlueToothList);

                    //修改IOS socket一次返回
                    BlueToothListObj blueToothListObj = new BlueToothListObj();
                    blueToothListObj.setPairedBlueDevicelist(pairedBlueDeviceObj);
                    blueToothListObj.setScanBlueDevicelist(blueDeviceObjs);
                    ReceiveObj resultReceiveObj = new ReceiveObj();
                    resultReceiveObj.setType(2);
                    resultReceiveObj.setMessage(gson.toJson(blueToothListObj));
                    String resultReceiveObjStr = gson.toJson(resultReceiveObj);
                    sendServerSockerMessage(resultReceiveObjStr);
                    break;
                case 3://连接指定蓝牙
                    LogUtil.d("连接指定蓝牙");
                    connectBlurTooth(receiveObj.getMessage());
                    break;
                case 4://返回本机所有应用列表
                    LogUtil.d("return applist");
                    ReceiveObj appListReceiveObj = new ReceiveObj();
                    AppListObj appListObj = new AppListObj();
                    ArrayList<AppDetialObj> applist = new ArrayList<AppDetialObj>();
                    applist.addAll(appListUtil.getAppList());
                    appListObj.setApplist(applist);
                    String appListObjJsonStr = gson.toJson(appListObj);
                    appListReceiveObj.setType(4);
                    appListReceiveObj.setMessage(appListObjJsonStr);
                    String appLisreceiveObjStr = gson.toJson(appListReceiveObj);
                    sendServerSockerMessage(appLisreceiveObjStr);
                    break;
                case 5://请求卸载指定apk
                    LogUtil.d("请求卸载指定apk");
                    int uninstallRows = appListUtil.savrUninstallApp(receiveObj.getMessage());
                    ReceiveObj uninstallReceiveObj = new ReceiveObj();
                    uninstallReceiveObj.setType(5);
                    if (uninstallRows == 0) {//成功
                        uninstallReceiveObj.setMessage("0");
                    } else {//失败
                        uninstallReceiveObj.setMessage("1");
                    }
                    String uninstallReceiveObjStr = gson.toJson(uninstallReceiveObj);
                    sendServerSockerMessage(uninstallReceiveObjStr);
                    break;
                case 6://请求更新指定apk类型
                    LogUtil.d("请求更新指定apk类型");
                    AppDetialObj appDetialObj = gson.fromJson(receiveObj.getMessage(), AppDetialObj.class);
                    int updataAppRows = appListUtil.updataAppType(appDetialObj);
                    ReceiveObj updataAppReceiveObj = new ReceiveObj();
                    updataAppReceiveObj.setType(6);
                    if (updataAppRows == 0) {//成功
                        updataAppReceiveObj.setMessage("0");
                    } else {//失败
                        updataAppReceiveObj.setMessage("1");
                    }
                    String updataAppReceiveObjStr = gson.toJson(updataAppReceiveObj);
                    sendServerSockerMessage(updataAppReceiveObjStr);
                    EventBus.getDefault().post(new AppUpdateInstallEvent(""));//去触发AppFragment中的initAllApp
                    break;
                case 7://下载指定应用
                    LogUtil.d("下载指定应用");
                    DownloadListObj downloadListObj = gson.fromJson(receiveObj.getMessage(), DownloadListObj.class);
                    for (DownloadListItemObj downloadListItemObj : downloadListObj.getDownloadList()) {
                        downloadListItemObj.setDownType("0");// 0 应用  1 视频   2 系统固件
                        DownloadUtil.startDownload(mContext, downloadListItemObj);
                    }
                    break;
                case 8://连接指定wifi
                    LogUtil.d("连接指定wifi");
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
                case 11://3box请求助手购买
                    LogUtil.d(gson.toJson(receiveObj.getMessage()));
//                    ReceiveObj buyReceiveObj = new ReceiveObj();
//                    BuyInfoObj buyInfoObj = new BuyInfoObj(1, "aaaa", "2.3", "bbbb", 123, 123);
//                    String buyObjJsonStr = gson.toJson(buyInfoObj);
//                    buyReceiveObj.setType(11);
//                    buyReceiveObj.setMessage(buyObjJsonStr);
//                    sendServerSockerMessage(gson.toJson(buyReceiveObj));

                    break;
                case 12://返回固件版本号
                    VersionUtil versionUtil = new VersionUtil(AdvertiserService.this);
                    String version = versionUtil.getFrameworkVersion();
                    ReceiveObj resultObj = new ReceiveObj();
                    resultObj.setType(12);
                    resultObj.setMessage(version);
                    String objStr = gson.toJson(resultObj);
                    sendServerSockerMessage(objStr);
                    break;
                case 13://返回时区
                    String defaultStr = TimeZone.getDefault().getDisplayName();//获取默认时区
                    ReceiveObj timeResultObj = new ReceiveObj();
                    timeResultObj.setType(13);
                    timeResultObj.setMessage(defaultStr);
                    String timeObjStr = gson.toJson(timeResultObj);

                    sendServerSockerMessage(timeObjStr);
                    break;
            }
        }
    }


    private void sendServerSockerMessage(String message) {
        OutputStream os = null;
        try {
            os = socketClient.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.write((message.replace("\n", "").replace("\r", "") + "\n").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //连接指定蓝牙
    private void connectBlurTooth(String blueToothAddress) {
        mConnectDevice = getAddressDevice(blueToothAddress);
        HID_NAME = mConnectDevice.getName();
        HID_ADDR = mConnectDevice.getAddress();
        LogUtil.d("getName---" + mConnectDevice.getName());
        LogUtil.d("getAddress---" + mConnectDevice.getAddress());
        mBluetoothAdapter.cancelDiscovery();
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
        for (BluetoothDevice bluetoothDevice : resultBlueDeviceObjs) {
            if (bluetoothDevice.getAddress().equals(blueToothAddress)) {
                return bluetoothDevice;
            }
        }
        return null;
    }

    //连接wifi
    private void connectWifi(String ssid, String password, int wifiType) {
        int netId = wifiManager.addNetwork(createWifiConfig(ssid, password, wifiType));
        boolean enable = wifiManager.enableNetwork(netId, true);
        Log.d("ZJTest", "enable: " + enable);
        boolean reconnect = wifiManager.reconnect();
        Log.d("ZJTest", "reconnect: " + reconnect);
    }

    //设置时区
    public void setTimeZone(String timeZone) {
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setTimeZone(timeZone);
//        mAlarmManager.setTimeZone("Asia/Taipei");
    }

    @Subscribe
    public void onEventMainThread(ToServerEvent event) {
        MLog.d("socket接收到消息");
        ReceiveObj receiveObj = event.getMsg();
        if (receiveObj.getType() == 11) {
            String receiveObjStr = gson.toJson(receiveObj);
            ReceiveObj buyReceiveObj = new ReceiveObj();
            buyReceiveObj.setType(11);
            buyReceiveObj.setMessage(receiveObj.getMessage());
            sendServerSockerMessage(gson.toJson(buyReceiveObj));
//            sendServerSockerMessage(receiveObjStr);
        }
    }

    public void getScanBlueDeviceObjs(ArrayList<BluetoothDevice> bluetoothDevices) {
        com.hch.viewlib.util.MLog.d("socketr服务接收到消息" + bluetoothDevices.size());
//        allBlueToothList.removeAll(tempScanBlueDeviceObjs);
//        tempScanBlueDeviceObjs.clear();
//        tempScanBlueDeviceObjs.addAll(bluetoothDevices);
//        allBlueToothList.addAll(tempScanBlueDeviceObjs);
//            ArrayList<BlueDeviceObj> blueDeviceObjs = new ArrayList<BlueDeviceObj>();
//            for (BluetoothDevice bluetoothDevice : event.getMsg()) {
//                blueDeviceObjs.add(new BlueDeviceObj(bluetoothDevice.getName(), bluetoothDevice.getAddress(), bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
//            }
//            ReceiveObj receiveObj = new ReceiveObj();
//            BlueToothTypeList blueToothTypeList = new BlueToothTypeList();
//            blueToothTypeList.setListType(2);
//            blueToothTypeList.setDevicelist(blueDeviceObjs);
//            String blueToothTypeListStr = gson.toJson(blueToothTypeList);
//            receiveObj.setType(2);
//            receiveObj.setMessage(blueToothTypeListStr);
//            String receiveObjStr = gson.toJson(receiveObj);
//            sendServerSockerMessage(receiveObjStr);
        scanBlueDeviceObjs.clear();
        scanBlueDeviceObjs.addAll(bluetoothDevices);
    }


    /**
     * 获取当前时区
     *
     * @return
     */
    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        String strTz = tz.getDisplayName(false, TimeZone.SHORT);
        return strTz;

    }


    /**
     * 获取当前系统语言格式
     *
     * @param mContext
     * @return
     */
    public static String getCurrentLanguage(Context mContext) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String lc = language + "_" + country;
        return lc;
    }

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
                        com.hch.viewlib.util.MLog.d("socket扫描到蓝牙：" + device.getName());
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
                com.hch.viewlib.util.MLog.d("socket扫描完毕");
                temp1ScanBlueDeviceObjs.clear();
            }
        }
    }

    public void startDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            com.hch.viewlib.util.MLog.d("取消扫描");
        }else{
            // Request discover from BluetoothAdapter
            com.hch.viewlib.util.MLog.d("开始扫描");
            mBluetoothAdapter.startDiscovery();
        }
    }
}
