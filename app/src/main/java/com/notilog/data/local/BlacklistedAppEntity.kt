package com.notilog.data.local

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklisted_apps")
@Stable
data class BlacklistedAppEntity(
    @PrimaryKey val packageName: String
)
