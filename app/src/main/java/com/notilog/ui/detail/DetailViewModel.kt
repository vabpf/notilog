package com.notilog.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val blacklistedAppDao: BlacklistedAppDao
) : ViewModel() {

    fun getVersions(systemId: Int, tag: String?): Flow<List<NotificationEntity>> {
        return notificationDao.getVersionsBySystemId(systemId, tag)
    }

    fun isBlacklisted(packageName: String): Flow<Boolean> {
        return blacklistedAppDao.getAll().map { list -> list.any { it.packageName == packageName } }
    }

    fun toggleBlacklist(packageName: String) {
        viewModelScope.launch {
            if (blacklistedAppDao.isBlacklisted(packageName)) {
                blacklistedAppDao.delete(packageName)
            } else {
                blacklistedAppDao.insert(BlacklistedAppEntity(packageName))
            }
        }
    }

    fun deleteAllVersions(systemId: Int, tag: String?) {
        viewModelScope.launch {
            // Room doesn't have a direct deleteBySystemId that isn't a query
            // We can add it to DAO or use a query
            notificationDao.deleteBySystemId(systemId, tag)
        }
    }
}
