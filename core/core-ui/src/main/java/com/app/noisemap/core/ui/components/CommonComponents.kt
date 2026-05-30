package com.app.noisemap.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.noisemap.core.ui.theme.NoisemapColors
import com.app.noisemap.core.ui.theme.NoisemapTypography
import kotlinx.coroutines.delay

enum class Verdict {
    SIGNAL, NOISE, MIXED
}

enum class NotificationAction {
    TAPPED, DISMISSED, ACTIVE
}

enum class InsightType {
    NOISE, SIGNAL
}

@Composable
fun NoisemapCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NoisemapColors.Surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, NoisemapColors.BorderDefault, RoundedCornerShape(12.dp))
            .padding(12.dp),
        content = content,
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = NoisemapColors.TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = modifier,
    )
}

@Composable
fun VerdictBadge(verdict: Verdict) {
    val (label, bg, fg) = when (verdict) {
        Verdict.NOISE  -> Triple("NOISE",  NoisemapColors.TintRed,   NoisemapColors.AccentRed)
        Verdict.SIGNAL -> Triple("SIGNAL", NoisemapColors.TintGreen, NoisemapColors.AccentGreen)
        Verdict.MIXED  -> Triple("MIXED",  NoisemapColors.TintAmber, NoisemapColors.AccentAmber)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun ActionTag(action: NotificationAction, timeLabel: String) {
    val (label, bg, fg) = when (action) {
        NotificationAction.TAPPED    -> Triple("Tapped",    NoisemapColors.TintGreen, NoisemapColors.AccentGreen)
        NotificationAction.DISMISSED -> Triple("Dismissed", NoisemapColors.TintRed,   NoisemapColors.AccentRed)
        NotificationAction.ACTIVE    -> Triple("Active",    NoisemapColors.TintTeal,  NoisemapColors.AccentTeal)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$label · $timeLabel", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
fun NoisemapFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) NoisemapColors.TintTeal else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) NoisemapColors.AccentTeal else NoisemapColors.BorderDefault,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) NoisemapColors.AccentTeal else NoisemapColors.TextSecondary,
        )
    }
}

@Composable
fun AnimatedCounter(
    target: Int,
    style: TextStyle = NoisemapTypography.displayMedium,
) {
    val animatedValue by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "counter",
    )
    Text(text = animatedValue.toString(), style = style)
}

@Composable
fun PulsingDot(color: Color = NoisemapColors.AccentTeal, size: Dp = 6.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    Box(
        modifier = Modifier
            .size(size * scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.5f)),
    )
}

@Composable
fun FocusScoreArc(
    score: Int,
    modifier: Modifier = Modifier.size(100.dp),
    strokeWidth: Dp = 5.dp,
) {
    val scoreColor = when {
        score < 40 -> NoisemapColors.AccentRed
        score < 70 -> NoisemapColors.AccentAmber
        else       -> NoisemapColors.AccentTeal
    }
    val animatedSweep by animateFloatAsState(
        targetValue = score / 100f * 240f,
        animationSpec = tween(1500, easing = EaseOutBack),
        label = "arcSweep",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = NoisemapColors.SurfaceElevated,
                startAngle = 150f, sweepAngle = 240f,
                useCenter = false, style = stroke,
            )
            drawArc(
                color = scoreColor,
                startAngle = 150f, sweepAngle = animatedSweep,
                useCenter = false, style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedCounter(score, style = NoisemapTypography.displayLarge)
            Text("Focus Score", style = NoisemapTypography.labelSmall)
        }
    }
}

@Composable
fun ShimmerCard(height: Dp = 120.dp) {
    // Placeholder loading card with pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(NoisemapColors.Surface)
            .alpha(alpha),
    )
}

@Composable
fun AnimatedListItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 80L); visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialOffsetY = { it / 3 },
        ),
    ) { content() }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NoisemapColors.SurfaceElevated)
            .border(1.dp, NoisemapColors.BorderDefault, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = NoisemapColors.AccentTeal,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NoisemapColors.TextSecondary,
        )
    }
}

@Composable
fun InsightBadge(text: String, type: InsightType) {
    val (bgColor, fgColor) = when (type) {
        InsightType.NOISE -> NoisemapColors.TintRed to NoisemapColors.AccentRed
        InsightType.SIGNAL -> NoisemapColors.TintGreen to NoisemapColors.AccentGreen
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, fgColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fgColor,
        )
    }
}
