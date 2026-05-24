package com.notilog.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.LruCache
import com.notilog.data.local.NotificationEntity
import com.notilog.data.repository.CategoryRepository
import com.notilog.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotilogListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // In-memory deduplication cache of the last 20 notifications
    private val deduplicationCache = LruCache<String, Boolean>(20)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            return
        }

        val packageName = sbn.packageName
        
        // Fast in-memory deduplication check
        val cacheKey = "$packageName:${sbn.id}:${sbn.tag ?: ""}:$title:$text"
        if (deduplicationCache.get(cacheKey) == true) {
            return
        }

        val appName = try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }

        scope.launch {
            // Check cache inside coroutine again to handle race conditions
            if (deduplicationCache.get(cacheKey) == true) {
                return@launch
            }

            // Deduplication logic with DB fallback
            val latest = notificationRepository.getLatestBySystemId(packageName, sbn.id, sbn.tag)
            if (latest != null && latest.title == title && latest.textContent == text) {
                // Populate cache for subsequent checks
                deduplicationCache.put(cacheKey, true)
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
            notificationRepository.insert(entity)
            deduplicationCache.put(cacheKey, true)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        scope.launch {
            notificationRepository.markAsDismissed(sbn.packageName, sbn.id, sbn.tag)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
