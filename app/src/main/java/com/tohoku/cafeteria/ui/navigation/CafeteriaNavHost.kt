package com.tohoku.cafeteria.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.cart.rememberCartViewModel
import com.tohoku.cafeteria.ui.history.HistoryScreen
import com.tohoku.cafeteria.ui.menu.MenuScreen
import com.tohoku.cafeteria.ui.recommendation.RecommendationScreen
import com.tohoku.cafeteria.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

object SnackbarManager {
    private val _messages = MutableStateFlow<String?>(null)
    val messages = _messages.asStateFlow()

    fun showMessage(message: String) {
        _messages.value = message
    }

    fun clearMessage() {
        _messages.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeteriaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = currentRoute(navController)

    // Shared cart ViewModel
    val cartViewModel: CartViewModel = rememberCartViewModel(
        viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    )

    // Collect cart data
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    // Collect and display messages from the SnackbarManager
    val message by SnackbarManager.messages.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            SnackbarManager.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentRoute == navItem.screen.route,
                        onClick = {
                            if (currentRoute != navItem.screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Menu.route,
            modifier = Modifier.consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Menu.route) {
                MenuScreen(
                    modifier = Modifier.padding(innerPadding),
                    cartViewModel = cartViewModel
                )
            }
            composable(Screen.Recommendation.route) {
                RecommendationScreen(
                    modifier = Modifier.padding(innerPadding),
                    cartViewModel = cartViewModel
                )
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
