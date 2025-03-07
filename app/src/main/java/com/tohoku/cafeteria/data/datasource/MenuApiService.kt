package com.tohoku.cafeteria.data.datasource;

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import retrofit2.http.GET

interface MenuApiService {
    @GET("menu")
    suspend fun getMenu(): List<FoodCategoryResponse>
}
