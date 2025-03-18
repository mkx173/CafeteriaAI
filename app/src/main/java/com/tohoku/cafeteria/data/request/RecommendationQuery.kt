package com.tohoku.cafeteria.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationQuery(
    val gender: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    @SerialName(value = "bmr_calculation_method")
    val bmrCalculationMethod: String,
    val bmr: Int,
    @SerialName(value = "activity_level")
    val activityLevel: String,
    @SerialName(value = "food_preferences")
    val foodPreferences: String,
    @SerialName(value = "food_allergies")
    val foodAllergies: String,
    @SerialName(value = "additional_notes")
    val additionalNotes: String
)
