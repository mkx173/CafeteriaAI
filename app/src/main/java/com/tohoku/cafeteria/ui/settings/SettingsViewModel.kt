package com.tohoku.cafeteria.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.data.repository.SettingsRepository
import com.tohoku.cafeteria.data.repository.SettingsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    // Expose settings as flows
    val settingsState: StateFlow<SettingsState> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { settingsRepository.getSettings().first() }
        )

    // Update functions that delegate to the repository
    fun setDarkModeOption(option: DarkModeOption) {
        viewModelScope.launch {
            settingsRepository.setDarkModeOption(option)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColorEnabled(enabled)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val settingsRepository = application.appContainer.settingsRepository
                SettingsViewModel(settingsRepository = settingsRepository)
            }
        }
    }
}
