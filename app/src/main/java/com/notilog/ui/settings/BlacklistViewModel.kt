package com.notilog.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notilog.data.local.BlacklistedAppDao
import com.notilog.data.local.BlacklistedAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val blacklistedAppDao: BlacklistedAppDao
) : ViewModel() {

    val blacklistedApps: Flow<List<BlacklistedAppEntity>> = blacklistedAppDao.getAll()

    fun removeFromBlacklist(packageName: String) {
        viewModelScope.launch {
            blacklistedAppDao.delete(packageName)
        }
    }
}
