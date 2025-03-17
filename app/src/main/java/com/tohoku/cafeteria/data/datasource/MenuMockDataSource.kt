package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.FoodItemResponse
import com.tohoku.cafeteria.data.response.FoodVariantResponse

class MenuMockDataSource : MenuDataSource {
    override suspend fun getMenu(): List<FoodCategoryResponse> {
        val sampleFoods = FoodItemResponse(
            foodId = 1,
            name = "Sample Burger",
            url = "android.resource://com.tohoku.cafeteria/drawable/sample_food",
            variants = listOf(
                FoodVariantResponse(
                    variantName = "S",
                    variantId = 101,
                    price = 500,
                    calories = 500,
                    protein = 25,
                    fat = 20,
                    carbohydrates = 50
                ),
                FoodVariantResponse(
                    variantName = "M",
                    variantId = 102,
                    price = 600,
                    calories = 600,
                    protein = 30,
                    fat = 25,
                    carbohydrates = 60
                )
            )
        )
        return listOf(
            FoodCategoryResponse(
                category = "Burgers",
                items = List(5) {
                    sampleFoods
                }
            ),
            FoodCategoryResponse(
                category = "Drinks",
                items = List(5) {
                    sampleFoods
                }
            )
        )
    }
}