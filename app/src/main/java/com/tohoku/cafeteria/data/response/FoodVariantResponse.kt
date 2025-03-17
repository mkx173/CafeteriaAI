package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class FoodVariantResponse(
    val variantName: String,
    val variantId: Int,
    val price: Int,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbohydrates: Float
)