package com.tohoku.cafeteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.data.repository.SettingsRepository
import com.tohoku.cafeteria.ui.CafeteriaApp
import com.tohoku.cafeteria.ui.settings.DarkModeOption
import com.tohoku.cafeteria.ui.settings.SettingsViewModel
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep splash screen visible until we're done initializing
        val splashScreen = installSplashScreen()

        // Get repository before super.onCreate to load theme settings early
        settingsRepository = (application as CafeteriaApplication).appContainer.settingsRepository
        val initialSettings = settingsRepository.settingsState.value

        // Set the night mode based on saved preferences BEFORE super.onCreate
        val nightMode = when (initialSettings.darkModeOption) {
            DarkModeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            DarkModeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DarkModeOption.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        // Allow splash screen to hide when ready
        splashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Now use the pre-loaded values directly
            val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)

            // Use collectAsState for updates after initial load
            val settingsState by viewModel.settingsState.collectAsState()

            // Determine if dark theme should be used based on the setting
            val isDarkTheme = when (settingsState.darkModeOption) {
                DarkModeOption.DARK -> true
                DarkModeOption.LIGHT -> false
                DarkModeOption.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme

            CafeteriaAITheme(
                darkTheme = isDarkTheme,
                dynamicColor = settingsState.dynamicColorEnabled
            ) {
                CafeteriaApp()
            }
        }
    }
}