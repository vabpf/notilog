package com.notilog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklisted_apps")
data class BlacklistedAppEntity(
    @PrimaryKey val packageName: String
)
