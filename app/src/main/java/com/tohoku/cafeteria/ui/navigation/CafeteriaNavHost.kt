package com.tohoku.cafeteria.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.cart.rememberCartViewModel
import com.tohoku.cafeteria.ui.history.HistoryScreen
import com.tohoku.cafeteria.ui.menu.MenuScreen
import com.tohoku.cafeteria.ui.recommendation.RecommendationResultScreen
import com.tohoku.cafeteria.ui.recommendation.RecommendationScreen
import com.tohoku.cafeteria.ui.recommendation.RecommendationViewModel
import com.tohoku.cafeteria.ui.settings.SettingsScreen

// Define the screens as sealed objects
sealed class Screen(val route: String, @StringRes val label: Int) {
    object Menu : Screen("menu", R.string.tab_menu)
    object Recommendation : Screen("recommendation", R.string.tab_recommendation)
    object History : Screen("history", R.string.tab_history)
    object Settings : Screen("settings", R.string.tab_settings)
}

private data class NavigationItemContent(
    val screen: Screen,
    val icon: ImageVector,
)

// List of bottom navigation items
private val bottomNavItems = listOf(
    NavigationItemContent(Screen.Menu, Icons.AutoMirrored.Default.MenuBook),
    NavigationItemContent(Screen.Recommendation, Icons.Default.Reviews),
    NavigationItemContent(Screen.History, Icons.Default.History),
    NavigationItemContent(Screen.Settings, Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeteriaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentRoute = currentRoute(navController)
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isInRecommendationSection = currentDestination?.hierarchy?.any { it.route == Screen.Recommendation.route } == true
    val isOnRecommendationMain = currentRoute == "main"

    // Shared cart ViewModel
    val cartViewModel: CartViewModel = rememberCartViewModel(
        viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    )

    val recommendationViewModel: RecommendationViewModel = viewModel(factory = RecommendationViewModel.Factory)

    // Collect cart data
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { navItem ->
                    NavigationBarItem(
                        selected = isInRecommendationSection && navItem.screen == Screen.Recommendation
                                || currentRoute == navItem.screen.route,
                        onClick = {
                            if (navItem.screen == Screen.Recommendation) {
                                if (!isInRecommendationSection) {
                                    navController.navigate(navItem.screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else if (!isOnRecommendationMain) {
                                    navController.popBackStack("main", false)
                                }
                            } else if (currentRoute != navItem.screen.route) {
                                navController.navigate(navItem.screen.route) {
                                    // Avoid building up a large stack of destinations
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    // Only show badge on the Recommendation tab
                                    if (navItem.screen == Screen.Recommendation && cartItemCount > 0) {
                                        Badge {
                                            Text(
                                                text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = stringResource(navItem.screen.label)
                                )
                            }
                        }
                    )
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Menu.route,
        ) {
            composable(Screen.Menu.route) {
                MenuScreen(
                    modifier = Modifier.padding(innerPadding),
                    cartViewModel = cartViewModel
                )
            }
            navigation(
                startDestination = "main",
                route = Screen.Recommendation.route
            ) {
                composable("main") {
                    RecommendationScreen(
                        modifier = Modifier.padding(innerPadding),
                        cartViewModel = cartViewModel,
                        recommendationViewModel = recommendationViewModel,
                        onGetRecommendationClick = {
                            recommendationViewModel.fetchRecommendation(cartViewModel.getCartItems())
                            navController.navigate("result")
                        }
                    )
                }
                composable("result") {
                    RecommendationResultScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = recommendationViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        cartViewModel = cartViewModel
                    )
                }
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
