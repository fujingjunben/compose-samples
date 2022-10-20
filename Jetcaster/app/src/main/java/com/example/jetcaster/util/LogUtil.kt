package com.example.jetcaster.util

import android.util.Log

object LogUtil {
    private const val TAG = "PlayerBar"
    fun d(msg: String){
        Log.d(TAG, msg)
    }
}