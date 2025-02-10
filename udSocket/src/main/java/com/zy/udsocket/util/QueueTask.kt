package com.zy.udsocket.util

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.LinkedBlockingQueue
/**
 *
 * @ClassName:      QueueTask$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2025/1/14$
 * @UpdateUser:     updater
 * @UpdateDate:     2025/1/14$
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */
class QueueTask {
    private val TAG = "QueueTask"
    private val mQueue = LinkedBlockingQueue<Task>() //替换为线程安全的队列

    //设置为IO 到上限65就会停止
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private var readAction: suspend () -> Unit = {}

    companion object {
        private const val NoTaskDelay = 10L
        private const val TaskTimeMillis = 1000L

        // 实时任务的超时时间务必小于TaskTimeMillis,
        private const val ReadTimeoutMillis = 100L

        var IsRead = true
    }

    private var indexTestAddTask = 0

    fun getScopeState() = scope.isActive
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "Caught exception in coroutine scope: ${exception.message}")
    }

    init {

        scope.launch {
            while (isActive) {
                simulateRead(readAction)
                async {
                    pollingTask()
                }
                delay(NoTaskDelay)
//                println( "QueueTask init"+indexTestAddTask)
            }

        }
    }


    fun setReadAction(action: suspend () -> Unit) {
        readAction = action
    }

    private suspend fun simulateRead(action: suspend () -> Unit) {
        if (IsRead.not()) return
        delay(ReadTimeoutMillis)
        addTasks(Task(indexTestAddTask, "simulateRead") { action.invoke() })
    }

    fun addTasks(task: Task) {
        synchronized(mQueue) {
            indexTestAddTask += 1
            task.index = indexTestAddTask
//                 println("Executing task----- {${task.index} ${task.name}--${mQueue.size}}")
            mQueue.add(task)
        }

    }

    private suspend fun pollingTask() {
        synchronized(mQueue) {
            if (mQueue.isNotEmpty()) {
                val task = mQueue.poll()

                task?.let {
                    scope.launch {
                        try {

                            val result = withTimeoutOrNull(TaskTimeMillis) {
                                task.action.invoke()
//                                println("Executing task-/---- {${task.index} ${task.name}}")
                            }
                            if (result == null) handleTimeout(task)

                        } catch (e: Exception) {
                            println("Task execution failed: ${e.message}")
                        }

                    }
                }
            }
        }
    }

    private suspend fun handleTimeout(task: Task?) {
        delay(10)
        println("Task execution timed out ${task?.index}${task?.name}")
    }

    private fun cancelAllTasks() {
        synchronized(mQueue) {
            mQueue.clear()
        }

    }

    fun cecal() {
        cancelAllTasks()
        try {
            scope.launch {
                joinAll()
                cancel()
            }
        } catch (e: Exception) {
            println("cancelAllTasks error: ${e.message}")
        }

    }

    data class Task(var index: Int, var name: String, val action: suspend () -> Unit)
}