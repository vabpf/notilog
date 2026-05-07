package com.notilog.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.CategoryCountEntry
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val blacklistedAppDao: BlacklistedAppDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(
        savedStateHandle.get<String>("selectedCategory") ?: "All"
    )
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blacklistedPackages: StateFlow<Set<String>> = blacklistedAppDao.getAll()
        .map { list -> list.map { it.packageName }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val categoryCounts: StateFlow<List<CategoryCountEntry>> = notificationDao.getCategoryNotificationCounts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = combine(
        _searchQuery,
        _selectedCategory,
        blacklistedPackages
    ) { query, category, blacklisted ->
        Triple(query, category, blacklisted)
    }.flatMapLatest { (query, category, blacklisted) ->
        val baseFlow = if (category == "All") {
            if (query.isBlank()) {
                notificationDao.getAllNotifications()
            } else {
                notificationDao.searchAllNotifications(query)
            }
        } else {
            if (query.isBlank()) {
                notificationDao.getNotificationsByCategory(category)
            } else {
                notificationDao.searchAllNotifications(query)
            }
        }
        baseFlow.map { list ->
            list.filter { notification ->
                if (query.isBlank()) true
                else {
                    val titleText = notification.title?.lowercase() ?: ""
                    val contentText = notification.textContent?.lowercase() ?: ""
                    val searchText = "$titleText $contentText".trim()
                    val terms = query.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
                    terms.all { term -> fuzzyMatch(term, searchText) }
                }
            }.filter { it.packageName !in blacklisted }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        savedStateHandle["selectedCategory"] = category
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
            notificationDao.softDeleteById(id, System.currentTimeMillis())
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            notificationDao.softDeleteByIds(_selectedIds.value.toList(), System.currentTimeMillis())
            clearSelection()
        }
    }

    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            notificationDao.restoreFromTrash(id)
        }
    }

    fun permanentDeleteNotification(id: Long) {
        viewModelScope.launch {
            notificationDao.deleteById(id)
        }
    }

    private fun fuzzyMatch(term: String, text: String): Boolean {
        if (text.contains(term)) return true
        val textWords = text.split("\\s+".toRegex())
        return textWords.any { word ->
            fuzzySubstringMatch(term, word)
        }
    }

    private fun fuzzySubstringMatch(term: String, word: String, threshold: Float = 0.6f): Boolean {
        if (term.length > word.length) return fuzzySubstringMatch(word, term, threshold)
        if (term.isEmpty()) return true
        var matchedChars = 0
        for (char in term) {
            val idx = word.indexOf(char, matchedChars)
            if (idx == -1) return false
            matchedChars = idx + 1
        }
        return term.length.toFloat() / word.length >= threshold
    }
}
