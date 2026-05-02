package com.notilog.ui.detail

import androidx.lifecycle.ViewModel
import com.notilog.data.local.NotificationDao
import com.notilog.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    fun getVersions(systemId: Int, tag: String?): Flow<List<NotificationEntity>> {
        return notificationDao.getVersionsBySystemId(systemId, tag)
    }
}
