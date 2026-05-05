package com.notilog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NotificationEntity::class, BlacklistedAppEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NotilogDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun blacklistedAppDao(): BlacklistedAppDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notifications ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE notifications ADD COLUMN deletedAt INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isDeleted ON notifications(isDeleted)")
    }
}
