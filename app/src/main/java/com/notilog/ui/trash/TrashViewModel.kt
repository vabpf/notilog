package com.notilog.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.NotificationEntity
import com.notilog.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val deletedNotifications: StateFlow<List<NotificationEntity>> = notificationRepository.getDeletedNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun permanentDeleteAllTrash() {
        viewModelScope.launch {
            notificationRepository.permanentDeleteAllTrash()
        }
    }
}
