package com.example.danzygram.base

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors

@TypeConverters(BaseTypeConverters::class)
abstract class BaseDatabase : RoomDatabase() {

    protected abstract fun clearAllTables()

    protected fun createDatabaseCallback(
        scope: CoroutineScope,
        onOpen: (() -> Unit)? = null,
        onCreate: (() -> Unit)? = null
    ) = object : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            onOpen?.let {
                scope.launch(Dispatchers.IO) {
                    try {
                        it.invoke()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            onCreate?.let {
                scope.launch(Dispatchers.IO) {
                    try {
                        it.invoke()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BaseDatabase? = null

        fun getInstance(context: Context, databaseClass: Class<out BaseDatabase>): BaseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, databaseClass).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context, databaseClass: Class<out BaseDatabase>): BaseDatabase {
            return androidx.room.Room.databaseBuilder(
                context.applicationContext,
                databaseClass,
                databaseClass.simpleName
            )
                .fallbackToDestructiveMigration()
                .setQueryExecutor(Executors.newSingleThreadExecutor())
                .build()
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

class BaseTypeConverters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }

    @androidx.room.TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    @androidx.room.TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @androidx.room.TypeConverter
    fun fromIntList(value: String?): List<Int> {
        return value?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    }

    @androidx.room.TypeConverter
    fun toIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @androidx.room.TypeConverter
    fun fromLongList(value: String?): List<Long> {
        return value?.split(",")?.mapNotNull { it.trim().toLongOrNull() } ?: emptyList()
    }

    @androidx.room.TypeConverter
    fun toLongList(list: List<Long>?): String {
        return list?.joinToString(",") ?: ""
    }
}