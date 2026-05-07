package com.notilog.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onSettingsClick: () -> Unit = {}
) {
    var headerBottomPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerBottomDp = with(density) { headerBottomPx.toDp() }

    Scaffold(
        topBar = {},
        containerColor = Color.Transparent
    ) { _ ->
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
                        contentPadding = PaddingValues(
                            top = 24.dp,
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 132.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                MetricCard(
                                    title = "Total Blocked",
                                    value = "1,402",
                                    change = "+12%",
                                    isPositive = true,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    title = "Recovered",
                                    value = "87",
                                    change = "-5%",
                                    isPositive = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            ChartCard(title = "Notification Volume") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Volume Graph Placeholder",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        item {
                            ChartCard(title = "Notifications by App") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Apps", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(Modifier.width(24.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        LegendItem(Color(0xFF00428E), "Messages")
                                        LegendItem(Color(0xFF4854BB), "Social")
                                        LegendItem(Color(0xFF7C2900), "System")
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(32.dp))
                        }
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
                        headerBottomPx =
                            (coords.positionInRoot().y + coords.size.height).toInt()
                    }
            ) {
                Text(
                    "Deep Insights",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).sp
                )
                Text(
                    "Analyzing your digital flow over the last 7 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isPositive) Icons.Default.Warning else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = if (isPositive) MaterialTheme.colorScheme.secondary
                           else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        change,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositive) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                }
            }
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
