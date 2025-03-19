package com.tohoku.cafeteria.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tohoku.cafeteria.data.entity.FoodHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodHistory(history: FoodHistoryEntity)

    @Query("SELECT * FROM food_history ORDER BY timestamp DESC")
    fun getFoodHistory(): Flow<List<FoodHistoryEntity>>
}
