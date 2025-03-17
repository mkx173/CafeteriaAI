package com.tohoku.cafeteria.domain.model

data class CartItem(
    val item: NutritionData,
    val name: String,
    var quantity: Int = 1
)
