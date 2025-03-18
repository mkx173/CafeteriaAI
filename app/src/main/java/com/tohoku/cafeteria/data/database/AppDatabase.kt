package com.tohoku.cafeteria.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tohoku.cafeteria.data.dao.FoodDao
import com.tohoku.cafeteria.data.dao.FoodHistoryDao
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.data.entity.FoodHistoryEntity

@Database(
    entities = [FoodEntity::class, FoodHistoryEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodHistoryDao(): FoodHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafeteria_database"
                )
                    // For development purposes; in production, you should implement proper migrations.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
