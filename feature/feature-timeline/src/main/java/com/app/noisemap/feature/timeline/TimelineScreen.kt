package com.app.noisemap.feature.timeline

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.ui.theme.ColorAccentGreen
import com.app.noisemap.core.ui.theme.ColorAccentRed
import com.app.noisemap.core.ui.theme.ColorAccentTeal
import com.app.noisemap.core.ui.theme.ColorBackground
import com.app.noisemap.core.ui.theme.ColorDivider
import com.app.noisemap.core.ui.theme.ColorSurface
import com.app.noisemap.core.ui.theme.ColorSurfaceElevated
import com.app.noisemap.core.ui.theme.ColorTextMuted
import com.app.noisemap.core.ui.theme.ColorTextPrimary
import com.app.noisemap.core.ui.theme.ColorTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground),
    ) {
        TimelineHeader(
            searchQuery = uiState.searchQuery,
            activeFilter = uiState.activeFilter,
            onSearchChange = viewModel::onSearchQueryChange,
            onFilterChange = viewModel::onFilterChange,
        )

        if (uiState.notifications.isEmpty() && !uiState.isLoading) {
            EmptyTimeline()
        } else {
            val grouped = uiState.notifications.groupByDay()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                grouped.forEach { (dayLabel, notifications) ->
                    item(key = dayLabel) {
                        DayHeader(label = dayLabel)
                    }
                    items(notifications, key = { it.key }) { notification ->
                        TimelineRow(notification = notification)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineHeader(
    searchQuery: String,
    activeFilter: TimelineFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (TimelineFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackground)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
    ) {
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorTextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search notifications…", color = ColorTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ColorTextMuted) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ColorSurfaceElevated,
                unfocusedContainerColor = ColorSurfaceElevated,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = ColorTextPrimary,
                unfocusedTextColor = ColorTextPrimary,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TimelineFilter.entries.forEach { filter ->
                val label = filter.name.lowercase().replaceFirstChar { it.uppercase() }
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(label, modifier = Modifier.animateContentSize()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorAccentTeal.copy(alpha = 0.15f),
                        selectedLabelColor = ColorAccentTeal,
                        containerColor = ColorSurfaceElevated,
                        labelColor = ColorTextSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = activeFilter == filter,
                        selectedBorderColor = ColorAccentTeal.copy(alpha = 0.5f),
                        borderColor = ColorDivider,
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DayHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = ColorTextMuted,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun TimelineRow(notification: Notification) {
    val indicatorColor = when {
        notification.removalReason == 1 -> ColorAccentGreen
        notification.removalReason != null -> ColorAccentRed
        else -> ColorAccentTeal
    }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(indicatorColor),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ColorSurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text("📱", fontSize = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.appName.ifEmpty { notification.packageName },
                style = MaterialTheme.typography.labelSmall,
                color = ColorTextSecondary,
            )
            Text(
                text = notification.title ?: "(No title)",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            notification.body?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = timeFormatter.format(Date(notification.postedAt)),
            style = MaterialTheme.typography.labelSmall,
            color = ColorTextMuted,
        )
    }
}

@Composable
private fun EmptyTimeline() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your notification history\nwill appear here",
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

private val dayFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
private val todayStr = dayFormatter.format(Date())

private fun List<Notification>.groupByDay(): Map<String, List<Notification>> {
    return groupBy { notification ->
        val day = dayFormatter.format(Date(notification.postedAt))
        if (day == todayStr) "Today" else day
    }
}
