package com.xunixianshi.vrlanucher;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.activeandroid.ActiveAndroid;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItem;

import java.util.List;

import zlc.season.rxdownload2.RxDownload;

/**
 * Created by xnxs-ptzx04 on 2017/9/8.
 */

public class MyApplication extends com.activeandroid.app.Application  {

    private static Context context;
    public static List<DownloadItem> localVideoList;
    public static int screenWidth;//屏幕宽度
    public static int screenHeight;//屏幕宽度

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        ActiveAndroid.initialize(this);
        RxDownload.getInstance(this)
                .defaultSavePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/3box/download")
                .maxThread(10)
                .maxRetryCount(10)
                .maxDownloadNumber(10);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        screenWidth = wm.getDefaultDisplay().getWidth();
//        screenHeight = wm.getDefaultDisplay().getHeight();
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }

    public static Context getContext() {
        return context;
    }


}
