package com.example.danzygram.base

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.util.Date

abstract class BaseEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String = ""

    @ColumnInfo(name = "created_at")
    var createdAt: Date? = null

    @ColumnInfo(name = "updated_at")
    var updatedAt: Date? = null

    @ColumnInfo(name = "is_synced")
    var isSynced: Boolean = false

    @ColumnInfo(name = "sync_timestamp")
    var syncTimestamp: Long? = null

    @ColumnInfo(name = "version")
    var version: Long = 1

    @ColumnInfo(name = "is_deleted")
    var isDeleted: Boolean = false

    @ColumnInfo(name = "deleted_at")
    var deletedAt: Date? = null

    fun markAsCreated() {
        val now = Date()
        createdAt = now
        updatedAt = now
    }

    fun markAsUpdated() {
        updatedAt = Date()
        version++
    }

    fun markAsSynced() {
        isSynced = true
        syncTimestamp = System.currentTimeMillis()
    }

    fun markAsUnsynced() {
        isSynced = false
        syncTimestamp = null
    }

    fun markAsDeleted() {
        isDeleted = true
        deletedAt = Date()
        markAsUpdated()
        markAsUnsynced()
    }

    fun softDelete() {
        markAsDeleted()
    }

    fun restore() {
        isDeleted = false
        deletedAt = null
        markAsUpdated()
        markAsUnsynced()
    }

    fun needsSync(): Boolean {
        return !isSynced
    }

    fun isNew(): Boolean {
        return createdAt == updatedAt
    }

    fun hasBeenModified(): Boolean {
        return createdAt != updatedAt
    }

    fun getAge(): Long {
        return createdAt?.time?.let { System.currentTimeMillis() - it } ?: 0
    }

    fun getLastModifiedAge(): Long {
        return updatedAt?.time?.let { System.currentTimeMillis() - it } ?: 0
    }

    fun getLastSyncAge(): Long {
        return syncTimestamp?.let { System.currentTimeMillis() - it } ?: 0
    }

    fun getDeletionAge(): Long {
        return deletedAt?.time?.let { System.currentTimeMillis() - it } ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}