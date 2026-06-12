package com.notilog.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.R
import com.notilog.data.local.NotificationEntity
import com.notilog.ui.feed.AppIcon
import com.notilog.ui.theme.StatusBadge
import com.notilog.ui.theme.Colors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    packageName: String,
    systemId: Int,
    tag: String?,
    onBack: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val versions by viewModel.getVersions(packageName, systemId, tag).collectAsState(initial = emptyList())
    val appName = versions.firstOrNull()?.appName ?: "Unknown App"
    val isBlacklisted by viewModel.isBlacklisted(packageName).collectAsState(initial = false)
    val detailListState = rememberLazyListState()
    val showHeaderShadow by remember {
        derivedStateOf {
            detailListState.firstVisibleItemIndex > 0 || detailListState.firstVisibleItemScrollOffset > 0
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                state = detailListState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "header") {
                    AppDetailHeader(
                        packageName = packageName,
                        appName = appName,
                        versionCount = versions.size,
                        currentCategory = viewModel.getCategory(packageName),
                        onCategoryChange = { viewModel.updateCategory(packageName, it) }
                    )
                }

                if (versions.isEmpty()) {
                    item(key = "empty") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(140.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    Text("No history found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    val latestVersion = versions.firstOrNull()
                    items(versions, key = { it.id }) { version ->
                        VersionCard(
                            version = version,
                            isLatest = version == latestVersion,
                            onDelete = {
                                viewModel.deleteVersion(version.id)
                                Toast.makeText(context, "1 notification deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                item(key = "deleteAll") {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete All Versions")
                    }
                }
            }
        }

        DetailTopBar(
            appName = appName,
            isBlacklisted = isBlacklisted,
            onBack = onBack,
            onToggleBlacklist = { viewModel.toggleBlacklist(packageName) }
        )

        if (showHeaderShadow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp)
                    .height(10.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete All Versions?") },
            text = { Text("This will move all ${versions.size} recorded versions of notifications from this app to trash.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllVersions(packageName, systemId, tag)
                    showDeleteDialog = false
                    onBack()
                    Toast.makeText(context, "${versions.size} notifications deleted", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DetailAppIcon(packageName: String) {
    val context = LocalContext.current
    val icon = remember(packageName) { try { context.packageManager.getApplicationIcon(packageName) } catch (e: Exception) { null } }
    Box(
        modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), Color.Transparent))).border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) { Image(bitmap = icon.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape)) }
    }
}

@Composable
private fun VersionCard(
    version: NotificationEntity,
    isLatest: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val borderBrush = if (isLatest) {
        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
    } else {
        SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(borderBrush).padding(1.dp)) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(19.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                if (isLatest) { StatusBadge(modifier = Modifier.padding(start = 16.dp, top = 8.dp), text = "Latest", color = MaterialTheme.colorScheme.primary) }
                Column(modifier = Modifier.padding(16.dp).padding(top = if (isLatest) 20.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDateTime(version.postTime), style = MaterialTheme.typography.labelMedium, color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Notification", buildShareText(version))
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.size(36.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_content_copy_24),
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            FilledTonalIconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, buildShareText(version))
                                }
                                context.startActivity(Intent.createChooser(intent, "Share notification"))
                            }, modifier = Modifier.size(36.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Icon(Icons.Rounded.Share, contentDescription = "Share", modifier = Modifier.size(18.dp)) }
                            FilledTonalIconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete record", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (!version.title.isNullOrBlank()) { SelectionContainer { Text(version.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) } }
                    if (!version.textContent.isNullOrBlank()) { SelectionContainer { Text(version.textContent, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    if (version.isDismissed) { StatusBadge(text = "Dismissed", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    appName: String,
    isBlacklisted: Boolean,
    onBack: () -> Unit,
    onToggleBlacklist: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    appName,
                    modifier = Modifier.align(Alignment.CenterStart),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    if (isBlacklisted) "Blocked" else "Block",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isBlacklisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Switch(checked = isBlacklisted, onCheckedChange = { onToggleBlacklist() })
            }
        }
    }
}

@Composable
private fun AppDetailHeader(
    packageName: String,
    appName: String,
    versionCount: Int,
    currentCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box {
                    AppIcon(packageName, size = 64)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$versionCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(8.dp))

                    Box {
                        var showCategoryMenu by remember { mutableStateOf(false) }
                        val categoryColor = Colors.getCategoryColor(currentCategory)
                        Surface(
                            onClick = { showCategoryMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = categoryColor.copy(alpha = 0.15f),
                            contentColor = categoryColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.List, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = formatCategory(currentCategory),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            val categories = listOf(
                                "COMMUNICATION", "SOCIAL", "NEWS_AND_MAGAZINES", "LIFESTYLE",
                                "ENTERTAINMENT", "BUSINESS", "TOOLS", "FINANCE", "SHOPPING",
                                "PRODUCTIVITY", "VIDEO_PLAYERS", "MUSIC_AND_AUDIO", "PHOTOGRAPHY",
                                "BOOKS_AND_REFERENCE", "HEALTH_AND_FITNESS", "TRAVEL_AND_LOCAL",
                                "EDUCATION", "FOOD_AND_DRINK", "SPORTS", "Uncategorized"
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(formatCategory(category)) },
                                    onClick = {
                                        onCategoryChange(category)
                                        showCategoryMenu = false
                                    }
                                )
                            }
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

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun buildShareText(version: NotificationEntity): String {
    val title = version.title?.takeIf { it.isNotBlank() } ?: "(No title)"
    val body = version.textContent?.takeIf { it.isNotBlank() } ?: "(No content)"
    return "Time: ${formatDateTime(version.postTime)}\nTitle: $title\nContent: $body"
}
