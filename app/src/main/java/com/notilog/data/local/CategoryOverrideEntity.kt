package com.notilog.data.local

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_overrides")
@Stable
data class CategoryOverrideEntity(
    @PrimaryKey val packageName: String,
    val category: String,
    val updatedAt: Long = System.currentTimeMillis()
)
