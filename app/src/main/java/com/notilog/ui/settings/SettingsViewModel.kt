package com.notilog.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.notilog.data.local.NotificationDao
import com.notilog.worker.BackupWorker
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

    private val _googleAccount = MutableStateFlow<GoogleSignInAccount?>(GoogleSignIn.getLastSignedInAccount(context))
    val googleAccount: StateFlow<GoogleSignInAccount?> = _googleAccount.asStateFlow()

    fun getGoogleSignInClient() = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    )

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        _googleAccount.value = account
        if (account != null) {
            scheduleBackup()
        }
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

    fun scheduleBackup() {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresCharging(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "backup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runBackupNow() {
        val request = OneTimeWorkRequestBuilder<BackupWorker>().build()
        workManager.enqueue(request)
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            val days = _retentionDays.value
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            notificationDao.deleteOldNotifications(threshold)
        }
    }
}
