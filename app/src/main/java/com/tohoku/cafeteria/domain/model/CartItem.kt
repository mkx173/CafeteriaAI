package com.tohoku.cafeteria.domain.model

data class CartItem(
    val item: FoodVariant,
    val name: String,
    val url: String
)
