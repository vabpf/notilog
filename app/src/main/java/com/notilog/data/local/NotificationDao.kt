package com.notilog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 ORDER BY postTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 AND packageName = :packageName AND (title LIKE '%' || :query || '%' OR textContent LIKE '%' || :query || '%') ORDER BY postTime DESC")
    fun searchNotifications(packageName: String, query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR textContent LIKE '%' || :query || '%') ORDER BY postTime DESC")
    fun searchAllNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 AND category = :category ORDER BY postTime DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long)

    @Query("UPDATE notifications SET isDeleted = 1, deletedAt = :deletedAt WHERE id IN (:ids)")
    suspend fun softDeleteByIds(ids: List<Long>, deletedAt: Long)

    @Query("DELETE FROM notifications WHERE postTime < :threshold")
    suspend fun deleteOldNotifications(threshold: Long)

    @Query("DELETE FROM notifications WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun permanentDeleteOldTrash(threshold: Long)

    @Query("SELECT * FROM notifications WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM notifications WHERE isDeleted = 1")
    suspend fun permanentDeleteAllTrash()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT category, COUNT(*) as count FROM notifications WHERE isDeleted = 0 GROUP BY category ORDER BY count DESC")
    fun getCategoryNotificationCounts(): Flow<List<CategoryCountEntry>>

    @Query("UPDATE notifications SET isDismissed = 1 WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL))")
    suspend fun markAsDismissed(systemId: Int, tag: String?)

    @Query("SELECT * FROM notifications WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL)) ORDER BY postTime DESC LIMIT 1")
    suspend fun getLatestBySystemId(systemId: Int, tag: String?): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL)) ORDER BY postTime DESC")
    fun getVersionsBySystemId(systemId: Int, tag: String?): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL))")
    suspend fun deleteBySystemId(systemId: Int, tag: String?)

    @Query("SELECT packageName, appName, MAX(postTime) as lastPostTime FROM notifications WHERE isDeleted = 0 GROUP BY packageName ORDER BY MAX(postTime) DESC LIMIT :limit")
    fun getRecentApps(limit: Int): Flow<List<AppInfoEntry>>

    @Query("SELECT packageName, appName FROM notifications WHERE isDeleted = 0 GROUP BY packageName ORDER BY appName ASC")
    fun getAllApps(): Flow<List<AppInfoEntry>>

    // Insights queries
    @Query("SELECT COUNT(*) FROM notifications WHERE isDeleted = 0")
    fun getTotalNotificationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE isDeleted = 0 AND postTime >= :startOfDay")
    fun getNotificationCountSince(startOfDay: Long): Flow<Int>

    @Query("SELECT category, COUNT(*) as count FROM notifications WHERE isDeleted = 0 GROUP BY category ORDER BY count DESC")
    fun getCategoryBreakdown(): Flow<List<CategoryCountEntry>>

    @Query("SELECT packageName, appName, COUNT(*) as count FROM notifications WHERE isDeleted = 0 GROUP BY packageName ORDER BY count DESC LIMIT :limit")
    fun getTopApps(limit: Int): Flow<List<AppCountEntry>>

    @Query("""
        SELECT strftime('%Y-%m-%d', postTime/1000, 'unixepoch') as date, COUNT(*) as count 
        FROM notifications 
        WHERE isDeleted = 0 AND postTime >= :sinceTimestamp 
        GROUP BY strftime('%Y-%m-%d', postTime/1000, 'unixepoch') 
        ORDER BY date DESC
    """)
    fun getDailyCounts(sinceTimestamp: Long): Flow<List<DailyCountEntry>>
}

data class CategoryCountEntry(
    val category: String,
    val count: Int
)

data class AppInfoEntry(
    val packageName: String,
    val appName: String,
    val lastPostTime: Long? = null
)

data class AppCountEntry(
    val packageName: String,
    val appName: String,
    val count: Int
)

data class DailyCountEntry(
    val date: String,
    val count: Int
)
