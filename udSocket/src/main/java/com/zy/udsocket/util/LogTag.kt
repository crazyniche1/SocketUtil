package com.zy.socketutil.util

import android.util.Log

/**
 */
object LogTag {

    var DEBUG = true

    @JvmStatic
    private fun LogTag() {
        throw IllegalStateException("you can't instantiate me!")
    }

    @JvmStatic
    fun isDebug(): Boolean {
        return DEBUG
    }

    @JvmStatic
    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }


    @JvmStatic
    fun d(log: String) {
        if (DEBUG) {
            Log.d(getTag(), getLog(log))
        }
    }

    @JvmStatic
    fun d(tag: String?, log: String) {
        if (DEBUG) {
            Log.d(tag, getLog(log))
        }
    }

    @JvmStatic
    fun e(log: String) {
        if (DEBUG) {
            Log.e(getTag(), getLog(log))
        }
    }

    @JvmStatic
    fun e(tag: String?, log: String) {
        if (DEBUG) {
            Log.e(tag, getLog(log))
        }
    }

    @JvmStatic
    fun i(log: String) {
        if (DEBUG) {
            Log.i(getTag(), getLog(log))
        }
    }

    @JvmStatic
    fun i(tag: String?, log: String) {
        if (DEBUG) {
            Log.i(tag, getLog(log))
        }
    }

    @JvmStatic
    fun v(log: String) {
        if (DEBUG) {
            Log.v(getTag(), getLog(log))
        }
    }

    @JvmStatic
    fun v(tag: String?, log: String) {
        if (DEBUG) {
            Log.v(tag, getLog(log))
        }
    }

    @JvmStatic
    fun w(log: String) {
        if (DEBUG) {
            Log.w(getTag(), getLog(log))
        }
    }

    @JvmStatic
    fun w(tag: String?, log: String) {
        if (DEBUG) {
            Log.w(tag, getLog(log))
        }
    }

    // 自定义Tag的前缀，可以是作者名
    @JvmStatic
    val customTagPrefix = "LogTag"

    @JvmStatic
    private fun getTag(): String? {
        return customTagPrefix
    }

    @JvmStatic
    private fun getLog(log: String): String {
        return print(log)
    }

    /**
     * 由于是按照层级采集的打印定位，所以千万不要动这个方法名和内部叠加数字
     * 这里用来实现点击跳转到对应代码行的需求
     *
     * @param content
     * @return
     */
    @JvmStatic
    fun print(content: String?): String {
        val traceElements = Thread.currentThread().stackTrace
        var poi = 0
        for (i in traceElements!!.indices) {
            if (traceElements != null && traceElements.size > i) {
                val traceElement = traceElements[i]
                val methodName = traceElement.methodName
                if (methodName == "print") { //当前方法名，千万不要动
                    poi = i + 3
                    break
                }
            }
        }
        val taskName = StringBuilder()
        if (traceElements != null && traceElements.size > poi) {
            val traceElement = traceElements[poi]
            taskName.append(traceElement.methodName)
            taskName.append("(").append(traceElement.fileName).append(":").append(traceElement.lineNumber).append(")")
        }
        taskName.append(content)
        return taskName.toString()
    }
}