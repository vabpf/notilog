package com.notilog.ui.insights

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.AppCountEntry
import com.notilog.data.local.CategoryCountEntry
import com.notilog.data.local.DailyCountEntry
import com.notilog.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Stable
data class InsightsSummary(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val weekCount: Int = 0
)

@Stable
data class CategoryInsight(
    val category: String,
    val count: Int,
    val percentage: Float
)

@Stable
data class AppInsight(
    val packageName: String,
    val appName: String,
    val count: Int
)

@Stable
data class DailyInsight(
    val date: String,
    val count: Int
)

@Stable
data class HourlyInsight(
    val hour: Int,
    val count: Int
)

@Stable
data class InsightsState(
    val summary: InsightsSummary = InsightsSummary(),
    val categoryInsights: List<CategoryInsight> = emptyList(),
    val topApps: List<AppInsight> = emptyList(),
    val dailyInsights: List<DailyInsight> = emptyList(),
    val hourlyInsights: List<HourlyInsight> = emptyList(),
    val mostActiveHour: Int? = null,
    val disruptionScore: Int = 0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
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
                    notificationRepository.getTotalNotificationCount(),
                    notificationRepository.getNotificationCountSince(startOfToday),
                    notificationRepository.getNotificationCountSince(startOfWeek)
                ) { total, today, week ->
                    Triple(total, today, week)
                }

                // Combine next 3 flows
                val dataFlow = combine(
                    notificationRepository.getCategoryBreakdown(),
                    notificationRepository.getTopApps(5),
                    notificationRepository.getDailyCounts(sevenDaysAgo),
                    notificationRepository.getHourlyDistribution()
                ) { categories, topApps, daily, hourly ->
                    Triple(Pair(categories, topApps), daily, hourly)
                }

                // Final combine
                combine(countsFlow, dataFlow) { counts, data ->
                    val (total, today, week) = counts
                    val (categories, topApps) = data.first
                    val daily = data.second
                    val hourly = data.third
                    
                    val maxCount = categories.maxOfOrNull { it.count } ?: 1
                    
                    val hourlyList = hourly.map { HourlyInsight(it.hour.toInt(), it.count) }
                    val mostActive = hourlyList.maxByOrNull { it.count }?.hour
                    
                    // Disruption score: Avg notifications per hour during peak hours (top 3 hours)
                    val peakAvg = if (hourlyList.isNotEmpty()) {
                        hourlyList.sortedByDescending { it.count }.take(3).map { it.count }.average().toInt()
                    } else 0

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
                        },
                        hourlyInsights = hourlyList,
                        mostActiveHour = mostActive,
                        disruptionScore = peakAvg
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