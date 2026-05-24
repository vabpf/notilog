package com.notilog.ui.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
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
fun AdvancedFilterScreen(
    onBack: () -> Unit = {},
    onApplyFilters: (Map<String, String>) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedApps = remember { mutableStateListOf<String>() }
    val selectedCategories = remember { mutableStateListOf<String>() }
    var selectedTimeRange by remember { mutableStateOf("This Week") }
    var isResetPressed by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showAppSearchDialog by remember { mutableStateOf(false) }

    var headerBottomPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerBottomDp = with(density) { headerBottomPx.toDp() }

    Scaffold(
        topBar = {},
        containerColor = Color.Transparent,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 116.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = { /* Apply */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Apply Filters (142)", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                            bottom = 200.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            FilterSearchSection(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it }
                            )
                        }
                        item { AppSelectionSection(onShowAppSearch = { showAppSearchDialog = true }) }
                        item { CategoryGridSection() }
                        item {
                            TimeRangeSection(
                                selectedRange = selectedTimeRange,
                                onRangeSelected = { selectedTimeRange = it },
                                onShowCustomDatePicker = { showCustomDatePicker = true }
                            )
                        }
                    }
                }
            }

            // Floating header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                    .onGloballyPositioned { coords ->
                        headerBottomPx =
                            (coords.positionInRoot().y + coords.size.height).toInt()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "Filters",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                val resetBgColor by animateColorAsState(
                    targetValue = if (isResetPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                   else Color(0xFFF0F0F0),
                    label = "resetButtonBg"
                )
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(resetBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isResetPressed = !isResetPressed }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showAppSearchDialog) {
        AppSearchDialog(
            onDismiss = { showAppSearchDialog = false },
            onAppSelected = { app ->
                selectedApps.add(app)
                showAppSearchDialog = false
            }
        )
    }

    if (showCustomDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onDateRangeSelected = { _, _ ->
                showCustomDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSearchSection(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search senders or keywords...") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
}

@Composable
private fun AppSelectionSection(onShowAppSearch: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onShowAppSearch) {
                Text("See More >", style = MaterialTheme.typography.labelSmall)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listOf("Mail", "Messages", "Slack", "Calendar", "System")) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (app == "Mail") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = if (app == "Mail") MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        app,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSearchDialog(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val mockApps = listOf(
        "Mail", "Messages", "Slack", "Calendar", "System",
        "Gmail", "WhatsApp", "Telegram", "Discord", "Teams",
        "Twitter", "Instagram", "Reddit", "LinkedIn", "Facebook"
    )
    val filteredApps = mockApps.filter { 
        it.contains(searchQuery, ignoreCase = true) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Apps") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("Search...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredApps) { app ->
                        Text(
                            app,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(app) }
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CategoryGridSection() {
    Column {
        Text(
            "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Finance", "Travel", "Shopping", "Social", "Security", "Promotions").forEach { category ->
                val isSelected = category == "Finance" || category == "Social"
                FilterChip(
                    selected = isSelected,
                    onClick = { },
                    label = { Text(category) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSection(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    onShowCustomDatePicker: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Time Range",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onShowCustomDatePicker) {
                Text(
                    "Custom >",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeRangeCard(
                    "Today", "Last 24 hours", isSelected = false,
                    modifier = Modifier.weight(1f)
                )
                TimeRangeCard(
                    "This Week", "Past 7 days", isSelected = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeRangeCard(
                    "This Month", "Past 30 days", isSelected = false,
                    modifier = Modifier.weight(1f)
                )
                TimeRangeCard(
                    "Older", "Archive", isSelected = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimeRangeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Start Date",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("MM/DD/YYYY") },
                    singleLine = true
                )
                Text(
                    "End Date",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("MM/DD/YYYY") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                        onDateRangeSelected(startDate, endDate)
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width + mainAxisSpacing.roundToPx() > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val height = rows.sumOf { row -> row.maxOf { it.height } } +
            (rows.size - 1) * crossAxisSpacing.roundToPx()

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += rowHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}
