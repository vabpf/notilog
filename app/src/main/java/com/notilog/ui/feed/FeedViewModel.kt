package com.notilog.ui.feed

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppEntity
import com.notilog.data.local.CategoryCountEntry
import com.notilog.data.local.NotificationEntity
import com.notilog.data.local.AppInfoEntry
import com.notilog.data.repository.BlacklistRepository
import com.notilog.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DateRangeOption {
    ALL, TODAY, YASTEDAY, LAST_7_DAYS, LAST_30_DAYS, CUSTOM
}

enum class SortOption {
    NEWEST, OLDEST, APP_NAME
}

data class FilterState(
    val dateRange: DateRangeOption = DateRangeOption.ALL,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedApps: Set<String> = emptySet(),
    val sortOption: SortOption = SortOption.NEWEST
) {
    val hasActiveFilters: Boolean
        get() = dateRange != DateRangeOption.ALL || selectedApps.isNotEmpty() || sortOption != SortOption.NEWEST
}

private data class FilterParams(
    val query: String,
    val category: String,
    val blacklisted: Set<String>,
    val filter: FilterState
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository,
    private val blacklistRepository: BlacklistRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val prefs = context.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow(loadSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private fun loadSearchHistory(): List<String> {
        val historyString = prefs.getString("search_history", "") ?: ""
        return if (historyString.isBlank()) emptyList() else historyString.split("|")
    }

    private fun saveSearchHistory(history: List<String>) {
        prefs.edit().putString("search_history", history.joinToString("|")).apply()
    }

    fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        val current = _searchHistory.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        val newHistory = current.take(5)
        _searchHistory.value = newHistory
        saveSearchHistory(newHistory)
    }

    fun removeFromSearchHistory(query: String) {
        val newHistory = _searchHistory.value.filter { it != query }
        _searchHistory.value = newHistory
        saveSearchHistory(newHistory)
    }

    private val _filterState = MutableStateFlow(loadFilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val hasActiveFilters: StateFlow<Boolean> = _filterState.map { it.hasActiveFilters }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isFilterExpanded = MutableStateFlow(false)

    private val _recentApps = MutableStateFlow<List<AppInfoEntry>>(emptyList())
    val recentApps: StateFlow<List<AppInfoEntry>> = _recentApps.asStateFlow()

    private val _allApps = MutableStateFlow<List<AppInfoEntry>>(emptyList())
    val allApps: StateFlow<List<AppInfoEntry>> = _allApps.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            try {
                notificationRepository.getRecentApps(5).collect { apps ->
                    _recentApps.value = apps
                }
            } catch (e: Exception) {
                _recentApps.value = emptyList()
            }
        }
        viewModelScope.launch {
            try {
                notificationRepository.getAllApps().collect { apps ->
                    _allApps.value = apps
                }
            } catch (e: Exception) {
                _allApps.value = emptyList()
            }
        }
    }

    private fun loadFilterState(): FilterState {
        return try {
            val dateRangeOrdinal = prefs.getInt("filter_date_range", 0)
            val customStartDate = prefs.getLong("filter_custom_start", -1L).takeIf { it > 0 }
            val customEndDate = prefs.getLong("filter_custom_end", -1L).takeIf { it > 0 }
            val selectedAppsSet = prefs.getStringSet("filter_apps", null)
            val selectedApps = selectedAppsSet ?: emptySet()
            val sortOrdinal = prefs.getInt("filter_sort", 0)
            FilterState(
                dateRange = DateRangeOption.entries.getOrElse(dateRangeOrdinal) { DateRangeOption.ALL },
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                selectedApps = selectedApps,
                sortOption = SortOption.entries.getOrElse(sortOrdinal) { SortOption.NEWEST }
            )
        } catch (e: Exception) {
            FilterState()
        }
    }

    private fun saveFilterState(state: FilterState) {
        prefs.edit()
            .putInt("filter_date_range", state.dateRange.ordinal)
            .putLong("filter_custom_start", state.customStartDate ?: -1L)
            .putLong("filter_custom_end", state.customEndDate ?: -1L)
            .putStringSet("filter_apps", state.selectedApps)
            .putInt("filter_sort", state.sortOption.ordinal)
            .apply()
    }

    fun setFilterState(state: FilterState) {
        _filterState.value = state
        saveFilterState(state)
    }

    fun toggleFilterExpanded() {
        isFilterExpanded.value = !isFilterExpanded.value
    }

    fun resetFilters() {
        val newState = FilterState()
        _filterState.value = newState
        saveFilterState(newState)
    }

    private val _selectedCategory = MutableStateFlow(
        savedStateHandle.get<String>("selectedCategory") ?: "All"
    )
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blacklistedPackages: StateFlow<Set<String>> = blacklistRepository.getAll()
        .map { list -> list.map { it.packageName }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val categoryCounts: StateFlow<List<CategoryCountEntry>> = notificationRepository.getCategoryNotificationCounts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notifications: StateFlow<List<NotificationEntity>> = combine(
        _searchQuery.debounce(300),
        _selectedCategory,
        _filterState
    ) { query, category, filter ->
        FilterParams(query, category, emptySet(), filter)
    }.flatMapLatest { params ->
        val baseFlow = if (params.category == "All") {
            if (params.query.isBlank()) {
                notificationRepository.getAllNotifications()
            } else {
                notificationRepository.searchAllNotifications(params.query)
            }
        } else {
            if (params.query.isBlank()) {
                notificationRepository.getNotificationsByCategory(params.category)
            } else {
                notificationRepository.searchAllNotifications(params.query)
            }
        }
        val now = System.currentTimeMillis()
        val filter = params.filter
        val queryTerms = if (params.query.isNotBlank()) {
            params.query.trim().lowercase().normalize().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else emptyList()

        baseFlow.map { list ->
            list.filter { notification ->
                if (queryTerms.isEmpty()) true
                else {
                    val titleText = (notification.title ?: "").lowercase().normalize()
                    val contentText = (notification.textContent ?: "").lowercase().normalize()
                    val searchText = "$titleText $contentText".trim()
                    queryTerms.all { term -> fuzzyMatch(term, searchText) }
                }
            }.filter { notification ->
                val postTime = notification.postTime
                when (filter.dateRange) {
                    DateRangeOption.ALL -> true
                    DateRangeOption.TODAY -> postTime >= now - 86400000
                    DateRangeOption.YASTEDAY -> postTime >= now - 172800000 && postTime < now - 86400000
                    DateRangeOption.LAST_7_DAYS -> postTime >= now - 604800000
                    DateRangeOption.LAST_30_DAYS -> postTime >= now - 2592000000L
                    DateRangeOption.CUSTOM -> {
                        val start = filter.customStartDate ?: 0L
                        val end = filter.customEndDate ?: Long.MAX_VALUE
                        postTime >= start && postTime <= end
                    }
                }
            }.filter { notification ->
                if (filter.selectedApps.isEmpty()) true
                else notification.packageName in filter.selectedApps
            }.let { filteredList ->
                when (filter.sortOption) {
                    SortOption.NEWEST -> filteredList.sortedByDescending { it.postTime }
                    SortOption.OLDEST -> filteredList.sortedBy { it.postTime }
                    SortOption.APP_NAME -> filteredList.sortedBy { it.appName.lowercase() }
                }
            }
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
            blacklistRepository.insert(BlacklistedAppEntity(packageName))
        }
    }

    fun blacklistSelected() {
        viewModelScope.launch {
            val selectedNotifications = notifications.value.filter { it.id in _selectedIds.value }
            val packages = selectedNotifications.map { it.packageName }.distinct()
            packages.forEach { pkg ->
                blacklistRepository.insert(BlacklistedAppEntity(pkg))
            }
            clearSelection()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.softDeleteById(id, System.currentTimeMillis())
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            notificationRepository.softDeleteByIds(_selectedIds.value.toList(), System.currentTimeMillis())
            clearSelection()
        }
    }

    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            notificationRepository.restoreFromTrash(id)
        }
    }

    fun permanentDeleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteById(id)
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
        if (term.length > word.length) return false
        if (term.isEmpty()) return true
        var matchedChars = 0
        for (char in term) {
            val idx = word.indexOf(char, matchedChars)
            if (idx == -1) return false
            matchedChars = idx + 1
        }
        return term.length.toFloat() / word.length >= threshold
    }

    private fun String.normalize(): String {
        val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}
