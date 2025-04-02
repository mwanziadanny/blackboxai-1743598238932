package com.example.danzygram.base

import com.google.gson.annotations.SerializedName

open class BaseResponse {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("error")
    var error: ErrorResponse? = null

    fun isSuccess(): Boolean {
        return status == "success" && error == null
    }

    fun getErrorMessage(): String {
        return error?.message ?: message ?: "Unknown error occurred"
    }
}

data class ErrorResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("details")
    val details: Map<String, String>? = null
) {
    fun getDetailedMessage(): String {
        return details?.values?.joinToString("\n") ?: message ?: "Unknown error occurred"
    }
}

data class PagedResponse<T>(
    @SerializedName("data")
    val data: List<T>? = null,

    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("per_page")
    val perPage: Int? = null,

    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("total_pages")
    val totalPages: Int? = null,

    @SerializedName("has_more")
    val hasMore: Boolean? = null
) : BaseResponse() {

    fun hasNextPage(): Boolean {
        return hasMore ?: (page != null && totalPages != null && page < totalPages)
    }

    fun isFirstPage(): Boolean {
        return page == 1
    }

    fun isLastPage(): Boolean {
        return !hasNextPage()
    }

    fun getNextPage(): Int {
        return (page ?: 0) + 1
    }
}

data class DataResponse<T>(
    @SerializedName("data")
    val data: T? = null
) : BaseResponse()

data class ListResponse<T>(
    @SerializedName("data")
    val data: List<T>? = null
) : BaseResponse()

data class EmptyResponse(
    @SerializedName("timestamp")
    val timestamp: Long? = null
) : BaseResponse()

data class MetaResponse(
    @SerializedName("version")
    val version: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long? = null,

    @SerializedName("server")
    val server: String? = null
)

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    @SerializedName("token_type")
    val tokenType: String? = null,

    @SerializedName("expires_in")
    val expiresIn: Long? = null
) : BaseResponse() {

    fun getFullToken(): String {
        return "$tokenType $accessToken"
    }

    fun isValid(): Boolean {
        return !accessToken.isNullOrEmpty() && !tokenType.isNullOrEmpty()
    }
}