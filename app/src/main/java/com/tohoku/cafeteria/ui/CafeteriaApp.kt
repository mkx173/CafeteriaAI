package com.tohoku.cafeteria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tohoku.cafeteria.ui.navigation.CafeteriaNavHost

@Composable
fun CafeteriaApp(navController: NavHostController = rememberNavController()) {
    CafeteriaNavHost(navController = navController)
}
