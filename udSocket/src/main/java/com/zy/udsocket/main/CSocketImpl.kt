package com.zy.socketutil.main

import com.zy.udsocket.api.OnResult
import com.zy.socketutil.api.Operation2
import com.zy.socketutil.util.LogTag
import com.zy.udsocket.main.CompSocket
import convertToIntList
import createReadRequest
import createWriteMultipleRegistersRequest
import isMatchingPair
import parseModbusResponse
import verifyWriteMultipleRegistersResponse
import kotlin.jvm.Throws
/**
 *
 * @ClassName:      Csocket$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2024/9/25$
 * @UpdateUser:     updater
 * @UpdateDate:     2024/9/25$
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */
class CSocketImpl : CSocket() {
    private var mSocketI: Operation2? = null
    private var isCorrect:Boolean = false
    private var sendMessageRegister:ByteArray = byteArrayOf()

    @Throws
    override fun inSocket(socket: CompSocket?): CSocket {
        if (socket == null ){
            LogTag.e("CSocketImpl----socket not init ")
            return this
        }

        if (socket.getInitResult()){
            LogTag.e("CSocketImpl----socket needs initialized or isClosed")
        }
        mSocketI = socket
        return this
    }

    override fun inSocket(socket: CompSocket?, onResult: OnResult<String>): CSocket {

        socket?.let {
            val recData:String = it.receivedData()
            if (this.isCorrect  ){
                onResult.success(recData)
                return@let
            }

        }
        return inSocket(socket)

    }

    override fun correctData(isCorrect: Boolean): CSocket {
        this.isCorrect = isCorrect
        return this
    }

    override fun write(transaction:Int,functionCode:Int,address: Int, data: List<Any>) {
        sendMessageRegister = createWriteMultipleRegistersRequest(transaction,functionCode,address,data)
        mSocketI?.sendMessage(sendMessageRegister)
    }

    override fun read(transaction:Int,functionCode:Int,address: Int, data: Int): CSocket {
        sendMessageRegister = createReadRequest(transaction,functionCode,address,data)
        mSocketI?.sendMessage(sendMessageRegister)

        return this
    }


    override fun parseWriteDataDetection(onResult: (Boolean)->Unit): CSocket {
        mSocketI?.receivedDataH({
            if (this.isCorrect ){
                return@receivedDataH
            }

            kotlin.runCatching {
                val result = verifyWriteMultipleRegistersResponse( sendMessageRegister,it)
                onResult.invoke(result)
            }.apply {
                if (isFailure){
                    onResult.invoke(false)
                }
            }
        }, byteArrayOf())
        return this
    }
    override fun <T: Any> parseReadDataDetection(onResult: (List<T>)->Unit): CSocket {
        mSocketI?.receivedDataH({
            if (this.isCorrect && !isMatchingPair( sendMessageRegister,it)){
                return@receivedDataH
            }

            kotlin.runCatching {
                val cLit = parseModbusResponse(it) as List<T>
//                val list =  convertToIntList(cLit)
                onResult.invoke(cLit)
            }.apply {
                if (isFailure){
                    onResult.invoke(emptyList())
                }
            }
        }, byteArrayOf())
        return this
    }

    override fun sendMessage(dataDetection: String) {
        correctData(false)
        mSocketI?.sendMessage(dataDetection)
    }
/*------------------------------弃用API--------------------------------------------*/
    @Deprecated(
        "解析报文已弃用，请使用",
        replaceWith = ReplaceWith("parseDataDetection(onResult: (String)->Unit )")
    )
    override fun parseDataDetection(onResult: OnResult<MutableList<Int>>): CSocket {
//        mSocketI?.let {
//            val recData:String = it.receivedData()
//            if (this.isCorrect && !Address.isSuccessWrite(recData,sendMessageRegister)){
//                return@let
//            }
//            onResult.success(Address.parseDataAI(recData))
//        }
        return this
    }

}