package com.example.danzygram.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtil {
    private const val USERS_COLLECTION = "users"
    private const val POSTS_COLLECTION = "posts"
    private const val COMMENTS_COLLECTION = "comments"
    private const val LIKES_COLLECTION = "likes"
    private const val FOLLOWS_COLLECTION = "follows"

    // Auth
    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = getAuth().currentUser

    // Firestore Collections
    fun getFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUsersCollection(): CollectionReference = getFirestore().collection(USERS_COLLECTION)

    fun getPostsCollection(): CollectionReference = getFirestore().collection(POSTS_COLLECTION)

    fun getCommentsCollection(postId: String): CollectionReference =
        getPostsCollection().document(postId).collection(COMMENTS_COLLECTION)

    fun getLikesCollection(postId: String): CollectionReference =
        getPostsCollection().document(postId).collection(LIKES_COLLECTION)

    fun getFollowsCollection(): CollectionReference = getFirestore().collection(FOLLOWS_COLLECTION)

    // Storage References
    fun getStorage(): StorageReference = FirebaseStorage.getInstance().reference

    // Query Helpers
    fun getPostsByUser(userId: String): Query =
        getPostsCollection()
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

    fun getFeedPosts(): Query =
        getPostsCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)

    fun getFollowers(userId: String): Query =
        getFollowsCollection()
            .whereEqualTo("followedId", userId)

    fun getFollowing(userId: String): Query =
        getFollowsCollection()
            .whereEqualTo("followerId", userId)

    fun searchUsers(query: String): Query =
        getUsersCollection()
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)

    // Utility Functions
    fun isCurrentUser(userId: String): Boolean =
        getCurrentUser()?.uid == userId

    fun isPostLiked(postId: String, userId: String): Query =
        getLikesCollection(postId)
            .whereEqualTo("userId", userId)
            .limit(1)

    fun isFollowing(followerId: String, followedId: String): Query =
        getFollowsCollection()
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followedId", followedId)
            .limit(1)

    // Storage Paths
    fun getProfilePicturePath(userId: String): StorageReference =
        getStorage()
            .child("profile_pictures")
            .child(userId)

    fun getPostImagePath(userId: String, postId: String): StorageReference =
        getStorage()
            .child("posts")
            .child(userId)
            .child(postId)
}