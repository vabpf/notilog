package com.notilog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistedAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: BlacklistedAppEntity)

    @Query("DELETE FROM blacklisted_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("SELECT * FROM blacklisted_apps")
    fun getAll(): Flow<List<BlacklistedAppEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blacklisted_apps WHERE packageName = :packageName)")
    suspend fun isBlacklisted(packageName: String): Boolean
}
