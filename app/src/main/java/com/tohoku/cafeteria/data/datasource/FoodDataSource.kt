package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse

interface FoodDataSource {
    suspend fun getMenu(): List<FoodCategoryResponse>
}