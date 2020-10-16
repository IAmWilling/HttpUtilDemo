package com.zhy.kotlinfdemo2.http

/**
 * 文件上传进度回调
 */
interface UploadProgress {
    fun progress(progress: Float, length: Long)
}