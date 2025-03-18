package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.request.RecommendationRequest
import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FoodApiService {
    @GET("api/request_current_menu")
    suspend fun getMenu(): List<FoodCategoryResponse>

    @POST("api/request_recommendation/")
    suspend fun requestRecommendation(@Body request: RecommendationRequest): Response<RecommendationResponse>
}
