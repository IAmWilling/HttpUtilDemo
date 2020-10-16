package com.zhy.kotlinfdemo2.http

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.zhy.httputil.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Buffer
import okio.IOException
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

/**
 * @author zhy
 * @desc Http Util
 */
class AppHttp : LifecycleEventObserver {
    private var httpClient: OkHttpClient
    private lateinit var call: Call
    private lateinit var lifecycle: Lifecycle

    init {
        httpClient = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            addInterceptor(AppHttpInterceptor())
        }.build()
    }

    companion object {
        @JvmStatic
        private var appHttp: AppHttp? = null

        @JvmStatic
        val JSON_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()

        @JvmStatic
        fun getInstance(): AppHttp {
            synchronized(this) {
                return appHttp
                    ?: AppHttp()
            }
        }
    }

    /**
     * 普通get请求
     * @param activity 绑定activity
     * @param lifecycle 绑定activity或者fragment生命周期
     * @param url 请求地址
     * @param value 参数
     * @param result 请求回调
     */
    fun <T> get(
        activity: Activity,
        lifecycle: Lifecycle,
        url: String,
        value: Map<String, Any>? = null,
        result: Result<T>
    ) {
        this.lifecycle = lifecycle
        this.lifecycle?.addObserver(this)
        result.lifecycle = lifecycle
        var data = StringBuilder();
        var length = value?.size
        var index = 0
        value?.map {
            data.append("${it.key}=${it.value}")
            index++
            if (index != length) data.append("&") else Unit
        }
        var request: Request = Request.Builder()
            .url("${url}?${data}")
            .get()
            .tag(activity)
            .build()

        send(request, result)
    }

    /**
     * 普通发送json post请求
     * @param lifecycle activity，fragment生命周期
     * @param url 地址
     * @param json json数据
     * @param result 回调函数
     */
    fun <T> post(
        lifecycle: Lifecycle,
        url: String,
        json: String,
        result: Result<T>
    ) {
        this.lifecycle = lifecycle
        this.lifecycle?.addObserver(this)
        result.lifecycle = lifecycle
        val request = Request.Builder().apply {
            url(url)
            //设置json请求
            post(json.toRequestBody(JSON_TYPE))
            tag(url)
        }.build()

        send(request, result)
    }

    /**
     * 普通发送表单 post请求
     * @param lifecycle activity，fragment生命周期
     * @param url 地址
     * @param map key-value
     * @param result 回调函数
     */
    fun <T> post(lifecycle: Lifecycle, url: String, map: Map<String, Any?>, result: Result<T>) {
        this.lifecycle = lifecycle
        this.lifecycle.addObserver(this)
        result.lifecycle = lifecycle
        val fromBody = FormBody.Builder().apply {
            map.map {
                add(it.key, it.value as String)
            }
        }.build()
        val request = Request.Builder().apply {
            url(url)
            post(fromBody)
            tag(url)
        }.build()

        send(request, result)
    }

    /**
     * post发送目标文件
     * @param lifecycle 生命周期
     * @param url 上传文件地址
     * @param file 目标文件
     * @param result 回调地址
     * @param progress 上传文件进度回调
     */
    fun <T> postFile(
        lifecycle: Lifecycle,
        url: String,
        file: File,
        result: Result<T>,
        progress: UploadProgress
    ) {
        this.lifecycle = lifecycle
        this.lifecycle.addObserver(this)
        result.lifecycle = lifecycle
        val multipartBody = MultipartBody.Builder().apply {
            addFormDataPart(
                "file",
                file.name,
                FileUploadRequestBody(MultipartBody.FORM, file, progress)
            )
            setType(MultipartBody.FORM)
        }.build()
        val request = Request.Builder().apply {
            url(url)
            post(multipartBody)
            tag(url)
        }.build()
        send(request, result)
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            call?.cancel()
            println("取消当前请求");
            this.lifecycle.removeObserver(this)
        }
    }

    /**
     * 发送请求
     */
    fun <T> send(request: Request, result: Result<T>) {
        call = httpClient.newCall(request)
        call.enqueue(result)
    }

    //拦截器
    inner class AppHttpInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            if (BuildConfig.DEBUG) {
                //开发模式
                println("AppHttp_Log [${request?.method}] ->>>> url【${request?.url}】")
                when (request?.method) {
                    "POST" -> {
                        try {
                            if (request?.body is MultipartBody) {
                                //文件上传body fromdata
                            } else if (request?.body is RequestBody) {
                                val buffer = Buffer()
                                request.body?.writeTo(buffer)
                                var printData = buffer.readUtf8()
                                printData = URLDecoder.decode(printData, "utf-8")
                                println("AppHttp_Log [${request?.method}] ->>>> data【${printData}】")
                            }
                        } catch (exception: Exception) {
                        }
                    }
                    "GET" -> {

                    }
                }
            }
            return response.newBuilder().build();
        }
    }

}