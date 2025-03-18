package com.tohoku.cafeteria.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationResponse(
    @SerialName("additional_notes")
    val additionalNotes: String,

    @SerialName("detail_nutritions")
    val detailNutritions: List<String>,

    @SerialName("min_nutritions")
    val minNutritions: List<Int>,

    @SerialName("recommended_meal_detail")
    val recommendedMealDetail: String,

    @SerialName("list_meals")
    val listMeals: List<String>,

    @SerialName("verbose_in_function")
    val verboseInFunction: Boolean,

    @SerialName("recommended_meals")
    val recommendedMeals: List<Int>,

    @SerialName("id")
    val id: String
)
