package com.tohoku.cafeteria.domain.model

data class FoodCategory(
    val category: String,
    val items: List<MenuItem>
)
