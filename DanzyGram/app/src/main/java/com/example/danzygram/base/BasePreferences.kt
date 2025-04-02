package com.example.danzygram.base

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BasePreferences(
    context: Context,
    private val preferencesName: String,
    private val encrypted: Boolean = false
) {
    private val gson = Gson()
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    protected val preferences: SharedPreferences = if (encrypted) {
        EncryptedSharedPreferences.create(
            context,
            preferencesName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    fun clear() {
        preferences.edit { clear() }
    }

    protected fun stringPreference(
        key: String,
        defaultValue: String = ""
    ): ReadWriteProperty<Any, String> = object : ReadWriteProperty<Any, String> {
        private val flow = MutableStateFlow(preferences.getString(key, defaultValue) ?: defaultValue)

        override fun getValue(thisRef: Any, property: KProperty<*>): String {
            return preferences.getString(key, defaultValue) ?: defaultValue
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            preferences.edit { putString(key, value) }
            flow.value = value
        }
    }

    protected fun intPreference(
        key: String,
        defaultValue: Int = 0
    ): ReadWriteProperty<Any, Int> = object : ReadWriteProperty<Any, Int> {
        private val flow = MutableStateFlow(preferences.getInt(key, defaultValue))

        override fun getValue(thisRef: Any, property: KProperty<*>): Int {
            return preferences.getInt(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
            preferences.edit { putInt(key, value) }
            flow.value = value
        }
    }

    protected fun longPreference(
        key: String,
        defaultValue: Long = 0L
    ): ReadWriteProperty<Any, Long> = object : ReadWriteProperty<Any, Long> {
        private val flow = MutableStateFlow(preferences.getLong(key, defaultValue))

        override fun getValue(thisRef: Any, property: KProperty<*>): Long {
            return preferences.getLong(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            preferences.edit { putLong(key, value) }
            flow.value = value
        }
    }

    protected fun floatPreference(
        key: String,
        defaultValue: Float = 0f
    ): ReadWriteProperty<Any, Float> = object : ReadWriteProperty<Any, Float> {
        private val flow = MutableStateFlow(preferences.getFloat(key, defaultValue))

        override fun getValue(thisRef: Any, property: KProperty<*>): Float {
            return preferences.getFloat(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
            preferences.edit { putFloat(key, value) }
            flow.value = value
        }
    }

    protected fun booleanPreference(
        key: String,
        defaultValue: Boolean = false
    ): ReadWriteProperty<Any, Boolean> = object : ReadWriteProperty<Any, Boolean> {
        private val flow = MutableStateFlow(preferences.getBoolean(key, defaultValue))

        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            preferences.edit { putBoolean(key, value) }
            flow.value = value
        }
    }

    protected fun <T> objectPreference(
        key: String,
        defaultValue: T,
        clazz: Class<T>
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        private val flow = MutableStateFlow(getObject(key, defaultValue, clazz))

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return getObject(key, defaultValue, clazz)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            setObject(key, value)
            flow.value = value
        }
    }

    protected fun <T> observePreference(key: String, defaultValue: T): Flow<T> {
        return MutableStateFlow(key).map { getPreference(key, defaultValue) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getPreference(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> preferences.getString(key, defaultValue) as T
            is Int -> preferences.getInt(key, defaultValue) as T
            is Long -> preferences.getLong(key, defaultValue) as T
            is Float -> preferences.getFloat(key, defaultValue) as T
            is Boolean -> preferences.getBoolean(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    private fun <T> getObject(key: String, defaultValue: T, clazz: Class<T>): T {
        val json = preferences.getString(key, null)
        return try {
            json?.let { gson.fromJson(it, clazz) } ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun <T> setObject(key: String, value: T) {
        preferences.edit { putString(key, gson.toJson(value)) }
    }
}