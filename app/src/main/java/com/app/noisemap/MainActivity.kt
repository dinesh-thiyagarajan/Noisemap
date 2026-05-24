package com.app.noisemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.noisemap.core.common.util.PermissionUtil
import com.app.noisemap.core.ui.theme.NoisemapTheme
import com.app.noisemap.navigation.DashboardRoute
import com.app.noisemap.navigation.NoisemapNavGraph
import com.app.noisemap.navigation.OnboardingRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoisemapTheme {
                val startDestination = if (PermissionUtil.isNotificationListenerPermissionGranted(this)) {
                    DashboardRoute
                } else {
                    OnboardingRoute
                }
                NoisemapNavGraph(startDestination = startDestination)
            }
        }
    }
}
