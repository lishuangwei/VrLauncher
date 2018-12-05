package com.xunixianshi.vrlanucher.server.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.lidroid.xutils.util.LogUtils;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Jack.Fan on 2017/10/15.
 */

public class AppInstallReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();
    public final String ACTIVIONNAME = "com.xunixianshi.vrlanucher.PACKAGE_ADDED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "安装成功", Toast.LENGTH_LONG).show();
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------安装成功" + packageName);
//            Toast.makeText(context, "安装成功" + packageName, Toast.LENGTH_LONG).show();
            //将应用信息存入数据库 数据线安装apkType为0



        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------替换成功" + packageName);
//            Toast.makeText(context, "替换成功" + packageName, Toast.LENGTH_LONG).show();

        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "--------卸载成功" + packageName);
//            Toast.makeText(context, "卸载成功" + packageName, Toast.LENGTH_LONG).show();
        }
    }
}
