package com.tohoku.cafeteria.domain.model

data class MenuItem(
    val foodId: Int,
    val name: String,
    val url: String,
    val nutritionDataList: List<NutritionData>
)
