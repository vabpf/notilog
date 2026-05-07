package com.notilog.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.notilog.data.local.NotificationDao
import com.notilog.worker.CleanupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val prefs = context.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)

    private val _autoCleanupEnabled = MutableStateFlow(prefs.getBoolean("auto_cleanup", false))
    val autoCleanupEnabled: StateFlow<Boolean> = _autoCleanupEnabled.asStateFlow()

    private val _retentionDays = MutableStateFlow(prefs.getInt("retention_days", 30))
    val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(checkNotificationAccess())
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private fun checkNotificationAccess(): Boolean {
        val pkgName = context.packageName
        val flat = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(pkgName) == true
    }

    fun refreshNotificationAccess() {
        _hasNotificationAccess.value = checkNotificationAccess()
    }

    fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun setAutoCleanup(enabled: Boolean) {
        _autoCleanupEnabled.value = enabled
        prefs.edit().putBoolean("auto_cleanup", enabled).apply()
        if (enabled) {
            scheduleCleanup()
        } else {
            cancelCleanup()
        }
    }

    fun setRetentionDays(days: Int) {
        _retentionDays.value = days
        prefs.edit().putInt("retention_days", days).apply()
        if (_autoCleanupEnabled.value) {
            scheduleCleanup()
        }
    }

    private fun scheduleCleanup() {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "cleanup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelCleanup() {
        workManager.cancelUniqueWork("cleanup_work")
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            val days = _retentionDays.value
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            notificationDao.deleteOldNotifications(threshold)
        }
    }
}