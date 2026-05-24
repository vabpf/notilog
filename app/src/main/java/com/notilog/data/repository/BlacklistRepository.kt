package com.notilog.data.repository

import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.BlacklistedAppEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlacklistRepository @Inject constructor(
    private val blacklistedAppDao: BlacklistedAppDao
) {
    suspend fun insert(app: BlacklistedAppEntity) = blacklistedAppDao.insert(app)

    suspend fun delete(packageName: String) = blacklistedAppDao.delete(packageName)

    fun getAll(): Flow<List<BlacklistedAppEntity>> = blacklistedAppDao.getAll()

    suspend fun isBlacklisted(packageName: String): Boolean = blacklistedAppDao.isBlacklisted(packageName)
}
