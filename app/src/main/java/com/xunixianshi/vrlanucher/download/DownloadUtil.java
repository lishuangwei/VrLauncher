package com.xunixianshi.vrlanucher.download;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItem;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.server.utils.DownloadListItemObj;
import com.xunixianshi.vrlanucher.tvui.home.HomeActivity;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;

import java.io.File;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import zlc.season.rxdownload2.RxDownload;
import zlc.season.rxdownload2.entity.DownloadEvent;
import zlc.season.rxdownload2.entity.DownloadFlag;
import zlc.season.rxdownload2.entity.DownloadRecord;


/**
 * Created by Administrator on 2017/9/29.
 * 七牛云存储下载
 */

public class DownloadUtil {

    public static String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/3box/download/apk";
    public static String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/3box/download/video";
    public static String firmwarePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/3box/download/firmware";
    public static String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/3box/image/";
//            +File.separator+"3box"+File.separator+"download"+File.separator+"firmware";


    public static String downloadRUL1 = "http://xnxs.inter.video.vrshow.com/009a44bbae8e69a94e14736c1c3d3871.mp4";
    public static String downloadRUL2 = "http://xnxs.inter.video.vrshow.com/1234561483696829269.mp4";
    private static String downloadRUL = "";
    private static boolean isPause = false;

    private DownloadUtil() {
    }

    public static String create(Context context, String... params) {
        //第一个参数为url  第二个参数Access_Key 第三个参数Secret_Key(访问密钥)
        String ACCESS_KEY = params[1];
        String SECRET_KEY = params[2];
        //密钥配置
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        downloadRUL = auth.privateDownloadUrl(params[0], 3600);
        return downloadRUL;
    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-07 : 13:34:23
     * <p>
     * 常规下载
     * 只能在subscribe回调中监听下载状态
     * <p>
     * 取消订阅, 即可暂停下载
     * if (disposable != null && !disposable.isDisposed()) {
     * disposable.dispose();
     * }
     * <p>
     * 再次调用继续下载
     **/
    public static void multiThreadNormalDownload(Context context, String downloadRUL, Consumer consumer) {
        Disposable disposable = RxDownload.getInstance(context)
                .download(downloadRUL)//只传url即可
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-06 : 18:06:27
     * xxDownload(String url) 当只传 url 时，会自动从服务器获取文件名
     * xxDownload(String url, String saveName) 也可手动指定保存的文件名称
     * xxDownload(String url,String saveName,String savePath) 手动指定文件名和保存路径
     * <p>
     * 后台服务下载，不能在subscribe回调中接受到下载状态
     * 需要调用 {@link #receiveDownloadStatus} 方法监听
     * 再次调用继续下载
     **/
    public static void multiThreadServiceDownload(Context context,
                                                  String downloadRUL,
                                                  String fileName,
                                                  String filePath,
                                                  Consumer consumer,
                                                  Consumer consumerError,
                                                  Action action) {
        RxDownload.getInstance(context)
                .serviceDownload(downloadRUL, fileName, filePath)   //只需传url即可，添加一个下载任务
                .subscribe(consumer, consumerError, action);
    }

    public static void multiThreadServiceReDownload(Context context,
                                                    String downloadRUL,
                                                    Consumer consumer,
                                                    Consumer consumerError,
                                                    Action action) {
        RxDownload.getInstance(context)
                .serviceDownload(downloadRUL)   //只需传url即可，添加一个下载任务
                .subscribe(consumer, consumerError, action);
    }


    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-07 : 13:34:23
     * 暂停下载 （使用{@link #multiThreadServiceDownload }的时候使用）
     **/
    public static void pauseDownload(Context context, String downloadRUL) {
        isPause = true;
        RxDownload.getInstance(context).pauseServiceDownload(downloadRUL).subscribe();

    }

    /**
     * author:
     * En_Name:
     * E-mail:
     * version:
     * Created Time: 2017-10-07 : 13:34:23
     * 接收事件可以在任何地方接收，不管该任务是否开始下载均可接收.（ 使用{@link #multiThreadServiceDownload }的时候使用）
     *
     * @param consumer 下载状态
     **/
    public static void receiveDownloadStatus(Context context, final String downloadRUL, Consumer<DownloadEvent> consumer) {
        RxDownload.getInstance(context).receiveDownloadStatus(downloadRUL)
                .subscribe(consumer);
    }

    //暂停地址为 url 的下载并从数据库中删除记录，deleteFile 为 true 会同时删除该 url 下载产生的所有文件
    public static void deleteServiceDownload(Context context, final String downloadRUL, boolean deleteFile) {
        RxDownload.getInstance(context).deleteServiceDownload(downloadRUL, deleteFile).subscribe();
        //从数据库删除记录
        DownloadItem.deleteItem(downloadRUL);
    }

    //暂停地址为 url 的下载并从数据库中删除记录，deleteFile 为 true 会同时删除该 url 下载产生的所有文件
    public static void deleteDBByUrl(final String downloadRUL) {
        //从数据库删除记录
        DownloadItem.deleteItem(downloadRUL);
    }

    //获取下载记录
    public static void getDownloadRecords(Context context) {
        RxDownload.getInstance(context).getTotalDownloadRecords()
                .subscribe(new Consumer<List<DownloadRecord>>() {
                    @Override
                    public void accept(@NonNull List<DownloadRecord> downloadRecords) throws Exception {
                    }
                });
    }


    public static File getDownloadPath(Context context, String url) {
        //利用url获取
        File[] files = RxDownload.getInstance(context).getRealFiles(url);
        if (files != null) {
            return files[0];
        }
        return null;
    }

    public static void startDownload(final Context mContext, final DownloadListItemObj downloadListItemObj) {
        String downUrl = downloadListItemObj.getAppDownloadUrl();
        downUrl = downUrl.substring(0, downUrl.lastIndexOf("?"));
        LogUtil.d("startDownload:开始下载：" + downUrl);
        //保存数据库
        String filePath = "";
        final DownloadItem downloadItem = new DownloadItem();
        downloadItem.setId(Integer.parseInt(downloadListItemObj.getAppResourceId()));
        downloadItem.setType(Integer.parseInt(downloadListItemObj.getDownType()));
        downloadItem.setAppType(Integer.parseInt(downloadListItemObj.getApkType()));
        downloadItem.setUrl(downUrl);
        downloadItem.setName(downloadListItemObj.getAppName());
        downloadItem.setIcon(downloadListItemObj.getAppIconUrl());
        downloadItem.setVideoType(downloadListItemObj.getVideoType());
        downloadItem.setPackageName(downloadListItemObj.getAppPackageName());

        if (downloadListItemObj.getDownType().equals("0")) {// 0 应用  1 视频   2 系统固件
            filePath = apkPath;
        } else if (downloadListItemObj.getDownType().equals("1")) {
            filePath = videoPath;
        } else if (downloadListItemObj.getDownType().equals("2")) {
            filePath = firmwarePath;
        }
        DownloadUtil.multiThreadServiceDownload(mContext,
                downUrl,
                downloadListItemObj.getAppName(),
                filePath,
                new Consumer<DownloadEvent>() {
                    @Override
                    public void accept(DownloadEvent event) throws Exception {
                        //当事件为Failed时, 才会有异常信息, 其余时候为null.
                        if (event.getFlag() == DownloadFlag.FAILED) {
                            Throwable throwable = event.getError();
                            Log.w("Error", throwable);
                        }
                        LogUtil.d(downloadListItemObj.getAppName() + "getDownloadSize" + event.getDownloadStatus().getDownloadSize());
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
//                File downloadFile =  DownloadUtil.getDownloadPath(mContext,downUrl);
//                AppDetialObj appDetialObj = new AppDetialObj();
//                appDetialObj.setAppResourceId(downloadListItemObj.getAppResourceId());
//                appDetialObj.setAppType(downloadListItemObj.getApkType());
//                appDetialObj.setAppName(downloadListItemObj.getAppName());
//                appDetialObj.setAppPackageName(downloadListItemObj.getAppPackageName());
//                AppListUtil appListUtil = new AppListUtil(mContext);
//                appListUtil.saveInstallApp(downloadFile.getPath(),appDetialObj);
                    }
                });

        //接收事件可以在任何地方接收，不管该任务是否开始下载均可接收.
        final String finalDownUrl = downUrl;
        RxDownload.getInstance(mContext).receiveDownloadStatus(downUrl)
                .subscribe(new Consumer<DownloadEvent>() {
                    @Override
                    public void accept(DownloadEvent event) throws Exception {
                        //当事件为 Failed 时, 才会有异常信息, 其余时候为 null.
                        if (event.getFlag() == DownloadFlag.STARTED) {
                            LogUtil.d("startDownload -- 开始下载 -- STARTED：" + finalDownUrl);
                            //开始下载.
                            DownloadItem item = DownloadItem.getDownloadItemById(downloadListItemObj.getAppResourceId() + "");

                            if (item == null) {
                                downloadItem.setState(DownloadItem.DOWNLOAING);
                                downloadItem.save();
                            } else {
                                long percent = event.getDownloadStatus().getPercentNumber();
                                float f_percent = (float) (percent / 100.0);
                                if (!isPause) {
                                    DownloadItem.update(downloadItem.getRId(), DownloadItem.DOWNLOAING, f_percent);
                                }
                                LogUtil.d("startDownload -- process：" + f_percent);
                            }

                        } else if (event.getFlag() == DownloadFlag.PAUSED) {//暂停
                            LogUtil.d("startDownload -- 暂停 -- PAUSED：" + finalDownUrl);
//                            DownloadItem.update(downloadItem.getRId(),DownloadItem.PAUSE);
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.PAUSE, f_percent);
                        } else if (event.getFlag() == DownloadFlag.FAILED) {//下载失败
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.FAILED, f_percent);
                            LogUtil.d("startDownload -- 下载失败 -- FAILED："+ finalDownUrl);
//                            DownloadItem.update(downloadItem.getRId(),DownloadItem.FAILED);
//                            Throwable throwable = event.getError();
//                            Log.w("Error", throwable);
                        } else if (event.getFlag() == DownloadFlag.COMPLETED) {//下载成功
                            LogUtil.d("startDownload -- 下载完成 -- COMPLETED：" + finalDownUrl);
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.DOWNLOADED, f_percent);
                            if (downloadListItemObj.getDownType().equals("0")) {//apk
                                // update url字段为报名
                                DownloadItem.updateUrl(downloadItem.getRId(), downloadListItemObj.getAppPackageName());
                                File downloadFile = DownloadUtil.getDownloadPath(mContext, finalDownUrl);
                                AppDetialObj appDetialObj = new AppDetialObj();
                                appDetialObj.setAppResourceId(downloadListItemObj.getAppResourceId());
                                appDetialObj.setAppType(downloadListItemObj.getApkType());
                                appDetialObj.setAppName(downloadListItemObj.getAppName());
                                appDetialObj.setAppPackageName(downloadListItemObj.getAppPackageName());
                                AppListUtil appListUtil = new AppListUtil(mContext);
                                appListUtil.saveInstallApp(downloadFile.getPath(), appDetialObj);
                            }
                            if (downloadListItemObj.getDownType().equals("2")) {
                                //系统固件下载完成  调用升级固件方法

                            } else { //视频
                                File filePath = DownloadUtil.getDownloadPath(mContext, finalDownUrl);
                                DownloadItem.updateUrl(downloadItem.getRId(), filePath.getAbsolutePath());
                            }


                            //发送系统广播
//                            Intent intent = new Intent();  //Itent就是我们要发送的内容
//                            intent.putExtra("data", "this is data ");
//                            intent.putExtra("Type",1);//1 代表下载安装 2代表数据线安装
//                            intent.setAction("com.xunixianshi.vrlanucher.PACKAGE_ADDED");//设置action，和这个action相同的的接受者才能接收广播
//                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                            mContext.sendBroadcast(intent);
                        }
                    }
                });
    }

    public static void reDownload(final Context mContext, String downUrl) {
        LogUtil.d("恢复下载：" + downUrl);
        isPause = false;
        String filePath = "";
        final DownloadItem downloadItem = DownloadItem.getDownloadItemByUrl(downUrl);

        DownloadUtil.multiThreadServiceReDownload(mContext,
                downUrl,
                new Consumer<DownloadEvent>() {
                    @Override
                    public void accept(DownloadEvent event) throws Exception {
                        //当事件为Failed时, 才会有异常信息, 其余时候为null.
                        if (event.getFlag() == DownloadFlag.FAILED) {
                            Throwable throwable = event.getError();
                            Log.w("Error", throwable);
                        }
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });

        //接收事件可以在任何地方接收，不管该任务是否开始下载均可接收.
        RxDownload.getInstance(mContext).receiveDownloadStatus(downUrl)
                .subscribe(new Consumer<DownloadEvent>() {
                    @Override
                    public void accept(DownloadEvent event) throws Exception {
                        //当事件为 Failed 时, 才会有异常信息, 其余时候为 null.
                        if (event.getFlag() == DownloadFlag.STARTED) {
                            LogUtil.d("reDownload -- STARTED -- 开始下载");
                            //开始下载.
                            DownloadItem item = DownloadItem.getDownloadItemById(downloadItem.getRId() + "");
                            if (item == null) {
                                downloadItem.setState(DownloadItem.DOWNLOAING);
                                downloadItem.save();
                            } else {
                                long percent = event.getDownloadStatus().getPercentNumber();
                                float f_percent = (float) (percent / 100.0);
                                LogUtil.d("reDownload -- process：" + f_percent);
                                if (!isPause) {
                                    DownloadItem.update(downloadItem.getRId(), DownloadItem.DOWNLOAING, f_percent);
                                }
                            }

                        } else if (event.getFlag() == DownloadFlag.PAUSED) {//暂停
                            LogUtil.d("reDownload -- PAUSED -- 暂停：");
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.PAUSE, f_percent);
                        } else if (event.getFlag() == DownloadFlag.FAILED) {//下载失败
                            LogUtil.d("reDownload -- FAILED -- 失败：");
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.FAILED, f_percent);
//                            DownloadItem.update(downloadItem.getRId(),DownloadItem.FAILED);
//                            Throwable throwable = event.getError();
//                            Log.w("Error", throwable);
                        } else if (event.getFlag() == DownloadFlag.COMPLETED) {//下载成功
                            LogUtil.d("reDownload -- COMPLETED --完成");
                            long percent = event.getDownloadStatus().getPercentNumber();
                            float f_percent = (float) (percent / 100.0);
                            DownloadItem.update(downloadItem.getRId(), DownloadItem.DOWNLOADED, f_percent);
                            if (downloadItem.getType() == 0) {//apk
                                // update url字段为报名
                                DownloadItem.updateUrl(downloadItem.getRId(), downloadItem.getName());
                                File downloadFile = DownloadUtil.getDownloadPath(mContext, downloadItem.getUrl());
                                AppDetialObj appDetialObj = new AppDetialObj();
                                appDetialObj.setAppResourceId(downloadItem.getRId() + "");
                                appDetialObj.setAppType(downloadItem.getType() + "");
                                appDetialObj.setAppName(downloadItem.getName());
                                appDetialObj.setAppPackageName(downloadItem.getPackageName());
                                AppListUtil appListUtil = new AppListUtil(mContext);
                                appListUtil.saveInstallApp(downloadFile.getPath(), appDetialObj);
                            }else if (downloadItem.getType() == 2) {//系统固件
                                //系统固件下载完成  调用升级固件方法

                            } else {//视频
                                File filePath = DownloadUtil.getDownloadPath(mContext, downloadItem.getUrl());
                                DownloadItem.updateUrl(downloadItem.getRId(), filePath.getAbsolutePath());
                            }
                        }
                    }
                });
    }
}
