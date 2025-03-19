package com.example.foodnutrition.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.datasource.FoodDataSource
import com.tohoku.cafeteria.data.datasource.FoodApiService
import com.tohoku.cafeteria.data.response.DetectResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class FoodRemoteDataSource(private val service: FoodApiService) : FoodDataSource {
    override suspend fun getMenu(): List<FoodCategoryResponse> = service.getMenu()
    override suspend fun requestRecommendation(query: String): Response<RecommendationResponse> =
        service.requestRecommendation(query)
    override suspend fun requestNewRecommendation(query: String, rating: String): Response<RecommendationResponse> =
        service.requestNewRecommendation(query, rating)
    override suspend fun resetMenu() {
        service.resetMenu()
    }
    override suspend fun detectAndSetCurrentMenu(
        imageUpload: MultipartBody.Part,
        method: RequestBody
    ): Response<DetectResponse> = service.detectAndSetCurrentMenu(imageUpload, method)
}
