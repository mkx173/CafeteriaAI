package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.DetectResponse
import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface FoodDataSource {
    suspend fun getMenu(): List<FoodCategoryResponse>
    suspend fun requestRecommendation(query: String): Response<RecommendationResponse>
    suspend fun requestNewRecommendation(query: String, rating: String): Response<RecommendationResponse>
    suspend fun resetMenu()
    suspend fun detectAndSetCurrentMenu(imageUpload: MultipartBody.Part, method: RequestBody): Response<DetectResponse>
}