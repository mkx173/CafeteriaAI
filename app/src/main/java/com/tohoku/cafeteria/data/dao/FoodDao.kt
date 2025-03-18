package com.tohoku.cafeteria.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tohoku.cafeteria.data.entity.FoodEntity

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Query("SELECT * FROM food")
    suspend fun getAllFoods(): List<FoodEntity>

    @Query("SELECT * FROM food WHERE variantId = :variantId")
    suspend fun getFoodByVariantId(variantId: Int): FoodEntity?

    @Query("SELECT * FROM food WHERE variantId IN (:variantIds)")
    suspend fun getFoodsByVariantIds(variantIds: List<Int>): List<FoodEntity>
}