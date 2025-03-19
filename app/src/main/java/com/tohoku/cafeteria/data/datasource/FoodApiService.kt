package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface FoodApiService {
    @GET("api/request_current_menu")
    suspend fun getMenu(): List<FoodCategoryResponse>

    @FormUrlEncoded
    @POST("api/request_recommendation/")
    suspend fun requestRecommendation(@Field("query") query: String): Response<RecommendationResponse>
}
