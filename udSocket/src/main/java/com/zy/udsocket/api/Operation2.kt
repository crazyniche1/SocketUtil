package com.zy.socketutil.api

/**
 *
 * @ClassName:      Operation$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2024/9/25$
 * @UpdateUser:     updater
 * @UpdateDate:     2024/9/25$
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */
interface Operation2 {

    fun<T> sendMessage(message: T)
    fun<R>  receivedData() :R

    fun<T> receivedDataH(data:(T)->Unit ,default:T){
        data(default)
    }

}
