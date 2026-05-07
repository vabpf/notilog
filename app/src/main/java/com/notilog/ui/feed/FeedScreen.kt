package com.notilog.ui.feed

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.*
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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.data.local.NotificationEntity
import com.notilog.ui.theme.GlassCard
import com.notilog.ui.theme.GlassSurface
import com.notilog.ui.theme.LocalGlassTokens
import com.notilog.ui.theme.StatusBadge
import com.notilog.ui.theme.TimeChip
import java.text.SimpleDateFormat
import java.util.*

private val categories = listOf("All", "Social", "Banking", "Shopping", "System", "Uncategorized")

private fun formatCategory(category: String): String {
    return category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

private val categoryColors = mapOf(
    "SOCIAL" to Color(0xFF818CF8),
    "BANKING" to Color(0xFF34D399),
    "SHOPPING" to Color(0xFFF59E0B),
    "SYSTEM" to Color(0xFF94A3B8),
    "UNCATEGORIZED" to Color(0xFFA78BFA),
    "COMMUNICATION" to Color(0xFF818CF8),
    "ENTERTAINMENT" to Color(0xFFF472B6),
    "PRODUCTIVITY" to Color(0xFF60A5FA),
    "FINANCE" to Color(0xFF34D399),
    "TRAVEL_AND_LOCAL" to Color(0xFFF59E0B),
    "HEALTH_AND_FITNESS" to Color(0xFF10B981),
    "EDUCATION" to Color(0xFF8B5CF6),
    "NEWS_AND_MAGAZINES" to Color(0xFF6366F1),
    "MUSIC_AND_AUDIO" to Color(0xFFEC4899),
    "VIDEO_PLAYERS" to Color(0xFFEF4444),
    "LIFESTYLE" to Color(0xFFF59E0B),
    "BOOKS_AND_REFERENCE" to Color(0xFF8B5CF6),
    "FOOD_AND_DRINK" to Color(0xFFF97316),
    "SPORTS" to Color(0xFF22C55E),
    "PHOTOGRAPHY" to Color(0xFF06B6D4),
    "TOOLS" to Color(0xFF94A3B8),
    "BUSINESS" to Color(0xFF3B82F6),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    onNotificationClick: (Int, String?) -> Unit = { _, _ -> },
    navController: androidx.navigation.NavHostController? = null,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()

    val hazeState = remember { HazeState() }

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
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(state = hazeState),
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
                                items(dayNotifications, key = { it.id }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        isSelected = notification.id in selectedIds,
                                        isSelectionMode = isSelectionMode,
                                        searchQuery = searchQuery,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(notification.id)
                                            } else {
                                                onNotificationClick(notification.systemId, notification.tag)
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                viewModel.toggleSelection(notification.id)
                                            }
                                        }
                                    )
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
                modifier = Modifier.fillMaxWidth()
            )
            QuickFilterBar(
                categoryCounts = categoryCounts,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::setCategory,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HeroBanner() {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        cornerRadius = 24.dp,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
    GlassSurface(
        cornerRadius = 0.dp,
        modifier = Modifier.fillMaxWidth()
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                Text(
                    "Search notifications...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            shape = RoundedCornerShape(32.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                containerColor = Color.Transparent
            ),
            singleLine = true
        )
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
    GlassSurface(cornerRadius = 0.dp) {
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
        GlassCard(
            modifier = Modifier.padding(16.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(40.dp, 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
                onClick = { onCategorySelected("All") }
            )
        }
        items(activeCategories) { entry ->
            val isSelected = selectedCategory == entry.category
            ShadowChip(
                label = formatCategory(entry.category),
                count = entry.count,
                isSelected = isSelected,
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
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surface
    val labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                     else MaterialTheme.colorScheme.onSurfaceVariant
    val countColor = labelColor.copy(alpha = 0.6f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = 4.dp
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
fun AppIcon(packageName: String, category: String, size: Int = 40) {
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
        if (icon != null) {
            Image(
                bitmap = icon!!.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
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

        AppIcon(notification.packageName, notification.category, size = 44)

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
