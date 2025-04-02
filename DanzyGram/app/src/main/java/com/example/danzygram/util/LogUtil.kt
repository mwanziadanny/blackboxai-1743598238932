package com.example.danzygram.util

import android.util.Log
import com.example.danzygram.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

object LogUtil {
    private const val MAX_LOG_LENGTH = 4000
    private const val JSON_INDENT = 2

    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            log(Log.DEBUG, tag, message, throwable)
        }
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(Log.INFO, tag, message, throwable)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(Log.WARN, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(Log.ERROR, tag, message, throwable)
    }

    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        log(Log.ASSERT, tag, message, throwable)
    }

    private fun log(priority: Int, tag: String, message: String, throwable: Throwable? = null) {
        if (message.length < MAX_LOG_LENGTH) {
            logChunk(priority, tag, message)
            throwable?.let { logThrowable(priority, tag, it) }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = minOf(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                logChunk(priority, tag, part)
                i = end
            } while (i < newline)
            i++
        }
        throwable?.let { logThrowable(priority, tag, it) }
    }

    private fun logChunk(priority: Int, tag: String, chunk: String) {
        when (priority) {
            Log.DEBUG -> Timber.d("$tag: $chunk")
            Log.INFO -> Timber.i("$tag: $chunk")
            Log.WARN -> Timber.w("$tag: $chunk")
            Log.ERROR -> Timber.e("$tag: $chunk")
            Log.ASSERT -> Timber.wtf("$tag: $chunk")
        }
    }

    private fun logThrowable(priority: Int, tag: String, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        log(priority, tag, sw.toString())
    }

    fun logJson(tag: String, json: String) {
        try {
            val trimmed = json.trim()
            if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                log(Log.DEBUG, tag, jsonObject.toString(JSON_INDENT))
            } else if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                log(Log.DEBUG, tag, jsonArray.toString(JSON_INDENT))
            } else {
                log(Log.ERROR, tag, "Invalid JSON format: $json")
            }
        } catch (e: Exception) {
            log(Log.ERROR, tag, "Error parsing JSON: $json")
            e.printStackTrace()
        }
    }

    private class DebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
        }
    }

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.ERROR || priority == Log.WARN) {
                // Here you could send logs to your crash reporting service
                // Example: Crashlytics.log(priority, tag, message)
            }
        }
    }

    fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun getCurrentMethodName(): String {
        return Thread.currentThread().stackTrace[3].methodName
    }

    fun getCurrentClassName(): String {
        return Thread.currentThread().stackTrace[3].className
    }

    fun getCurrentFileName(): String {
        return Thread.currentThread().stackTrace[3].fileName ?: "Unknown"
    }

    fun getCurrentLineNumber(): Int {
        return Thread.currentThread().stackTrace[3].lineNumber
    }

    fun getCallerMethodName(): String {
        return Thread.currentThread().stackTrace[4].methodName
    }

    fun getCallerClassName(): String {
        return Thread.currentThread().stackTrace[4].className
    }

    fun getCallerFileName(): String {
        return Thread.currentThread().stackTrace[4].fileName ?: "Unknown"
    }

    fun getCallerLineNumber(): Int {
        return Thread.currentThread().stackTrace[4].lineNumber
    }

    fun getMethodCallStack(depth: Int = 5): String {
        val stackTrace = Thread.currentThread().stackTrace
        return buildString {
            for (i in 3 until minOf(stackTrace.size, depth + 3)) {
                val element = stackTrace[i]
                appendLine("${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
    }
}