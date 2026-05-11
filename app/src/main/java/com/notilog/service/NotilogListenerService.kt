package com.notilog.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import com.notilog.data.repository.CategoryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotilogListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            return
        }

        val packageName = sbn.packageName
        val appName = try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }

        scope.launch {
            // Deduplication logic
            val latest = notificationDao.getLatestBySystemId(packageName, sbn.id, sbn.tag)
            if (latest != null && latest.title == title && latest.textContent == text) {
                // Ignore exact duplicates
                return@launch
            }

            val category = categoryRepository.getCategoryForPackage(packageName)

            val entity = NotificationEntity(
                systemId = sbn.id,
                tag = sbn.tag,
                packageName = packageName,
                appName = appName,
                title = title,
                textContent = text,
                postTime = sbn.postTime,
                category = category
            )
            notificationDao.insert(entity)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        scope.launch {
            notificationDao.markAsDismissed(sbn.packageName, sbn.id, sbn.tag)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
