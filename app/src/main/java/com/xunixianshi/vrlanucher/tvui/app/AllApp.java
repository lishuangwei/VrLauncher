package com.xunixianshi.vrlanucher.tvui.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xunixianshi.vrlanucher.MyApplication;
import com.xunixianshi.vrlanucher.R;
import com.xunixianshi.vrlanucher.server.utils.DensityUtil;
import com.xunixianshi.vrlanucher.server.utils.ViewHelpUtil;
import com.xunixianshi.vrlanucher.tvui.adapter.AppBean;

import java.util.List;

@SuppressLint("NewApi")
public class AllApp extends LinearLayout implements View.OnClickListener{

	private AppOnclickInterface appOnclickInterface;

	public AllApp(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AllApp(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
    private Context mContext;
	private ImageView appIcons[] = new ImageView[15];
	private LinearLayout appItems[] = new LinearLayout[15];
	int iconIds[] = {R.id.app_icon0,R.id.app_icon1,R.id.app_icon2,
			R.id.app_icon3,R.id.app_icon4,R.id.app_icon5};
	private TextView appNames[] = new TextView[15];
	int nameIds[] = {R.id.app_name0,R.id.app_name1,R.id.app_name2,
			R.id.app_name3,R.id.app_name4,R.id.app_name5};
	int itemIds[] = {
			R.id.app_item0,R.id.app_item1,R.id.app_item2,
			R.id.app_item3,R.id.app_item4,R.id.app_item5
	};

	public AllApp(Context context,AppOnclickInterface appOnclickInterface) {
		super(context);
		mContext = context;
		this.appOnclickInterface = appOnclickInterface;
	}
	
	private List<AppBean> mAppList = null;
	private int mPagerIndex  = -1;
	private int mPagerCount = -1;
	public void setAppList(List<AppBean> list, int pagerIndex, int pagerCount)
	{
		mAppList = list;
		mPagerIndex = pagerIndex;
		mPagerCount = pagerCount;
	}
	
	public void managerAppInit()
	{
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_pager_layout, null);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
        int itemCount = -1;
		if(mPagerIndex < mPagerCount - 1)
		{
			itemCount = 6;
		}else
		{
			itemCount = (mAppList.size() - (mPagerCount-1)*6);
		}
		for(int i = 0; i < itemCount; i++)
		{
			appIcons[i] = (ImageView) v.findViewById(iconIds[i]);
			appNames[i] = (TextView)v.findViewById(nameIds[i]);
			appIcons[i].setImageDrawable(mAppList.get(mPagerIndex*6 + i).getIcon());
            appItems[i] = (LinearLayout)v.findViewById(itemIds[i]);
            appNames[i].setText(mAppList.get(mPagerIndex * 6 + i).getName());
            appItems[i].setVisibility(View.VISIBLE);
            appItems[i].setOnClickListener(this);
//            appItems[i].setOnFocusChangeListener(focusChangeListener);

			ViewHelpUtil.setViewLayoutParams(appItems[i],
					(MyApplication.screenWidth- DensityUtil.dip2px(mContext,100)) /3,
					(MyApplication.screenWidth- DensityUtil.dip2px(mContext,100)) /3);

			ViewHelpUtil.setViewLayoutParams(appIcons[i],
					MyApplication.screenWidth / 10,
					MyApplication.screenWidth / 10);

        }
		addView(v);
	}

//    public OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//
//            int focus = 0;
//            if (hasFocus) {
//                focus = R.anim.enlarge;
//            } else {
//                focus = R.anim.decrease;
//            }
////            如果有焦点就放大，没有焦点就缩小
//            Animation mAnimation = AnimationUtils.loadAnimation(
//                    mContext, focus);
//            mAnimation.setBackgroundColor(Color.TRANSPARENT);
//            mAnimation.setFillAfter(hasFocus);
//            v.startAnimation(mAnimation);
//            mAnimation.start();
//            v.bringToFront();
//        }
//    };
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		int position = -1;
		switch(id)
		{
		case R.id.app_item0:
			position = mPagerIndex*6 + 0;
			break;
		case R.id.app_item1:
			position = mPagerIndex*6 + 1;
			break;
		case R.id.app_item2:
			position = mPagerIndex*6 + 2;
			break;
		case R.id.app_item3:
			position = mPagerIndex*6 + 3;
			break;
		case R.id.app_item4:
			position = mPagerIndex*6 + 4;
			break;
		case R.id.app_item5:
			position = mPagerIndex*6 + 5;
			break;
			default:
				break;
		}
      if(position != -1)
		{
			appOnclickInterface.appClick(position);
//			PackageManager manager = mContext.getPackageManager();
//			String packageName = mAppList.get(position).getPackageName();
//			Intent intent=new Intent();
//		    intent =manager.getLaunchIntentForPackage(packageName);
//		    mContext.startActivity(intent);
		}
	}
}
