package com.notilog.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.AppCountEntry
import com.notilog.data.local.CategoryCountEntry
import com.notilog.data.local.DailyCountEntry
import com.notilog.data.local.NotificationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class InsightsSummary(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val weekCount: Int = 0
)

data class CategoryInsight(
    val category: String,
    val count: Int,
    val percentage: Float
)

data class AppInsight(
    val packageName: String,
    val appName: String,
    val count: Int
)

data class DailyInsight(
    val date: String,
    val count: Int
)

data class InsightsState(
    val summary: InsightsSummary = InsightsSummary(),
    val categoryInsights: List<CategoryInsight> = emptyList(),
    val topApps: List<AppInsight> = emptyList(),
    val dailyInsights: List<DailyInsight> = emptyList()
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _insightsState = MutableStateFlow(InsightsState())
    val insightsState: StateFlow<InsightsState> = _insightsState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startOfWeek = calendar.timeInMillis
                
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = calendar.timeInMillis

                // Combine first 3 flows
                val countsFlow = combine(
                    notificationDao.getTotalNotificationCount(),
                    notificationDao.getNotificationCountSince(startOfToday),
                    notificationDao.getNotificationCountSince(startOfWeek)
                ) { total, today, week ->
                    Triple(total, today, week)
                }

                // Combine next 3 flows
                val dataFlow = combine(
                    notificationDao.getCategoryBreakdown(),
                    notificationDao.getTopApps(5),
                    notificationDao.getDailyCounts(sevenDaysAgo)
                ) { categories, topApps, daily ->
                    Pair(Pair(categories, topApps), daily)
                }

                // Final combine
                combine(countsFlow, dataFlow) { counts, data ->
                    val (total, today, week) = counts
                    val (categories, topApps) = data.first
                    val daily = data.second
                    
                    val maxCount = categories.maxOfOrNull { it.count } ?: 1
                    InsightsState(
                        summary = InsightsSummary(
                            totalCount = total,
                            todayCount = today,
                            weekCount = week
                        ),
                        categoryInsights = categories.map { cat ->
                            CategoryInsight(
                                category = cat.category,
                                count = cat.count,
                                percentage = (cat.count.toFloat() / maxCount) * 100
                            )
                        },
                        topApps = topApps.map { app ->
                            AppInsight(
                                packageName = app.packageName,
                                appName = app.appName,
                                count = app.count
                            )
                        },
                        dailyInsights = daily.reversed().map { dailyCount ->
                            DailyInsight(
                                date = dailyCount.date,
                                count = dailyCount.count
                            )
                        }
                    )
                }.collect { state ->
                    _insightsState.value = state
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        _isLoading.value = true
        loadInsights()
    }
}