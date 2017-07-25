package com.example.util;

import android.util.Log;

/**
 * Created by Yusuzhan on 2017/4/24.
 */

public class L {
    public static final String TAG = "EventBike";

    public static void  i(Object obj){
        Log.i(TAG, obj.toString());
    }

    public static void v(Object obj){
        Log.v(TAG, obj.toString());
    }
}
