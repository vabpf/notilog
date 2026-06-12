package com.notilog.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.NotificationEntity
import com.notilog.data.repository.BlacklistRepository
import com.notilog.data.repository.CategoryRepository
import com.notilog.data.repository.NotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val blacklistRepository: BlacklistRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    fun getCategory(packageName: String): String {
        return categoryRepository.getCategoryForPackage(packageName)
    }

    fun updateCategory(packageName: String, newCategory: String) {
        viewModelScope.launch {
            coroutineScope {
                val override = async { categoryRepository.updateOverride(packageName, newCategory) }
                val records = async { notificationRepository.updateCategoryForPackage(packageName, newCategory) }
                override.await()
                records.await()
            }
        }
    }

    fun getVersions(packageName: String, systemId: Int, tag: String?): Flow<List<NotificationEntity>> {
        return notificationRepository.getVersionsBySystemId(packageName, systemId, tag)
    }

    fun isBlacklisted(packageName: String): Flow<Boolean> {
        return blacklistRepository.getAll().map { list -> list.any { it.packageName == packageName } }
    }

    fun toggleBlacklist(packageName: String) {
        viewModelScope.launch {
            if (blacklistRepository.isBlacklisted(packageName)) {
                blacklistRepository.delete(packageName)
            } else {
                blacklistRepository.insert(BlacklistedAppEntity(packageName))
            }
        }
    }

    fun deleteAllVersions(packageName: String, systemId: Int, tag: String?) {
        viewModelScope.launch {
            notificationRepository.softDeleteBySystemId(packageName, systemId, tag, System.currentTimeMillis())
        }
    }

    fun deleteVersion(id: Long) {
        viewModelScope.launch {
            notificationRepository.softDeleteById(id, System.currentTimeMillis())
        }
    }
}
