package com.notilog.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val autoCleanup by viewModel.autoCleanupEnabled.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Cloud Backup", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Google Drive OAuth */ },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Connect Google Drive") }
                        OutlinedButton(
                            onClick = { /* TODO: Trigger backup */ },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Sync Now") }
                        Text(
                            "Last sync: Never",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Data Retention", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-cleanup", style = MaterialTheme.typography.bodyLarge)
                            Switch(checked = autoCleanup, onCheckedChange = viewModel::setAutoCleanup)
                        }
                        if (autoCleanup) {
                            Text("Retention period", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(30, 60, 90).forEach { days ->
                                    FilterChip(
                                        selected = retentionDays == days,
                                        onClick = { viewModel.setRetentionDays(days) },
                                        label = { Text("$days days") }
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.runCleanupNow() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Clean Up Now") }
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("System", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Check Notification Permission") }
                    }
                }
            }
        }
    }
}

