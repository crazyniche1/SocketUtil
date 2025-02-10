package com.zy.socketutil.util

import java.util.*


object DataUtils {
    //byte[]转换成HexString
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.size <= 0) {
            return null
        }
        for (i in src.indices) {
            val v = src[i] .toInt() and 0xff
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    //转换char to byte
    fun charToByte(c: Char): Byte {
        return hexString.indexOf(c).toByte()
    }

     private val hexString:String = "0123456789ABCDEF"

    //16进制字符串转byte数组
     fun Hex2Byte(inHex: String): ByteArray? {
        val hex = inHex.split(" ").toTypedArray() //将接收的字符串按空格分割成数组
        val byteArray = ByteArray(hex.size)
        for (i in hex.indices) {
            //parseInt()方法用于将字符串参数作为有符号的n进制整数进行解析
            byteArray[i] = hex[i].toInt(16).toByte()
        }
        return byteArray
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexString = hexString
        if (hexString == null || "" == hexString) {
            return null
        }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            val a = charToByte(hexChars[pos]) .toInt()
            val b = charToByte(hexChars[pos + 1]) .toInt()
            d[i] = (a shl 4 or b).toByte()
        }
        return d
    }


}