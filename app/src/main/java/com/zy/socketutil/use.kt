package com.zy.socketutil

import com.zy.socketutil.main.CSocket
import com.zy.socketutil.util.LogTag
import com.zy.udsocket.main.CompSocket
import com.zy.udsocket.util.QueueTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 *
 * @ClassName:      use$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2025/1/22$
 * @UpdateUser:     updater
 * @UpdateDate:     2025/1/22$
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */
class use {
    fun main() {
    }

    private var mSocket: CompSocket = CompSocket.Builder()
        .setScope(CoroutineScope(Dispatchers.Default + Job()))
        .setHost("192.168.2.251")
        .setPorts(5020)
        .isShowLog(true)
//            .isShowLog(false)
        .getError {
            LogTag.e("getError----------${it}")
//                queueTask.cecal()
        }
        .build()


    private val queueTask by lazy {
        QueueTask()
    }

    //  添加实时任务
    private fun test2(): Unit {
        queueTask.setReadAction {
              readHolding()

        }
    }

    // 添加任务
    private fun test3(): Unit {
        queueTask.addTasks(QueueTask.Task(0, "add") {
            write()
        })
    }

    private fun readHolding(index: Int = 0): Unit {
        CSocket.getInstance()
            .inSocket(mSocket)
            .readHolding(transaction = 0, address = 7, data = 10)
            .parseReadDataDetection<List<Int>> {
                LogTag.d("---parseDataDetection2---- $it")
            }
    }

    private fun write(): Unit {
        CSocket.getInstance()
            .inSocket(mSocket)
            .parseWriteDataDetection {
                LogTag.d("parseDataDetection--addWrite-- $it")
            }
            .writeHolding(transaction = 1, address = 100, data = arrayListOf(45, 46))
    }

// override fun onStop() {
//  super.onStop()
//  queueTask.cecal()
// }

}