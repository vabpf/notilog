package com.notilog.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val insightsState by viewModel.insightsState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val insightsListState = rememberLazyListState()
    val showHeaderShadow by remember {
        derivedStateOf {
            insightsListState.firstVisibleItemIndex > 0 || insightsListState.firstVisibleItemScrollOffset > 0
        }
    }

    Scaffold(
        topBar = {},
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 42.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Colors.MainBlue)
                    }
                } else {
                    LazyColumn(
                        state = insightsListState,
                        contentPadding = PaddingValues(top = 34.dp, start = 16.dp, end = 16.dp, bottom = 132.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { SummaryCardsRow(totalCount = insightsState.summary.totalCount, todayCount = insightsState.summary.todayCount, weekCount = insightsState.summary.weekCount) }
                        item { FocusAnalysisCard(mostActiveHour = insightsState.mostActiveHour, disruptionScore = insightsState.disruptionScore) }
                        item { CategoryChartCard(categories = insightsState.categoryInsights) }
                        item { TopAppsCard(apps = insightsState.topApps) }
                        item { DailyTrendCard(dailyData = insightsState.dailyInsights) }
                    }
                }
            }
            FloatingHeader(
                title = "Deep Insights",
                showShadow = showHeaderShadow,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun FloatingHeader(title: String, showShadow: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        if (showShadow) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .offset(y = 3.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
            ) {}
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(32.dp),
            color = if (showShadow) MaterialTheme.colorScheme.surface else Color.Transparent
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FocusAnalysisCard(mostActiveHour: Int?, disruptionScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Focus Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Peak Activity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val hourText = mostActiveHour?.let { 
                        val period = if (it < 12) "AM" else "PM"
                        val displayHour = when {
                            it == 0 -> 12
                            it > 12 -> it - 12
                            else -> it
                        }
                        "$displayHour:00 $period"
                    } ?: "No data"
                    Text(hourText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Disruption Score", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "$disruptionScore",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (disruptionScore > 20) Colors.Error else Colors.Success,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "avg/hr at peak",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            }
            
            if (disruptionScore > 25) {
                Surface(
                    color = Colors.Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Colors.Error, modifier = Modifier.size(18.dp))
                        Text(
                            "High notification intensity detected during peak hours. Consider blacklisting noisy apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Colors.Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCardsRow(
    totalCount: Int,
    todayCount: Int,
    weekCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total",
            value = totalCount.toString(),
            icon = Icons.Rounded.Notifications,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Today",
            value = todayCount.toString(),
            icon = Icons.Rounded.List,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "This Week",
            value = weekCount.toString(),
            icon = Icons.Rounded.DateRange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Colors.MainBlue,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryChartCard(
    categories: List<CategoryInsight>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "By Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (categories.isEmpty()) {
                Text(
                    "No data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categories.take(6).forEachIndexed { index, category ->
                    CategoryBarItem(
                        category = formatCategory(category.category),
                        count = category.count,
                        percentage = category.percentage,
                        color = Colors.getCategoryColor(category.category)
                    )
                    if (index < categories.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBarItem(
    category: String,
    count: Int,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun TopAppsCard(
    apps: List<AppInsight>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Apps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (apps.isEmpty()) {
                Text(
                    "No data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                apps.forEachIndexed { index, app ->
                    key(app.packageName) {
                        AppListItem(
                            rank = index + 1,
                            appName = app.appName,
                            count = app.count
                        )
                    }
                    if (index < apps.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    rank: Int,
    appName: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Colors.MainBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rank.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Colors.MainBlue
                )
            }
            Text(
                appName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Colors.MainBlue.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Colors.MainBlue
            )
        }
    }
}

@Composable
private fun DailyTrendCard(
    dailyData: List<DailyInsight>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Last 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (dailyData.isEmpty()) {
                Text(
                    "No data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxCount = dailyData.maxOfOrNull { it.count } ?: 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyData.forEach { day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            val heightFraction = if (maxCount > 0) day.count.toFloat() / maxCount else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightFraction.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Colors.MainBlue)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dailyData.forEach { day ->
                        Text(
                            text = formatAxisDate(day.date),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun formatCategory(category: String): String {
    return category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

private fun formatAxisDate(raw: String): String {
    return if (raw.length >= 10) "${raw.substring(5, 7)}/${raw.substring(8, 10)}" else raw
}
