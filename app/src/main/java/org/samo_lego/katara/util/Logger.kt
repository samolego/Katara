package org.samo_lego.katara.util

import android.util.Log

object Logger {
    private const val TAG = "KataraDebug"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
