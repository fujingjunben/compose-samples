package com.example.jetcaster.util

import android.util.Log

object LogUtil {
    private const val TAG = "PlayerBar"
    fun d(msg: String){
        d(TAG, msg)
    }
    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

}