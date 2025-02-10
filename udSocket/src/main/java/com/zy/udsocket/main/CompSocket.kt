package com.zy.udsocket.main

import android.util.Log
import com.zy.socketutil.api.Operation2
import com.zy.socketutil.util.DataUtils
import com.zy.socketutil.util.LogTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import toUnsignedHexString
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor
import kotlin.properties.Delegates

/**
 *
 * @ClassName:      Constraints$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2024/9/25$
 * @UpdateUser:     updater
 * @UpdateDate:     2025/01/16$
 * @UpdateRemark:   添加 错误捕获机制 优化send 、received 函数
 * @Version:        2.0
 */
class CompSocket : Operation2 {

    private var cScope: CoroutineScope
    private var cHost: String
    private var cPort by Delegates.notNull<Int>()
    private var cRetryCount = 0
    private var cRetryTime = 0

    private var retryNum = 1
    private var sendAndReceivedTime: Long = 0
    private var showLog = false
    private var errorAction: (Throwable) -> Unit = {}
    private var receivedDataH: (ByteArray) -> Unit = {}


    private  var mSocket: Socket ?=null

    constructor(builder: Builder) {
        this.cScope = builder.bscope
        this.cHost = builder.bhost
        this.cPort = builder.bport
        this.cRetryCount = builder.bRetryCount
        this.cRetryTime = builder.bRetryTime
        this.sendAndReceivedTime = builder.sendAndReceivedTime
        this.showLog = builder.showLog
        this.errorAction = builder.errorAction

        initS()
    }

    fun getInitResult(): Boolean {
        return mSocket==null || mSocket?.isClosed == true
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        errorAction.invoke(exception)
    }

    private fun printThreadInfo() {
        Log.e("TAG", "printThreadInfo:${Thread.currentThread().name} ")
    }


    fun CoroutineScope.isUsingIODispatcher(): Boolean {
        val context = coroutineContext[ContinuationInterceptor]
        return context is CoroutineDispatcher && context == Dispatchers.IO
    }


    // 初始化socket 如果失败会自动重试3次
    private fun initS() {
//        val scope = CoroutineScope(Dispatchers.IO + Job())
        cScope.launch{
            LogTag.d("CompSocket---初始化-开始")

            runCatching {
                mSocket = Socket()
                mSocket?.connect(InetSocketAddress(cHost, cPort), cRetryTime.toInt())
            }.also {
                it.onFailure { it2 ->
                    if (showLog) LogTag.e("CompSocket---初始化结束-失败$it2 ${retryNum < cRetryCount}")
                    if (retryNum > cRetryCount - 1) {
                        retryNum = 1
                        errorAction.invoke(it2)
                        return@launch
                    }
                    retryNum += 1
                    cScope.cancel()
                    initS()
                }
                it.onSuccess {
                    LogTag.d("CompSocket---初始化结束-成功")
                    di = DataOutputStream(mSocket?.getOutputStream())
                    ds = DataInputStream(mSocket?.getInputStream())
                }
            }

        }
    }

    fun closeSocket() {
        if (!getInitResult()) mSocket?.close()
        ds?.let { ds = null }
        di?.let { di = null }
    }

    private fun checkScope() {
        if (!cScope.isActive) {
            if (showLog) LogTag.e("CompSocket---baseScope inexistence")
            resetData()
            errorAction.invoke(Exception("CompSocket---baseScope inexistence"))

        }
    }

    private var di: DataOutputStream? = null

    private fun sendNew(msg:ByteArray ) {
        cScope.launch {
            if (showLog) LogTag.d("客户端发送的信息:${toUnsignedHexString (msg)}")
            kotlin.runCatching {
                di?.let {
                    it.write(msg)
                    it.flush()
                }
                receivedNew()
            }.also {
                it.onFailure { it2 ->
                    resetData()
                    errorAction.invoke(it2)
                }
            }

        }

    }

    private var receivedData = ""

    private var ds: DataInputStream? = null

    private suspend fun receivedNew() {
        ds?.let {inputStream->
            val buffer = ByteArray(256)
            var size :Int
            runCatching {
//                while (inputStream.read(buffer ).also { size = it } != -1) {
//                    val str = DataUtils.bytesToHexString(buffer .copyOfRange(0, size)) ?: ""
//                    if (showLog) LogTag.d("服务端给客户端接收的信息:$str")
//                    receivedData = str
//                    receivedDataH.invoke(str)
//                }
                val bytesRead = inputStream.read(buffer)
                val responseBytes = ByteArray(bytesRead)
                System.arraycopy(buffer, 0, responseBytes, 0, bytesRead)
                val str = toUnsignedHexString(responseBytes)
                if (showLog) LogTag.d("客户端接收的信息:$str")
                receivedData = str
                receivedDataH.invoke(responseBytes)

            }.also {
                it.onFailure { it2 ->
                    resetData()
                    errorAction.invoke(it2)
                }
            }
        }

    }

    //当Broken pipe 时重置receivedData
    private fun resetData() {
        receivedData = ""
    }

    /**
     * old function
     */

    private suspend fun send(msg: String) {
        delay(0)
        runCatching {
//            di = DataOutputStream(mSocket.getOutputStream())
            di?.let {
                it.write(DataUtils.hexStringToBytes(msg))
                it.flush()
            }
            if (showLog) LogTag.d("客户端发送的信息:$msg")

        }.also { it ->
            it.onFailure { it2 ->
                if (showLog) LogTag.e("CompSocket---send$it2")
                //Broken pipe   服务端中途断开
                resetData()
            }
        }
    }

    private suspend fun received() = withContext(Dispatchers.Default) {

        runCatching {
            ds = DataInputStream(mSocket?.getInputStream())
            ds?.let {
                val data = ByteArray(1024)
                var size = 0
                while (it.read(data).also { size = it } !== -1) {
                    val str = DataUtils.bytesToHexString(data.copyOfRange(0, size)) ?: ""
                    if (showLog) LogTag.d("服务端给客户端接收的信息:$str")
                    receivedData = str
                }
            }

        }.also { it ->
            it.onFailure {
                if (showLog) LogTag.e("CompSocket---received:$it")
                resetData()
            }
        }

        return@withContext receivedData
    }

    private var fixedThreadPool = Executors.newSingleThreadExecutor()
    private  fun sends(msg: ByteArray) {

        checkScope()
        fixedThreadPool.submit {
            runCatching {
                di?.let {
                    it.write(msg)
                    it.flush()
                }

            } .also {
                it.onFailure { it2 ->
                    resetData()
                    errorAction.invoke(it2)
                }
            }
        }
    }

    class Builder {

        lateinit var bscope: CoroutineScope
        lateinit var bhost: String
        var bport by Delegates.notNull<Int>()
        var bRetryCount: Int = 2
        var bRetryTime: Int = 2000

        //跟据实际情况设置
        var sendAndReceivedTime: Long = 0
        var showLog = true
        lateinit var errorAction: (Throwable) -> Unit

        fun build(): CompSocket {
            return CompSocket(this@Builder)
        }

        fun setHost(host: String): Builder {
            this.bhost = host
            return this
        }

        fun setPorts(port: Int): Builder {
            this.bport = port
            return this
        }

        fun setScope(scope: CoroutineScope): Builder {
            this.bscope = scope
            return this
        }

        /**
         * 超时次数
         */
        fun setRetryCount(retryCount: Int): Builder {
            this.bRetryCount = retryCount
            return this
        }

        /**
         * 超时时间
         */
        fun setRetryTime(retryTime: Int): Builder {
            this.bRetryTime = retryTime
            return this
        }

        /**
         * 设置发送和接收报文时间间隔，默认0毫秒
         */
        fun sendAndReceivedTime(time: Long): Builder {
            this.sendAndReceivedTime = time
            return this
        }

        /**
         * 默认展示报文日志
         */
        fun isShowLog(isShowLog: Boolean): Builder {
            this.showLog = isShowLog
            return this
        }

        /**
         * 获取Error
         */
        fun getError(handler: (Throwable) -> Unit): Builder {
            this.errorAction = handler
            return this
        }
    }

    override fun <T> sendMessage(message: T) {
        if (message !is ByteArray) return
        val sm = message as ByteArray
        if (getInitResult()) return
        if (sm.isEmpty()) return
        checkScope()
        sendNew(sm)
    }

    override fun <R> receivedData(): R {
        @Suppress("UNCHECKED_CAST")
        return  receivedData as R
    }

    override fun <T> receivedDataH(data: (T) -> Unit, default: T) {

        receivedDataH = {
            data.invoke(it as T)
        }
    }

}

