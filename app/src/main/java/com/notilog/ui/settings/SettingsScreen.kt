package com.notilog.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.DocumentsContract
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
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
    var showBackupAuthDialog by remember { mutableStateOf(false) }
    var showBackupWebAuthDialog by remember { mutableStateOf(false) }
    var pendingEnableAutoBackup by remember { mutableStateOf(false) }
    var pendingSaveLabel by remember { mutableStateOf("Export data") }
    var pendingBackupProvider by remember { mutableStateOf<BackupProvider?>(null) }
    var pendingFolderProvider by remember { mutableStateOf<BackupProvider?>(null) }
    var pendingWebAuthProvider by remember { mutableStateOf<BackupProvider?>(null) }
    var selectedBackupProvider by remember { mutableStateOf(backupProvider) }

    LaunchedEffect(showBackupProviderDialog) {
        if (showBackupProviderDialog) {
            selectedBackupProvider = backupProvider
        }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Unable to open destination stream")
                viewModel.exportDataStreaming(outputStream)
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
                val importedCount = viewModel.importData(inputStream)
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
        val uri = result.data?.data ?: run {
            if (pendingEnableAutoBackup) {
                pendingEnableAutoBackup = false
                Toast.makeText(context, "Backup folder is required to enable auto backup", Toast.LENGTH_SHORT).show()
            }
            pendingFolderProvider = null
            return@rememberLauncherForActivityResult
        }
        val flags = result.data?.flags ?: 0
        val persistableFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
        } catch (_: SecurityException) {
            Toast.makeText(context, "Could not keep folder permission", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        val provider = pendingFolderProvider ?: backupProvider
        pendingFolderProvider = null
        viewModel.setBackupProvider(provider)
        viewModel.setBackupFolderUri(uri)
        Toast.makeText(context, "Backup folder selected", Toast.LENGTH_SHORT).show()
        if (pendingEnableAutoBackup) {
            val enabled = viewModel.setBackupEnabled(true)
            pendingEnableAutoBackup = false
            if (!enabled) {
                Toast.makeText(context, "Backup folder is required to enable auto backup", Toast.LENGTH_SHORT).show()
            } else {
                showBackupFrequencyDialog = true
            }
        }
    }

    fun backupProviderInitialUri(provider: BackupProvider): Uri? {
        return when (provider) {
            BackupProvider.GOOGLE_DRIVE -> DocumentsContract.buildRootUri("com.google.android.apps.docs.storage", "root")
            BackupProvider.ONEDRIVE -> DocumentsContract.buildRootUri("com.microsoft.skydrive.content", "root")
            BackupProvider.LOCAL -> null
        }
    }

    fun backupProviderPackage(provider: BackupProvider): String? {
        return when (provider) {
            BackupProvider.GOOGLE_DRIVE -> "com.google.android.apps.docs"
            BackupProvider.ONEDRIVE -> "com.microsoft.skydrive"
            BackupProvider.LOCAL -> null
        }
    }

    fun backupProviderWebAuthUrl(provider: BackupProvider): String? {
        return when (provider) {
            BackupProvider.GOOGLE_DRIVE -> "https://drive.google.com/"
            BackupProvider.ONEDRIVE -> "https://onedrive.live.com/"
            BackupProvider.LOCAL -> null
        }
    }

    fun isProviderAppAvailable(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun launchBackupFolderPicker(provider: BackupProvider) {
        pendingFolderProvider = provider
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        val providerPackage = backupProviderPackage(provider)
        if (providerPackage != null) {
            if (!isProviderAppAvailable(providerPackage)) {
                pendingFolderProvider = null
                pendingWebAuthProvider = provider
                showBackupWebAuthDialog = true
                return
            }
            intent.setPackage(providerPackage)
        }
        backupProviderInitialUri(provider)?.let { initialUri ->
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }
        backupFolderLauncher.launch(intent)
    }

    fun proceedWithBackupProvider(provider: BackupProvider) {
        viewModel.setBackupProvider(provider)
        if (provider.requiresAuth && !viewModel.isBackupProviderAuthenticated(provider)) {
            pendingBackupProvider = provider
            showBackupAuthDialog = true
        } else {
            pendingBackupProvider = null
            launchBackupFolderPicker(provider)
        }
    }

    Scaffold(topBar = {}, containerColor = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 42.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    state = settingsListState,
                    contentPadding = PaddingValues(top = 34.dp, start = 16.dp, end = 16.dp, bottom = 132.dp),
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
                                    icon = Icons.Rounded.Info,
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
                                icon = Icons.Rounded.Share,
                                title = "Export Data",
                                subtitle = "Save as compressed JSON (.json.gz)",
                                showChevron = true,
                                onClick = {
                                    pendingSaveLabel = "Export data"
                                    saveJsonLauncher.launch(timestampedCompressedJsonName("notilog_export"))
                                }
                            )
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Rounded.Build,
                                title = "Import Data",
                                subtitle = "Restore from .json.gz or legacy formats",
                                showChevron = true,
                                onClick = {
                                    importDataLauncher.launch(arrayOf("application/json", "application/gzip", "application/x-gzip", "text/*"))
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
                                                pendingEnableAutoBackup = true
                                                showBackupProviderDialog = true
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
                                    title = "Backup Type",
                                    subtitle = backupProvider.label,
                                    showChevron = true,
                                    onClick = {
                                        pendingEnableAutoBackup = true
                                        showBackupProviderDialog = true
                                    }
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
                                        if (backupProvider.requiresAuth && !viewModel.isBackupProviderAuthenticated(backupProvider)) {
                                            pendingBackupProvider = backupProvider
                                            showBackupAuthDialog = true
                                        } else {
                                            launchBackupFolderPicker(backupProvider)
                                        }
                                    }
                                )
                                SettingsDivider()
                                SettingsRow(
                                    icon = Icons.Rounded.PlayArrow,
                                    title = "Backup Now",
                                    subtitle = "Create backup file for ${backupProvider.label}",
                                    showChevron = true,
                                    onClick = {
                                        val prefix = when (backupProvider) {
                                            BackupProvider.GOOGLE_DRIVE -> "notilog_backup_google"
                                            BackupProvider.ONEDRIVE -> "notilog_backup_onedrive"
                                            BackupProvider.LOCAL -> "notilog_backup_local"
                                        }
                                        pendingSaveLabel = "${backupProvider.label} backup"
                                        saveJsonLauncher.launch(timestampedCompressedJsonName(prefix))
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
                                icon = Icons.Rounded.Lock,
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
            onDismissRequest = {
                showBackupProviderDialog = false
                pendingEnableAutoBackup = false
                pendingBackupProvider = null
            },
            title = { Text("Choose backup type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupProvider.entries.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBackupProvider = provider
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = provider == selectedBackupProvider,
                                onClick = {
                                    selectedBackupProvider = provider
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
                TextButton(onClick = {
                    showBackupProviderDialog = false
                    proceedWithBackupProvider(selectedBackupProvider)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupProviderDialog = false
                    pendingEnableAutoBackup = false
                    pendingBackupProvider = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackupAuthDialog) {
        val authProvider = pendingBackupProvider ?: backupProvider
        AlertDialog(
            onDismissRequest = {
                showBackupAuthDialog = false
                pendingEnableAutoBackup = false
                pendingBackupProvider = null
            },
            title = { Text("Authenticate ${authProvider.label}") },
            text = {
                Text("Sign in to ${authProvider.label} to continue and select a backup folder.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackupAuthDialog = false
                        pendingBackupProvider = null
                        launchBackupFolderPicker(authProvider)
                    }
                ) {
                    Text("Authenticate")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackupAuthDialog = false
                        pendingEnableAutoBackup = false
                        pendingBackupProvider = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackupWebAuthDialog) {
        val webProvider = pendingWebAuthProvider ?: backupProvider
        AlertDialog(
            onDismissRequest = {
                showBackupWebAuthDialog = false
                pendingEnableAutoBackup = false
                pendingWebAuthProvider = null
            },
            title = { Text("${webProvider.label} not installed") },
            text = {
                Text("Open ${webProvider.label} in your browser to authenticate. Install the app to choose a backup folder.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val url = backupProviderWebAuthUrl(webProvider)
                        if (url == null) {
                            Toast.makeText(context, "No web sign-in available", Toast.LENGTH_SHORT).show()
                        } else {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(webIntent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showBackupWebAuthDialog = false
                        pendingEnableAutoBackup = false
                        pendingWebAuthProvider = null
                    }
                ) {
                    Text("Open Web")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackupWebAuthDialog = false
                        pendingEnableAutoBackup = false
                        pendingWebAuthProvider = null
                    }
                ) {
                    Text("Cancel")
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
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp, tonalElevation = 0.dp) {
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

private fun timestampedCompressedJsonName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_${timestamp}.json.gz"
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
