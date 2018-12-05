package com.xunixianshi.vrlanucher.tvui.setting;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import com.xunixianshi.vrlanucher.R;
import com.xunixianshi.vrlanucher.WoDouGameBaseFragment;
import com.xunixianshi.vrlanucher.tvui.app.AppUninstall;
import com.xunixianshi.vrlanucher.tvui.bluetooth.Bluetooth;
import com.xunixianshi.vrlanucher.tvui.utils.ImageCache;
import com.xunixianshi.vrlanucher.tvui.utils.ImageFetcher;
import com.xunixianshi.vrlanucher.tvui.utils.ImageWorker;
import com.xunixianshi.vrlanucher.tvui.wifi.WifiActivity;

import java.util.List;

/**
 * Created by Administrator on 2014/9/9.
 */
public class SettingFragment extends WoDouGameBaseFragment implements
        View.OnClickListener {
    private ImageWorker mImageLoader;
//    private ImageButton Setting_Clean;// 垃圾清理
//    private ImageButton Setting_Accelerate;// 一键加速
    private ImageButton appUninstall;
//    private ImageButton setWifi;
//    private ImageButton setBlueTooth;
//    private ImageButton setVolume;
    private ImageButton about;
    private ImageButton fileManage;
//    private ImageButton setNet;
//    private ImageButton setMore;
//    private ImageButton netSpeed;
//    private ImageButton sysUpdate;
//    private ImageButton fileManage;
//    private ImageButton about;
//    private ImageButton autoRun;
    private View view;// 视图
    private Intent JumpIntent;
    private Context context;

    /**
     * 用来存放
     */
    private List<ContentValues> datas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_setting, null);
        initView(view);
        setListener();
        // Bundle bundle = this.getArguments();
        // String data = bundle.getString("url_data");
        // UIResponseParam uiResponseParam = null;
        // try {
        // uiResponseParam = new UIResponseParam(data);
        // datas = uiResponseParam.getUIInfo();
        // showImages();
        // } catch (JSONException e) {
        // e.printStackTrace();
        // }
        return view;
    }

    private void initView(View view) {

//        FocusedRelativeLayout focus = (FocusedRelativeLayout) view
//                .findViewById(R.id.setting_focus_rl);
//        focus.setFocusResId(R.drawable.focus_bg);
//        focus.setFocusShadowResId(R.drawable.focus_shadow);
//        focus.setFocusable(true);
//        focus.setFocusableInTouchMode(true);
//        focus.requestFocus();
//        focus.requestFocusFromTouch();

        appUninstall = (ImageButton) view.findViewById(R.id.setting_uninstall);
//        setWifi = (ImageButton) view.findViewById(R.id.setting_wifi);
//        setBlueTooth = (ImageButton) view.findViewById(R.id.setting_bluetooth);
//        setVolume = (ImageButton) view.findViewById(R.id.setting_volume);
        about = (ImageButton) view.findViewById(R.id.setting_about);
        fileManage = (ImageButton) view.findViewById(R.id.setting_file);
//        about = (ImageButton) view.findViewById(R.id.setting_about);
//        Setting_Clean = (ImageButton) view.findViewById(R.id.setting_clean);
//        Setting_Accelerate = (ImageButton) view.findViewById(R.id.setting_accelerate);
//        autoRun = (ImageButton) view.findViewById(R.id.setting_autorun);

//        appUninstall.setOnFocusChangeListener(mFocusChangeListener);
//        setWifi.setOnFocusChangeListener(mFocusChangeListener);
//        setBlueTooth.setOnFocusChangeListener(mFocusChangeListener);
//        setVolume.setOnFocusChangeListener(mFocusChangeListener);
//        about.setOnFocusChangeListener(mFocusChangeListener);
//        fileManage.setOnFocusChangeListener(mFocusChangeListener);
//        about.setOnFocusChangeListener(mFocusChangeListener);
//        Setting_Clean.setOnFocusChangeListener(mFocusChangeListener);
//        Setting_Accelerate.setOnFocusChangeListener(mFocusChangeListener);
//        autoRun.setOnFocusChangeListener(mFocusChangeListener);

        }

    private void setListener() {
//        Setting_Clean.setOnClickListener(this);
//        Setting_Accelerate.setOnClickListener(this);
//        about.setOnClickListener(this);
//        setMore.setOnClickListener(this);
        appUninstall.setOnClickListener(this);
//        setWifi.setOnClickListener(this);
//        setBlueTooth.setOnClickListener(this);
//        setVolume.setOnClickListener(this);
        about.setOnClickListener(this);
        fileManage.setOnClickListener(this);
    }

    private void showImages() {
        mImageLoader = new ImageFetcher(this.getActivity());
        mImageLoader.setImageCache(ImageCache.getInstance(this.getActivity()));
        datas = datas.subList(11, 17);
        for (int i = 0; i < datas.size(); i++) {
            int picPosition = datas.get(i).getAsInteger("picPosition");
            String picPath = datas.get(i).getAsString("picPath");
            switch (picPosition) {
                case 1:
                    // mImageLoader.loadImage(picPath, iv_1,
                    // R.drawable.where_is_father);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.setting_wifi:
//                JumpIntent = new Intent(context, WifiActivity.class);
//                startActivity(JumpIntent);
//                break;
//            case R.id.setting_bluetooth:
//                JumpIntent = new Intent(context, Bluetooth.class);
//                startActivity(JumpIntent);
//                break;
//            case R.id.setting_volume:
//                break;
            case R.id.setting_about:
                JumpIntent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(JumpIntent);
                break;
            case R.id.setting_file:
                doStartApplicationWithPackageName("com.cyanogenmod.filemanager");
                break;
//            case R.id.setting_update:
//                break;
//            case R.id.setting_net:
//                JumpIntent = new Intent(context, SettingCustom.class);
//                startActivity(JumpIntent);
//                break;
            case R.id.setting_uninstall:
                JumpIntent = new Intent(context, AppUninstall.class);
                startActivity(JumpIntent);
                break;
//            case R.id.setting_autorun:
//                JumpIntent = new Intent(context, AppAutoRun.class);
//                startActivity(JumpIntent);
//                break;
//            case R.id.setting_net_speed:
//                JumpIntent = new Intent(context, SpeedTestActivity.class);
//                startActivity(JumpIntent);
//                break;
        }
    }

    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getActivity().getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getActivity().getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }
}
