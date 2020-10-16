package com.zhy.kotlinfdemo2.http

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.lang.Exception

abstract class Result<T>(val clazz: Class<T>) : okhttp3.Callback, LifecycleEventObserver {
    companion object {
        @JvmStatic
        private val SUCCESS = 200

        @JvmStatic
        private val JSON_EXCEPTION = 300

        @JvmStatic
        private val EXCEPTION = 305

        @JvmStatic
        private val ERROR = 404
    }

    var lifecycle: Lifecycle? = null
        get() = field
        set(value) {
            field = value
            field?.addObserver(this)
        }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg?.what) {
                SUCCESS -> onSuccess(msg?.obj as T)
                JSON_EXCEPTION -> onError(msg?.obj as String, null)
                EXCEPTION -> onError(msg?.obj as String, null)
                ERROR -> onError(msg?.obj as String, null)
            }
            lifecycle?.removeObserver(this@Result)
        }
    }

    abstract fun onSuccess(result: T)
    abstract fun onError(msg: String, e: IOException?)

    override fun onFailure(call: Call, e: IOException) {
        handler?.sendMessage(Message().apply {
            what = ERROR
            obj = "请求失败，请检查"
        })
    }

    override fun onResponse(call: Call, response: Response) {
        var msgEvt = Message()
        var json = response.body?.string()
        var gson = Gson()
        msgEvt.apply {

        }
        try {
            var data = gson.fromJson(json, clazz) as T
            msgEvt.apply {
                what = SUCCESS
                obj = data
            }
        } catch (jsonException: JsonSyntaxException) {
            msgEvt.apply {
                what = JSON_EXCEPTION
                obj = "json数据解析异常，请查看返回结果"
            }
        } catch (e: Exception) {
            msgEvt.apply {
                what = EXCEPTION
                obj = "出现其他异常行为"
            }
        }
        handler?.sendMessage(msgEvt)

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                handler.removeMessages(1)
                lifecycle?.removeObserver(this)
            }
        }
    }

}