package com.xunixianshi.vrlanucher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.hch.viewlib.widget.PullToRefreshBase;
import com.hch.viewlib.widget.PullToRefreshListView;
import com.xunixianshi.vrlanucher.server.AdvertiserService;
import com.xunixianshi.vrlanucher.server.BlueToothServer;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.tvui.home.HomeActivity;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.utils.VersionObj;
import com.xunixianshi.vrlanucher.vrui.UnityPlayerActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends Activity {
    //    private Button tv_desktop_bt;
    private Button tv_experience_bt;
    private Button vr_experience_bt;
    //    private Button tv_desktop1_bt;
    private Button tv_experience1_bt;
    private Button vr_experience1_bt;
    private TextView hint_tv;
    private TextView hint1_tv;
    private int tvOrVr = 2;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    private boolean timeRun = false;
    private TimeCount time;
    private boolean isPause = false;

    private IntentFilter intentFilter;
    private AudioChangeReceiver audioChangeReceiver;

    private AppListUtil appListUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "wakeLock");
        wakeLock.acquire();
        listenNearSensor();
//        tv_desktop_bt = (Button) findViewById(R.id.tv_desktop_bt);
        tv_experience_bt = (Button) findViewById(R.id.tv_experience_bt);
        vr_experience_bt = (Button) findViewById(R.id.vr_experience_bt);
//        tv_desktop1_bt = (Button) findViewById(R.id.tv_desktop1_bt);
        tv_experience1_bt = (Button) findViewById(R.id.tv_experience1_bt);
        vr_experience1_bt = (Button) findViewById(R.id.vr_experience1_bt);
        hint_tv = (TextView) findViewById(R.id.hint_tv);
        hint1_tv = (TextView) findViewById(R.id.hint1_tv);
        vrExperienceSelected(true);
//        tv_desktop_bt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openLuancherActivity();
//            }
//        });
        tv_experience_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                appListUtil.getAppList();
                openTvActivity();
            }
        });
        vr_experience_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVrActivity();
            }
        });
        time = new TimeCount(30000, 1000);

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);  //添加要收到的广播
        audioChangeReceiver = new AudioChangeReceiver();                    //广播实例
        registerReceiver(audioChangeReceiver, intentFilter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            } else {
                // 初始化本地应用列表
                appListUtil = new AppListUtil(MainActivity.this);
                appListUtil.initAppListToDb();
                writeVersionFile();
            }
            int permissionCheck = 0;
            permissionCheck = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                //注册权限
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        1001); //Any number
            } else {//已获得过权限
                //进行蓝牙设备搜索操作
            }
        }
        //开启android蓝牙通信服务
        startService(new Intent(MainActivity.this, BlueToothServer.class));
        //开启ios蓝牙通信服务
        startService(new Intent(MainActivity.this, AdvertiserService.class));

    }

    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        Log.d("TAG", "onResume::" + timeRun);
        if (time != null) {
            Log.d("TAG", "o==========");
            time.start();
        }
        isPause = false;
        hideBottomUIMenu();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("TAG", "onPause::");
        time.cancel();
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(audioChangeReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                // 初始化本地应用列表
                appListUtil = new AppListUtil(MainActivity.this);
                appListUtil.initAppListToDb();
                writeVersionFile();
            } else {
                //用户不同意

            }
        }
    }

    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private void selectText(boolean leftOrRight) {
        if (leftOrRight) {
            if (tvOrVr == 1) {
                tvOrVr = 2;
            } else {
                tvOrVr = 1;
            }
        } else {
            if (tvOrVr == 2) {
                tvOrVr = 1;
            } else {
                tvOrVr = 2;
            }
        }
//        if(leftOrRight){
//            if(tvOrVr == 0){
//                tvOrVr = 2;
//            }else{
//                tvOrVr -= 1;
//            }
//        }else{
//            if(tvOrVr == 2){
//                tvOrVr = 0;
//            }else{
//                tvOrVr +=1;
//            }
//        }
        if (timeRun) {
            time.onFinish();
            time.cancel();
        }
        time.start();
        switch (tvOrVr) {
//            case 0:
////                luancherExperienceSelected(true);
//                tvExperienceSelected(false);
//                vrExperienceSelected(false);
//                break;
            case 1:
//                luancherExperienceSelected(false);
                tvExperienceSelected(true);
                vrExperienceSelected(false);
                break;
            case 2:
//                luancherExperienceSelected(false);
                tvExperienceSelected(false);
                vrExperienceSelected(true);
                break;
        }
    }

    //    private void luancherExperienceSelected(boolean isSelect) {
//        if (isSelect) {
//            tv_desktop_bt.setBackgroundResource(R.mipmap.select_bg);
//            tv_desktop1_bt.setBackgroundResource(R.mipmap.select_bg);
//        } else {
//            tv_desktop_bt.setBackgroundResource(R.mipmap.no_select_bg);
//            tv_desktop1_bt.setBackgroundResource(R.mipmap.no_select_bg);
//        }
//    }
    private void tvExperienceSelected(boolean isSelect) {
        if (isSelect) {
            tv_experience_bt.setBackgroundResource(R.mipmap.select_bg);
            tv_experience1_bt.setBackgroundResource(R.mipmap.select_bg);
        } else {
            tv_experience_bt.setBackgroundResource(R.mipmap.no_select_bg);
            tv_experience1_bt.setBackgroundResource(R.mipmap.no_select_bg);
        }
    }

    private void vrExperienceSelected(boolean isSelect) {
        if (isSelect) {
            vr_experience_bt.setBackgroundResource(R.mipmap.select_bg);
            vr_experience1_bt.setBackgroundResource(R.mipmap.select_bg);
        } else {
            vr_experience_bt.setBackgroundResource(R.mipmap.no_select_bg);
            vr_experience1_bt.setBackgroundResource(R.mipmap.no_select_bg);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("TAG", "KEYCODE_VOLUME_DOWN::");
        }
        if (event.getAction() == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("TAG", "KEYCODE_VOLUME_UP::");
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("TAG", "keyCode::" + keyCode);
        Log.d("TAG", "KeyEvent.KEYCODE_VOLUME_DOWN::" + KeyEvent.KEYCODE_VOLUME_DOWN);
        switch (keyCode) {

            case 21://左
                selectText(true);
                break;
            case 22://右
                selectText(false);
                break;
            case 23://确定
            case 96://确定
                switch (tvOrVr) {
//                    case 0:
//                        openLuancherActivity();
//                        break;
                    case 1:
                        openTvActivity();
                        break;
                    case 2:
                        openVrActivity();
                        break;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void openVrActivity() {
        if (timeRun) {
            time.cancel();
            time.onFinish();
            Log.d("TAG", "3333333");
        }
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        ComponentName cn = new ComponentName("com.xunixianshi.vrui", "com.xunixianshi.vrui.UnityPlayerActivity");
//        intent.setComponent(cn);
//        startActivity(intent);

        startActivity(new Intent(MainActivity.this, UnityPlayerActivity.class));
    }

    private void openTvActivity() {
        if (timeRun) {
            time.cancel();
            time.onFinish();
            Log.d("TAG", "222222222");
        }
//        doStartApplicationWithPackageName("com.FDGEntertainment.Oceanhorn.gp");
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        ComponentName cn = new ComponentName("com.hch.black", "com.hch.black.MainActivity");
//        intent.setComponent(cn);
//        startActivity(intent);

        startActivity(new Intent(MainActivity.this, HomeActivity.class));
    }

    private void openLuancherActivity() {
        if (timeRun) {
            time.cancel();
            time.onFinish();
            Log.d("TAG", "1111111111");
        }
//        doStartApplicationWithPackageName("com.android.launcher3");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher");
        intent.setComponent(cn);
        startActivity(intent);
    }

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
                Log.d("vrui", "its[0]:::" + value);
                if (value == 0.0) {// 贴近手机
                    wakeLock.acquire();
                    Log.d("vrui", "open");
                } else {// 远离手机
                    if (wakeLock != null) {
                        wakeLock.release();
                    }
                    Log.d("vrui", "close");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();

            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    int over = 0;

    /**
     * @author hechuang
     * @ClassName: TimeCount
     * @Description: TODO 验证码倒计时
     * @date 2016-3-10 下午 5:25:01
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            timeRun = false;// 停止计时
            if (over <= 1) {
                if (!isPause) {
                    switch (tvOrVr) {
//                        case 0:
//                            openLuancherActivity();
//                            break;
                        case 1:
                            openTvActivity();
                            break;
                        case 2:
                            openVrActivity();
                            break;
                    }
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            timeRun = true;// 正在计时
            over = (int) (millisUntilFinished / 1000);
            hint_tv.setText("Auto Start " + millisUntilFinished / 1000 + " Seconds");
            hint1_tv.setText("Auto Start " + millisUntilFinished / 1000 + " Seconds");
//            hint_tv.setText(millisUntilFinished / 1000 + "秒后自动跳转");
//            hint1_tv.setText(millisUntilFinished / 1000 + "秒后自动跳转");
            Log.d("TAG", "millisUntilFinished:" + millisUntilFinished);
        }

    }

    class AudioChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    //写入本地版本文件
    private void writeVersionFile() {
        VersionObj versionObj = new VersionObj();
        versionObj.setAndroidVerison("" + android.os.Build.VERSION.SDK_INT);
        versionObj.setWifiAddress(getWifiAddress());
        LogUtil.d("WifiAddress:" + getWifiAddress());
        versionObj.setBlueToothAddress(GetLocalMacAddress());
        LogUtil.d("MacAddress:" + GetLocalMacAddress());
        versionObj.setSerialNumber(getSerialNumber());
        versionObj.setLauncherVersion(getVersion());
        versionObj.setBlueToothName(getBlueToothName());
        Gson gson = new Gson();
        String versionJson = gson.toJson(versionObj);
        if (!SimpleSharedPreferences.getBoolean("writerVersion", MainActivity.this)) {
            recordVersion(versionJson);
        }
    }

    private String getWifiAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

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

    private String getBlueToothName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getName();
    }

    private String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveString(String versionJson) {
        String dir = getExternalStorageDirectory() + "/vrui";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public int recordVersion(String writeInfo) {
        LogUtil.d("writeInfo:" + writeInfo);
        BufferedWriter bufferedWriter = null;
        try {
            File fdirs = new File(getExternalStorageDirectory() + "/Version/");
            if (!fdirs.exists()) {
                fdirs.mkdirs();
            }
            File fLog = new File(getExternalStorageDirectory() + "/Version/" + "3boxVersion.txt");
            if (!fLog.exists()) {
                fLog.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(fLog, true);
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fos));
            bufferedWriter.write(writeInfo);
            bufferedWriter.close();
            fos.close();
            LogUtil.d("写入成功");
            SimpleSharedPreferences.putBoolean("writerVersion", true, MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
