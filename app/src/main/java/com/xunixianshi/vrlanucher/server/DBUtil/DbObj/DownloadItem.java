package com.xunixianshi.vrlanucher.server.DBUtil.DbObj;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.util.List;

/**
 * Created by air on 2017/10/6.
 *
 */

@Table(name = "DownloadItem")
public class DownloadItem extends Model{

    private final static String ID = "r_id";
    private final static String TYPE = "type";
    private final static String APPTYPE = "appType";
    private final static String STATE = "state";
    private final static String NAME = "name";
    private final static String PACKAGENAME = "packageName";
    private final static String ICON = "icon";
    private final static String URL = "url";
    private final static String PROCESS = "process";
    private final static String VIDEOTYPE = "videoType";

    public final static int DOWNLOAING = 0;//下载中
    public final static int PAUSE = 1;//暂停
    public final static int DOWNLOADED = 2;//完成
    public final static int VIDEO = 3;//本地视频
    public final static int FAILED = 4;//失败

    // 资源id
    @Column(name = ID,index = true)
    public int id;
    // 资源类型，0应用，1视频
    @Column(name = TYPE)
    public int type;
    // 应用类型 //0未知  1 tv   2 vr 3 系统应用
    @Column(name = APPTYPE)
    public int appType;
    // 0正在下载，1暂停，2下载完成(对于app来说是安装完成) 3本地视频(只有视频会有这个状态)
    @Column(name = STATE)
    public int state;
    // app的title，或影片的名字
    @Column(name = NAME)
    public String name;
    @Column(name = PACKAGENAME)
    public String packageName;
    // 图标
    @Column(name = ICON)
    public String icon;
    // 应用的包名，或视频的播放地址
    @Column(name = URL)
    public String url;
    // 下载进度,范围0.0-1.0
    @Column(name = PROCESS)
    public float process;
    // 视频格式,1普通，2左右3D，3单画面全景，4上下3D，5上下全景，-1异常未知
    @Column(name = VIDEOTYPE)
    public int videoType;


    public int getRId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getProcess() {
        return process;
    }

    public void setProcess(float process) {
        this.process = process;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(int videoType) {
        this.videoType = videoType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static List<DownloadItem> findDownloadItem(){

        return new Select().from(DownloadItem.class)
                .execute();
    }

    public static DownloadItem getDownloadItemById(String id){

        return new Select().from(DownloadItem.class)
                .where(ID +" = '" + id+"'")
                .executeSingle();
    }

    public static DownloadItem getDownloadItemByUrl(String url){

        return new Select().from(DownloadItem.class)
                .where(URL +" = '" + url+"'")
                .executeSingle();
    }

    public static DownloadItem getDownloadItemByPackageName(String packageName){

        return new Select().from(DownloadItem.class)
                .where(PACKAGENAME +" = ?", packageName)
                .executeSingle();
    }

    public static void update(int id,int state){
        new Update(DownloadItem.class)
                .set(STATE +" = ?", state)
                .where(ID+" = ? ",id)
                .execute();
    }

    public static void update(int id,int state,float process){
        new Update(DownloadItem.class)
                    .set(STATE +" = ? , "+ PROCESS +" = ? ", state,process)
                .where(ID+" = ? ",id)
                .execute();
    }

    public static void updateUrl(int id,String newUrl){
        new Update(DownloadItem.class)
                .set(URL +" = ?", newUrl)
                .where(ID+" = ? ",id)
                .execute();
    }

    public static void updateStatus(String url,int status){
        new Update(DownloadItem.class)
                .set(STATE +" = ?", status)
                .where(URL+" = ? ",url)
                .execute();
    }

    public static void updateStatusById(int id,int status){
        new Update(DownloadItem.class)
                .set(STATE +" = ?", status)
                .where(ID+" = ? ",id)
                .execute();
    }

    public static void deleteItem(String url){
        new Delete().from(DownloadItem.class).where("URL = ?", url).execute();
    }

    public static void deleteByPackage(String packageName){
        new Delete().from(DownloadItem.class).where(PACKAGENAME + " = ?", packageName).execute();
    }

    public static void deleteAll(){
        new Delete().from(DownloadItem.class).execute();
    }

    public static List<DownloadItem> getAll(){
        return new Select().from(DownloadItem.class)
                .execute();
    }
}
