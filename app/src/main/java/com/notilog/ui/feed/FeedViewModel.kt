package com.notilog.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val blacklistedAppDao: BlacklistedAppDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blacklistedPackages: StateFlow<Set<String>> = blacklistedAppDao.getAll()
        .map { list -> list.map { it.packageName }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = combine(
        _searchQuery,
        _selectedCategory,
        blacklistedPackages
    ) { query, category, blacklisted ->
        Triple(query, category, blacklisted)
    }.flatMapLatest { (query, category, blacklisted) ->
        val flow = if (query.isBlank()) {
            notificationDao.getAllNotifications()
        } else {
            notificationDao.searchAllNotifications(query)
        }
        flow.map { list ->
            list.filter { it.packageName !in blacklisted }
                .filter { category == "All" || it.category == category }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (id in current) current - id else current + id
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun blacklistPackage(packageName: String) {
        viewModelScope.launch {
            blacklistedAppDao.insert(BlacklistedAppEntity(packageName))
        }
    }

    fun blacklistSelected() {
        viewModelScope.launch {
            val selectedNotifications = notifications.value.filter { it.id in _selectedIds.value }
            val packages = selectedNotifications.map { it.packageName }.distinct()
            packages.forEach { pkg ->
                blacklistedAppDao.insert(BlacklistedAppEntity(pkg))
            }
            clearSelection()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationDao.deleteById(id)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                notificationDao.deleteById(id)
            }
            clearSelection()
        }
    }
}
