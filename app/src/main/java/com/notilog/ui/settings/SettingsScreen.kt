package com.notilog.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.theme.ThemeMode
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onManageBlacklist: () -> Unit = {},
    onManageTrash: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val autoCleanup by viewModel.autoCleanupEnabled.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val backupEnabled by viewModel.backupEnabled.collectAsState()
    val backupProvider by viewModel.backupProvider.collectAsState()
    val backupFrequency by viewModel.backupFrequency.collectAsState()
    val backupFolderUri by viewModel.backupFolderUri.collectAsState()
    val settingsListState = rememberLazyListState()
    val showHeaderShadow by remember {
        derivedStateOf {
            settingsListState.firstVisibleItemIndex > 0 || settingsListState.firstVisibleItemScrollOffset > 0
        }
    }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showBackupProviderDialog by remember { mutableStateOf(false) }
    var showBackupFrequencyDialog by remember { mutableStateOf(false) }
    var pendingEnableAutoBackup by remember { mutableStateOf(false) }
    var pendingSaveLabel by remember { mutableStateOf("Export data") }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            try {
                val csv = viewModel.buildExportCsv()
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Unable to open destination stream")
                outputStream.bufferedWriter().use { writer ->
                    writer.write(csv)
                }
                Toast.makeText(context, "$pendingSaveLabel completed", Toast.LENGTH_SHORT).show()
            } catch (error: IOException) {
                Toast.makeText(context, "$pendingSaveLabel failed", Toast.LENGTH_SHORT).show()
            } catch (error: SecurityException) {
                Toast.makeText(context, "No permission to write selected file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Unable to open source stream")
                val content = inputStream.bufferedReader().use { reader -> reader.readText() }
                val importedCount = viewModel.importData(content)
                Toast.makeText(context, "Imported $importedCount notifications", Toast.LENGTH_SHORT).show()
            } catch (error: IOException) {
                Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            } catch (error: SecurityException) {
                Toast.makeText(context, "No permission to read selected file", Toast.LENGTH_SHORT).show()
            } catch (error: IllegalArgumentException) {
                Toast.makeText(context, "Unsupported import format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val flags = result.data?.flags ?: 0
        val persistableFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
        } catch (_: SecurityException) {
            Toast.makeText(context, "Could not keep folder permission", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        viewModel.setBackupFolderUri(uri)
        Toast.makeText(context, "Backup folder selected", Toast.LENGTH_SHORT).show()
        if (pendingEnableAutoBackup) {
            val enabled = viewModel.setBackupEnabled(true)
            pendingEnableAutoBackup = false
            if (!enabled) {
                Toast.makeText(context, "Backup folder is required to enable auto backup", Toast.LENGTH_SHORT).show()
            } else {
                showBackupProviderDialog = true
                showBackupFrequencyDialog = true
            }
        }
    }

    Scaffold(topBar = {}, containerColor = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 52.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                LazyColumn(
                    state = settingsListState,
                    contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 132.dp),
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
                                subtitle = "Save notifications as CSV",
                                showChevron = true,
                                onClick = {
                                    pendingSaveLabel = "Export data"
                                    saveCsvLauncher.launch(timestampedCsvName("notilog_export"))
                                }
                            )
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.List,
                                title = "Import Data",
                                subtitle = "Restore notifications from CSV or JSON",
                                showChevron = true,
                                onClick = {
                                    importDataLauncher.launch(arrayOf("text/*", "application/json"))
                                }
                            )
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.Refresh,
                                title = "Auto Backup Data",
                                subtitle = when {
                                    backupEnabled -> "Enabled · ${backupFrequency.label}"
                                    backupFolderUri == null -> "Disabled · Folder required"
                                    else -> "Disabled"
                                },
                                action = {
                                    Switch(
                                        checked = backupEnabled,
                                        onCheckedChange = { enabled ->
                                            if (enabled) {
                                                if (backupFolderUri == null) {
                                                    pendingEnableAutoBackup = true
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                                        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                                    }
                                                    backupFolderLauncher.launch(intent)
                                                } else {
                                                    viewModel.setBackupEnabled(true)
                                                    showBackupProviderDialog = true
                                                    showBackupFrequencyDialog = true
                                                }
                                            } else {
                                                pendingEnableAutoBackup = false
                                                viewModel.setBackupEnabled(false)
                                            }
                                        }
                                    )
                                }
                            )
                            if (backupEnabled) {
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.List,
                                    title = "Backup Provider",
                                    subtitle = backupProvider.label,
                                    showChevron = true,
                                    onClick = { showBackupProviderDialog = true }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.Refresh,
                                    title = "Backup Frequency",
                                    subtitle = backupFrequency.label,
                                    showChevron = true,
                                    onClick = { showBackupFrequencyDialog = true }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.Settings,
                                    title = "Backup Folder",
                                    subtitle = if (backupFolderUri == null) "Not selected" else "Selected",
                                    showChevron = true,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                        }
                                        backupFolderLauncher.launch(intent)
                                    }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.Settings,
                                    title = "Backup Now",
                                    subtitle = "Create backup file for ${backupProvider.label}",
                                    showChevron = true,
                                    onClick = {
                                        val prefix = if (backupProvider == BackupProvider.GOOGLE_DRIVE) {
                                            "notilog_backup_google"
                                        } else {
                                            "notilog_backup_onedrive"
                                        }
                                        pendingSaveLabel = "${backupProvider.label} backup"
                                        saveCsvLauncher.launch(timestampedCsvName(prefix))
                                    }
                                )
                            }
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.Delete,
                                title = "Trash",
                                subtitle = "View and restore deleted notifications",
                                showChevron = true,
                                onClick = onManageTrash
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
            FloatingSettingsHeader(
                title = "Settings",
                showShadow = showHeaderShadow,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
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

    if (showBackupProviderDialog) {
        AlertDialog(
            onDismissRequest = { showBackupProviderDialog = false },
            title = { Text("Choose backup location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupProvider.entries.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setBackupProvider(provider)
                                    showBackupProviderDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = provider == backupProvider,
                                onClick = {
                                    viewModel.setBackupProvider(provider)
                                    showBackupProviderDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = provider.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupProviderDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBackupFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showBackupFrequencyDialog = false },
            title = { Text("Choose backup frequency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupFrequency.entries.forEach { frequency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setBackupFrequency(frequency)
                                    showBackupFrequencyDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = frequency == backupFrequency,
                                onClick = {
                                    viewModel.setBackupFrequency(frequency)
                                    showBackupFrequencyDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = frequency.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupFrequencyDialog = false }) {
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

private fun timestampedCsvName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_${timestamp}.csv"
}

@Composable
private fun FloatingSettingsHeader(title: String, showShadow: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        if (showShadow) {
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
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface
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
