package com.tohoku.cafeteria.domain.model

data class FoodItem(
    val foodId: Int,
    val name: String,
    val url: String,
    val foodVariantsList: List<FoodVariant>
)
