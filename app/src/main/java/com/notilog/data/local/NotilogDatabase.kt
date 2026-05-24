package com.notilog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NotificationEntity::class, BlacklistedAppEntity::class, CategoryOverrideEntity::class],
    version = 4,
    exportSchema = false
)
abstract class NotilogDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun blacklistedAppDao(): BlacklistedAppDao
    abstract fun categoryOverrideDao(): CategoryOverrideDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notifications ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE notifications ADD COLUMN deletedAt INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isDeleted ON notifications(isDeleted)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create category_overrides table
        db.execSQL("CREATE TABLE IF NOT EXISTS `category_overrides` (`packageName` TEXT NOT NULL, `category` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
        // Add new indexes to notifications table
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_packageName` ON `notifications` (`packageName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_category` ON `notifications` (`category`)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_isDeleted_postTime` ON `notifications` (`isDeleted`, `postTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_isDeleted_category_postTime` ON `notifications` (`isDeleted`, `category`, `postTime`)")
    }
}
