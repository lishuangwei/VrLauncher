package com.xunixianshi.vrlanucher.server.utils;

import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/27.
 */

public class AppListObj implements Serializable {
    ArrayList<AppDetialObj> applist;

    public ArrayList<AppDetialObj> getApplist() {
        return applist;
    }

    public void setApplist(ArrayList<AppDetialObj> applist) {
        this.applist = applist;
    }
}
