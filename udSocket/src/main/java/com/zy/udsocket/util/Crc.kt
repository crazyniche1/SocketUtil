package com.zy.socketutil.util

import java.lang.RuntimeException

const val BITS_OF_BYTE = 8
const val POLYNOMIAL = 0xA001
const val INITIAL_VALUE = 0xFFFF
const val FF  = 0xFF

fun ByteArray.crc16(): ByteArray{
    var res = INITIAL_VALUE
    for (data in this) {
        res = res xor (data.toInt() and FF)
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res  shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()
    return this.plus(highByte).plus(lowByte)
}
fun IntArray.crc16(): ByteArray{
    val byteArray = ByteArray(this.size+ 2)
    var res = INITIAL_VALUE
    for (index in this.indices) {
        res = res xor this[index]
        byteArray[index] = this[index].toByte()
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res  shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()
    byteArray[this.size] = highByte
    byteArray[this.size + 1] = lowByte
    return byteArray
}
fun ByteArray.crc16Verify(): Boolean{
    if (this.size < 3) throw RuntimeException("数组长度不合法")
    var res = INITIAL_VALUE
    for (index in 0..this.size-3) {
        res = res xor (this[index].toInt() and FF)
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res  shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()
    return highByte == this[this.size - 2] && lowByte == this[this.size - 1]
}

fun Make_CRC(data: ByteArray): String {
    val buf = ByteArray(data.size) // 存储需要产生校验码的数据
    for (i in data.indices) {
        buf[i] = data[i]
    }
    val len = buf.size
    var crc = 0xFFFF //16位
    for (pos in 0 until len) {
        crc = if (buf[pos] < 0) {
            crc xor buf[pos].toInt() + 256 // XOR byte into least sig. byte of
            // crc
        } else {
            crc xor buf[pos].toInt() // XOR byte into least sig. byte of crc
        }
        for (i in 8 downTo 1) { // Loop over each bit
            if (crc and 0x0001 != 0) { // If the LSB is set
                crc = crc shr 1 // Shift right and XOR 0xA001
                crc = crc xor 0xA001
            } else  // Else LSB is not set
                crc = crc shr 1 // Just shift right
        }
    }

    var c = Integer.toHexString(crc)
    if (c.length == 4) {
        c = c.substring(2, 4) + c.substring(0, 2)
    } else if (c.length == 3) {
        c = "0$c"
        c = c.substring(2, 4) + c.substring(0, 2)
    } else if (c.length == 2) {
        c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1)
    }
    return c
}

