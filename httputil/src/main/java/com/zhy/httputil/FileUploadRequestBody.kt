package com.zhy.kotlinfdemo2.http

import android.os.Handler
import android.os.Looper
import android.os.Message
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.*
import java.io.File
import java.io.IOException
import okio.buffer
import java.lang.Exception

/**
 * @author zhy
 * @param mediaType 请求体类型
 * @param file 需要上传的文件
 */
class FileUploadRequestBody(
    val mediaType: MediaType,
    val file: File,
    val progress: UploadProgress?
) : RequestBody() {
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                progress?.progress(msg.obj as Float, contentLength())
            }
        }
    }

    override fun contentType(): MediaType? = mediaType
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        //主要进度计算 此方法都在子线程中运行，所以设置回调也会是子线程，因此使用handler切换到主线程中
        try {
            val source = file.source()
            var buffer = Buffer()
            //当前流中写入的字节数
            var remaing = contentLength()
            var readCount: Long = 0
            var o: Long = -1
            while (readCount != o) {
                readCount = source.read(buffer, 2048)
                sink.write(buffer, readCount)
                remaing -= readCount
                var pro = ((contentLength() * 1.0f) - (remaing * 1.0f)) / (contentLength() * 1.0f)
                val msg = Message().apply {
                    what = 1
                    obj = pro
                }
                handler.sendMessage(msg)
                readCount = 0
            }
        } catch (Ex: Exception) {

        }
    }
}