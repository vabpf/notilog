package com.notilog.data.repository

import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import com.notilog.data.local.CategoryCountEntry
import com.notilog.data.local.AppInfoEntry
import com.notilog.data.local.AppCountEntry
import com.notilog.data.local.DailyCountEntry
import com.notilog.data.local.HourlyCountEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    fun getAllNotificationsCursor(): android.database.Cursor = notificationDao.getAllNotificationsCursor()

    suspend fun insert(notification: NotificationEntity) = notificationDao.insert(notification)

    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun searchNotifications(packageName: String, query: String): Flow<List<NotificationEntity>> =
        notificationDao.searchNotifications(packageName, query)

    fun searchAllNotifications(query: String): Flow<List<NotificationEntity>> =
        notificationDao.searchAllNotifications(query)

    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsByCategory(category)

    suspend fun softDeleteById(id: Long, deletedAt: Long) = notificationDao.softDeleteById(id, deletedAt)

    suspend fun softDeleteByIds(ids: List<Long>, deletedAt: Long) = notificationDao.softDeleteByIds(ids, deletedAt)

    suspend fun deleteOldNotifications(threshold: Long) = notificationDao.deleteOldNotifications(threshold)

    suspend fun deleteAllNotifications() = notificationDao.deleteAllNotifications()

    suspend fun permanentDeleteOldTrash(threshold: Long) = notificationDao.permanentDeleteOldTrash(threshold)

    fun getDeletedNotifications(): Flow<List<NotificationEntity>> = notificationDao.getDeletedNotifications()

    suspend fun restoreFromTrash(id: Long) = notificationDao.restoreFromTrash(id)

    suspend fun permanentDeleteAllTrash() = notificationDao.permanentDeleteAllTrash()

    suspend fun deleteById(id: Long) = notificationDao.deleteById(id)

    fun getCategoryNotificationCounts(): Flow<List<CategoryCountEntry>> = notificationDao.getCategoryNotificationCounts()

    suspend fun markAsDismissed(packageName: String, systemId: Int, tag: String?) =
        notificationDao.markAsDismissed(packageName, systemId, tag)

    suspend fun getLatestBySystemId(packageName: String, systemId: Int, tag: String?): NotificationEntity? =
        notificationDao.getLatestBySystemId(packageName, systemId, tag)

    fun getVersionsBySystemId(packageName: String, systemId: Int, tag: String?): Flow<List<NotificationEntity>> =
        notificationDao.getVersionsBySystemId(packageName, systemId, tag)

    suspend fun softDeleteBySystemId(packageName: String, systemId: Int, tag: String?, deletedAt: Long) =
        notificationDao.softDeleteBySystemId(packageName, systemId, tag, deletedAt)

    suspend fun deleteBySystemId(packageName: String, systemId: Int, tag: String?) =
        notificationDao.deleteBySystemId(packageName, systemId, tag)

    fun getRecentApps(limit: Int): Flow<List<AppInfoEntry>> = notificationDao.getRecentApps(limit)

    fun getAllApps(): Flow<List<AppInfoEntry>> = notificationDao.getAllApps()

    fun getTotalNotificationCount(): Flow<Int> = notificationDao.getTotalNotificationCount()

    fun getNotificationCountSince(startOfDay: Long): Flow<Int> = notificationDao.getNotificationCountSince(startOfDay)

    fun getCategoryBreakdown(): Flow<List<CategoryCountEntry>> = notificationDao.getCategoryBreakdown()

    fun getTopApps(limit: Int): Flow<List<AppCountEntry>> = notificationDao.getTopApps(limit)

    fun getDailyCounts(sinceTimestamp: Long): Flow<List<DailyCountEntry>> = notificationDao.getDailyCounts(sinceTimestamp)

    fun getHourlyDistribution(): Flow<List<HourlyCountEntry>> = notificationDao.getHourlyDistribution()
}
