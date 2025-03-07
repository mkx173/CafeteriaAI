package com.tohoku.cafeteria.domain.model

data class NutritionData(
    val variantName: String,
    val variantId: Int,
    val price: Int,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbohydrates: Int
)
