package com.notilog.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.feed.AppIcon
import com.notilog.ui.theme.GlassCard
import com.notilog.ui.theme.GlassSurface
import com.notilog.ui.theme.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(
    onBack: () -> Unit = {},
    viewModel: BlacklistViewModel = hiltViewModel()
) {
    val blacklistedApps by viewModel.blacklistedApps.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            GlassSurface(cornerRadius = 0.dp) {
                TopAppBar(
                    title = {
                        Text(
                            "Managed Blacklist",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (blacklistedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(cornerRadius = 20.dp) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusBadge(
                            text = "All clear",
                            color = Color(0xFF34D399)
                        )
                        Text(
                            "No blacklisted apps",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(blacklistedApps) { app ->
                    BlacklistedAppCard(
                        packageName = app.packageName,
                        onRemove = { viewModel.removeFromBlacklist(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun BlacklistedAppCard(packageName: String, onRemove: () -> Unit) {
    GlassCard(cornerRadius = 20.dp, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppIcon(packageName, "Uncategorized", size = 40)
            Text(
                packageName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            StatusBadge(
                text = "Blocked",
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
