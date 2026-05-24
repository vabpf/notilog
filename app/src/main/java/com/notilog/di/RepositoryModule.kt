package com.notilog.di

import android.content.Context
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.CategoryOverrideDao
import com.notilog.data.local.NotificationDao
import com.notilog.data.repository.BlacklistRepository
import com.notilog.data.repository.CategoryRepository
import com.notilog.data.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(
        @ApplicationContext context: Context,
        categoryOverrideDao: CategoryOverrideDao
    ): CategoryRepository {
        return CategoryRepository(context, categoryOverrideDao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao
    ): NotificationRepository {
        return NotificationRepository(notificationDao)
    }

    @Provides
    @Singleton
    fun provideBlacklistRepository(
        blacklistedAppDao: BlacklistedAppDao
    ): BlacklistRepository {
        return BlacklistRepository(blacklistedAppDao)
    }
}
