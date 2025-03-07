package com.tohoku.cafeteria.domain.mapper

import com.tohoku.cafeteria.data.response.MenuResponse
import com.tohoku.cafeteria.domain.model.MenuItem
import com.tohoku.cafeteria.domain.model.NutritionData

object MenuItemMapper {
    fun fromResponse(response: MenuResponse): MenuItem {
        return MenuItem(
            foodId = response.foodId,
            name = response.name,
            url = response.url,
            nutritionDataList = response.nutritionResponseList.map { nutritionResponse ->
                NutritionData(
                    variantName = nutritionResponse.variantName,
                    variantId = nutritionResponse.variantId,
                    price = nutritionResponse.price,
                    calories = nutritionResponse.calories,
                    protein = nutritionResponse.protein,
                    fat = nutritionResponse.fat,
                    carbohydrates = nutritionResponse.carbohydrates
                )
            }
        )
    }
}