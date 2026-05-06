package com.notilog.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.MIGRATION_1_2
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotilogDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotilogDatabase {
        return try {
            Room.databaseBuilder(
                context,
                NotilogDatabase::class.java,
                "notilog.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Error creating database, falling back", e)
            Room.databaseBuilder(
                context,
                NotilogDatabase::class.java,
                "notilog.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    @Provides
    fun provideNotificationDao(db: NotilogDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideBlacklistedAppDao(db: NotilogDatabase): BlacklistedAppDao = db.blacklistedAppDao()
}
