package com.tohoku.cafeteria.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_history")
data class FoodHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val variantId: Int,
    val mealOption: String
)
