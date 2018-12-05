package com.xunixianshi.vrlanucher.utils;

import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/9/30.
 */

public class AppListJson implements Serializable {
    List<AppDetialObj> vrAppList;

    public List<AppDetialObj> getVrAppList() {
        return vrAppList;
    }

    public void setVrAppList(List<AppDetialObj> vrAppList) {
        this.vrAppList = vrAppList;
    }
}
