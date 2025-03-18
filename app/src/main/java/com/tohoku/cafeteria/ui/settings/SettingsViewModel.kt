package com.tohoku.cafeteria.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.data.repository.PersonalInfo
import com.tohoku.cafeteria.data.repository.SettingsRepository
import com.tohoku.cafeteria.data.repository.SettingsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    // Expose settings as flows
    val settingsState: StateFlow<SettingsState> = settingsRepository.settingsState

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

    fun setBmrOption(option: BmrCalculationOption) {
        viewModelScope.launch {
            settingsRepository.setBmrOption(option)
        }
    }

    fun setCustomBmrValue(value: Int) {
        viewModelScope.launch {
            settingsRepository.setCustomBmrValue(value)
        }
    }

    fun setPersonalInfo(personalInfo: PersonalInfo) {
        viewModelScope.launch {
            settingsRepository.setPersonalInfo(personalInfo)
        }
    }

    fun setFoodPreferences(foodPreferences: String) {
        viewModelScope.launch {
            settingsRepository.setFoodPreferences(foodPreferences)
        }
    }

    fun setFoodAllergies(foodAllergies: String) {
        viewModelScope.launch {
            settingsRepository.setFoodAllergies(foodAllergies)
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
