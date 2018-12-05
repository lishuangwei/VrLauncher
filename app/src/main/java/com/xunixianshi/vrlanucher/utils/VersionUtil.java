package com.xunixianshi.vrlanucher.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.xunixianshi.vrlanucher.MainActivity;
import com.xunixianshi.vrlanucher.tvui.utils.*;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import net.vidageek.mirror.dsl.Mirror;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Administrator on 2017/10/10.
 */

public class VersionUtil {
    private Context mContext;

    public VersionUtil(Context mContext) {
        this.mContext = mContext;
    }

    //写入本地版本文件
    public void writeVersionFile() {
        LogUtil.d("writeVersionFile::::");
        VersionObj versionObj = new VersionObj();
        versionObj.setAndroidVerison("" + android.os.Build.VERSION.SDK_INT);
        versionObj.setFrameworkVersion("" + android.os.Build.VERSION.CODENAME);
        versionObj.setWifiAddress(getWifiAddress());
        com.xunixianshi.vrlanucher.tvui.utils.LogUtil.d("WifiAddress:" + getWifiAddress());
        versionObj.setBlueToothAddress(GetLocalMacAddress());
        com.xunixianshi.vrlanucher.tvui.utils.LogUtil.d("MacAddress:" + GetLocalMacAddress());
        versionObj.setSerialNumber(getSerialNumber());
        versionObj.setLauncherVersion(getVersion());
        versionObj.setBlueToothName(getBlueToothName());
        Gson gson = new Gson();
        String versionJson = gson.toJson(versionObj);
        recordVersion(versionJson);
    }

    public String getWifiAddress() {
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
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
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

    private int recordVersion(String writeInfo) {
        LogUtil.d("~~~~~~~~~~~~~~recordVersion" + writeInfo);
        com.xunixianshi.vrlanucher.tvui.utils.LogUtil.d("writeInfo:" + writeInfo);
        BufferedWriter bufferedWriter = null;
        try {
            File fdirs = new File(getExternalStorageDirectory() + "/Version/");
            if (!fdirs.exists()) {
                fdirs.mkdirs();
            }
            File fLog = new File(getExternalStorageDirectory() + "/Version/" + "3boxVersion.txt");
            if (!fLog.exists()) {
                fLog.createNewFile();
            } else {
                LogUtil.d("~~~~~~~~~~~~~~fLog.delete");
                fLog.delete();
                fLog.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(fLog, true);
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fos));
            bufferedWriter.write(writeInfo);
            bufferedWriter.close();
            fos.close();
            com.xunixianshi.vrlanucher.tvui.utils.LogUtil.d("写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //获取固件版本号
    public String getFrameworkVersion() {
        return android.os.Build.ID;
    }
}
