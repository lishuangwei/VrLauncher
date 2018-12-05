package com.xunixianshi.vrlanucher.server.utils;

import android.view.View;
import android.view.ViewGroup;


public class ViewHelpUtil {

    /**
     * 设置控件的宽高
     */
    public static void setViewLayoutParams(View view,int width,int height){
        ViewGroup.LayoutParams para = view.getLayoutParams();
        if(width>0){
            para.width = width;
        }
        if(height>0){
            para.height = height;
        }
        view.setLayoutParams(para);
    }
}
