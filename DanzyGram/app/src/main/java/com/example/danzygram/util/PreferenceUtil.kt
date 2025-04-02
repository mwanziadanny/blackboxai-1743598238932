package com.example.danzygram.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.danzygram.util.Constants.PREFS_NAME
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PreferenceUtil {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private val _themeFlow = MutableStateFlow(false)
    val themeFlow = _themeFlow.asStateFlow()

    private val _notificationsFlow = MutableStateFlow(true)
    val notificationsFlow = _notificationsFlow.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeFlow.value = isDarkMode()
        _notificationsFlow.value = areNotificationsEnabled()
    }

    // User Session
    fun saveUserId(userId: String) = prefs.edit { putString(Constants.KEY_USER_ID, userId) }
    fun getUserId(): String? = prefs.getString(Constants.KEY_USER_ID, null)
    fun clearUserId() = prefs.edit { remove(Constants.KEY_USER_ID) }

    fun saveUsername(username: String) = prefs.edit { putString(Constants.KEY_USERNAME, username) }
    fun getUsername(): String? = prefs.getString(Constants.KEY_USERNAME, null)
    fun clearUsername() = prefs.edit { remove(Constants.KEY_USERNAME) }

    fun saveEmail(email: String) = prefs.edit { putString(Constants.KEY_EMAIL, email) }
    fun getEmail(): String? = prefs.getString(Constants.KEY_EMAIL, null)
    fun clearEmail() = prefs.edit { remove(Constants.KEY_EMAIL) }

    fun saveProfileImage(url: String) = prefs.edit { putString(Constants.KEY_PROFILE_IMAGE, url) }
    fun getProfileImage(): String? = prefs.getString(Constants.KEY_PROFILE_IMAGE, null)
    fun clearProfileImage() = prefs.edit { remove(Constants.KEY_PROFILE_IMAGE) }

    // Theme
    fun setDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(Constants.KEY_DARK_MODE, enabled) }
        _themeFlow.value = enabled
    }
    fun isDarkMode(): Boolean = prefs.getBoolean(Constants.KEY_DARK_MODE, false)

    // Notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled) }
        _notificationsFlow.value = enabled
    }
    fun areNotificationsEnabled(): Boolean = 
        prefs.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)

    fun getLastNotificationId(): Int = 
        prefs.getInt(Constants.KEY_LAST_NOTIFICATION_ID, 0)
    fun incrementNotificationId() = prefs.edit {
        val currentId = getLastNotificationId()
        putInt(Constants.KEY_LAST_NOTIFICATION_ID, currentId + 1)
    }

    // Generic methods for storing objects
    fun <T> saveObject(key: String, obj: T) {
        val json = gson.toJson(obj)
        prefs.edit { putString(key, json) }
    }

    inline fun <reified T> getObject(key: String): T? {
        val json = prefs.getString(key, null)
        return try {
            json?.let { gson.fromJson(it, T::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    // Session Management
    fun isUserLoggedIn(): Boolean = getUserId() != null

    fun saveUserSession(userId: String, username: String, email: String, profileImage: String? = null) {
        saveUserId(userId)
        saveUsername(username)
        saveEmail(email)
        profileImage?.let { saveProfileImage(it) }
    }

    fun clearUserSession() {
        clearUserId()
        clearUsername()
        clearEmail()
        clearProfileImage()
    }

    // Preference Clearing
    fun clearAll() {
        prefs.edit { clear() }
        _themeFlow.value = false
        _notificationsFlow.value = true
    }

    fun clearPreference(key: String) {
        prefs.edit { remove(key) }
    }

    // Preference Existence Check
    fun contains(key: String): Boolean = prefs.contains(key)

    // Type-specific getters with default values
    fun getString(key: String, defaultValue: String = ""): String = 
        prefs.getString(key, defaultValue) ?: defaultValue

    fun getInt(key: String, defaultValue: Int = 0): Int = 
        prefs.getInt(key, defaultValue)

    fun getLong(key: String, defaultValue: Long = 0L): Long = 
        prefs.getLong(key, defaultValue)

    fun getFloat(key: String, defaultValue: Float = 0f): Float = 
        prefs.getFloat(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = 
        prefs.getBoolean(key, defaultValue)

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> = 
        prefs.getStringSet(key, defaultValue) ?: defaultValue

    // Type-specific setters
    fun putString(key: String, value: String) = prefs.edit { putString(key, value) }
    fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    fun putLong(key: String, value: Long) = prefs.edit { putLong(key, value) }
    fun putFloat(key: String, value: Float) = prefs.edit { putFloat(key, value) }
    fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    fun putStringSet(key: String, value: Set<String>) = prefs.edit { putStringSet(key, value) }
}