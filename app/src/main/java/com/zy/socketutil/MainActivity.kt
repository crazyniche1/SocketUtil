package com.zy.socketutil

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.zy.socketutil.main.CSocket
import com.zy.udsocket.main.CompSocket
import com.zy.socketutil.util.LogTag
import com.zy.udsocket.util.QueueTask
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock


class MainActivity : AppCompatActivity() {
//    private val scope = CoroutineScope(Dispatchers.Default  + Job())
    private var mSocket : CompSocket ?=null

    private var data = 3

    private fun compSocketBuild():CompSocket{

        return CompSocket.Builder()
            .setScope(CoroutineScope(Dispatchers.Default  + Job()))
            .setHost("192.168.2.251")
            .setPorts(5020)
            .isShowLog(true)
//            .isShowLog(false)
            .getError{
                LogTag.e("getError----------${it}")
//                queueTask.cecal()
            }
            .build()
    }

    private val xc = Executors.newScheduledThreadPool(3)
    var semaphore: Semaphore = Semaphore(1)
    val lock: ReentrantLock = ReentrantLock(true)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        发送指令
        findViewById<TextView>(R.id.textView).setOnClickListener {
            queueTaskAddTasks()
        }
//        connect
        findViewById<Button>(R.id.button).setOnClickListener {
            mSocket =null
            mSocket = compSocketBuild()
        }

//        getScopeState
        findViewById<Button>(R.id.button2).setOnClickListener {
            println("queueTask.getScopeState----${queueTask.getScopeState()}")

        }
//        Start
        findViewById<Button>(R.id.button3).setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                for (i in 1..5) {
                    delay(30)
                    test(i)
                }

            }
        }
//        sendSocket
        findViewById<Button>(R.id.button4).setOnClickListener {
            runCatching {

                    lifecycleScope.launch(Dispatchers.Main) {
                        for (i in 0..100) {
                            delay(10)
                            test()
                        }

                }
            }.also {
                if (it.isFailure) {
                    LogTag.e("----------${it.exceptionOrNull()}")
                }
            }

        }

        findViewById<EditText>(R.id.editText).addTextChangedListener(afterTextChanged = {
            runCatching {
                if (it.toString().isNotEmpty()) {
                    data = it.toString().toInt()
                }
            }

        })

//       断开
        findViewById<Button>(R.id.button5).setOnClickListener {
            QueueTask.IsRead = QueueTask.IsRead.not()
        }
//        GC
        findViewById<Button>(R.id.button6).setOnClickListener {
            addWrite()
        }

        mSocket = compSocketBuild()

        test2()

    }

    private fun test2(): Unit {
        queueTask.setReadAction {
            realTimeRead()
        }
    }

    private fun queueTaskAddTasks() {
        queueTask.addTasks(QueueTask.Task(0, "add") {
            test()
        })
    }

    private val queueTask by lazy {
        QueueTask() // Display the preview image from Camera
    }

    override fun onStop() {
        super.onStop()
        queueTask.cecal()
    }

    var indexC = 0
    private fun test(index:Int = 0 ): Unit {
        indexC+=1
        CSocket.getInstance()
            .inSocket(mSocket)
            .readHolding(transaction = indexC, address = 7, data = 10)
            .parseReadDataDetection<Int> {
                LogTag.d("${indexC}----parseDataDetection2---- ${it[1]}")
            }
    }

    private fun addWrite(): Unit {
        indexC+=1
        CSocket.getInstance()
            .inSocket(mSocket)
            .parseWriteDataDetection {
                LogTag.d("parseDataDetection--addWrite-- $it")
            }
            .writeHolding(transaction = indexC, address = 100, data = arrayListOf(45,46))
//            .writeCoils(transaction = indexC, address = 0, data = listOf(true,true,true ,true,true,true ,true,true))
    }

//    实时读取
    private fun realTimeRead (){
        indexC+=1
//        CSocket.getInstance()
//            .inSocket(mSocket)
//            .parseReadDataDetection<List<Boolean>> {
//                LogTag.d("parseDataDetection--realTimeRead-- $it")
//            }
//            .writeCoils(transaction = indexC, address = 0, data = listOf(1,1,1 ,1,1,1 ,1,1))

    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.closeSocket()
    }
}