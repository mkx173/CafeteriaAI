package com.tohoku.cafeteria.data.repository

import com.tohoku.cafeteria.data.datasource.FoodDataSource
import com.tohoku.cafeteria.domain.mapper.FoodCategoryMapper
import com.tohoku.cafeteria.domain.model.FoodCategory

class FoodRepository(private val dataSource: FoodDataSource) {
    suspend fun getMenu(): List<FoodCategory> {
        val responses = dataSource.getMenu()
        return responses.map { FoodCategoryMapper.fromResponse(it) }
    }
}
