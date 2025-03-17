package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class FoodCategoryResponse(
    val category: String,
    val items: List<FoodItemResponse>
)
