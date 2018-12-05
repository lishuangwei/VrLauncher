package com.xunixianshi.vrlanucher.server.DBUtil.DbObj;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/27.
 */

@Table(name = "app_table")
public class AppDetialObj extends Model{
    @Column(name = "_id")
    int _id;
    @Column(name = "app_resource_id")
    String appResourceId;
    @Column(name = "app_name")
    String appName;
    @Column(name = "app_icon")
    String appIcon;
    @Column(name = "app_package_name")
    String appPackageName;
    @Column(name = "app_type")
    String appType;//0未知  1 tv   2 vr 3 系统应用

    public String getAppResourceId() {
        return appResourceId;
    }

    public void setAppResourceId(String appResourceId) {
        this.appResourceId = appResourceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}
