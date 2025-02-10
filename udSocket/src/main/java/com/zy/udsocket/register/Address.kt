package com.zy.socketutil.register

//import com.ktjt.grouting.util.byte.BITS_OF_BYTE
//import com.ktjt.grouting.util.byte.FF
import android.util.Log
import com.zy.socketutil.util.BITS_OF_BYTE
import com.zy.socketutil.util.ByteUtil
import com.zy.socketutil.util.ByteUtil.bytesToInts
import com.zy.socketutil.util.ByteUtil.toDec
import com.zy.socketutil.util.DataUtils
import com.zy.socketutil.util.FF

/**
 * 寄存器
 */
object Address {

    /**
     * 十六进制
     */
    const val radix16 = 16

    //从机地址
    private var dataDetectionHead1 = "E0".toDec()
    private var dataDetectionHead2 = "FE".toDec()
    private var dataDetectionHead4 = "4".toDec()

    /*------------------------------无效-------------------*/
    //事务标识符
    private var transactionIdentifier = ByteArray(2).also {
        it[0] = dataDetectionHead1.toByte()
        it[1] = dataDetectionHead2.toByte()
    }
    fun transactionIdentifier(vl:Int): Unit {
        val a = ByteUtil.intTo2Bytes2(vl)
        transactionIdentifier = a
    }

    //协议标识符
    private val protocolIdentifier = ByteArray(2).also {
        it[0] = 0.toByte()
        it[1] = 0.toByte()
    }

    //长度字段
    private val  length = ByteArray(2)

    //单元标识符
    private val  unitIdentifier = byteArrayOf(1.toByte())
    //功能码
    private val  functionCode = ByteArray(1)

    private val  addCode = ByteArray(2)

    private const val  twoCode = 2

    /**
     * 功能码
     */
    class FunctionCode {

        companion object {
            //读,读取保持寄存器
             const val readAdd = 3
            //写,写入多个寄存器
             const val writeAdd = 16
            //写入单个寄存器
            const val readHeadAddNew =  6
            //用于查询从站设备的诊断信息
            const val writeHeadAddNew = 11

        }
    }

    /**
     *  所有参数必须是10进制的
     *  @param dataAdd :寄存器
     *  @param size :寄存器长度
     */

    fun read(dataAdd:Int,size:Int = twoCode) = plusMessageCompare(dataAdd,0,
        FunctionCode.readAdd,
        FunctionCode.readHeadAddNew,size)

    /**
     * 解析返回报文
     */
    private fun parsingReturned(dataAdd:Int,size:Int = twoCode) = plusMessageCompare(dataAdd,0,
        FunctionCode.writeAdd,
        FunctionCode.readHeadAddNew,
        size
    ).dropLast(10)


    /**
     *  所有参数必须是10进制的
     *  @param dataAdd :寄存器
     *  @param data :写入数据
     */
    fun write(dataAdd:Int,data:Int): String = plusMessageCompare(dataAdd,data,
        FunctionCode.writeAdd,
        FunctionCode.writeHeadAddNew,
        twoCode
    )

    /**
     * 根据协议拼接报文
     */
    private fun plusMessageCompare(dataAdd: Int, data: Int, type: Int, typeNew: Int,dataSize:Int): String {
        var modbus = byteArrayOf()
        modbus += transactionIdentifier
        modbus += protocolIdentifier

        //         dataAddHigh
        length[0] = ((typeNew shr BITS_OF_BYTE) and FF).toByte()
        //         dataAddLow
        length[1] = (typeNew and FF).toByte()

        modbus += length
        modbus += unitIdentifier

        functionCode[0] = type.toByte()
        modbus += functionCode

        addCode[0] = ((dataAdd shr BITS_OF_BYTE) and FF).toByte()
        addCode[1] = (dataAdd and FF).toByte()

        modbus += addCode
//        modbus += twoCode
        modbus += ByteUtil.intTo2Bytes2(dataSize)

        if (type == FunctionCode.writeAdd) {
            modbus += dataDetectionHead4.toByte()
            modbus += ByteUtil.intToBytes2(data)
        }

        return DataUtils.bytesToHexString(modbus)!!.uppercase()
    }

    /**
     * 判断写指令是否成功
     */
    fun isSuccessWrite(receiver:String,dataAdd:Int)  = receiver == parsingReturned(dataAdd).lowercase()

    /**
     * 解析返回数据帧
     *
     * 校验crc 如果不对直接抛出
     * @param data :ps:"01 03 04 A1 50  A1 60 B8 78"
     * @return 解析后的值(十进制)
     */

    fun parseData(data:String) :MutableList<Int>{
        val strList = mutableListOf<Int>()

        val bts  = DataUtils.hexStringToBytes(data)
        bts?:return strList
        val a = data.replace(" ","").substring(18)
        val b = DataUtils.hexStringToBytes(a)
        val byteList = b?: byteArrayOf() .copyOf()
        val mList = byteList.copyOfRange(0,byteList.size)
        if (mList.size %2 !=0)return strList
        for (i in mList.indices step 2 ){
            val subArray = byteArrayOf(mList[i], mList[i + 1],0.toByte(),0.toByte())
            strList.add(ByteUtil.bytesToInt2(subArray))
        }
        return strList
    }

    fun parseDataAI(data:String) :MutableList<Int>{
        val strList = mutableListOf<Int>()

        // 移除字符串中的空格
        val cleanData = data.replace(" ", "")

        // 去除前18个字符
        if (cleanData.length < 18)return strList
        val hexData = cleanData.substring(18)
//        Log.e("TAG", "parseDataAI: ${hexData}", )
        val byteList = DataUtils.hexStringToBytes(hexData) ?: return strList

        // 检查字节数组长度是否为偶数
        if (byteList.size % 2 != 0) return strList

        // 逐对读取字节，并转换为整数
        for (i in byteList.indices step 2) {
//            println("${byteList[i]}-----${byteList[i + 1]} ")
            val subArray = byteArrayOf(byteList[i], byteList[i + 1])
            strList.add(bytesToInts(subArray))
        }

        return strList
    }

    //温度解析
    private fun parseTemByArray(value :ByteArray): Double {
        // 获取符号位及温度数值
        val rawData = ByteUtil.bytesToInt2(value)
        val low16 = rawData and 0xFFFF
        val signBit =  (low16) and 1  // 最高位表示符号位，取出来判断正负
        val temperatureValue = (low16 and 0x7FFF ).toDouble()  // 去掉符号位，得到温度数值

        // 判断并计算实际温度
        val temperature = if (signBit == 1) {
            temperatureValue // 正数情况，直接使用温度数值
        } else {
            //这里解释下，协议中 ，正数1 负数0  但实际硬件不会小于0
//            -temperatureValue// 负数情况，加上负号
            temperatureValue
        }

        return if (temperature == 0.0) 0.0 else temperature /10
    }

    private fun parseTemByArray1(value :ByteArray): Double {
        return ByteUtil.bytesToInt2(value)/10 .toDouble()
    }

    /**
     * ByteUtil.bytesToInt2(b)
     */
    // 温度解析
    fun parseDataTemps(data:String) : MutableList<Double> {
        val strList = mutableListOf<Double>()
        val bts  = DataUtils.hexStringToBytes(data)
        bts?:return strList
        val a = data.replace(" ","").substring(18)
        val b = DataUtils.hexStringToBytes(a)
        val byteList = b?: byteArrayOf() .copyOf()
        val mList = byteList.copyOfRange(0,byteList.size)

        for (i in mList.indices step 4 ){
            val subArray = byteArrayOf(mList[i], mList[i + 1],mList[i + 2],mList[i+3 ])
            strList.add(parseTemByArray1(subArray))
        }
        return strList
    }

    /**
     * 补充0
     * 返回字符串表示的两个字节
     */
    fun String.plus1() = this.padStart(4,'0').uppercase()

}