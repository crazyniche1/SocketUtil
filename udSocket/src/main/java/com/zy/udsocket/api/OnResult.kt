package com.zy.udsocket.api

/**
 *
 * @ClassName:      OnResult$
 * @Description:    java类作用描述
 * @Author:         author
 * @CreateDate:     2024/9/26$
 * @UpdateUser:     updater
 * @UpdateDate:     2024/9/26$
 * @UpdateRemark:   更新内容
 * @Version:        1.0
 */
fun interface OnResult<T> {
 fun success(data: T)
}