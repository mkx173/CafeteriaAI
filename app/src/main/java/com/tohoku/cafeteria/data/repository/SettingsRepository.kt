package com.tohoku.cafeteria.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tohoku.cafeteria.ui.settings.DarkModeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SettingsState(
    val darkModeOption: DarkModeOption = DarkModeOption.FOLLOW_SYSTEM,
    val dynamicColorEnabled: Boolean = true
)

// Extension property to create a DataStore instance
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // Define keys for the settings
    private val DARK_MODE_OPTION_KEY = intPreferencesKey("dark_mode_option")
    private val DYNAMIC_COLOR_ENABLED_KEY = booleanPreferencesKey("dynamic_color_enabled")

    // Retrieve the unified settings state as a Flow.
    fun getSettings(): Flow<SettingsState> =
        context.dataStore.data.map { preferences ->
            val ordinal = preferences[DARK_MODE_OPTION_KEY] ?: DarkModeOption.FOLLOW_SYSTEM.ordinal
            val darkModeOption = DarkModeOption.entries[ordinal]
            val dynamicColorEnabled = preferences[DYNAMIC_COLOR_ENABLED_KEY] ?: true
            SettingsState(darkModeOption, dynamicColorEnabled)
        }

    // Persist the unified settings state
    suspend fun updateSettings(newSettings: SettingsState) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_OPTION_KEY] = newSettings.darkModeOption.ordinal
            preferences[DYNAMIC_COLOR_ENABLED_KEY] = newSettings.dynamicColorEnabled
        }
    }

    // Persist the dark mode option
    suspend fun setDarkModeOption(option: DarkModeOption) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_OPTION_KEY] = option.ordinal
        }
    }

    // Persist the dynamic color enabled flag
    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_ENABLED_KEY] = enabled
        }
    }
}
