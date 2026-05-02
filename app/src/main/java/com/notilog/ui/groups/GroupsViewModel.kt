package com.notilog.ui.groups

import androidx.lifecycle.ViewModel
import com.notilog.data.local.NotificationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AppNotificationCount(val packageName: String, val appName: String, val count: Int)
data class CategoryCount(val category: String, val count: Int)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    val categoryStats: Flow<List<CategoryCount>> = notificationDao.getAllNotifications().map { list ->
        list.groupBy { it.category }
            .map { (cat, items) -> CategoryCount(cat, items.size) }
            .sortedByDescending { it.count }
    }

    val topApps: Flow<List<AppNotificationCount>> = notificationDao.getAllNotifications().map { list ->
        list.groupBy { it.packageName }
            .map { (pkg, items) -> AppNotificationCount(pkg, items.first().appName, items.size) }
            .sortedByDescending { it.count }
            .take(10)
    }
}
