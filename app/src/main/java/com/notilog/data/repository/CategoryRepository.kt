package com.notilog.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notilog.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CategoryMapping(val `package`: String, val category: String)

@Singleton
class CategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val categoryMap = mutableMapOf<String, String>()

    suspend fun loadCategories() {
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
    }

    fun getCategoryForPackage(packageName: String): String {
        return categoryMap[packageName] ?: "Uncategorized"
    }
}
