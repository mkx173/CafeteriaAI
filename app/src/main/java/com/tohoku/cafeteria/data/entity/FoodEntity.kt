package com.tohoku.cafeteria.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food",
    indices = [Index(value = ["foodId"]), Index(value = ["orderIndex"])]
)
data class FoodEntity(
    @PrimaryKey val variantId: Int,
    val foodId: Int,
    val variantName: String,
    val foodName: String,
    val price: Int,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbohydrates: Int,
    val category: String,
    val imageUrl: String,
    val orderIndex: Int
)

