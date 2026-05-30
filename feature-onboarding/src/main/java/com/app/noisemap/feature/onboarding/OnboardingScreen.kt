package com.app.noisemap.feature.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.noisemap.core.ui.theme.NoisemapColors
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners",
            )
            if (flat?.contains(context.packageName) == true) {
                onPermissionGranted()
                break
            }
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoisemapColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Animated icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(NoisemapColors.AccentTeal.copy(alpha = 0.1f))
                    .border(2.dp, NoisemapColors.AccentTeal.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔔", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Know your noise.\nOwn your focus.",
                style = MaterialTheme.typography.headlineLarge,
                color = NoisemapColors.TextPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "See what's really interrupting you — all on your device.",
                style = MaterialTheme.typography.bodyLarge,
                color = NoisemapColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            val features = listOf(
                "🔔" to "We listen to your notifications",
                "📊" to "We analyse patterns — all on your device",
                "🔒" to "Nothing ever leaves your phone",
            )

            features.forEachIndexed { index, (icon, text) ->
                AnimatedFeatureItem(index = index, icon = icon, text = text)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(56.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoisemapColors.AccentTeal),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "Give Notification Access",
                    color = NoisemapColors.Background,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You can revoke access anytime in Settings",
                style = MaterialTheme.typography.bodyMedium,
                color = NoisemapColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AnimatedFeatureItem(index: Int, icon: String, text: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400L + index * 150L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NoisemapColors.SurfaceElevated)
                .border(1.dp, NoisemapColors.BorderDefault, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(icon, fontSize = 24.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = NoisemapColors.TextSecondary,
            )
        }
    }
}
