package com.app.noisemap.feature.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.noisemap.core.ui.theme.NoisemapColors
import kotlinx.coroutines.delay

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoisemapColors.Background),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 8.dp),
        ) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium,
                color = NoisemapColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Last 7 days",
                style = MaterialTheme.typography.bodyMedium,
                color = NoisemapColors.TextSecondary,
            )
        }

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NoisemapColors.AccentTeal)
            }
            uiState.insightCards.isEmpty() -> EmptyInsights()
            else -> InsightsContent(uiState = uiState)
        }
    }
}

@Composable
private fun InsightsContent(uiState: InsightsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Summary chip
        item {
            SummaryBanner(total = uiState.totalThisWeek)
        }

        // Insight cards with stagger
        itemsIndexed(uiState.insightCards) { index, card ->
            StaggeredInsightCard(index = index, card = card)
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SummaryBanner(total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoisemapColors.AccentTeal.copy(alpha = 0.1f))
            .border(1.dp, NoisemapColors.AccentTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔔", fontSize = 32.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NoisemapColors.AccentTeal,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "interruptions this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoisemapColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun StaggeredInsightCard(index: Int, card: InsightCard) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 120L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        ) + fadeIn(),
    ) {
        InsightCardItem(card = card)
    }
}

@Composable
private fun InsightCardItem(card: InsightCard) {
    val (bgColor, borderColor) = when (card.type) {
        InsightCardType.POSITIVE -> NoisemapColors.AccentGreen.copy(0.08f) to NoisemapColors.AccentGreen.copy(0.25f)
        InsightCardType.NEGATIVE -> NoisemapColors.AccentRed.copy(0.08f) to NoisemapColors.AccentRed.copy(0.25f)
        InsightCardType.TIP -> NoisemapColors.AccentTeal.copy(0.08f) to NoisemapColors.AccentTeal.copy(0.25f)
        InsightCardType.NEUTRAL -> NoisemapColors.AccentAmber.copy(0.08f) to NoisemapColors.AccentAmber.copy(0.25f)
    }
    val dotColor = when (card.type) {
        InsightCardType.POSITIVE -> NoisemapColors.AccentGreen
        InsightCardType.NEGATIVE -> NoisemapColors.AccentRed
        InsightCardType.TIP -> NoisemapColors.AccentTeal
        InsightCardType.NEUTRAL -> NoisemapColors.AccentAmber
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(card.emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.headline,
                style = MaterialTheme.typography.bodyLarge,
                color = NoisemapColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = NoisemapColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun EmptyInsights() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💡", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Insights will appear once\nyou've had some notifications",
                style = MaterialTheme.typography.bodyLarge,
                color = NoisemapColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
