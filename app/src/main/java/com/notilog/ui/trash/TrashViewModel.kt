package com.notilog.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    val deletedNotifications: StateFlow<List<NotificationEntity>> = notificationDao.getDeletedNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun permanentDeleteAllTrash() {
        viewModelScope.launch {
            notificationDao.permanentDeleteAllTrash()
        }
    }
}
