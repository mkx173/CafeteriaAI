package com.tohoku.cafeteria.domain.mapper

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.domain.model.FoodCategory

object FoodCategoryMapper {
    fun fromResponse(response: FoodCategoryResponse): FoodCategory {
        val items = response.items.map { FoodItemMapper.fromResponse(it) }
        return FoodCategory(
            category = response.category,
            items = items
        )
    }
}