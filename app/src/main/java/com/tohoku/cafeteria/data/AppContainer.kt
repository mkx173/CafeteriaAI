package com.tohoku.cafeteria.data

import android.content.Context
import com.example.foodnutrition.data.datasource.FoodRemoteDataSource
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tohoku.cafeteria.data.database.AppDatabase
import com.tohoku.cafeteria.data.datasource.FoodMockDataSource
import com.tohoku.cafeteria.data.datasource.FoodApiService
import com.tohoku.cafeteria.data.repository.FoodRepository
import com.tohoku.cafeteria.data.repository.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

interface AppContainer {
    val foodRepository: FoodRepository
    val settingsRepository: SettingsRepository
    val database: AppDatabase
}

abstract class BaseAppContainer(private val context: Context) : AppContainer {
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
}

class DefaultAppContainer(context: Context) : BaseAppContainer(context) {
    private val BASE_URL =
        "http://34.229.85.230:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    private val retrofitService: FoodApiService by lazy {
        retrofit.create(FoodApiService::class.java)
    }

    override val foodRepository: FoodRepository by lazy {
        FoodRepository(
            FoodRemoteDataSource(retrofitService),
            settingsRepository,
            database.foodDao(),
            database.foodHistoryDao()
        )
    }
}

class MockAppContainer(context: Context) : BaseAppContainer(context) {
    override val foodRepository: FoodRepository by lazy {
        FoodRepository(
            FoodMockDataSource(),
            settingsRepository,
            database.foodDao(),
            database.foodHistoryDao()

        )
    }
}