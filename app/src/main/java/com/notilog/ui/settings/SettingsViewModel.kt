package com.notilog.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.notilog.data.local.NotificationEntity
import com.notilog.data.repository.NotificationRepository
import com.notilog.ui.theme.ThemeMode
import com.notilog.ui.theme.ThemePreferences
import com.notilog.worker.BackupWorker
import com.notilog.worker.CleanupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)

    private val _autoCleanupEnabled = MutableStateFlow(prefs.getBoolean("auto_cleanup", false))
    val autoCleanupEnabled: StateFlow<Boolean> = _autoCleanupEnabled.asStateFlow()

    private val _retentionDays = MutableStateFlow(prefs.getInt("retention_days", 30))
    val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(checkNotificationAccess())
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()
    private val _themeMode = MutableStateFlow(ThemePreferences.getThemeMode(prefs))
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    private val _backupEnabled = MutableStateFlow(prefs.getBoolean("backup_enabled", false))
    val backupEnabled: StateFlow<Boolean> = _backupEnabled.asStateFlow()
    private val _backupProvider = MutableStateFlow(
        BackupProvider.fromStoredValue(
            prefs.getString("backup_provider", BackupProvider.GOOGLE_DRIVE.value)
        )
    )
    val backupProvider: StateFlow<BackupProvider> = _backupProvider.asStateFlow()
    private val _backupFrequency = MutableStateFlow(
        BackupFrequency.fromStoredValue(
            prefs.getString("backup_frequency", BackupFrequency.DAILY.value)
        )
    )
    val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency.asStateFlow()
    private val _backupFolderUri = MutableStateFlow(prefs.getString("backup_folder_uri", null))
    val backupFolderUri: StateFlow<String?> = _backupFolderUri.asStateFlow()

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

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        ThemePreferences.setThemeMode(prefs, mode)
    }

    fun setBackupEnabled(enabled: Boolean): Boolean {
        if (enabled && _backupFolderUri.value.isNullOrBlank()) {
            _backupEnabled.value = false
            prefs.edit().putBoolean("backup_enabled", false).apply()
            cancelAutoBackup()
            return false
        }
        _backupEnabled.value = enabled
        prefs.edit().putBoolean("backup_enabled", enabled).apply()
        if (enabled) scheduleAutoBackup() else cancelAutoBackup()
        return true
    }

    fun setBackupProvider(provider: BackupProvider) {
        _backupProvider.value = provider
        prefs.edit().putString("backup_provider", provider.value).apply()
    }

    fun isBackupProviderAuthenticated(provider: BackupProvider): Boolean {
        if (!provider.requiresAuth) return true
        return prefs.getBoolean(provider.authKey, false)
    }

    fun setBackupProviderAuthenticated(provider: BackupProvider, authenticated: Boolean) {
        if (!provider.requiresAuth) return
        prefs.edit().putBoolean(provider.authKey, authenticated).apply()
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        _backupFrequency.value = frequency
        prefs.edit().putString("backup_frequency", frequency.value).apply()
        if (_backupEnabled.value) {
            scheduleAutoBackup()
        }
    }

    fun setBackupFolderUri(uri: Uri?) {
        val raw = uri?.toString()
        _backupFolderUri.value = raw
        prefs.edit().putString("backup_folder_uri", raw).apply()
        if (uri != null) {
            setBackupProviderAuthenticated(_backupProvider.value, true)
        }
        if (_backupEnabled.value) {
            scheduleAutoBackup()
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

    private fun scheduleAutoBackup() {
        val folderUri = _backupFolderUri.value
        if (!shouldScheduleAutoBackup(_backupEnabled.value, folderUri)) return
        val frequency = _backupFrequency.value
        val provider = _backupProvider.value
        val networkType = if (provider.requiresAuth) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED
        val request = PeriodicWorkRequestBuilder<BackupWorker>(
            frequency.intervalDays,
            TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .setRequiresBatteryNotLow(true)
                .build()
        ).setInputData(
            workDataOf(
                BackupWorker.KEY_BACKUP_FOLDER_URI to folderUri
            )
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "auto_backup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelAutoBackup() {
        workManager.cancelUniqueWork("auto_backup_work")
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            val days = _retentionDays.value
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            notificationRepository.deleteOldNotifications(threshold)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }

    suspend fun exportDataStreaming(outputStream: java.io.OutputStream) = withContext(Dispatchers.IO) {
        val bos = outputStream
        java.util.zip.GZIPOutputStream(bos).bufferedWriter().use { writer ->
            val jsonWriter = com.google.gson.stream.JsonWriter(writer)
            jsonWriter.setIndent("  ")
            jsonWriter.beginArray()
            
            val cursor = notificationRepository.getAllNotificationsCursor()
            cursor.use { c ->
                val idIdx = c.getColumnIndex("id")
                val sysIdIdx = c.getColumnIndex("systemId")
                val tagIdx = c.getColumnIndex("tag")
                val pkgIdx = c.getColumnIndex("packageName")
                val appIdx = c.getColumnIndex("appName")
                val titleIdx = c.getColumnIndex("title")
                val textIdx = c.getColumnIndex("textContent")
                val timeIdx = c.getColumnIndex("postTime")
                val dismissedIdx = c.getColumnIndex("isDismissed")
                val categoryIdx = c.getColumnIndex("category")
                val deletedIdx = c.getColumnIndex("isDeleted")
                val deletedAtIdx = c.getColumnIndex("deletedAt")

                while (c.moveToNext()) {
                    jsonWriter.beginObject()
                    if (idIdx != -1) jsonWriter.name("id").value(c.getLong(idIdx))
                    if (sysIdIdx != -1) jsonWriter.name("systemId").value(c.getInt(sysIdIdx))
                    if (tagIdx != -1) jsonWriter.name("tag").value(c.getString(tagIdx))
                    if (pkgIdx != -1) jsonWriter.name("packageName").value(c.getString(pkgIdx))
                    if (appIdx != -1) jsonWriter.name("appName").value(c.getString(appIdx))
                    if (titleIdx != -1) jsonWriter.name("title").value(c.getString(titleIdx))
                    if (textIdx != -1) jsonWriter.name("textContent").value(c.getString(textIdx))
                    if (timeIdx != -1) jsonWriter.name("postTime").value(c.getLong(timeIdx))
                    if (dismissedIdx != -1) jsonWriter.name("isDismissed").value(c.getInt(dismissedIdx) == 1)
                    if (categoryIdx != -1) jsonWriter.name("category").value(c.getString(categoryIdx))
                    if (deletedIdx != -1) jsonWriter.name("isDeleted").value(c.getInt(deletedIdx) == 1)
                    if (deletedAtIdx != -1 && !c.isNull(deletedAtIdx)) jsonWriter.name("deletedAt").value(c.getLong(deletedAtIdx))
                    jsonWriter.endObject()
                }
            }
            
            jsonWriter.endArray()
            jsonWriter.close()
        }
    }

    suspend fun importData(stream: InputStream): Int = withContext(Dispatchers.IO) {
        val bis = stream.buffered()
        bis.mark(10)
        val header = bis.read() or (bis.read() shl 8)
        bis.reset()

        val content = if (header == GZIPInputStream.GZIP_MAGIC) {
            GZIPInputStream(bis).bufferedReader().use { it.readText() }
        } else {
            bis.bufferedReader().use { it.readText() }
        }

        val parsedNotifications = NotificationImportParser.parse(content)
        parsedNotifications.forEach { notification ->
            notificationRepository.insert(notification)
        }
        parsedNotifications.size
    }
}

enum class BackupProvider(val value: String, val label: String, val requiresAuth: Boolean) {
    LOCAL("local", "Local folder", false),
    GOOGLE_DRIVE("google_drive", "Google Drive", true),
    ONEDRIVE("onedrive", "OneDrive", true);

    companion object {
        fun fromStoredValue(value: String?): BackupProvider = entries.firstOrNull { it.value == value } ?: GOOGLE_DRIVE
    }

    val authKey: String
        get() = "backup_auth_${value}"
}

enum class BackupFrequency(val value: String, val label: String, val intervalDays: Long) {
    DAILY("daily", "Daily", 1),
    WEEKLY("weekly", "Weekly", 7);

    companion object {
        fun fromStoredValue(value: String?): BackupFrequency = entries.firstOrNull { it.value == value } ?: DAILY
    }
}

internal fun shouldScheduleAutoBackup(enabled: Boolean, folderUri: String?): Boolean {
    return enabled && !folderUri.isNullOrBlank()
}

internal object NotificationImportParser {
    fun parse(content: String, nowMillis: Long = System.currentTimeMillis()): List<NotificationEntity> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()
        return if (trimmed.startsWith("[")) parseJson(trimmed, nowMillis) else parseCsv(trimmed, nowMillis)
    }

    private fun parseCsv(content: String, nowMillis: Long): List<NotificationEntity> {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val header = parseCsvLine(lines.first())
        val defaultHeader = listOf(
            "systemId", "tag", "packageName", "appName", "title",
            "textContent", "postTime", "isDismissed", "category", "isDeleted", "deletedAt"
        )
        val columns = if (header.contains("packageName")) header else defaultHeader
        val startIndex = if (header.contains("packageName")) 1 else 0

        return lines.drop(startIndex).map { line ->
            val values = parseCsvLine(line)
            val data = columns.associateWith { column ->
                values.getOrNull(columns.indexOf(column)).orEmpty()
            }
            NotificationEntity(
                id = 0,
                systemId = data["systemId"]?.toIntOrNull() ?: 0,
                tag = data["tag"].toNullable(),
                packageName = data["packageName"].toNullable() ?: "unknown.package",
                appName = data["appName"].toNullable() ?: "Unknown App",
                title = data["title"].toNullable(),
                textContent = data["textContent"].toNullable(),
                postTime = data["postTime"]?.toLongOrNull() ?: nowMillis,
                isDismissed = data["isDismissed"]?.toBooleanStrictOrNullCompat() ?: false,
                category = data["category"].toNullable() ?: "Uncategorized",
                isDeleted = data["isDeleted"]?.toBooleanStrictOrNullCompat() ?: false,
                deletedAt = data["deletedAt"]?.toLongOrNull()
            )
        }
    }

    private fun parseJson(content: String, nowMillis: Long): List<NotificationEntity> {
        try {
            val jsonArray = JsonParser.parseString(content).asJsonArray
            return jsonArray.mapNotNull { element ->
                if (!element.isJsonObject) return@mapNotNull null
                val obj = element.asJsonObject
                NotificationEntity(
                    id = 0,
                    systemId = obj.get("systemId")?.asInt ?: 0,
                    tag = obj.get("tag")?.takeUnless { it.isJsonNull }?.asString.toNullable(),
                    packageName = obj.get("packageName")?.takeUnless { it.isJsonNull }?.asString ?: "unknown.package",
                    appName = obj.get("appName")?.takeUnless { it.isJsonNull }?.asString ?: "Unknown App",
                    title = obj.get("title")?.takeUnless { it.isJsonNull }?.asString.toNullable(),
                    textContent = obj.get("textContent")?.takeUnless { it.isJsonNull }?.asString.toNullable(),
                    postTime = obj.get("postTime")?.takeUnless { it.isJsonNull }?.asLong ?: nowMillis,
                    isDismissed = obj.get("isDismissed")?.takeUnless { it.isJsonNull }?.asBoolean ?: false,
                    category = obj.get("category")?.takeUnless { it.isJsonNull }?.asString ?: "Uncategorized",
                    isDeleted = obj.get("isDeleted")?.takeUnless { it.isJsonNull }?.asBoolean ?: false,
                    deletedAt = obj.get("deletedAt")?.takeUnless { it.isJsonNull }?.asLong
                )
            }
        } catch (error: JsonSyntaxException) {
            throw IllegalArgumentException("Invalid JSON import format", error)
        } catch (error: IllegalStateException) {
            throw IllegalArgumentException("Invalid JSON import format", error)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }
        result += current.toString()
        return result
    }

    private fun String?.toNullable(): String? = this?.takeIf { it.isNotBlank() && it != "null" }

    private fun String.toBooleanStrictOrNullCompat(): Boolean? = when (lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }
}
