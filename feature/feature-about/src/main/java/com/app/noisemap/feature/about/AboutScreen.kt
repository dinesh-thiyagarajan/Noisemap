package com.app.noisemap.feature.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.noisemap.core.ui.theme.ColorAccentGreen
import com.app.noisemap.core.ui.theme.ColorAccentTeal
import com.app.noisemap.core.ui.theme.ColorBackground
import com.app.noisemap.core.ui.theme.ColorDivider
import com.app.noisemap.core.ui.theme.ColorSurface
import com.app.noisemap.core.ui.theme.ColorSurfaceElevated
import com.app.noisemap.core.ui.theme.ColorTextMuted
import com.app.noisemap.core.ui.theme.ColorTextPrimary
import com.app.noisemap.core.ui.theme.ColorTextSecondary
import kotlinx.coroutines.delay

private val howItWorksSteps = listOf(
    Triple("🔔", "We listen", "We use Android's NotificationListenerService to observe notifications as they arrive."),
    Triple("📝", "We record", "Each notification's arrival time, removal time, and how it was dismissed is stored in a local database on your device."),
    Triple("🔍", "We analyse", "We calculate patterns: which apps interrupt you most, at what hours, and whether you actually respond."),
    Triple("💡", "We show you", "You get charts, scores, and plain-language insights to help you reclaim your attention."),
)

private val privacyPoints = listOf(
    "All data is stored only on this device",
    "We never read the content of your notifications for analytics",
    "No account, no cloud, no tracking",
)

@Composable
fun AboutScreen() {
    var versionTapCount by remember { mutableIntStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Hero
        item { HeroSection() }

        // How it works
        item {
            Text(
                text = "Under the Hood",
                style = MaterialTheme.typography.titleLarge,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        itemsIndexed(howItWorksSteps) { index, (icon, title, detail) ->
            StepCard(index = index, icon = icon, title = title, detail = detail)
        }

        // Privacy
        item {
            Text(
                text = "Your Data, Stays Yours",
                style = MaterialTheme.typography.titleLarge,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        privacyPoints.forEach { point ->
            item { PrivacyCard(text = point) }
        }

        // Footer
        item {
            AppFooter(
                onVersionTap = {
                    versionTapCount++
                    if (versionTapCount >= 5) showEasterEgg = true
                },
            )
        }

        if (showEasterEgg) {
            item { EasterEgg() }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun HeroSection() {
    var pulsing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            pulsing = true
            delay(1200)
            pulsing = false
            delay(800)
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (pulsing) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "LogoPulse",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ColorAccentTeal.copy(alpha = 0.3f),
                            ColorAccentTeal.copy(alpha = 0.05f),
                        ),
                    ),
                )
                .border(2.dp, ColorAccentTeal.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("🔔", fontSize = 42.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Notifiq",
            style = MaterialTheme.typography.headlineLarge,
            color = ColorTextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Know your noise. Own your focus.",
            style = MaterialTheme.typography.bodyLarge,
            color = ColorAccentTeal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepCard(index: Int, icon: String, title: String, detail: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 150L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ColorSurfaceElevated)
                .border(1.dp, ColorDivider, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Step number + icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ColorAccentTeal.copy(alpha = 0.12f))
                        .border(1.dp, ColorAccentTeal.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(icon, fontSize = 20.sp)
                }
                if (index < howItWorksSteps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(ColorDivider),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PrivacyCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorAccentGreen.copy(alpha = 0.07f))
            .border(1.dp, ColorAccentGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("✅", fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextPrimary,
        )
    }
}

@Composable
private fun AppFooter(onVersionTap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .border(1.dp, ColorDivider, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Notifiq",
            style = MaterialTheme.typography.titleLarge,
            color = ColorTextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Version 1.0",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextSecondary,
            modifier = Modifier.clickable { onVersionTap() },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Made with ❤️ and Kotlin",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextMuted,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Privacy-first · On-device · No tracking",
            style = MaterialTheme.typography.labelSmall,
            color = ColorTextMuted,
        )
    }
}

@Composable
private fun EasterEgg() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorAccentTeal.copy(alpha = 0.1f))
            .border(1.dp, ColorAccentTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You found the Easter egg!",
                style = MaterialTheme.typography.bodyLarge,
                color = ColorAccentTeal,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Thanks for exploring Notifiq 🔔",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextSecondary,
            )
        }
    }
}
