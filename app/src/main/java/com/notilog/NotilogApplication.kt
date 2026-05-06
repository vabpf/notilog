package com.notilog

import android.app.Application
import android.util.Log
import com.notilog.data.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotilogApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d("NotilogApp", "Application onCreate START")
        
        applicationScope.launch {
            try {
                categoryRepository.loadCategories()
                Log.d("NotilogApp", "Categories loaded OK")
            } catch (e: Throwable) {
                Log.e("NotilogApp", "Category loading failed", e)
            }
        }
        
        Log.d("NotilogApp", "Application onCreate END")
    }
}
