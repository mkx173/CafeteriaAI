package com.tohoku.cafeteria.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tohoku.cafeteria.ui.settings.BmrCalculationOption
import com.tohoku.cafeteria.ui.settings.DarkModeOption
import com.tohoku.cafeteria.ui.settings.ExerciseLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val darkModeOption: DarkModeOption = DarkModeOption.FOLLOW_SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val bmrOption: BmrCalculationOption = BmrCalculationOption.DEFAULT,
    val customBmrValue: Int = 2000,
    val personalInfo: PersonalInfo = PersonalInfo(),
    val foodPreferences: String = "",
    val foodAllergies: String = ""
)

data class PersonalInfo(
    val isMale: Boolean = true,
    val age: Int = 20,
    val weight: Int = 60,
    val height: Int = 170,
    val exerciseLevel: ExerciseLevel = ExerciseLevel.MODERATE
)

// Extension property to create a DataStore instance
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // Define keys for the settings
    private val DARK_MODE_OPTION_KEY = intPreferencesKey("dark_mode_option")
    private val DYNAMIC_COLOR_ENABLED_KEY = booleanPreferencesKey("dynamic_color_enabled")
    private val BMR_OPTION_KEY = intPreferencesKey("bmr_option")
    private val CUSTOM_BMR_VALUE_KEY = intPreferencesKey("custom_bmr_value")

    private val GENDER_OPTION_KEY = booleanPreferencesKey("gender_option")
    private val AGE_VALUE_KEY = intPreferencesKey("age_value")
    private val WEIGHT_VALUE_KEY = intPreferencesKey("weight_value")
    private val HEIGHT_VALUE_KEY = intPreferencesKey("height_value")
    private val EXERCISE_LEVEL_VALUE_KEY = intPreferencesKey("exercise_level_value")

    private val FOOD_PREFERENCES_KEY = stringPreferencesKey("food_preferences")
    private val FOOD_ALLERGIES_KEY = stringPreferencesKey("food_allergies")

    // MutableStateFlow to hold the current settings
    private val _settingsState = MutableStateFlow<SettingsState?>(null)

    // Retrieve the unified settings state as a Flow.
    val settingsState: StateFlow<SettingsState> = _settingsState
        .filterNotNull()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.Eagerly,
            initialValue = SettingsState()
        )

    // Initialize settings on creation
    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data
                .map { preferences -> preferencesToSettingsState(preferences) }
                .collect { settings ->
                    _settingsState.value = settings
                }
        }
    }

    private fun preferencesToSettingsState(preferences: Preferences): SettingsState {
        val darkModeOrdinal =
            preferences[DARK_MODE_OPTION_KEY] ?: DarkModeOption.FOLLOW_SYSTEM.ordinal
        val darkModeOption = DarkModeOption.entries[darkModeOrdinal]
        val dynamicColorEnabled = preferences[DYNAMIC_COLOR_ENABLED_KEY] ?: true

        val bmrOrdinal = preferences[BMR_OPTION_KEY] ?: BmrCalculationOption.DEFAULT.ordinal
        val bmrOption = BmrCalculationOption.entries[bmrOrdinal]
        val customBmrValue = preferences[CUSTOM_BMR_VALUE_KEY] ?: 2000

        val isMaleOption = preferences[GENDER_OPTION_KEY] ?: true
        val ageValue = preferences[AGE_VALUE_KEY] ?: 20
        val weightValue = preferences[WEIGHT_VALUE_KEY] ?: 60
        val heightValue = preferences[HEIGHT_VALUE_KEY] ?: 170
        val exerciseLevelOrdinal =
            preferences[EXERCISE_LEVEL_VALUE_KEY] ?: ExerciseLevel.MODERATE.ordinal
        val exerciseLevelOption = ExerciseLevel.entries[exerciseLevelOrdinal]

        val foodPreferences = preferences[FOOD_PREFERENCES_KEY] ?: ""
        val foodAllergies = preferences[FOOD_ALLERGIES_KEY] ?: ""

        val personalInfo = PersonalInfo(
            isMale = isMaleOption,
            age = ageValue,
            weight = weightValue,
            height = heightValue,
            exerciseLevel = exerciseLevelOption
        )

        return SettingsState(
            darkModeOption = darkModeOption,
            dynamicColorEnabled = dynamicColorEnabled,
            bmrOption = bmrOption,
            customBmrValue = customBmrValue,
            personalInfo = personalInfo,
            foodPreferences = foodPreferences,
            foodAllergies = foodAllergies
        )
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

    suspend fun setBmrOption(option: BmrCalculationOption) {
        context.dataStore.edit { preferences ->
            preferences[BMR_OPTION_KEY] = option.ordinal
        }
    }

    suspend fun setCustomBmrValue(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_BMR_VALUE_KEY] = value
        }
    }

    suspend fun setPersonalInfo(personalInfo: PersonalInfo) {
        context.dataStore.edit { preferences ->
            preferences[GENDER_OPTION_KEY] = personalInfo.isMale
            preferences[AGE_VALUE_KEY] = personalInfo.age
            preferences[WEIGHT_VALUE_KEY] = personalInfo.weight
            preferences[HEIGHT_VALUE_KEY] = personalInfo.height
            preferences[EXERCISE_LEVEL_VALUE_KEY] = personalInfo.exerciseLevel.ordinal
        }
    }

    suspend fun setFoodPreferences(foodPreferences: String) {
        context.dataStore.edit { preferences ->
            preferences[FOOD_PREFERENCES_KEY] = foodPreferences
        }
    }

    suspend fun setFoodAllergies(foodAllergies: String) {
        context.dataStore.edit { preferences ->
            preferences[FOOD_ALLERGIES_KEY] = foodAllergies
        }
    }
}
