package com.example.danzygram.base

import com.example.danzygram.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface BaseApiService {
    companion object {
        private const val TIMEOUT_CONNECT = 30L
        private const val TIMEOUT_READ = 30L
        private const val TIMEOUT_WRITE = 30L

        fun <T> createService(
            serviceClass: Class<T>,
            baseUrl: String = BuildConfig.BASE_URL,
            getToken: () -> String? = { null }
        ): T {
            val okHttpClient = createOkHttpClient(getToken)
            val retrofit = createRetrofit(baseUrl, okHttpClient)
            return retrofit.create(serviceClass)
        }

        private fun createOkHttpClient(getToken: () -> String?): OkHttpClient {
            return OkHttpClient.Builder().apply {
                connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                addInterceptor(NetworkInterceptor())
                addInterceptor(AuthInterceptor(getToken))
                if (BuildConfig.DEBUG) {
                    addInterceptor(LoggingInterceptor())
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }.build()
        }

        private fun createRetrofit(
            baseUrl: String,
            okHttpClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    // Common API endpoints
    suspend fun ping(): BaseResponse

    suspend fun getVersion(): DataResponse<String>

    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse

    suspend fun logout(): EmptyResponse
}

interface AuthApiService : BaseApiService {
    suspend fun login(request: LoginRequest): TokenResponse

    suspend fun signup(request: SignupRequest): TokenResponse

    suspend fun forgotPassword(email: String): EmptyResponse

    suspend fun resetPassword(
        token: String,
        password: String
    ): EmptyResponse

    suspend fun validateResetToken(token: String): EmptyResponse
}

interface UserApiService : BaseApiService {
    suspend fun getProfile(): DataResponse<com.example.danzygram.data.User>

    suspend fun updateProfile(
        request: UpdateProfileRequest
    ): DataResponse<com.example.danzygram.data.User>

    suspend fun uploadProfileImage(
        image: okhttp3.MultipartBody.Part
    ): DataResponse<String>

    suspend fun searchUsers(
        query: String,
        page: Int = 1,
        perPage: Int = 20
    ): PagedResponse<com.example.danzygram.data.User>

    suspend fun followUser(userId: String): EmptyResponse

    suspend fun unfollowUser(userId: String): EmptyResponse

    suspend fun getFollowers(
        userId: String,
        page: Int = 1,
        perPage: Int = 20
    ): PagedResponse<com.example.danzygram.data.User>

    suspend fun getFollowing(
        userId: String,
        page: Int = 1,
        perPage: Int = 20
    ): PagedResponse<com.example.danzygram.data.User>
}

interface PostApiService : BaseApiService {
    suspend fun createPost(
        request: CreatePostRequest
    ): DataResponse<com.example.danzygram.data.Post>

    suspend fun uploadPostImage(
        image: okhttp3.MultipartBody.Part
    ): DataResponse<String>

    suspend fun getFeed(
        page: Int = 1,
        perPage: Int = 20
    ): PagedResponse<com.example.danzygram.data.Post>

    suspend fun getUserPosts(
        userId: String,
        page: Int = 1,
        perPage: Int = 20
    ): PagedResponse<com.example.danzygram.data.Post>

    suspend fun likePost(postId: String): EmptyResponse

    suspend fun unlikePost(postId: String): EmptyResponse

    suspend fun commentOnPost(
        request: CommentRequest
    ): DataResponse<com.example.danzygram.data.Comment>

    suspend fun deleteComment(
        postId: String,
        commentId: String
    ): EmptyResponse

    suspend fun deletePost(postId: String): EmptyResponse
}