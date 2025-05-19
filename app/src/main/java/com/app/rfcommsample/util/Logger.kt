package com.app.rfcommsample.util

import android.util.Log

object Logger {
    interface LogListener {
        fun onLog(message: String)
    }

    private var logListener: LogListener? = null
    private var logFiler: LogFiler = LogFiler.DEV

    enum class LogLevel(val tag: String) {
        DEBUG("D"), INFO("I"), WARN("W"), ERROR("E")
    }

    enum class LogFiler {
        DEV, QA
    }

    fun setLogListener(listener: LogListener?) {
        logListener = listener
    }

    fun log(tag: String = "BT-RFCOMM", message: String, logLevel: LogLevel = LogLevel.INFO) {
        val isLogAllowed = when (logFiler) {
            LogFiler.DEV -> true
            LogFiler.QA -> logLevel != LogLevel.DEBUG
        }

        "[${logLevel.tag}] $message".let {
            Log.d(tag, it)
            if (isLogAllowed)
                logListener?.onLog(it)
        }
    }

}