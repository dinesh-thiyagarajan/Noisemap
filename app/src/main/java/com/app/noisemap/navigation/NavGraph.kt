package com.app.noisemap.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.noisemap.R
import com.app.noisemap.core.ui.theme.NoisemapColors
import com.app.noisemap.feature.about.AboutScreen
import com.app.noisemap.feature.appdetail.AppDetailScreen
import com.app.noisemap.feature.dashboard.DashboardScreen
import com.app.noisemap.feature.insights.InsightsScreen
import com.app.noisemap.feature.onboarding.OnboardingScreen
import com.app.noisemap.feature.timeline.TimelineScreen

private data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val isSelected: (NavDestination?) -> Boolean,
    val navigate: (NavHostController) -> Unit,
)

@SuppressLint("RestrictedApi")
private fun bottomNavItems() = listOf(
    BottomNavItem(
        label = "Dashboard", iconRes = R.drawable.ic_nav_dashboard,
        isSelected = { it?.hasRoute(DashboardRoute::class) == true },
        navigate = { nav -> nav.navigate(DashboardRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "Timeline", iconRes = R.drawable.ic_nav_timeline,
        isSelected = { it?.hasRoute(TimelineRoute::class) == true },
        navigate = { nav -> nav.navigate(TimelineRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "Insights", iconRes = R.drawable.ic_nav_insights,
        isSelected = { it?.hasRoute(InsightsRoute::class) == true },
        navigate = { nav -> nav.navigate(InsightsRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
    BottomNavItem(
        label = "About", iconRes = R.drawable.ic_nav_about,
        isSelected = { it?.hasRoute(AboutRoute::class) == true },
        navigate = { nav -> nav.navigate(AboutRoute) { launchSingleTop = true; restoreState = true; popUpTo(DashboardRoute) { saveState = true } } },
    ),
)

@Composable
fun NoisemapBottomNav(
    navController: NavHostController,
    currentDest: NavDestination?,
) {
    val items = bottomNavItems()
    NavigationBar(
        containerColor = NoisemapColors.SurfaceNav,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 0.5.dp,
            color = NoisemapColors.BorderDefault,
            shape = RectangleShape,
        ),
    ) {
        items.forEach { item ->
            val selected = item.isSelected(currentDest)
            NavigationBarItem(
                selected = selected,
                onClick = { item.navigate(navController) },
                icon = {
                    Box(
                        modifier = if (selected)
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NoisemapColors.AccentTeal)
                                .padding(6.dp)
                        else
                            Modifier.padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label,
                            tint = if (selected) NoisemapColors.Background
                                   else NoisemapColors.TextMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 10.sp,
                        color = if (selected) NoisemapColors.AccentTeal
                                else NoisemapColors.TextMuted,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

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

    Scaffold(
        containerColor = NoisemapColors.Background,
        contentColor = NoisemapColors.TextPrimary,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                NoisemapBottomNav(navController, currentDest)
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
