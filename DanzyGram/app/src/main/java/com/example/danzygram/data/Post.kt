package com.example.danzygram.data

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Post(
    @DocumentId
    val postId: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("username")
    val username: String = "",
    
    @PropertyName("userProfileImageUrl")
    val userProfileImageUrl: String = "",
    
    @PropertyName("imageUrl")
    val imageUrl: String = "",
    
    @PropertyName("caption")
    val caption: String = "",
    
    @PropertyName("likes")
    val likes: Int = 0,
    
    @PropertyName("comments")
    val comments: Int = 0,
    
    @PropertyName("location")
    val location: String = "",
    
    @PropertyName("isLiked")
    var isLiked: Boolean = false,
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date? = null
) : Parcelable {
    
    companion object {
        fun create(
            userId: String,
            username: String,
            userProfileImageUrl: String,
            imageUrl: String,
            caption: String,
            location: String = ""
        ): Post = Post(
            userId = userId,
            username = username,
            userProfileImageUrl = userProfileImageUrl,
            imageUrl = imageUrl,
            caption = caption,
            location = location
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "postId" to postId,
        "userId" to userId,
        "username" to username,
        "userProfileImageUrl" to userProfileImageUrl,
        "imageUrl" to imageUrl,
        "caption" to caption,
        "likes" to likes,
        "comments" to comments,
        "location" to location,
        "timestamp" to timestamp
    )

    fun copyWithUpdates(
        caption: String? = null,
        likes: Int? = null,
        comments: Int? = null,
        isLiked: Boolean? = null
    ): Post = copy(
        caption = caption ?: this.caption,
        likes = likes ?: this.likes,
        comments = comments ?: this.comments,
        isLiked = isLiked ?: this.isLiked
    )
}

@Parcelize
data class Comment(
    @DocumentId
    val commentId: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("username")
    val username: String = "",
    
    @PropertyName("userProfileImageUrl")
    val userProfileImageUrl: String = "",
    
    @PropertyName("text")
    val text: String = "",
    
    @PropertyName("likes")
    val likes: Int = 0,
    
    @PropertyName("isLiked")
    var isLiked: Boolean = false,
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date? = null
) : Parcelable {
    
    companion object {
        fun create(
            userId: String,
            username: String,
            userProfileImageUrl: String,
            text: String
        ): Comment = Comment(
            userId = userId,
            username = username,
            userProfileImageUrl = userProfileImageUrl,
            text = text
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "commentId" to commentId,
        "userId" to userId,
        "username" to username,
        "userProfileImageUrl" to userProfileImageUrl,
        "text" to text,
        "likes" to likes,
        "timestamp" to timestamp
    )
}

@Parcelize
data class Like(
    @DocumentId
    val likeId: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("username")
    val username: String = "",
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date? = null
) : Parcelable {
    
    companion object {
        fun create(
            userId: String,
            username: String
        ): Like = Like(
            userId = userId,
            username = username
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "likeId" to likeId,
        "userId" to userId,
        "username" to username,
        "timestamp" to timestamp
    )
}

@Parcelize
data class Follow(
    @DocumentId
    val followId: String = "",
    
    @PropertyName("followerId")
    val followerId: String = "",
    
    @PropertyName("followerUsername")
    val followerUsername: String = "",
    
    @PropertyName("followedId")
    val followedId: String = "",
    
    @PropertyName("followedUsername")
    val followedUsername: String = "",
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date? = null
) : Parcelable {
    
    companion object {
        fun create(
            followerId: String,
            followerUsername: String,
            followedId: String,
            followedUsername: String
        ): Follow = Follow(
            followerId = followerId,
            followerUsername = followerUsername,
            followedId = followedId,
            followedUsername = followedUsername
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "followId" to followId,
        "followerId" to followerId,
        "followerUsername" to followerUsername,
        "followedId" to followedId,
        "followedUsername" to followedUsername,
        "timestamp" to timestamp
    )
}