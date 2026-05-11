package com.notilog.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.data.local.NotificationEntity
import com.notilog.ui.theme.StatusBadge
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
    val context = LocalContext.current
    val versions by viewModel.getVersions(packageName, systemId, tag).collectAsState(initial = emptyList())
    val appName = versions.firstOrNull()?.appName ?: "Unknown App"
    val isBlacklisted by viewModel.isBlacklisted(packageName).collectAsState(initial = false)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {},
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header - in normal flow with padding like search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(checked = isBlacklisted, onCheckedChange = { viewModel.toggleBlacklist(packageName) })
                    Text(
                        if (isBlacklisted) "Blocked" else "Block",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBlacklisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Content area with rounded top corners
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box {
                                        DetailAppIcon(packageName)
                                        if (versions.size > 1) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-4).dp, y = 4.dp)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${versions.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent))))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("${versions.size} version(s) recorded", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Notification", versions.joinToString("\n---\n") { it.textContent ?: "" })
                                            clipboard.setPrimaryClip(clip)
                                        }) { Icon(Icons.Rounded.Info, contentDescription = "Copy all", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, versions.joinToString("\n---\n") { it.textContent ?: "" }) }
                                            context.startActivity(Intent.createChooser(intent, "Share all versions"))
                                        }) { Icon(Icons.Rounded.Share, contentDescription = "Share all", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
                                    }
                                }
                            }
                        }
                    }

                    if (versions.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
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
                        items(versions) { version -> VersionCard(version, isLatest = version == latestVersion) }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Delete All Versions")
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete All Versions?") },
                text = { Text("This will permanently delete all ${versions.size} recorded versions of notifications from this app. This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteAllVersions(packageName, systemId, tag); showDeleteDialog = false; onBack() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete All") }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }
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
        if (icon != null) { Image(bitmap = icon.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().padding(4.dp)) }
    }
}

@Composable
private fun VersionCard(version: NotificationEntity, isLatest: Boolean) {
    val context = LocalContext.current
    val borderColor = if (isLatest) Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) else Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent))

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(borderColor).padding(1.dp)) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Column {
                if (isLatest) { StatusBadge(modifier = Modifier.padding(start = 16.dp, top = 8.dp), text = "Latest", color = MaterialTheme.colorScheme.primary) }
                Column(modifier = Modifier.padding(16.dp).padding(top = if (isLatest) 20.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDateTime(version.postTime), style = MaterialTheme.typography.labelMedium, color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(onClick = { val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; val clip = ClipData.newPlainText("Notification", version.textContent ?: ""); clipboard.setPrimaryClip(clip) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Rounded.Info, contentDescription = "Copy", modifier = Modifier.size(18.dp)) }
                            FilledTonalIconButton(onClick = { val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, version.textContent ?: "") }; context.startActivity(Intent.createChooser(intent, "Share notification")) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Rounded.Share, contentDescription = "Share", modifier = Modifier.size(18.dp)) }
                        }
                    }
                    if (!version.title.isNullOrBlank()) { Text(version.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) }
                    if (!version.textContent.isNullOrBlank()) { SelectionContainer { Text(version.textContent, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    if (version.isDismissed) { StatusBadge(text = "Dismissed", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
