package com.app.noisemap.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.noisemap.core.ui.theme.ColorAccentTeal
import com.app.noisemap.core.ui.theme.ColorBackground
import com.app.noisemap.core.ui.theme.ColorSurface
import com.app.noisemap.core.ui.theme.ColorTextMuted
import com.app.noisemap.core.ui.theme.ColorTextPrimary
import com.app.noisemap.feature.about.AboutScreen
import com.app.noisemap.feature.appdetail.AppDetailScreen
import com.app.noisemap.feature.dashboard.DashboardScreen
import com.app.noisemap.feature.insights.InsightsScreen
import com.app.noisemap.feature.onboarding.OnboardingScreen
import com.app.noisemap.feature.timeline.TimelineScreen

private data class BottomNavItem(
    val label: String,
    val icon: String,
    val isSelected: (NavDestination?) -> Boolean,
    val navigate: (NavHostController) -> Unit,
)

@SuppressLint("RestrictedApi")
private fun bottomNavItems() = listOf(
    BottomNavItem(
        label = "Dashboard", icon = "📊",
        isSelected = { it?.hasRoute(DashboardRoute::class) == true },
        navigate = { nav -> nav.navigate(DashboardRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "Timeline", icon = "📋",
        isSelected = { it?.hasRoute(TimelineRoute::class) == true },
        navigate = { nav -> nav.navigate(TimelineRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "Insights", icon = "💡",
        isSelected = { it?.hasRoute(InsightsRoute::class) == true },
        navigate = { nav -> nav.navigate(InsightsRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "About", icon = "ℹ️",
        isSelected = { it?.hasRoute(AboutRoute::class) == true },
        navigate = { nav -> nav.navigate(AboutRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
)

@SuppressLint("RestrictedApi")
@Composable
fun NoisemapNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any,
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDest = currentBackStack?.destination

    val showBottomBar = currentDest != null &&
        !currentDest.hasRoute(OnboardingRoute::class) &&
        !currentDest.hasRoute(AppDetailRoute::class)

    val items = bottomNavItems()

    Scaffold(
        containerColor = ColorBackground,
        contentColor = ColorTextPrimary,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                NavigationBar(
                    containerColor = ColorSurface,
                    tonalElevation = 0.dp,
                ) {
                    items.forEach { item ->
                        val selected = item.isSelected(currentDest)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { item.navigate(navController) },
                            icon = { Text(item.icon) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorAccentTeal,
                                selectedTextColor = ColorAccentTeal,
                                unselectedIconColor = ColorTextMuted,
                                unselectedTextColor = ColorTextMuted,
                                indicatorColor = ColorAccentTeal.copy(alpha = 0.12f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onPermissionGranted = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<DashboardRoute> {
                DashboardScreen(
                    onAppClick = { packageName ->
                        navController.navigate(AppDetailRoute(packageName))
                    },
                )
            }

            composable<TimelineRoute> {
                TimelineScreen()
            }

            composable<InsightsRoute> {
                InsightsScreen()
            }

            composable<AboutRoute> {
                AboutScreen()
            }

            composable<AppDetailRoute> { backStack ->
                val route = backStack.toRoute<AppDetailRoute>()
                AppDetailScreen(
                    packageName = route.packageName,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
