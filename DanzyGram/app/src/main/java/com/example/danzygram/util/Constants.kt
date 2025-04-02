package com.example.danzygram.util

object Constants {
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val POSTS_COLLECTION = "posts"
    const val COMMENTS_COLLECTION = "comments"
    const val LIKES_COLLECTION = "likes"
    const val FOLLOWS_COLLECTION = "follows"

    // Firebase Storage
    const val PROFILE_PICTURES_STORAGE = "profile_pictures"
    const val POST_IMAGES_STORAGE = "post_images"

    // SharedPreferences
    const val PREFS_NAME = "danzygram_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_PROFILE_IMAGE = "profile_image"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_LAST_NOTIFICATION_ID = "last_notification_id"

    // Intent Keys
    const val KEY_POST_ID = "post_id"
    const val KEY_USER_DATA = "user_data"
    const val KEY_IMAGE_URI = "image_uri"
    const val KEY_POST_DATA = "post_data"

    // Request Codes
    const val RC_SIGN_IN = 100
    const val RC_GALLERY = 101
    const val RC_CAMERA = 102
    const val RC_PERMISSIONS = 103
    const val RC_CROP_IMAGE = 104

    // Permissions
    const val PERMISSION_CAMERA = android.Manifest.permission.CAMERA
    const val PERMISSION_READ_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
    const val PERMISSION_WRITE_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "danzygram_channel"
    const val NOTIFICATION_CHANNEL_NAME = "DanzyGram Notifications"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for likes, comments, and follows"

    // Time Constants
    const val SPLASH_DELAY = 1500L
    const val DEBOUNCE_TIME = 300L
    const val ANIMATION_DURATION = 300L
    const val DOUBLE_CLICK_DELAY = 300L

    // Network
    const val TIMEOUT_CONNECT = 30L
    const val TIMEOUT_READ = 30L
    const val TIMEOUT_WRITE = 30L
    const val MAX_RETRIES = 3
    const val INITIAL_BACKOFF_DELAY = 1000L

    // Cache
    const val CACHE_SIZE = 10L * 1024 * 1024 // 10MB
    const val CACHE_DURATION = 7 * 24 * 60 * 60 * 1000L // 7 days

    // Pagination
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 3

    // Image
    const val MAX_IMAGE_SIZE = 1024
    const val IMAGE_QUALITY = 80
    const val PROFILE_IMAGE_SIZE = 400
    const val THUMBNAIL_SIZE = 200

    // Validation
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 30
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_CAPTION_LENGTH = 2200
    const val MAX_COMMENT_LENGTH = 1000

    // UI Constants
    const val GRID_SPAN_COUNT = 3
    const val SHIMMER_DURATION = 1000L
    const val REFRESH_THRESHOLD = 100f

    // Error Messages
    const val ERROR_NETWORK = "No internet connection"
    const val ERROR_UNKNOWN = "An unknown error occurred"
    const val ERROR_SERVER = "Server error"
    const val ERROR_TIMEOUT = "Request timed out"
    const val ERROR_INVALID_CREDENTIALS = "Invalid email or password"
    const val ERROR_WEAK_PASSWORD = "Password should be at least 6 characters"
    const val ERROR_EMAIL_EXISTS = "Email already exists"
    const val ERROR_USERNAME_EXISTS = "Username already exists"
    const val ERROR_INVALID_USERNAME = "Invalid username"
    const val ERROR_INVALID_EMAIL = "Invalid email address"
    const val ERROR_UPLOAD_FAILED = "Failed to upload image"
    const val ERROR_DELETE_FAILED = "Failed to delete"
    const val ERROR_UPDATE_FAILED = "Failed to update"
    const val ERROR_PERMISSION_DENIED = "Permission denied"

    // Success Messages
    const val SUCCESS_PROFILE_UPDATED = "Profile updated successfully"
    const val SUCCESS_POST_CREATED = "Post created successfully"
    const val SUCCESS_POST_DELETED = "Post deleted successfully"
    const val SUCCESS_COMMENT_ADDED = "Comment added successfully"
    const val SUCCESS_COMMENT_DELETED = "Comment deleted successfully"
    const val SUCCESS_PASSWORD_RESET = "Password reset email sent"
}