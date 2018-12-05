package com.hch.viewlib.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by xnxs-ptzx04 on 2017/5/15.
 */

public class BaseActivity extends FragmentActivity {
    InputMethodManager manager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }
    private Toast toast = null;  //用于判断是否已有Toast执行
    public void showToastMsg(String msg) {
        if (toast == null) {
            toast = Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT);  //正常执行
        } else {
            toast.setText(msg);  //用于覆盖前面未消失的提示信息
        }
        toast.show();
    }
}
