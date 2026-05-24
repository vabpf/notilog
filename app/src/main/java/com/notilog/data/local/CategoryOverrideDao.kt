package com.notilog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(override: CategoryOverrideEntity)

    @Query("SELECT * FROM category_overrides")
    fun getAll(): Flow<List<CategoryOverrideEntity>>

    @Query("SELECT * FROM category_overrides WHERE packageName = :packageName LIMIT 1")
    suspend fun getForPackage(packageName: String): CategoryOverrideEntity?

    @Query("DELETE FROM category_overrides WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
