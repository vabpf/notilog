package com.notilog.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.NotificationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val prefs = context.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)

    private val _autoCleanupEnabled = MutableStateFlow(prefs.getBoolean("auto_cleanup", false))
    val autoCleanupEnabled: StateFlow<Boolean> = _autoCleanupEnabled.asStateFlow()

    private val _retentionDays = MutableStateFlow(prefs.getInt("retention_days", 30))
    val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()

    fun setAutoCleanup(enabled: Boolean) {
        _autoCleanupEnabled.value = enabled
        prefs.edit().putBoolean("auto_cleanup", enabled).apply()
    }

    fun setRetentionDays(days: Int) {
        _retentionDays.value = days
        prefs.edit().putInt("retention_days", days).apply()
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            val days = _retentionDays.value
            val threshold = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
            notificationDao.deleteOldNotifications(threshold)
        }
    }
}
