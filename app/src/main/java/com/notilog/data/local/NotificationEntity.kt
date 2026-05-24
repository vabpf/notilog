package com.notilog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["systemId"]),
        Index(value = ["postTime"]),
        Index(value = ["isDeleted"]),
        Index(value = ["packageName"]),
        Index(value = ["category"]),
        Index(value = ["isDeleted", "postTime"]),
        Index(value = ["isDeleted", "category", "postTime"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemId: Int,
    val tag: String?,
    val packageName: String,
    val appName: String,
    val title: String?,
    val textContent: String?,
    val postTime: Long,
    val isDismissed: Boolean = false,
    val category: String = "Uncategorized",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
