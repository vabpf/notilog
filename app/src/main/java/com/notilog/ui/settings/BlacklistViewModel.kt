package com.notilog.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.AppInfoEntry
import com.notilog.data.repository.BlacklistRepository
import com.notilog.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val blacklistRepository: BlacklistRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val blacklistedAppsWithNames: Flow<List<AppInfoEntry>> = combine(
        blacklistRepository.getAll(),
        notificationRepository.getAllApps()
    ) { blacklisted, allApps ->
        val appNameMap = allApps.associate { it.packageName to it.appName }
        blacklisted.map { entity ->
            AppInfoEntry(
                packageName = entity.packageName,
                appName = appNameMap[entity.packageName] ?: entity.packageName
            )
        }
    }

    val blacklistedApps: Flow<List<BlacklistedAppEntity>> = blacklistRepository.getAll()

    fun addToBlacklist(packageName: String) {
        viewModelScope.launch {
            blacklistRepository.insert(BlacklistedAppEntity(packageName))
        }
    }

    fun removeFromBlacklist(packageName: String) {
        viewModelScope.launch {
            blacklistRepository.delete(packageName)
        }
    }
}
