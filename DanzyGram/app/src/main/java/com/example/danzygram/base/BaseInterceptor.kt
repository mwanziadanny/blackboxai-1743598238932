package com.example.danzygram.base

import com.example.danzygram.BuildConfig
import com.example.danzygram.util.DeviceUtil
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

abstract class BaseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        try {
            request = modifyRequest(request.newBuilder()).build()
            val response = chain.proceed(request)
            return modifyResponse(response)
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }

    protected open fun modifyRequest(builder: okhttp3.Request.Builder): okhttp3.Request.Builder {
        return builder.apply {
            addHeader("Accept", "application/json")
            addHeader("Content-Type", "application/json")
            addHeader("User-Agent", getUserAgent())
            addHeader("App-Version", BuildConfig.VERSION_NAME)
            addHeader("App-Version-Code", BuildConfig.VERSION_CODE.toString())
            addHeader("Device-Id", DeviceUtil.getDeviceId(BaseApplication.getApplicationContext()))
            addHeader("Device-Model", DeviceUtil.getModel())
            addHeader("Device-Manufacturer", DeviceUtil.getManufacturer())
            addHeader("Platform", "android")
            addHeader("OS-Version", DeviceUtil.getAndroidVersion())
        }
    }

    protected open fun modifyResponse(response: Response): Response {
        return response
    }

    private fun getUserAgent(): String {
        return "DanzyGram/${BuildConfig.VERSION_NAME} " +
                "(Android ${DeviceUtil.getAndroidVersion()}; " +
                "${DeviceUtil.getModel()}; " +
                "${DeviceUtil.getManufacturer()})"
    }

    protected fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is IOException
    }

    protected fun isHttpError(throwable: Throwable): Boolean {
        return throwable is retrofit2.HttpException
    }

    protected fun getErrorResponse(response: Response): ErrorResponse? {
        return try {
            val errorBody = response.body?.string()
            com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_USER_AGENT = "User-Agent"
        const val HEADER_APP_VERSION = "App-Version"
        const val HEADER_APP_VERSION_CODE = "App-Version-Code"
        const val HEADER_DEVICE_ID = "Device-Id"
        const val HEADER_DEVICE_MODEL = "Device-Model"
        const val HEADER_DEVICE_MANUFACTURER = "Device-Manufacturer"
        const val HEADER_PLATFORM = "Platform"
        const val HEADER_OS_VERSION = "OS-Version"

        const val CONTENT_TYPE_JSON = "application/json"
        const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
        const val CONTENT_TYPE_MULTIPART = "multipart/form-data"

        const val PLATFORM_ANDROID = "android"
    }
}

class AuthInterceptor(
    private val getToken: () -> String?
) : BaseInterceptor() {

    override fun modifyRequest(builder: okhttp3.Request.Builder): okhttp3.Request.Builder {
        return super.modifyRequest(builder).apply {
            getToken()?.let { token ->
                addHeader(HEADER_AUTHORIZATION, "Bearer $token")
            }
        }
    }
}

class NetworkInterceptor : BaseInterceptor() {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtil.isNetworkAvailable(BaseApplication.getApplicationContext())) {
            throw IOException("No internet connection")
        }
        return super.intercept(chain)
    }
}

class LoggingInterceptor : BaseInterceptor() {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        Timber.d("Sending request: ${request.url}")
        Timber.d("Headers: ${request.headers}")

        val response = chain.proceed(request)
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1e6 // Convert to milliseconds

        Timber.d("Received response for ${request.url} in ${duration}ms")
        Timber.d("Response code: ${response.code}")
        Timber.d("Response headers: ${response.headers}")

        return response
    }
}