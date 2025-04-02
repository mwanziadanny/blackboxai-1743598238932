package com.example.danzygram.data

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    @DocumentId
    val userId: String = "",
    
    @PropertyName("username")
    val username: String = "",
    
    @PropertyName("email")
    val email: String = "",
    
    @PropertyName("bio")
    val bio: String = "",
    
    @PropertyName("profileImageUrl")
    val profileImageUrl: String = "",
    
    @PropertyName("posts")
    val posts: Int = 0,
    
    @PropertyName("followers")
    val followers: Int = 0,
    
    @PropertyName("following")
    val following: Int = 0,
    
    @ServerTimestamp
    @PropertyName("createdAt")
    val createdAt: Date? = null,
    
    @ServerTimestamp
    @PropertyName("updatedAt")
    val updatedAt: Date? = null
) : Parcelable {
    
    companion object {
        fun create(
            userId: String,
            username: String,
            email: String
        ): User = User(
            userId = userId,
            username = username,
            email = email
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "username" to username,
        "email" to email,
        "bio" to bio,
        "profileImageUrl" to profileImageUrl,
        "posts" to posts,
        "followers" to followers,
        "following" to following,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun copyWithUpdates(
        bio: String? = null,
        profileImageUrl: String? = null,
        posts: Int? = null,
        followers: Int? = null,
        following: Int? = null
    ): User = copy(
        bio = bio ?: this.bio,
        profileImageUrl = profileImageUrl ?: this.profileImageUrl,
        posts = posts ?: this.posts,
        followers = followers ?: this.followers,
        following = following ?: this.following
    )
}