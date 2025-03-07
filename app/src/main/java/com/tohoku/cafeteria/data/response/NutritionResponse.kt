package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class NutritionResponse(
    val variantName: String,
    val variantId: Int,
    val price: Int,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbohydrates: Int
)