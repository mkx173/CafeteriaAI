package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.request.RecommendationRequest
import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import retrofit2.Response

interface FoodDataSource {
    suspend fun getMenu(): List<FoodCategoryResponse>
    suspend fun requestRecommendation(request: RecommendationRequest): Response<RecommendationResponse>
}