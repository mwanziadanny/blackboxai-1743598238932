package com.example.danzygram.base

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<T>): List<Long>

    @Update
    suspend fun update(item: T): Int

    @Update
    suspend fun update(items: List<T>): Int

    @Delete
    suspend fun delete(item: T): Int

    @Delete
    suspend fun delete(items: List<T>): Int

    suspend fun insertOrUpdate(item: T) {
        if (insert(item) == -1L) {
            update(item)
        }
    }

    suspend fun insertOrUpdate(items: List<T>) {
        val insertResult = insert(items)
        val updateItems = items.filterIndexed { index, _ ->
            insertResult[index] == -1L
        }
        if (updateItems.isNotEmpty()) {
            update(updateItems)
        }
    }
}

interface BaseReadOnlyDao<T> {
    suspend fun get(id: String): T?
    suspend fun getAll(): List<T>
    fun observe(id: String): Flow<T?>
    fun observeAll(): Flow<List<T>>
}

interface BaseWriteOnlyDao<T> {
    suspend fun insert(item: T): Long
    suspend fun insert(items: List<T>): List<Long>
    suspend fun update(item: T): Int
    suspend fun update(items: List<T>): Int
    suspend fun delete(item: T): Int
    suspend fun delete(items: List<T>): Int
    suspend fun deleteAll()
}

interface BaseReadWriteDao<T> : BaseReadOnlyDao<T>, BaseWriteOnlyDao<T>

interface BaseSyncableDao<T> {
    suspend fun getLastSyncTime(): Long?
    suspend fun setLastSyncTime(timestamp: Long)
    suspend fun markAsSynced(item: T)
    suspend fun markAsSynced(items: List<T>)
    suspend fun getUnsynced(): List<T>
    suspend fun getSyncStatus(id: String): Boolean
    fun observeSyncStatus(id: String): Flow<Boolean>
}

interface BaseCacheableDao<T> {
    suspend fun getLastCacheTime(): Long?
    suspend fun setLastCacheTime(timestamp: Long)
    suspend fun isCacheValid(maxAge: Long): Boolean
    suspend fun clearCache()
}

interface BaseVersionedDao<T> {
    suspend fun getVersion(id: String): Long?
    suspend fun setVersion(id: String, version: Long)
    suspend fun getLatestVersion(): Long?
    suspend fun setLatestVersion(version: Long)
    suspend fun needsUpdate(id: String, latestVersion: Long): Boolean
}

interface BaseSearchableDao<T> {
    suspend fun search(query: String): List<T>
    fun observeSearch(query: String): Flow<List<T>>
}

interface BasePageableDao<T> {
    suspend fun getPage(page: Int, pageSize: Int): List<T>
    fun observePage(page: Int, pageSize: Int): Flow<List<T>>
    suspend fun getCount(): Int
}