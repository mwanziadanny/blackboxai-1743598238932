package com.example.danzygram.base

import com.google.gson.annotations.SerializedName

abstract class BaseRequest {
    @SerializedName("device_id")
    var deviceId: String? = null

    @SerializedName("device_type")
    var deviceType: String = "android"

    @SerializedName("app_version")
    var appVersion: String? = null

    @SerializedName("timestamp")
    var timestamp: Long = System.currentTimeMillis()

    fun validate(): Boolean {
        return true // Override in child classes for specific validation
    }
}

data class PageRequest(
    @SerializedName("page")
    var page: Int = 1,

    @SerializedName("per_page")
    var perPage: Int = 20,

    @SerializedName("sort_by")
    var sortBy: String? = null,

    @SerializedName("sort_order")
    var sortOrder: String? = null,

    @SerializedName("search")
    var search: String? = null,

    @SerializedName("filters")
    var filters: Map<String, Any>? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return page > 0 && perPage > 0
    }

    fun nextPage() {
        page++
    }

    fun reset() {
        page = 1
    }
}

data class LoginRequest(
    @SerializedName("email")
    var email: String? = null,

    @SerializedName("password")
    var password: String? = null,

    @SerializedName("device_token")
    var deviceToken: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }
}

data class SignupRequest(
    @SerializedName("email")
    var email: String? = null,

    @SerializedName("password")
    var password: String? = null,

    @SerializedName("username")
    var username: String? = null,

    @SerializedName("device_token")
    var deviceToken: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !email.isNullOrEmpty() && 
               !password.isNullOrEmpty() && 
               !username.isNullOrEmpty()
    }
}

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    var refreshToken: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !refreshToken.isNullOrEmpty()
    }
}

data class UpdateProfileRequest(
    @SerializedName("username")
    var username: String? = null,

    @SerializedName("bio")
    var bio: String? = null,

    @SerializedName("website")
    var website: String? = null,

    @SerializedName("profile_image")
    var profileImage: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !username.isNullOrEmpty()
    }
}

data class CreatePostRequest(
    @SerializedName("caption")
    var caption: String? = null,

    @SerializedName("image_url")
    var imageUrl: String? = null,

    @SerializedName("location")
    var location: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !imageUrl.isNullOrEmpty()
    }
}

data class CommentRequest(
    @SerializedName("post_id")
    var postId: String? = null,

    @SerializedName("text")
    var text: String? = null
) : BaseRequest() {

    override fun validate(): Boolean {
        return !postId.isNullOrEmpty() && !text.isNullOrEmpty()
    }
}