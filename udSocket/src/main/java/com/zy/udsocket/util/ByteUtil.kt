package com.zy.socketutil.util

/**
 * 字节工具
 * 待测试
 */
object ByteUtil {
    /**
     * int to byte[] (高位到低位)
     */

    fun intToBytes(value:Int): ByteArray = ByteArray(4).also {
        it[0] = (value shr 24 and 0xFF).toByte()
        it[1] = (value shr 16 and 0xFF).toByte()
        it[2] = (value shr 8 and 0xFF).toByte()
        it[3] = (value shr 0 and 0xFF).toByte()
    }

    /**
     * int to byte[] (低位到高位)
     */
    fun intToBytes2(value:Int): ByteArray = ByteArray(4).also {
        it[3] = (value shr 24 and 0xFF).toByte()
        it[2] = (value shr 16 and 0xFF).toByte()
        it[1] = (value shr 8 and 0xFF).toByte()
        it[0] = (value shr 0 and 0xFF).toByte()
    }

    /**
     * 针对双字节
     */
    fun intTo2Bytes2(value:Int): ByteArray = ByteArray(2).also {
        it[1] = (value shr 0 and 0xFF).toByte()
        it[0] = (value shr 8 and 0xFF).toByte()
    }

    /**
     * byte to int
     * （高位在前，低位在后）
     */

    fun bytesToInt(value: ByteArray): Int {
        return ((value[0].toUInt() and 0xFFu)
                or ((value[1].toUInt() and 0xFFu) shl 8)
                or ((value[2].toUInt() and 0xFFu) shl 16)
                or ((value[3].toUInt() and 0xFFu) shl 24)).toInt()
    }

    /**
     * byte to int
     * （低位在前，高位在后)
     */

    fun bytesToInt2(value: ByteArray): Int {
        return ((value[0].toUInt() and 0xFFu)
                or ((value[1].toUInt() and 0xFFu) shl 8)
                or ((value[2].toUInt() and 0xFFu) shl 16)
                or ((value[3].toUInt() and 0xFFu) shl 24)).toInt()
    }

    // 辅助函数：将字节数组转换为整数
    fun bytesToInts(value: ByteArray): Int {
        return (((value[0].toInt() and 0xFF) shl 8)
                or (value[1].toInt() and 0xFF) )
    }


    /**
     * 字节转成16位
     */

    fun bytesToShort(value :ByteArray): Short {

        return ( ( value[0].toInt() shl 8 ) or (value[1].toInt() and 0xFF )) .toShort()
    }

    /**
     * hex To 10
     */
//    fun bitToByte(bit:String): Byte {
//
//
//    }


    /**
     * String to Hex
     */
    fun String.toHex(): String = Integer.toHexString(this.toInt())
    fun String.toDec() = Integer.parseInt(this, 16)
    fun Int.toHex(): String = Integer.toHexString(this)
    fun Int.toDec() =  Integer.parseInt(this.toString(), 16)


}