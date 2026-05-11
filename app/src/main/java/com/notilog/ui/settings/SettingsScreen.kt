package com.notilog.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onManageBlacklist: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoCleanup by viewModel.autoCleanupEnabled.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = {}, containerColor = Color.Transparent) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header - in normal flow
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        val hasAccess by viewModel.hasNotificationAccess.collectAsState()
                        SettingsSection(title = "Notification Access") {
                            SettingsRow(
                                icon = Icons.Rounded.Notifications,
                                title = if (hasAccess) "Access Granted" else "Grant Access",
                                subtitle = if (hasAccess) "You can receive notifications" else "Tap to enable notification access",
                                titleColor = if (hasAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                showChevron = !hasAccess,
                                onClick = { if (!hasAccess) viewModel.openNotificationAccessSettings() else viewModel.refreshNotificationAccess() }
                            )
                        }
                    }

                    item {
                        SettingsSection(title = "Privacy") {
                            SettingsRow(
                                icon = Icons.Rounded.Delete,
                                title = "Auto-delete logs",
                                subtitle = if (autoCleanup) "Notifications are removed automatically" else "Enable to remove old notifications automatically",
                                action = {
                                    Switch(
                                        checked = autoCleanup,
                                        onCheckedChange = { enabled ->
                                            viewModel.setAutoCleanup(enabled)
                                            if (enabled) {
                                                showRetentionDialog = true
                                            }
                                        }
                                    )
                                }
                            )
                            if (autoCleanup) {
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.Refresh,
                                    title = "Delete after",
                                    subtitle = "$retentionDays days",
                                    showChevron = true,
                                    onClick = { showRetentionDialog = true }
                                )
                            }
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.Settings,
                                title = "Theme",
                                subtitle = "Current: ${themeMode.displayLabel()}",
                                showChevron = true,
                                onClick = { showThemeModeDialog = true }
                            )
                        }
                    }

                    item {
                        SettingsSection(title = "Data Management") {
                            SettingsRow(
                                icon = Icons.Rounded.Refresh,
                                title = "Export Data",
                                subtitle = "Not implemented yet",
                                showChevron = false
                            )
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.Warning,
                                title = "Clear All Data",
                                subtitle = "Permanently delete all stored notifications",
                                titleColor = MaterialTheme.colorScheme.error,
                                showChevron = true,
                                onClick = { showClearAllDialog = true }
                            )
                        }
                    }

                    item {
                        SettingsSection(title = "Exclusion List") {
                            SettingsRow(
                                icon = Icons.Rounded.List,
                                title = "Manage Blocked Apps",
                                subtitle = "Apps whose notifications will not be logged",
                                showChevron = true,
                                onClick = onManageBlacklist
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Notilog v2.4.1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showRetentionDialog) {
        AlertDialog(
            onDismissRequest = { showRetentionDialog = false },
            title = { Text("Select retention period") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 14, 30, 60, 90).forEach { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRetentionDays(days)
                                    showRetentionDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = retentionDays == days,
                                onClick = {
                                    viewModel.setRetentionDays(days)
                                    showRetentionDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$days days", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRetentionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear all notifications?") },
            text = { Text("This will permanently delete all stored notifications. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete all")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            title = { Text("Choose theme mode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == themeMode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = mode.displayLabel(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, tonalElevation = 0.dp) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(modifier = Modifier.fillMaxWidth().padding(start = 56.dp).height(0.5.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = titleColor, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        if (action != null) {
            action()
        } else if (showChevron) {
            Icon(imageVector = Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) = SettingsRow(icon = icon, title = title, subtitle = subtitle, titleColor = titleColor, onClick = onClick, action = action)

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Always light"
    ThemeMode.DARK -> "Always dark"
}
