package com.notilog.ui.feed

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.notilog.ui.theme.LocalIsDarkTheme

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import com.notilog.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.data.local.NotificationEntity
import com.notilog.data.local.AppInfoEntry
import com.notilog.ui.theme.StatusBadge
import com.notilog.ui.theme.Colors
import com.notilog.ui.theme.TimeChip
import java.text.SimpleDateFormat
import java.util.*

private val categories = listOf("All", "Social", "Banking", "Shopping", "System", "Uncategorized")

private fun formatCategory(category: String): String {
    return category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    onNotificationClick: (String, Int, String?) -> Unit = { _, _, _ -> },
    viewModel: FeedViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    var isFilterExpanded by remember { mutableStateOf(false) }
    var showAllAppsDialog by remember { mutableStateOf(false) }

    var headerBottomPx by remember { mutableStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val headerBottomDp = with(density) { headerBottomPx.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Feed surface — rounded corners, same horizontal inset as search bar
        if (headerBottomPx > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerBottomDp - 12.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    if (isSelectionMode) {
                        SelectionTopBar(
                            selectedCount = selectedIds.size,
                            onClearSelection = viewModel::clearSelection,
                            onDeleteSelected = viewModel::deleteSelected,
                            onBlacklistSelected = viewModel::blacklistSelected
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = androidx.compose.foundation.lazy.rememberLazyListState(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 132.dp),
verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (notifications.isEmpty()) {
                            item { EmptyState() }
                        } else {
                            val grouped = notifications.groupByDay()
                            grouped.forEach { (dayLabel, dayNotifications) ->
                                item(key = "header_$dayLabel") {
                                    DaySeparator(label = dayLabel)
                                }
                                itemsIndexed(dayNotifications, key = { _, item -> item.id }) { index, notification ->
                                    NotificationItem(
                                        notification = notification,
                                        isSelected = notification.id in selectedIds,
                                        isSelectionMode = isSelectionMode,
                                        searchQuery = searchQuery,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(notification.id)
                                            } else {
                                                onNotificationClick(notification.packageName, notification.systemId, notification.tag)
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                viewModel.toggleSelection(notification.id)
                                            }
                                        }
                                    )
                                    if (index < dayNotifications.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .height(0.5.dp)
                                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search bar + chips — sticky above the feed card, each element has its own shadow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // .statusBarsPadding()
                .padding(top = 16.dp)
                .onGloballyPositioned { coords ->
                    headerBottomPx = (coords.positionInRoot().y + coords.size.height).toInt()
                },
verticalArrangement = Arrangement.Top
        ) {
            FeedSearchSection(
                query = searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onFilterClick = { isFilterExpanded = !isFilterExpanded },
                hasActiveFilters = hasActiveFilters,
                modifier = Modifier.fillMaxWidth()
            )

            // Filter options
            AnimatedVisibility(
                visible = isFilterExpanded,
                enter = expandVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(250)),
                exit = shrinkVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                FilterOptionsSection(
                    filterState = filterState,
                    onFilterChange = viewModel::setFilterState,
                    onReset = viewModel::resetFilters,
                    recentApps = recentApps,
                    allApps = allApps,
                    onShowAllApps = { showAllAppsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                )
            }
            QuickFilterBar(
                categoryCounts = categoryCounts,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::setCategory,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showAllAppsDialog) {
        var selectedApps by remember(showAllAppsDialog) { mutableStateOf(filterState.selectedApps) }
        AlertDialog(
            onDismissRequest = { showAllAppsDialog = false },
            title = { Text("Select apps") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(allApps.sortedBy { it.appName.lowercase(Locale.getDefault()) }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedApps = if (app.packageName in selectedApps) {
                                        selectedApps - app.packageName
                                    } else {
                                        selectedApps + app.packageName
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.packageName in selectedApps,
                                onCheckedChange = { checked ->
                                    selectedApps = if (checked) {
                                        selectedApps + app.packageName
                                    } else {
                                        selectedApps - app.packageName
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.appName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFilterState(filterState.copy(selectedApps = selectedApps))
                        showAllAppsDialog = false
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showAllAppsDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HeroBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = android.graphics.Path()
                path.moveTo(size.width * 0.7f, 0f)
                path.cubicTo(
                    size.width * 0.8f, size.height * 0.2f,
                    size.width * 0.6f, size.height * 0.8f,
                    size.width, size.height * 0.6f
                )
                path.lineTo(size.width, 0f)
                path.close()
                drawContext.canvas.nativeCanvas.drawPath(
                    path,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        alpha = 20
                        style = android.graphics.Paint.Style.FILL
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Your Digital Archive",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Organized and secure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Notilog",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.02).sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = "Feed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.statusBarsPadding()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val barColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    Box(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = 3.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(32.dp),
            color = barColor
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        "Search notifications...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
                    )
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = onFilterClick,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.filter_list_24),
                                contentDescription = "Filter",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        }
                        if (hasActiveFilters) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-12).dp, y = 12.dp)
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = if (isDark) Color.White else Color.Black,
                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onBlacklistSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$selectedCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("selected", fontWeight = FontWeight.SemiBold)
                }
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear selection")
                }
            },
            actions = {
                IconButton(onClick = onBlacklistSelected) {
                    Icon(Icons.Rounded.Warning, contentDescription = "Blacklist selected")
                }
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete selected")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp, 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "No notifications found",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Try adjusting your search or filters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun QuickFilterBar(
    categoryCounts: List<com.notilog.data.local.CategoryCountEntry>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCategories = categoryCounts
        .filter { it.count > 0 && it.category != "UNCATEGORIZED" }
        .sortedByDescending { it.count }

    val totalCount = categoryCounts.sumOf { it.count }

    if (activeCategories.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            val isSelected = selectedCategory == "All"
            ShadowChip(
                label = "All",
                count = totalCount,
                isSelected = isSelected,
                categoryColor = Colors.MainBlue,
                onClick = { onCategorySelected("All") }
            )
        }
        items(activeCategories) { entry ->
            val isSelected = selectedCategory == entry.category
            val categoryColor = Colors.chartColors[activeCategories.indexOf(entry) % Colors.chartColors.size]
            ShadowChip(
                label = formatCategory(entry.category),
                count = entry.count,
                isSelected = isSelected,
                categoryColor = categoryColor,
                onClick = { onCategorySelected(if (isSelected) "All" else entry.category) }
            )
        }
    }
}

@Composable
private fun ShadowChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    categoryColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.surface
                  else MaterialTheme.colorScheme.surface
    val labelColor = if (isSelected) categoryColor
                     else MaterialTheme.colorScheme.onSurfaceVariant
    val countColor = labelColor.copy(alpha = 0.6f)
    val borderColor = if (isSelected) categoryColor else Color.Transparent

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = labelColor)
            Text("$count", style = MaterialTheme.typography.bodySmall, color = countColor)
        }
    }
}

@Composable
fun AppIcon(packageName: String, size: Int = 40) {
    val context = LocalContext.current
    var icon by remember { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        if (packageName.isNotEmpty()) {
            try {
                icon = context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                icon = null
            }
            loaded = true
        }
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } ?: run {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size((size * 0.5f).dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationItem(
    notification: NotificationEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            )
        }

        AppIcon(notification.packageName, size = 44)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTime(notification.postTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (notification.title != null) {
                Text(
                    text = highlightText(notification.title, searchQuery),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!notification.textContent.isNullOrBlank()) {
                Text(
                    text = highlightText(notification.textContent, searchQuery),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun List<NotificationEntity>.groupByDay(): LinkedHashMap<String, List<NotificationEntity>> {
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val todayMs = today.timeInMillis
    val yesterdayMs = yesterday.timeInMillis
    val weekAgoMs = todayMs - 6 * 24 * 60 * 60 * 1000L
    val dayFmt = SimpleDateFormat("EEE d MMM", Locale.getDefault())
    val yearFmt = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault())
    val thisYear = today.get(Calendar.YEAR)

    val result = LinkedHashMap<String, List<NotificationEntity>>()
    for (n in this) {
        val label = when {
            n.postTime >= todayMs -> "Today"
            n.postTime >= yesterdayMs -> "Yesterday"
            n.postTime >= weekAgoMs -> dayFmt.format(Date(n.postTime))
            else -> {
                val cal = Calendar.getInstance().apply { timeInMillis = n.postTime }
                if (cal.get(Calendar.YEAR) == thisYear) dayFmt.format(Date(n.postTime))
                else yearFmt.format(Date(n.postTime))
            }
        }
        result[label] = (result[label] ?: emptyList()) + n
    }
    return result
}

@Composable
private fun DaySeparator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            fontWeight = FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterOptionsSection(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onReset: () -> Unit,
    recentApps: List<AppInfoEntry> = emptyList(),
    allApps: List<AppInfoEntry> = emptyList(),
    onShowAllApps: () -> Unit = {},
    onCustomDateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Date Range",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Custom",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = Colors.MainBlue,
                    modifier = Modifier.clickable(onClick = onCustomDateClick)
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dateOptions = listOf(
                    DateRangeOption.ALL to "All",
                    DateRangeOption.TODAY to "Today",
                    DateRangeOption.YASTEDAY to "Yesterday",
                    DateRangeOption.LAST_7_DAYS to "Last 7 days",
                    DateRangeOption.LAST_30_DAYS to "Last 30 days"
                )
                items(dateOptions) { (option, label) ->
                    FilterChip(
                        selected = filterState.dateRange == option,
                        onClick = { onFilterChange(filterState.copy(dateRange = option)) },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Colors.MainBlue,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // Apps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Apps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (allApps.size > 5) {
                    Text(
                        "See more",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = Colors.MainBlue,
                        modifier = Modifier.clickable(onClick = onShowAllApps)
                    )
                }
            }
            
            if (recentApps.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentApps) { app ->
                        FilterChip(
                            selected = app.packageName in filterState.selectedApps,
                            onClick = {
                                val newApps = if (app.packageName in filterState.selectedApps) {
                                    filterState.selectedApps - app.packageName
                                } else {
                                    filterState.selectedApps + app.packageName
                                }
                                onFilterChange(filterState.copy(selectedApps = newApps))
                            },
                            label = { Text(app.appName, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Colors.MainBlue,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // Sort
            Text(
                "Sort by",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    SortOption.NEWEST to "Newest",
                    SortOption.OLDEST to "Oldest",
                    SortOption.APP_NAME to "App name"
                ).forEach { (option, label) ->
                    FilterChip(
                        selected = filterState.sortOption == option,
                        onClick = { onFilterChange(filterState.copy(sortOption = option)) },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Colors.MainBlue,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Reset button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Reset filters",
                        color = Colors.MainBlue
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val terms = query.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (terms.isEmpty()) return AnnotatedString(text)
    
    val lowerText = text.lowercase()
    val matchingTerms = terms.filter { term -> lowerText.contains(term.lowercase()) }
    if (matchingTerms.isEmpty()) return AnnotatedString(text)
    
    return buildAnnotatedString {
        var lastIndex = 0
        val sortedMatches = matchingTerms.sortedBy { lowerText.indexOf(it.lowercase()) }
        
        for (term in sortedMatches) {
            val lowerTerm = term.lowercase()
            var index = lowerText.indexOf(lowerTerm, lastIndex)
            while (index >= 0) {
                append(text.substring(lastIndex, index))
                withStyle(SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    color = MaterialTheme.colorScheme.primary
                )) {
                    append(text.substring(index, index + term.length))
                }
                lastIndex = index + term.length
                index = lowerText.indexOf(lowerTerm, lastIndex)
            }
        }
        append(text.substring(lastIndex))
    }
}
