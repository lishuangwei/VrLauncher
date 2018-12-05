package com.xunixianshi.vrlanucher.tvui.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.hch.utils.MLog;
import com.xunixianshi.vrlanucher.R;
import com.xunixianshi.vrlanucher.WoDouGameBaseFragment;
import com.xunixianshi.vrlanucher.server.AppInstallEvent;
import com.xunixianshi.vrlanucher.server.AppUninstallEvent;
import com.xunixianshi.vrlanucher.server.AppUpdateInstallEvent;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;
import com.xunixianshi.vrlanucher.server.ReceiveObj;
import com.xunixianshi.vrlanucher.server.ToServerEvent;
import com.xunixianshi.vrlanucher.server.utils.AppListUtil;
import com.xunixianshi.vrlanucher.tvui.adapter.AppBean;
import com.xunixianshi.vrlanucher.tvui.adapter.DataPagerAdapter;
import com.xunixianshi.vrlanucher.tvui.utils.LogUtil;
import com.xunixianshi.vrlanucher.tvui.views.Rotate3dAnimation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends WoDouGameBaseFragment {

    private Context mContext;
    private List<AppBean> mAppList = null;
    private int mPagerCount = -1;//一共的页数
    private List<AllApp> mPagerListAllApp = new ArrayList<AllApp>();
    private ViewPager mViewPager = null;
    private static final String TAG = "AppFragment";
    private static final boolean d = false;
    private TextView pointer = null;
    private Rotate3dAnimation rotation;
    private Receiver receiver;
    private DataPagerAdapter<AllApp> adapter;
    private AppListUtil appListUtil;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_app, null);
        mViewPager = (ViewPager) view.findViewById(R.id.app_view_pager);
        pointer = (TextView) view.findViewById(R.id.app_pointer);

//        initAnimation();
//        pointer.startAnimation(rotation);
        appListUtil = new AppListUtil(getActivity());
        initAllApp();
        EventBus.getDefault().register(this);
        mViewPager.setOnPageChangeListener(pageChangeListener);
        return view;
    }

    /**
     * 3D旋转动画
     */
    private void initAnimation() {
        rotation = new Rotate3dAnimation(0, 360, 25,
                25, 0.0f, false);
        rotation.setDuration(700);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator(2.0f));
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int position) {
//            pointer.startAnimation(rotation);
            pointer.setText((position + 1) + "/" + mPagerCount);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (pointer != null) {
//                pointer.startAnimation(rotation);
            }
        } else {
        }
    }

    /**
     * 初始化app数据和布局
     */
    public void initAllApp() {
        LogUtil.i("update UI");
        GetAppList getAppInstance = new GetAppList(mContext);
        List<AppBean> removeApp = new ArrayList<>();
//        mAppList = getAppInstance.getLaunchAppList();
        mAppList = getAppInstance.getUninstallAppList();
        //去掉类型为vr的已知应用
        List<AppDetialObj> appDetialObjs = appListUtil.getTypeTwoList();
        for (AppBean appBean : mAppList) {
            for (AppDetialObj appDetialObj : appDetialObjs) {
                if (appBean.getPackageName().equals(appDetialObj.getAppPackageName())) {
                    removeApp.add(appBean);
                    MLog.d("delete "+appBean.getPackageName());
                }
            }
        }
        mAppList.removeAll(removeApp);
        if (mPagerListAllApp != null && mPagerListAllApp.size() > 0) {
            mPagerListAllApp.clear();
        }
        if (mAppList.size() % 6 == 0) {
            mPagerCount = mAppList.size() / 6;
        } else {
            mPagerCount = mAppList.size() / 6 + 1;
        }

        for (int i = 0; i < mPagerCount; i++) {
            AllApp mAllayout = new AllApp(mContext, new AppOnclickInterface() {
                @Override
                public void appClick(int position) {
//                    EventBus.getDefault().post(new ToServerEvent(new ReceiveObj(3, "E9:CD:52:8C:B5:23")));
                    //type==0  设置为1 更新数据库
                    openApk(mAppList.get(position).getPackageName());
                }
            });
            mAllayout.setAppList(mAppList, i, mPagerCount);
            mAllayout.managerAppInit();
            mPagerListAllApp.add(mAllayout);
        }
        pointer.setText(1 + "/" + mPagerCount);
        adapter = new DataPagerAdapter<AllApp>(mContext, mPagerListAllApp);
        mViewPager.setAdapter(adapter);
    }

    private void openApk(String packageName) {
        appListUtil.openApk(packageName);
//        if(appListUtil.checkApkInDb(packageName)){
//            appListUtil.openApk(packageName);
//        }else{
//            LogUtil.d("应用第一次打开");
//            //这里需要弹框选择
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.PACKAGE_ADDED");
//        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("com.xunixianshi.vrlanucher.UPDATA_APPLIST");
//        filter.addDataScheme("package");
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            mContext.unregisterReceiver(receiver);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //安装广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                initAllApp();
            }
            //卸载广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                initAllApp();
            }
            //更新列表广播
            if (intent.getAction().equals("com.xunixianshi.vrlanucher.UPDATA_APPLIST")) {
                initAllApp();
            }
        }
    }

    @Subscribe
    public void onEventMainThread(AppUpdateInstallEvent event) {
        initAllApp();
    }
}


