package com.notilog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notilog.data.local.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("auto_cleanup", false)
            if (!enabled) return@withContext Result.success()

            val days = prefs.getInt("retention_days", 30)
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            
            notificationDao.deleteOldNotifications(threshold)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
