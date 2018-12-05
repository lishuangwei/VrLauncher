package com.xunixianshi.vrlanucher.tvui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.Toast;

import com.xunixianshi.vrlanucher.BaseActivity;
import com.xunixianshi.vrlanucher.MainActivity;
import com.xunixianshi.vrlanucher.R;
import com.xunixianshi.vrlanucher.download.DownloadUtil;
import com.xunixianshi.vrlanucher.server.AdvertiserService;
import com.xunixianshi.vrlanucher.server.BlueToothServer;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.DownloadItem;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.tvui.adapter.MainActivityAdapter;
import com.xunixianshi.vrlanucher.tvui.app.AppFragment;
import com.xunixianshi.vrlanucher.tvui.setting.SettingFragment;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.utils.VersionUtil;
import com.xunixianshi.vrlanucher.vrui.UnityPlayerActivity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import zlc.season.rxdownload2.RxDownload;
import zlc.season.rxdownload2.entity.DownloadEvent;
import zlc.season.rxdownload2.entity.DownloadFlag;

/**
 * Created by xnxs-ptzx04 on 2017/8/25.
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private RadioButton setting;
    private RadioButton app;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private View mViews[];
    private int currentIndex;

    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private AppListUtil appListUtil;
    private VersionUtil versionUtil;
    private List<Integer> devices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        audioManager = (AudioManager) HomeActivity.this.getSystemService(Context.AUDIO_SERVICE);
        devices = new ArrayList<>();
        initView();
        //开启android蓝牙通信服务
        startService(new Intent(HomeActivity.this, BlueToothServer.class));
        //开启ios蓝牙通信服务
        startService(new Intent(HomeActivity.this, AdvertiserService.class));
        versionUtil = new VersionUtil(HomeActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            } else {
                // 初始化本地应用列表
                appListUtil = new AppListUtil(HomeActivity.this);
                appListUtil.initAppListToDb();
                versionUtil.writeVersionFile();
            }
//            int permissionCheck = 0;
//            permissionCheck = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
//            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                //注册权限
//                this.requestPermissions(
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION},
//                        1001); //Any number
//            } else {//已获得过权限
//                //进行蓝牙设备搜索操作
//            }
        }
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                // 初始化本地应用列表
                appListUtil = new AppListUtil(HomeActivity.this);
                appListUtil.initAppListToDb();
                versionUtil.writeVersionFile();
            } else {
                //用户不同意

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //去掉虚拟按键全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //       设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        for(UsbDevice device : map.values()){
            devices.add(device.getVendorId());
        }
        if(hasDevices(devices)){
            startActivity(new Intent(HomeActivity.this, UnityPlayerActivity.class));
        }else{
            //进入TV  不做处理
        }

    }

    private void initView() {
        mViewPager = (ViewPager) this.findViewById(R.id.main_viewpager);
        setting = (RadioButton) findViewById(R.id.main_title_setting);
        app = (RadioButton) findViewById(R.id.main_title_app);
        app.setSelected(true);
        mViews = new View[]{app, setting};
        setListener();
        initFragment();
    }

    private void setListener() {
        app.setOnClickListener(this);
        setting.setOnClickListener(this);

        app.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mViewPager.setCurrentItem(0);
                }
            }
        });
        setting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mViewPager.setCurrentItem(1);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(HomeActivity.this, BlueToothServer.class));
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        fragments.clear();//清空
//        int count = PAGE_NUMBER;

        FragmentManager manager;
        FragmentTransaction transaction;

		/* 获取manager */
        manager = this.getSupportFragmentManager();
        /* 创建事物 */
        transaction = manager.beginTransaction();

        SettingFragment setting = new SettingFragment();
        AppFragment app = new AppFragment();

        fragments.add(app);
        fragments.add(setting);

        transaction.commitAllowingStateLoss();

        MainActivityAdapter mAdapter = new MainActivityAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(pageListener);
        mViewPager.setCurrentItem(0);
    }

    /**
     * ViewPager切换监听方法
     */
    public ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            mViewPager.setCurrentItem(position);
            switch (position) {
                case 0:
                    currentIndex = 0;
                    app.setSelected(true);
                    setting.setSelected(false);
                    break;
                case 1:
                    currentIndex = 1;
                    setting.setSelected(true);
                    app.setSelected(false);
                    break;
//                case 2:
//                    currentIndex = 2;
//                    localService.setSelected(false);
//                    setting.setSelected(false);
//                    app.setSelected(true);
//                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_title_app:
                currentIndex = 0;
                mViewPager.setCurrentItem(0);
                break;
            case R.id.main_title_setting:
                currentIndex = 1;
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private boolean hasDevices(List<Integer> devices){
        for(int i :devices){
            if(i == 11036){
                return true;
            }
        }
        return false;
    }
    AudioManager audioManager;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.d("audioManager:"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        return super.onKeyDown(keyCode, event);
    }
}
