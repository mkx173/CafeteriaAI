package com.example.foodnutrition.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.datasource.FoodDataSource
import com.tohoku.cafeteria.data.datasource.FoodApiService

class FoodRemoteDataSource(private val service: FoodApiService) : FoodDataSource {
    override suspend fun getMenu(): List<FoodCategoryResponse> = service.getMenu()
}
