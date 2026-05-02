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

    @Query("SELECT * FROM notifications ORDER BY postTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND (title LIKE '%' || :query || '%' OR textContent LIKE '%' || :query || '%') ORDER BY postTime DESC")
    fun searchNotifications(packageName: String, query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR textContent LIKE '%' || :query || '%' ORDER BY postTime DESC")
    fun searchAllNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE postTime < :threshold")
    suspend fun deleteOldNotifications(threshold: Long)

    @Query("UPDATE notifications SET isDismissed = 1 WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL))")
    suspend fun markAsDismissed(systemId: Int, tag: String?)

    @Query("SELECT * FROM notifications WHERE systemId = :systemId AND (tag = :tag OR (tag IS NULL AND :tag IS NULL)) ORDER BY postTime DESC LIMIT 1")
    suspend fun getLatestBySystemId(systemId: Int, tag: String?): NotificationEntity?
}
