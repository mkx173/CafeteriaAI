package com.tohoku.cafeteria.data.repository

import com.tohoku.cafeteria.data.datasource.FoodDataSource
import com.tohoku.cafeteria.data.request.RecommendationQuery
import com.tohoku.cafeteria.data.request.RecommendationRequest
import com.tohoku.cafeteria.data.response.RecommendationResponse
import com.tohoku.cafeteria.domain.mapper.FoodCategoryMapper
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.ui.settings.BmrCalculationOption
import com.tohoku.cafeteria.ui.settings.ExerciseLevel
import kotlinx.serialization.json.Json
import retrofit2.Response

class FoodRepository(
    private val dataSource: FoodDataSource,
    private val settingsRepository: SettingsRepository
) {
    suspend fun getMenu(): List<FoodCategory> {
        val responses = dataSource.getMenu()
        return responses.map { FoodCategoryMapper.fromResponse(it) }
    }

    // Build the recommendation request using current settings and then call the API.
    suspend fun requestRecommendation(additionalNotes: String): Response<RecommendationResponse> {
        // Get the current settings from the settings repository.
        val currentSettings = settingsRepository.settingsState.value

        // Build a RecommendationRequest from your SettingsState and additional notes.
        val requestData = buildRecommendationQuery(currentSettings, additionalNotes)

        // Call the recommendation endpoint.
        return dataSource.requestRecommendation(
            RecommendationRequest(
                query = Json.encodeToString(requestData)
            )
        )
    }

    private fun buildRecommendationQuery(settings: SettingsState, additionalNotes: String): RecommendationQuery {
        // Map gender based on the isMale flag.
        val gender = if (settings.personalInfo.isMale) "male" else "female"

        // Get the basic values from PersonalInfo.
        val age = settings.personalInfo.age
        val height = settings.personalInfo.height
        val weight = settings.personalInfo.weight

        // Map the BMR calculation method from your enum.
        val bmrCalculationMethod = when (settings.bmrOption) {
            BmrCalculationOption.DEFAULT -> "default"
            BmrCalculationOption.CUSTOM -> "custom"
            BmrCalculationOption.CALCULATE -> "personal_info"
        }

        // Map the exercise level to an activity string.
        val activityLevel = when (settings.personalInfo.exerciseLevel) {
            ExerciseLevel.SEDENTARY -> "sedentary"
            ExerciseLevel.LIGHT -> "light"
            ExerciseLevel.MODERATE -> "moderate"
            ExerciseLevel.ACTIVE -> "active"
            ExerciseLevel.EXTRA_ACTIVE -> "extra active"
        }

        return RecommendationQuery(
            gender = gender,
            age = age,
            height = height,
            weight = weight,
            bmrCalculationMethod = bmrCalculationMethod,
            bmr = settings.customBmrValue,
            activityLevel = activityLevel,
            foodPreferences = settings.foodPreferences,
            foodAllergies = settings.foodAllergies,
            additionalNotes = additionalNotes
        )
    }
}
