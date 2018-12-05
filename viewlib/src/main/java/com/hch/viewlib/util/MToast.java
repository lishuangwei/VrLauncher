package com.hch.viewlib.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xnxs-ptzx04 on 2017/5/18.
 */

public class MToast {

    public static void show(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
