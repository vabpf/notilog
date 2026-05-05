package com.notilog.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notilog.ui.theme.GlassCard
import com.notilog.ui.theme.GlassSurface
import java.text.SimpleDateFormat
import java.util.*

private val filterCategories = listOf(
    FilterCategory("Time", listOf("Today", "Yesterday", "Last 7 days", "Last 30 days", "Custom")),
    FilterCategory("Apps", listOf("All Apps", "Social", "Banking", "Shopping", "System")),
    FilterCategory("Status", listOf("All", "Read", "Unread", "Archived")),
    FilterCategory("Priority", listOf("All", "High", "Normal", "Low"))
)

data class FilterCategory(val name: String, val options: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterScreen(
    onBack: () -> Unit = {},
    onApplyFilters: (Map<String, String>) -> Unit = {}
) {
    var selectedFilters = remember {
        mutableStateMapOf(
            "Time" to "Last 7 days",
            "Apps" to "All Apps",
            "Status" to "All",
            "Priority" to "All"
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                onSettingsClick = { /* already on settings/filter screen */ },
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    "Advanced Filters",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            filterCategories.forEach { category ->
                item {
                    FilterSection(
                        category = category,
                        selectedOption = selectedFilters[category.name] ?: category.options.first(),
                        onOptionSelected = { option ->
                            selectedFilters[category.name] = option
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedFilters.clear()
                            selectedFilters["Time"] = "Last 7 days"
                            selectedFilters["Apps"] = "All Apps"
                            selectedFilters["Status"] = "All"
                            selectedFilters["Priority"] = "All"
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { onApplyFilters(selectedFilters.toMap()) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    category: FilterCategory,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(
            category.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(category.options) { option ->
                val isSelected = option == selectedOption
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionSelected(option) },
                    label = {
                        Text(
                            option,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}