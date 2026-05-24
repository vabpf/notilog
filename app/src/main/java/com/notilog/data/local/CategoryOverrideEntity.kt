package com.notilog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_overrides")
data class CategoryOverrideEntity(
    @PrimaryKey val packageName: String,
    val category: String,
    val updatedAt: Long = System.currentTimeMillis()
)
