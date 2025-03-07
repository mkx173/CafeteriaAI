package com.tohoku.cafeteria.data.repository

import com.tohoku.cafeteria.data.datasource.MenuDataSource
import com.tohoku.cafeteria.domain.mapper.FoodCategoryMapper
import com.tohoku.cafeteria.domain.model.FoodCategory

class MenuRepository(private val dataSource: MenuDataSource) {
    suspend fun getMenu(): List<FoodCategory> {
        val responses = dataSource.getMenu()
        return responses.map { FoodCategoryMapper.fromResponse(it) }
    }
}
