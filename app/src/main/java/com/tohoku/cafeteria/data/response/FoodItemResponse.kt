package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class FoodItemResponse(
    val foodId: Int,
    val name: String,
    val url: String,
    val variants: List<FoodVariantResponse>
)
