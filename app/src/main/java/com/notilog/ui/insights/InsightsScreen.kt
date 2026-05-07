package com.notilog.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val insightsState by viewModel.insightsState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var headerBottomPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerBottomDp = with(density) { headerBottomPx.toDp() }

    Scaffold(
        topBar = {},
        containerColor = Color.Transparent
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (headerBottomPx > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerBottomDp - 12.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Colors.MainBlue)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = 24.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 132.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary Cards
                            item {
                                SummaryCardsRow(
                                    totalCount = insightsState.summary.totalCount,
                                    todayCount = insightsState.summary.todayCount,
                                    weekCount = insightsState.summary.weekCount
                                )
                            }

                            // Category Breakdown
                            item {
                                CategoryChartCard(
                                    categories = insightsState.categoryInsights
                                )
                            }

                            // Top Apps
                            item {
                                TopAppsCard(
                                    apps = insightsState.topApps
                                )
                            }

                            // Daily Trend
                            item {
                                DailyTrendCard(
                                    dailyData = insightsState.dailyInsights
                                )
                            }
                        }
                    }
                }
            }

            // Floating header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                    .onGloballyPositioned { coords ->
                        headerBottomPx =
                            (coords.positionInRoot().y + coords.size.height).toInt()
                    }
            ) {
                Text(
                    "Deep Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
            icon = Icons.Default.Settings,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "This Week",
            value = weekCount.toString(),
            icon = Icons.Default.Star,
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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
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
                val colors = Colors.chartColors
                categories.take(6).forEachIndexed { index, category ->
                    CategoryBarItem(
                        category = formatCategory(category.category),
                        count = category.count,
                        percentage = category.percentage,
                        color = colors[index % colors.size]
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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
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
                    AppListItem(
                        rank = index + 1,
                        appName = app.appName,
                        count = app.count
                    )
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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                day.date.takeLast(5),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCategory(category: String): String {
    return category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}