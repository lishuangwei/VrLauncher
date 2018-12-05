package com.xunixianshi.vrlanucher.server.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.xunixianshi.vrlanucher.server.AppInstallEvent;
import com.xunixianshi.vrlanucher.server.AppUninstallEvent;
import com.xunixianshi.vrlanucher.server.AppUpdateInstallEvent;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Jack.Fan on 2017/10/15.
 */

public class SysInstallReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------安装成功" + packageName);
//            Toast.makeText(context, "安装成功" + packageName, Toast.LENGTH_LONG).show();
//            //将应用信息存入数据库 数据线安装apkType为0
//            Intent tempIntent = new Intent();  //Itent就是我们要发送的内容
//            tempIntent.putExtra("data", "this is data ");
//            intent.putExtra("Type",2);//1 代表下载安装 2代表数据线安装
//            tempIntent.setAction("com.xunixianshi.vrlanucher.PACKAGE_ADDED");//设置action，和这个action相同的的接受者才能接收广播
//            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//            context.sendBroadcast(intent);
            EventBus.getDefault().post(new AppInstallEvent(packageName));//通知服务去做添加数据库操作
//            EventBus.getDefault().post(new AppInstallEvent("initAllApp"));//去触发AppFragment中的initAllApp
            EventBus.getDefault().post(new AppUpdateInstallEvent(""));
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------替换成功" + packageName);
            EventBus.getDefault().post(new AppUpdateInstallEvent(""));
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------卸载成功" + packageName);
            EventBus.getDefault().post(new AppUninstallEvent(packageName));//通知服务去做删除数据库操作
            EventBus.getDefault().post(new AppUpdateInstallEvent(""));
        }
    }

}
