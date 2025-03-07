package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class MenuResponse(
    val foodId: Int,
    val name: String,
    val url: String,
    val nutritionResponseList: List<NutritionResponse>
)
