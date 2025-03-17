package com.tohoku.cafeteria.domain.mapper

import com.tohoku.cafeteria.data.response.FoodItemResponse
import com.tohoku.cafeteria.domain.model.FoodItem
import com.tohoku.cafeteria.domain.model.FoodVariant

object FoodItemMapper {
    fun fromResponse(response: FoodItemResponse): FoodItem {
        return FoodItem(
            foodId = response.foodId,
            name = response.name,
            url = "http://34.229.85.230:8000" + response.url,
            foodVariantsList = response.variants.map { foodVariantResponse ->
                FoodVariant(
                    variantName = foodVariantResponse.variantName,
                    variantId = foodVariantResponse.variantId,
                    price = foodVariantResponse.price,
                    calories = foodVariantResponse.calories.toInt(),
                    protein = foodVariantResponse.protein.toInt(),
                    fat = foodVariantResponse.fat.toInt(),
                    carbohydrates = foodVariantResponse.carbohydrates.toInt()
                )
            }
        )
    }
}