package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.FoodItemResponse
import com.tohoku.cafeteria.data.response.FoodVariantResponse

class FoodMockDataSource : FoodDataSource {
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
                    calories = 500f,
                    protein = 25f,
                    fat = 20f,
                    carbohydrates = 50f
                ),
                FoodVariantResponse(
                    variantName = "M",
                    variantId = 102,
                    price = 600,
                    calories = 600f,
                    protein = 30f,
                    fat = 25f,
                    carbohydrates = 60f
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