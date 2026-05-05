package com.notilog.ui.groups

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notilog.ui.theme.GlassCard
import com.notilog.ui.theme.GlassSurface
import com.notilog.ui.theme.StatusBadge
import com.notilog.ui.feed.AppHeader
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.unit.dp

private val categoryIcons = mapOf(
    "Social" to Icons.Default.Person,
    "Banking" to Icons.Default.CreditCard,
    "Shopping" to Icons.Default.ShoppingCart,
    "System" to Icons.Default.Settings,
    "Uncategorized" to Icons.Default.Star,
)

private val categoryGradients = mapOf(
    "Social" to listOf(Color(0xFF818CF8), Color(0xFF6366F1)),
    "Banking" to listOf(Color(0xFF34D399), Color(0xFF10B981)),
    "Shopping" to listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
    "System" to listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
    "Uncategorized" to listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6)),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(
    onCategoryClick: (String) -> Unit = {},
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val categoryStats by viewModel.categoryStats.collectAsState(initial = emptyList())

    // Large chips panel at top - dynamic chips with category counts
    LargeChipsPanel(
        categoryStats = categoryStats,
        onCategoryClick = onCategoryClick
    )
    val topApps by viewModel.topApps.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppHeader(title = "Groups")
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
            }
            item {
                if (categoryStats.isEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No categories yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categoryStats.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { cat ->
                                    val gradient = categoryGradients[cat.category]
                                        ?: listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                    CategoryCard(
                                        category = cat.category,
                                        count = cat.count,
                                        gradient = gradient,
                                        onClick = { onCategoryClick(cat.category) }
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            if (topApps.isNotEmpty()) {
                item {
                    Text(
                        "Top Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                }
                items(topApps) { app ->
                    AppCard(app)
                }
            }
        }
    }
}

@Composable
fun LargeChipsPanel(
    categoryStats: List<CategoryCount>,
    onCategoryClick: (String) -> Unit
) {
    GlassCard(cornerRadius = 20.dp, shadowElevation = 8.dp, modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Column {
            Text("Large chips", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(12.dp))
            Spacer(Modifier.height(8.dp))
            if (categoryStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No categories", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryStats) { categoryCount ->
                        LargeChipItem(
                            label = categoryCount.category,
                            count = categoryCount.count,
                            onClick = { onCategoryClick(categoryCount.category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LargeChipItem(
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        cornerRadius = 16.dp,
        containerColor = Color(0xFF1F1F1F).copy(alpha = 0.85f),
        modifier = Modifier
            .height(60.dp)
            .width(110.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCard(
    category: String,
    count: Int,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        cornerRadius = 20.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(colors = gradient))
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = gradient.first().copy(alpha = 0.3f),
                            spotColor = gradient.first().copy(alpha = 0.4f),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcons[category] ?: Icons.Default.PushPin,
                        contentDescription = category,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Badge(
                    modifier = Modifier.padding(end = 4.dp),
                    containerColor = gradient.first().copy(alpha = 0.15f),
                    contentColor = gradient.first()
                ) {
                    Text(
                        text = "$count",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (count == 1) "1 notification" else "$count notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppCard(app: com.notilog.ui.groups.AppNotificationCount) {
    GlassCard(cornerRadius = 20.dp, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${app.count}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
