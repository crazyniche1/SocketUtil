@file : JvmName("AmExtension")

import com.zy.socketutil.main.CSocketImpl
import com.zy.socketutil.util.LogTag
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.or

/**
 *
 * @ClassName:      AmExtension
 * @Description:    协议的扩展类,包含了读取、单个、连续多个寄存器的功能 ，以及单个、连续多个寄存器的写功能, 同时包含解析协议
 * @Author:         author
 * @CreateDate:     2024/11/26
 * @UpdateUser:     updater
 * @UpdateDate:     2024/11/26
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */


/**
 * 假定以modbus协议格式为基础，可根据情况自己定义协议格式
 * 0, 1, 0, 0, 0, 6, 1, 3, 0, -2, 0, 6]
 * 读输入寄存器,如果要读其它类型的寄存器，得优化并动态改变功能码
 */

fun CSocketImpl.createReadRequest03(transaction:Int,address: Int, quantity: Int): ByteArray = createReadRequest(0x03, address, quantity,transaction)
fun CSocketImpl.createReadRequest04(transaction:Int,address: Int, quantity: Int): ByteArray = createReadRequest(0x04, address, quantity,transaction)

fun createReadRequest(transaction:Int,functionCode:Int, address: Int, quantity: Int): ByteArray {
    val bos = ByteArrayOutputStream()
    // Transaction Identifier (2 bytes)
    bos.write((transaction shr 8) and 0xFF)
    bos.write(transaction and 0xFF)
    // Protocol Identifier (2 bytes)
    bos.write((0x0000 shr 8) and 0xFF)
    bos.write(0x0000 and 0xFF)
    // Length (2 bytes)
    bos.write((0x0006 shr 8) and 0xFF)
    bos.write(0x0006 and 0xFF)
    // Unit Identifier (1 byte)
    bos.write(0x01)
    // Function Code (1 byte)
    bos.write(functionCode)
    // Starting Address (2 bytes)
    bos.write((address shr 8) and 0xFF)
    bos.write(address and 0xFF)
    // Quantity of Registers (2 bytes)
    bos.write((quantity shr 8) and 0xFF)
    bos.write(quantity and 0xFF)
    return bos.toByteArray()
}

/**
 * 仅仅运行 int 和 Float
 * 支持写入多个保持寄存器，多个线圈；
 */

fun CSocketImpl.createWriteMultipleRegistersRequest(transaction:Int,functionCode:Int,address: Int, values: List<Any>): ByteArray {
    val bos = ByteArrayOutputStream()
    try {
        // Transaction Identifier (2 bytes)
        bos.write((transaction shr 8) and 0xFF)
        bos.write(transaction and 0xFF)
        // Protocol Identifier (2 bytes)
        bos.write((0x0000 shr 8) and 0xFF)
        bos.write(0x0000 and 0xFF)
        // Length (2 bytes)
        var length = 7 + 2 * values.size
        if (functionCode == 0x0F) {
            length = 7 + ((values.size + 7) / 8)
        }
        bos.write((length shr 8) and 0xFF)
        bos.write(length and 0xFF)
        // Unit Identifier (1 byte)
        bos.write(0x01)
        // Function Code (1 byte)
//        val functionCode = functionCode
        bos.write(functionCode)
        // Starting Address (2 bytes)
        bos.write((address shr 8) and 0xFF)
        bos.write(address and 0xFF)


        if (functionCode == 0x0F) {

            val quantity = values.size
            if (quantity < 1 || quantity > 0x07B0) {
                throw IllegalArgumentException("Invalid number of coils: $quantity")
            }
            bos.write((quantity shr 8) and 0xFF)
            bos.write(quantity and 0xFF)

            val byteCount = (quantity + 7) / 8
            bos.write(byteCount)

            var bitIndex = 0
            val coilBytes = ByteArray(byteCount)
            for (value in values) {
                if (value !is Boolean) {
                    throw IllegalArgumentException("Write Multiple Coils requires a list of boolean values.")
                }
                if (value) {
                    coilBytes[bitIndex / 8] = coilBytes[bitIndex / 8].or((1 shl (bitIndex % 8)).toByte())
                }
                bitIndex++
            }
            bos.write(coilBytes)
            return bos.toByteArray()
        }

        // Quantity of Registers (2 bytes)
        val quantity = values.size
        bos.write((quantity shr 8) and 0xFF)
        bos.write(quantity and 0xFF)
        // Byte Count (1 byte)
        val byteCount = 2 * quantity
        bos.write(byteCount)

        // Register Values (2 bytes each)
        for (value in values) {
            when (value) {
                is Int -> {
                    bos.write((value shr 8) and 0xFF)
                    bos.write(value and 0xFF)
                }
                is Float -> {
                    val buffer = ByteBuffer.allocate(4)
                    buffer.order(ByteOrder.BIG_ENDIAN)
                    buffer.putFloat(value.toFloat())
                    val array = buffer.array()
                    bos.write(array)

                }
                else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass.name}")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        LogTag.d("createWriteRequest: $e")
    }
    return bos.toByteArray()
}


//仅仅运行 int 和 Float
fun CSocketImpl. createWriteRequest(address: Int, value: Any): ByteArray {
    val bos = ByteArrayOutputStream()
    try {
        // Transaction Identifier (2 bytes)
        bos.write((0x0001 shr 8) and 0xFF)
        bos.write(0x0001 and 0xFF)
        // Protocol Identifier (2 bytes)
        bos.write((0x0000 shr 8) and 0xFF)
        bos.write(0x0000 and 0xFF)
        // Length (2 bytes)
        bos.write((0x0006 shr 8) and 0xFF)
        bos.write(0x0006 and 0xFF)
        // Unit Identifier (1 byte)
        bos.write(0x01)
        // Function Code (1 byte)
        bos.write(0x06)
        // Starting Address (2 bytes)
        bos.write((address shr 8) and 0xFF)
        bos.write(address and 0xFF)
        // Register Value (2 bytes)
        when (value) {
            is Int -> {
                bos.write((value shr 8) and 0xFF)
                bos.write(value and 0xFF)
            }
            is Float  -> {
                val buffer = ByteBuffer.allocate(4)
                buffer.order(ByteOrder.BIG_ENDIAN)
                buffer.putFloat(value.toFloat())
                val array = buffer.array()
                bos.write(array)
            }
            else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass.name}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bos.toByteArray()
}

@Deprecated(message = "createWriteRequests" ,replaceWith = ReplaceWith("createWriteMultipleRegistersRequest"))
fun CSocketImpl.createWriteRequests(address: Int, values: List<Int>): ByteArray {
    val bos = ByteArrayOutputStream()
    try {
        // Transaction Identifier (2 bytes)
        bos.write((0x0001 shr 8) and 0xFF)
        bos.write(0x0001 and 0xFF)
        // Protocol Identifier (2 bytes)
        bos.write((0x0000 shr 8) and 0xFF)
        bos.write(0x0000 and 0xFF)
        // Length (2 bytes)
        val length = 6 + 2 * values.size
        bos.write((length shr 8) and 0xFF)
        bos.write(length and 0xFF)
        // Unit Identifier (1 byte)
        bos.write(0x01)
        // Function Code (1 byte)
        bos.write(0x10)
        // Starting Address (2 bytes)
        bos.write((address shr 8) and 0xFF)
        bos.write(address and 0xFF)
        // Byte Count (1 byte)
        val quantity = values.size
        bos.write((quantity shr 8) and 0xFF)
        bos.write(quantity and 0xFF)
        val byteCount = 2 * quantity
        bos.write(byteCount)
        // Register Value (2 bytes)
        for (i in values){
            bos.write((i shr 8) and 0xFF)
            bos.write(i and 0xFF)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bos.toByteArray()
}


@Throws(IllegalArgumentException::class)
fun CSocketImpl.parseModbusResponse(responseBytes: ByteArray?): List<Any> {

    if (responseBytes == null) throw IllegalArgumentException("响应字节数组为空")

    val bais = ByteArrayInputStream(responseBytes)
    val transactionId: Int = readShort(bais)
    val protocolId: Int = readShort(bais)
    val length: Int = readShort(bais)
    val unitId = bais.read()
    return when (val functionCode = bais.read()) {
        0x01, 0x02 -> parseCoils(bais, functionCode) // 添加线圈解析
        0x03,0x04 -> {
            val byteCount = bais.read()
            val registerValues: MutableList<Int> = ArrayList()
            var i = 0
            while (i < byteCount / 2) {
                registerValues.add(readShort(bais))
                i++
            }
             registerValues
        }

        0x06 -> {
            val startingAddress: Int = readShort(bais)
            val value: Int = readShort(bais)
//             listOf(startingAddress, value)
             listOf( value)
        }

        0x10 -> {
            val startingAddress: Int = readShort(bais)
            val quantityOfRegisters: Int = readShort(bais)
            val byteCount: Int = bais.read()
            val registerValues: MutableList<Any> = mutableListOf()

            // 根据寄存器数量和字节计数判断数据类型
            val registersPerValue = byteCount / 4 // 每个值占用的寄存器数量（2个寄存器为一个float/int，4个寄存器为一个double）

            for (i in 0 until quantityOfRegisters / registersPerValue) {
                if (registersPerValue == 2) {
                    // 解析为 int/float
                    val bytes = ByteArray(4)
                    bais.read(bytes)
                    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
                    registerValues.add(buffer.getInt())
                } else if (registersPerValue == 4) {
                    // 解析为 double
                    val bytes = ByteArray(8)
                    bais.read(bytes)
                    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
                    registerValues.add(buffer.double)
                } else {
                    throw IllegalArgumentException("不支持的数据格式")
                }
            }

//             listOf(startingAddress, quantityOfRegisters, registerValues)
             registerValues
        }

        else -> throw IllegalArgumentException("未知功能码: $functionCode")
    }

}

private fun parseCoils(bais: ByteArrayInputStream, functionCode: Int): List<Boolean> {
    val byteCount = bais.read()
    val coils: MutableList<Boolean> = mutableListOf()

    for (i in 0 until byteCount) {
        val coilByte = bais.read().toByte()
        for (j in 0..7) {
            // 检查每个位是否为 1
            val isCoilSet = coilByte.toInt() and (1 shl j) != 0
            coils.add(isCoilSet)
        }
    }

    // 如果是 Read Discrete Inputs (功能码 0x02)，返回前 16 个线圈状态
    if (functionCode == 0x02 && coils.size > 16) {
        return coils.subList(0, 16)
    }

    return coils
}

//解析16位有符号整数-大端
private fun readShort(bais: ByteArrayInputStream): Int {
    val highByte = bais.read()
    val lowByte = bais.read()
    if (highByte == -1 || lowByte == -1) {
        throw IllegalArgumentException("输入流结束")
    }
    return (highByte shl 8) or (lowByte and 0xFF)
}

//解析32位有符号整数-大端
private fun readInt(bais: ByteArrayInputStream): Int {
    val byte1 = bais.read() and 0xFF
    val byte2 = bais.read() and 0xFF
    val byte3 = bais.read() and 0xFF
    val byte4 = bais.read() and 0xFF
    return (byte1 shl 24) or (byte2 shl 16) or (byte3 shl 8) or byte4
}

fun CSocketImpl.hexStringToByteArray(hex: String): ByteArray {
    val cleanHex = hex.replace("\\s*".toRegex(), "")
    require(cleanHex.length % 2 == 0) { "Hex string must have an even number of characters" }
    val result = ByteArray(cleanHex.length / 2)
    for (i in 0 until cleanHex.length) {
        result[i / 2] = cleanHex.substring(i, i + 2).toInt(16).toByte()
    }
    return result
}

fun CSocketImpl.convertToIntList(list: List<Any>): List<Int> {
    return list.map {
        when (it) {
            is Int -> it
            else -> throw IllegalArgumentException("无法将类型 ${it::class.java.simpleName} 转换为整数")
        }
    }
}


fun CSocketImpl.isMatchingPair(request: ByteArray, response: ByteArray): Boolean {
    if (request.size < 8 || response.size < 8) return false // 最小报文长度检查

    // 提取事务标识符和功能码
    val requestMsg = ModbusMessage(
        transactionId = getShortFromByteArray(request, 0),
        functionCode = request[7]
    )

    val responseMsg = ModbusMessage(
        transactionId = getShortFromByteArray(response, 0),
        functionCode = response[6]
    )

    // 检查事务标识符和功能码是否匹配
    return requestMsg.transactionId == responseMsg.transactionId &&
            (requestMsg.functionCode == responseMsg.functionCode ||
                    (responseMsg.functionCode.toInt() and 0x80 != 0)) // 检查异常响应
}


fun CSocketImpl.verifyWriteMultipleRegistersResponse(request: ByteArray, response: ByteArray):Boolean {
        if (request.size < 8 || response.size < 8) return false // 最小报文长度检查

        // 提取事务标识符和功能码
        val requestMsg = ModbusMessage(
            transactionId = getShortFromByteArray(request, 0),
            functionCode = request[7]
        )

        val responseMsg = ModbusMessage(
            transactionId = getShortFromByteArray(response, 0),
            functionCode = response[7]
        )

        // 检查事务标识符和功能码是否匹配
        if (requestMsg.transactionId != responseMsg.transactionId) {
            throw Exception("Transaction IDs do not match")
        }

        if (responseMsg.functionCode.toInt() and 0x80 != 0) {
            // 异常响应处理
            val exceptionCode = response[8].toInt()
            throw Exception("Exception response with code $exceptionCode")
        } else if (requestMsg.functionCode != responseMsg.functionCode) {
            throw Exception("Function codes do not match")
        }

        // 进一步校验特定功能码的数据部分
//        when (requestMsg.functionCode.toInt()) {
//            0x06 -> verifyWriteSingleRegisterResponse(request, response)
//            0x10 -> verifyWriteMultipleRegistersResponse(request, response)
//            else -> throw Exception("Unsupported function code")
//        }

        return true
}

private fun getShortFromByteArray (bytes: ByteArray, offset: Int)
    =  ((bytes[offset].toInt() and 0xFF) shl 8 or (bytes[offset + 1].toInt() and 0xFF)).toShort()

data class ModbusMessage(val transactionId: Short, val functionCode: Byte)

fun CSocketImpl.printClassName() {
    println(this::class.simpleName)
}

fun <T : Any>  T .toUnsignedHexString(bytes: ByteArray): String {
    val hexString = StringBuilder()
    for (b in bytes) {
        hexString.append(String.format("%02X ", b.toInt() and 0xFF))
    }
    return hexString.toString().trim { it <= ' ' }
}