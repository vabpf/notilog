package com.notilog.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onManageBlacklist: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoCleanup by viewModel.autoCleanupEnabled.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()

    var headerBottomPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerBottomDp = with(density) { headerBottomPx.toDp() }

    Scaffold(topBar = {}, containerColor = Color.Transparent) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (headerBottomPx > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerBottomDp - 12.dp),
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
                                    icon = Icons.Default.Notifications,
                                    title = if (hasAccess) "Access Granted" else "Grant Access",
                                    subtitle = if (hasAccess) "You can receive notifications"
                                               else "Tap to enable notification access",
                                    titleColor = if (hasAccess) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.error,
                                    showChevron = !hasAccess,
                                    onClick = {
                                        if (!hasAccess) viewModel.openNotificationAccessSettings()
                                        else viewModel.refreshNotificationAccess()
                                    }
                                )
                            }
                        }

                        item {
                            SettingsSection(title = "Privacy") {
                                SettingsRow(
                                    icon = Icons.Default.Delete,
                                    title = "Auto-delete logs",
                                    subtitle = "Remove logs older than $retentionDays days",
                                    action = {
                                        Switch(
                                            checked = autoCleanup,
                                            onCheckedChange = viewModel::setAutoCleanup
                                        )
                                    }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Default.Settings,
                                    title = "Dark Theme",
                                    subtitle = "Switch between light and dark mode",
                                    action = { Switch(checked = false, onCheckedChange = {}) }
                                )
                            }
                        }

                        item {
                            SettingsSection(title = "Data Management") {
                                SettingsRow(
                                    icon = Icons.Default.Refresh,
                                    title = "Export Data",
                                    subtitle = "Download all your logs as a CSV file",
                                    showChevron = true,
                                    onClick = { /* TODO */ }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Default.Warning,
                                    title = "Clear All Data",
                                    subtitle = "Permanently delete all stored notifications",
                                    titleColor = MaterialTheme.colorScheme.error,
                                    showChevron = true,
                                    onClick = { viewModel.runCleanupNow() }
                                )
                            }
                        }

                        item {
                            SettingsSection(title = "Exclusion List") {
                                SettingsRow(
                                    icon = Icons.Default.List,
                                    title = "Manage Blocked Apps",
                                    subtitle = "Apps whose notifications will not be logged",
                                    showChevron = true,
                                    onClick = onManageBlacklist
                                )
                            }
                        }

                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Notilog v2.4.1",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }

                        item { Spacer(Modifier.height(100.dp)) }
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
                        headerBottomPx = (coords.positionInRoot().y + coords.size.height).toInt()
                    }
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            tonalElevation = 0.dp
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = titleColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (action != null) {
            action()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Keep old name as alias so other files that reference SettingsItem still compile
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) = SettingsRow(icon = icon, title = title, subtitle = subtitle, titleColor = titleColor, onClick = onClick, action = action)
