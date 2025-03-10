package com.tohoku.cafeteria.data

import android.content.Context
import com.example.foodnutrition.data.datasource.MenuRemoteDataSource
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tohoku.cafeteria.data.datasource.MenuMockDataSource
import com.tohoku.cafeteria.data.datasource.MenuApiService
import com.tohoku.cafeteria.data.repository.MenuRepository
import com.tohoku.cafeteria.data.repository.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val menuRepository: MenuRepository
    val settingsRepository: SettingsRepository
}

abstract class BaseAppContainer(private val context: Context) : AppContainer {
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }
}

class DefaultAppContainer(context: Context) : BaseAppContainer(context) {
    private val BASE_URL =
        "https://android-kotlin-fun-mars-server.appspot.com"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: MenuApiService by lazy {
        retrofit.create(MenuApiService::class.java)
    }

    override val menuRepository: MenuRepository by lazy {
        MenuRepository(MenuRemoteDataSource(retrofitService))
    }
}

class MockAppContainer(context: Context) : BaseAppContainer(context) {
    override val menuRepository: MenuRepository by lazy {
        MenuRepository(MenuMockDataSource())
    }
}