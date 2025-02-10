package com.zy.socketutil.main

import com.zy.udsocket.api.OnResult
import com.zy.udsocket.main.CompSocket

/**
 *
 * CSocket功能 ：
 *  1.初始化：socket  √
 *  2.发报文   √
 *  3.收到报文  √
 *  4.是否输出报文log √
 *  5.是否解析数据
 *  6.
 * socketImpl 单独的抽象类中实现  √
 * ParseMessageImpl 单独的抽象类中实现或工具类
 * 地址类
 *
 *   var mSocket:CompSocket? = null
 *   lifecycleScope.launch(Dispatchers.IO) {
 *
 *      mSocket = CompSocket.Builder()
 *          .setScope(MainScope())
 *          .setHost(WifiConfig().ip)
 *          .setPorts(WifiConfig().port)
 *          .build()
 *
 *      runCatching {
 *
*         CSocket.getInstance().inSocket(mSocket) {  LogTag.d("inSocket---- $it") }.sendMessage("E0 FE 00 00 00 06 01 03 07 EC 00 02")
 *
 *        CSocket.getInstance()
 *          //parseDataDetection 是解析后的数据为List类型，如果不希望解析可直接用inSocket(mSocket)方法
 *          .parseDataDetection {
 *              LogTag.d("parseDataDetection---- $it")
 *          }
 *          .correctData(false)
 *          .inSocket(mSocket) {
 *              LogTag.d("inSocket---- $it")
 *          }
 *          .write(2002, 2)
 *      }
 *
 *      //关闭socket
 *     mSocket?.closeSocket()
 *
 *   }
 */
abstract class CSocket {
    // 提供一个静态方法返回实例
    companion object {
        @Volatile
        private var instance: CSocketImpl? = null

        fun getInstance(): CSocket {
            return instance ?: CSocketImpl().also { instance = it }
        }

    }

    /**
     * 不返回响应＼解析报文
     */
    abstract fun  inSocket (socket: CompSocket?): CSocket

    /**
     * 返回响应报文
     */
    abstract fun inSocket(socket: CompSocket?, onResult: OnResult<String>): CSocket

    /**
     * iv 是否校验报文当为true 时不执行解析函数
     * 默认false即可
     */
    abstract fun  correctData (isCorrect:Boolean): CSocket

    /**
     * write
     */
    abstract fun  write(transaction:Int,functionCode:Int,address: Int,data :List<Any>)

    /**
     * write
     */
    abstract fun  read(transaction:Int,functionCode:Int,address: Int, data: Int): CSocket

    //保持寄存器
     fun readHolding(transaction:Int = 0,address: Int, data: Int,)  = read(transaction,0x03,address,data)
    //输入寄存器
     fun readInput(transaction:Int = 0,address: Int, data: Int)  = read(transaction,0x04,address,data)
    //线圈
     fun readCoils(transaction:Int = 0,address: Int, data: Int)  = read(transaction,0x01,address,data)
    //离散输入
     fun readDiscrete(transaction:Int = 0,address: Int, data: Int)  = read(transaction,0x02,address,data)

    //线圈
    fun writeCoils(transaction:Int = 0,address: Int, data: List<Any>)  = write(transaction,0x0F,address,data)

    //保持寄存器 ,write的最小地址是1
    fun writeHolding(transaction:Int = 0,address: Int, data: List<Any>)  = write(transaction,0x10,address,data)

    /**
     * 发送完整报文
     */
    abstract fun  sendMessage(dataDetection:String)

    /**
     * 解析响应报文
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "解析报文已弃用，请使用", replaceWith = ReplaceWith("parseDataDetection(onResult: (String)->Unit )"))
    abstract fun  parseDataDetection (onResult: OnResult<MutableList<Int>>): CSocket

    //read 会将解析值返回
    abstract fun <T: Any>parseReadDataDetection  (onResult: (List<T>)->Unit ): CSocket
    //write 会返回是否响应成功
    abstract fun parseWriteDataDetection  (onResult: (Boolean)->Unit ): CSocket

}