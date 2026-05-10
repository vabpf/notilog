package com.notilog.ui.groups

import androidx.lifecycle.ViewModel
import com.notilog.data.local.NotificationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AppNotificationCount(val packageName: String, val appName: String, val count: Int)
data class CategoryCount(val category: String, val count: Int)

private data class GroupedStats(
    val categories: List<CategoryCount>,
    val topApps: List<AppNotificationCount>
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val groupedStats: Flow<GroupedStats> = notificationDao.getAllNotifications().map { list ->
        val categories = list.groupBy { it.category }
            .map { (cat, items) -> CategoryCount(cat, items.size) }
            .sortedByDescending { it.count }
        val topApps = list.groupBy { it.packageName }
            .mapNotNull { (pkg, items) ->
                val firstItem = items.firstOrNull() ?: return@mapNotNull null
                AppNotificationCount(pkg, firstItem.appName, items.size)
            }
            .sortedByDescending { it.count }
            .take(10)
        GroupedStats(categories, topApps)
    }

    val categoryStats: Flow<List<CategoryCount>> = groupedStats.map { it.categories }

    val topApps: Flow<List<AppNotificationCount>> = groupedStats.map { it.topApps }
}
