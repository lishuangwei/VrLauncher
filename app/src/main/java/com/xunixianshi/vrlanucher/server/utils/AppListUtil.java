package com.xunixianshi.vrlanucher.server.utils;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hch.viewlib.util.MLog;
import com.hch.viewlib.util.SimpleSharedPreferences;
import com.xunixianshi.vrlanucher.server.AppInstallEvent;
import com.xunixianshi.vrlanucher.server.AppUpdateInstallEvent;
import com.xunixianshi.vrlanucher.server.DBUtil.AppListDBUtil;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.tvui.adapter.AppBean;
import com.xunixianshi.vrlanucher.tvui.app.GetAppList;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/26.
 */

public class AppListUtil {

    private Context mContext;
    private GetAppList getAppInstance;
    private Gson gson;

    public AppListUtil(Context context) {
        this.mContext = context;
        getAppInstance = new GetAppList(mContext);
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
        gson = builder.create();
    }

    private List<AppDetialObj> getLocalAllAppList() {
        List<AppDetialObj> appDetialObjs = new ArrayList<>();
        ArrayList<AppBean> appBeens = getAppInstance.getUninstallAppList();
        for (AppBean info : appBeens) {
            String path = "";
            Drawable icon = info.getIcon();
            if (icon != null) {
                String packagename = info.getPackageName();
                path = saveAppIcon(icon, packagename);
            }
            AppDetialObj appDetialObj = new AppDetialObj();
            appDetialObj.setAppName(info.getName().toString());
            appDetialObj.setAppPackageName(info.getPackageName());
            appDetialObj.setAppIcon(path);
            appDetialObj.setAppResourceId("0");
            appDetialObj.setAppType("0");
//            LogUtil.d("getLocalAllAppList:"+info.getName().toString());
            appDetialObjs.add(appDetialObj);
        }
        return appDetialObjs;
    }

    //初始化app列表数据入库
    public void initAppListToDb() {
        LogUtil.d("初始化app数据");
        Boolean appIsAllIn = SimpleSharedPreferences.getBoolean("appIsAllIn", mContext);
        LogUtil.d("appIsAllIn:" + appIsAllIn);
        if (!appIsAllIn) {
                AppListDBUtil.insterList(getLocalAllAppList());
        }
        SimpleSharedPreferences.putBoolean("appIsAllIn", true, mContext);
    }
    //入库一个app信息
    public void insterAppInfo(AppBean appBean){
        LogUtil.d("入库一个app信息"+appBean.getPackageName());
        deleteAppByPackageName(appBean.getPackageName());
        String path = "";
        Drawable icon = appBean.getIcon();
        if (icon != null) {
            String packagename = appBean.getPackageName();
            path = saveAppIcon(icon, packagename);
        }
        AppDetialObj appDetialObj = new AppDetialObj();
        appDetialObj.setAppName(appBean.getName().toString());
        appDetialObj.setAppPackageName(appBean.getPackageName());
        appDetialObj.setAppIcon(path);
        appDetialObj.setAppResourceId(appBean.getResourceId());
        appDetialObj.setAppType(appBean.getType());
        AppListDBUtil.inster(appDetialObj);
    }

    //获取app列表
    public List<AppDetialObj> getAppList() {
        LogUtil.d("获取数据库所有数据");
//        AppListDBUtil.selectAll();
        return AppListDBUtil.selectAll();
    }

    //获取所有tv应用列表
    public List<AppDetialObj> getTvAppList() {
//        LogUtil.d("获取数据库所有Tv应用数据");
        return AppListDBUtil.selectByCndition("1", "0");
    }

    //获取所有类型为2的应用列表
    public List<AppDetialObj> getTypeTwoList() {
//        LogUtil.d("获取数据库所有Tv应用数据");
        return AppListDBUtil.selectByCndition("2");
    }

    //获取所有vr应用列表
    public List<AppDetialObj> getVrAppList() {
        LogUtil.d("获取所有vr应用列表");
        return AppListDBUtil.selectByCndition("2", "0");
    }

    //更新指定应用类型
    public int updataAppType(AppDetialObj appDetialObj) {
        return updataAppTypeByPackageName(appDetialObj.getAppPackageName(),appDetialObj.getAppType());
    }

    //根据包名更新指定应用类型
    public int updataAppTypeByPackageName(String packageName, String appType) {
        return AppListDBUtil.updataAppTypeByPackageName(packageName, appType);
    }

    //入库安装应用
    public void saveInstallApp(String installApkPath, AppDetialObj appDetialObj) {
//        installApp(installApkPath);
//        Drawable icon = getAppInstance.getAppIcon(appDetialObj.getAppPackageName());
//        String path = saveAppIcon(icon, appDetialObj.getAppPackageName());
//        appDetialObj.setAppIcon(path);
//        if(!checkApkInDb(appDetialObj.getAppPackageName())){
//                AppListDBUtil.inster(appDetialObj);
//        }

        LogUtil.d("安装下载好的应用::"+appDetialObj.getAppPackageName());
        quietInstallApk(installApkPath);
//        if (quietInstallApk(installApkPath)) {
//            LogUtil.d("-----------静默安装success");
////            Drawable icon = getAppInstance.getAppIcon(appDetialObj.getAppPackageName());
////            String path = saveAppIcon(icon, appDetialObj.getAppPackageName());
////            appDetialObj.setAppIcon(path);
//            //如果数据没有记录则保存
//            if(!checkApkInDb(appDetialObj.getAppPackageName())){
//                AppListDBUtil.inster(appDetialObj);
//            }
//        }else{
//            LogUtil.d("-----------静默安装失败firle");
//        }
    }

    //入库卸载应用
    public int savrUninstallApp(String appPackageName) {
//        uninstallApk(appPackageName);
        LogUtil.d("卸载应用::"+appPackageName);
        quietUnintallApk(appPackageName);
//        if(quietUnintallApk(appPackageName)){
//            LogUtil.d("静默卸载成功");
//            return AppListDBUtil.deleteByPackageName(appPackageName);
//        }else{
//            LogUtil.d("静默卸载失败");
//        }
        return 0;
    }
    //根据包名删除数据库
    public void deleteAppByPackageName(String appPackageName){
        if(checkApkInDb(appPackageName)){
            AppListDBUtil.deleteByPackageName(appPackageName);
        }
    }

    //安装应用
    private void installApp(String installApkPath) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + installApkPath), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    //卸载ａｐｋ
    public void uninstallApk(String appPackageName) {
        Uri uri = Uri.parse("package:" + appPackageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mContext.startActivity(intent);
    }

    //静默安装apk
    private boolean quietInstallApk(String apkPath) {
        LogUtil.d("静默安装应用::"+apkPath);
//        PrintWriter PrintWriter = null;
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec("su");
//            PrintWriter = new PrintWriter(process.getOutputStream());
//            PrintWriter.println("chmod 777 " + apkPath);
//            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
//            PrintWriter.println("pm install -r " + apkPath);
////          PrintWriter.println("exit");
//            PrintWriter.flush();
//            PrintWriter.close();
//            int value = process.waitFor();
//            LogUtil.d("静默安装应用成功::");
//            return returnResult(value);
//        } catch (Exception e) {
//            e.printStackTrace();
//            LogUtil.d("静默安装应用失败::"+e);
//        } finally {
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        return false;

//        if(!TextUtils.isEmpty(apkPath)){
//
//            Process process = null;
//            try {
//                process = new ProcessBuilder().command("pm","install","-i",mContext.getPackageName(),"--user","0",
//                        apkPath).start();
//                process.waitFor();
//                BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                String msg = "";
//                String line;
//                while ((line = errorStream.readLine()) != null){
//                    msg += line;
//                }
//                LogUtil.d("AppListUtil", "error msg is : "+msg);
//                return TextUtils.isEmpty(msg);
//            }catch (Exception e){
//                e.printStackTrace();
//                return false;
//            }finally {
//                if(process != null){
//                    process.destroy();
//                }
//            }
//        }else {
//            return false;
//        }
        ComponentName componentName = new ComponentName("server.xunixianshi.com.installserver",
                "server.xunixianshi.com.installserver.MainActivity");
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("path", apkPath);
        bundle.putString("install", "installapk");
        intent.putExtras(bundle);
        intent.setComponent(componentName);
        mContext.startActivity(intent);
        return true;
    }

    //静默卸载apk
    private boolean quietUnintallApk(String appPackageName) {
        LogUtil.d("静默卸载应用::"+appPackageName);
//        PrintWriter PrintWriter = null;
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec("su");
//            PrintWriter = new PrintWriter(process.getOutputStream());
//            PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
//            PrintWriter.println("pm uninstall " + appPackageName);
//            PrintWriter.flush();
//            PrintWriter.close();
//            int value = process.waitFor();
//            return returnResult(value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        return false;

//        if(!TextUtils.isEmpty(appPackageName)){
//            try {
//                Intent intent = new Intent();
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                PendingIntent sender = PendingIntent.getActivity(mContext,0,intent,0);
//                PackageInstaller mPackageInstaller = mContext.getPackageManager().getPackageInstaller();
//                mPackageInstaller.uninstall(appPackageName,sender.getIntentSender());
//                LogUtil.d("静默卸载完成::"+appPackageName);
//                return true;
//            }catch (Exception e){
//                e.printStackTrace();
//                return false;
//            }
//        }else {
//            return false;
//        }

        ComponentName componentName = new ComponentName("server.xunixianshi.com.installserver",
                "server.xunixianshi.com.installserver.MainActivity");
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("path", appPackageName);
        bundle.putString("install", "uninstallapk");
        intent.putExtras(bundle);
        intent.setComponent(componentName);
        mContext.startActivity(intent);
        return true;
    }

    //判断返回是否成功
    private static boolean returnResult(int value) {
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }

    //检查apk是否入库
    public boolean checkApkInDb(String packageName) {
        AppDetialObj appDetialObj = AppListDBUtil.selectByPackageName(packageName);
        if (appDetialObj == null) {
            MLog.d("应用未安装");
            return false;
        }else{
            MLog.d("应用已安装"+appDetialObj.getAppPackageName());
            return true;
        }
    }

    //打开指定apk
    public void openApk(String appPackageName) {
        if (appPackageName != null && appPackageName.length() > 0) {
            doStartApplicationWithPackageName(appPackageName);
        }
    }

    //打开指定包名apk
    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
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
        List<ResolveInfo> resolveinfoList = mContext.getPackageManager()
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
            mContext.startActivity(intent);
        }
    }

    private String saveAppIcon(Drawable icon, String packagename) {
        String savePath = "";
        String dir = Environment.getExternalStorageDirectory() + "/vrui";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            BitmapDrawable iconBitmap = (BitmapDrawable) icon;
            Bitmap img = iconBitmap.getBitmap();
            String fn = packagename + ".png";
            savePath = dir + File.separator + fn;
            OutputStream os = new FileOutputStream(savePath);
            img.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (Exception e) {
            Log.e("TAG", "", e);
        }
        return savePath;
    }

}
