package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/10/3.
 */

public class DownloadListItemObj implements Serializable {

    String appDownUrl;
    String appName;
    String appPackageName;
    String appResourceId;
    String downType; // 0 应用  1 视频   2 系统固件
    String apkType;  //0 表示未知  1表示tv应用  2 vr
    int videoType;// 视频格式,1普通，2左右3D，3单画面全景，4上下3D，5上下全景，-1异常未知
    String appIconUrl;

    public String getAppDownloadUrl() {
        return appDownUrl;
    }

    public void setAppDownloadUrl(String appDownUrl) {
        this.appDownUrl = appDownUrl;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppResourceId() {
        return appResourceId;
    }

    public void setAppResourceId(String appResourceId) {
        this.appResourceId = appResourceId;
    }

    public String getApkType() {
        return apkType;
    }

    public void setApkType(String apkType) {
        this.apkType = apkType;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public void setAppIconUrl(String appIconUrl) {
        this.appIconUrl = appIconUrl;
    }

    public String getDownType() {
        return downType;
    }

    public void setDownType(String downType) {
        this.downType = downType;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(int videoType) {
        this.videoType = videoType;
    }
}
