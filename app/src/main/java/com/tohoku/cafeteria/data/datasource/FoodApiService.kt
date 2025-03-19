package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.DetectResponse
import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodApiService {
    @GET("api/request_current_menu")
    suspend fun getMenu(): List<FoodCategoryResponse>

    @FormUrlEncoded
    @POST("api/request_recommendation/")
    suspend fun requestRecommendation(
        @Field("query") query: String
    ): Response<RecommendationResponse>

    @FormUrlEncoded
    @POST("api/get_new_recommendation/")
    suspend fun requestNewRecommendation(
        @Field("query") query: String,
        @Field("rating") rating: String,
    ): Response<RecommendationResponse>

    @GET("api/reset_current_menu/")
    suspend fun resetMenu()

    @Multipart
    @POST("api/detect_and_set_current_menu/")
    suspend fun detectAndSetCurrentMenu(
        @Part imageUpload: MultipartBody.Part,
        @Part("method") method: RequestBody
    ): Response<DetectResponse>
}
