package com.xunixianshi.vrlanucher.server.utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/3.
 */

public class DownloadListObj implements Serializable {
    ArrayList<DownloadListItemObj> downlist;

    public ArrayList<DownloadListItemObj> getDownloadList() {
        return downlist;
    }

    public void setDownloadList(ArrayList<DownloadListItemObj> downloadList) {
        this.downlist = downloadList;
    }
}
