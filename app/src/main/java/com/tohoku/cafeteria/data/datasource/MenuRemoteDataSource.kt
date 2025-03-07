package com.example.foodnutrition.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.datasource.MenuDataSource
import com.tohoku.cafeteria.data.datasource.MenuApiService

class MenuRemoteDataSource(private val service: MenuApiService) : MenuDataSource {
    override suspend fun getMenu(): List<FoodCategoryResponse> = service.getMenu()
}
