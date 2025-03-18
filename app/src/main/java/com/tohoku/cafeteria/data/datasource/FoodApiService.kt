package com.tohoku.cafeteria.data.datasource;

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import retrofit2.http.GET

interface FoodApiService {
    @GET("api/request_current_menu")
    suspend fun getMenu(): List<FoodCategoryResponse>
}
