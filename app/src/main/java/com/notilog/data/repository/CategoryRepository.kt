package com.notilog.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notilog.R
import com.notilog.data.local.CategoryOverrideDao
import com.notilog.data.local.CategoryOverrideEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CategoryMapping(val `package`: String, val category: String)

@Singleton
class CategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryOverrideDao: CategoryOverrideDao
) {
    private val categoryMap = mutableMapOf<String, String>()
    private val overridesCache = mutableMapOf<String, String>()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun loadCategories() {
        // 1. Load static mappings synchronously in IO
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.categories_map)
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                val type = object : TypeToken<List<CategoryMapping>>() {}.type
                val mappings: List<CategoryMapping> = Gson().fromJson(jsonString, type)

                mappings.forEach {
                    categoryMap[it.`package`] = it.category
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Start collecting overrides in the background
        repositoryScope.launch {
            try {
                categoryOverrideDao.getAll().collectLatest { overrides ->
                    val newOverrides = mutableMapOf<String, String>()
                    overrides.forEach { 
                        newOverrides[it.packageName] = it.category
                    }
                    synchronized(overridesCache) {
                        overridesCache.clear()
                        overridesCache.putAll(newOverrides)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCategoryForPackage(packageName: String): String {
        return synchronized(overridesCache) {
            overridesCache[packageName]
        } ?: categoryMap[packageName] ?: "Uncategorized"
    }

    suspend fun updateOverride(packageName: String, category: String) {
        categoryOverrideDao.insert(CategoryOverrideEntity(packageName, category))
    }

    suspend fun deleteOverride(packageName: String) {
        categoryOverrideDao.delete(packageName)
    }
}
