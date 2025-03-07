package com.tohoku.cafeteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.data.repository.SettingsRepository
import com.tohoku.cafeteria.ui.CafeteriaApp
import com.tohoku.cafeteria.ui.settings.DarkModeOption
import com.tohoku.cafeteria.ui.settings.SettingsViewModel
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get repository before super.onCreate to load theme settings early
        settingsRepository = (application as CafeteriaApplication).appContainer.settingsRepository

        // Load theme settings synchronously, blocking only momentarily
        val settingsState = runBlocking {
            settingsRepository.getSettings().first()
        }

        // Set the night mode based on saved preferences BEFORE super.onCreate
        val nightMode = when (settingsState.darkModeOption) {
            DarkModeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            DarkModeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DarkModeOption.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

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