package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse

interface MenuDataSource {
    suspend fun getMenu(): List<FoodCategoryResponse>
}