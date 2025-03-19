package com.tohoku.cafeteria.data.repository

import com.tohoku.cafeteria.data.dao.FoodDao
import com.tohoku.cafeteria.data.dao.FoodHistoryDao
import com.tohoku.cafeteria.data.datasource.FoodDataSource
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.data.entity.FoodHistoryEntity
import com.tohoku.cafeteria.data.request.RatingQuery
import com.tohoku.cafeteria.data.request.RecommendationQuery
import com.tohoku.cafeteria.data.response.RecommendationResponse
import com.tohoku.cafeteria.domain.mapper.FoodCategoryMapper
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.ui.recommendation.Rating
import com.tohoku.cafeteria.ui.settings.BmrCalculationOption
import com.tohoku.cafeteria.ui.settings.ExerciseLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.util.Calendar

enum class MealOption(val key: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner");

    companion object {
        fun fromHour(hour: Int): MealOption {
            return when (hour) {
                in 0 until 11 -> BREAKFAST
                in 11 until 17 -> LUNCH
                else -> DINNER
            }
        }
    }
}

data class FoodHistoryWithDetails(
    val historyEntity: FoodHistoryEntity,
    val foodEntity: FoodEntity?
)

class FoodRepository(
    private val dataSource: FoodDataSource,
    private val settingsRepository: SettingsRepository,
    private val foodDao: FoodDao,
    private val foodHistoryDao: FoodHistoryDao
) {
    suspend fun getMenu(): List<FoodCategory> {
        val responses = dataSource.getMenu()
        val foodCategories = responses.map { FoodCategoryMapper.fromResponse(it) }
        saveMenuToDatabase(foodCategories) // Save to database after fetching
        return foodCategories
    }

    private suspend fun saveMenuToDatabase(foodCategories: List<FoodCategory>) {
        val foodEntities = mutableListOf<FoodEntity>()
        foodCategories.forEach { category ->
            category.items.forEach { item ->
                item.foodVariantsList.forEach { variant ->
                    foodEntities.add(
                        FoodEntity(
                            variantId = variant.variantId,
                            foodId = item.foodId,
                            variantName = variant.variantName,
                            foodName = item.name,
                            price = variant.price,
                            calories = variant.calories,
                            protein = variant.protein,
                            fat = variant.fat,
                            carbohydrates = variant.carbohydrates,
                            category = category.category,
                            imageUrl = item.url
                        )
                    )
                }
            }
        }
        foodDao.insertFoods(foodEntities)
    }

    suspend fun saveFoodToHistory(variantIds: List<Int>) {
        if (variantIds.isEmpty()) return

        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val mealOption = MealOption.fromHour(hour)

        variantIds.forEach { variantId ->
            val historyEntity = FoodHistoryEntity(
                timestamp = currentTimeMillis,
                variantId = variantId,
                mealOption = mealOption.key
            )
            foodHistoryDao.insertFoodHistory(historyEntity)
        }
    }

    // Build the recommendation request using current settings and then call the API.
    suspend fun requestRecommendation(cartItems: List<CartItem>, additionalNotes: String): Response<RecommendationResponse> {
        // Get the current settings from the settings repository.
        val currentSettings = settingsRepository.settingsState.value

        // Build a RecommendationRequest from your SettingsState and additional notes.
        val requestData = buildRecommendationQuery(currentSettings, cartItems, additionalNotes)

        // Call the recommendation endpoint.
        return dataSource.requestRecommendation(Json.encodeToString(requestData))
    }

    suspend fun requestNewRecommendation(cartItems: List<CartItem>, additionalNotes: String, ratingsMap: Map<Int, Rating>): Response<RecommendationResponse> {
        // Get the current settings from the settings repository.
        val currentSettings = settingsRepository.settingsState.value

        // Build a RecommendationQuery from your SettingsState and additional notes.
        val queryData = buildRecommendationQuery(currentSettings, cartItems, additionalNotes)
        val ratingData = ratingsMap.map { (variantId, rating) -> buildRatingQuery(variantId, rating) }

        // Call the recommendation endpoint.
        return dataSource.requestNewRecommendation(
            Json.encodeToString(queryData),
            Json.encodeToString(ratingData)
        )
    }

    suspend fun getFoodByVariantId(variantId: Int): FoodEntity? {
        return foodDao.getFoodByVariantId(variantId)
    }

    fun getFoodHistoryWithDetails(): Flow<List<FoodHistoryWithDetails>> {
        return foodHistoryDao.getFoodHistory().map { historyEntities ->
            historyEntities.map { historyEntity ->
                val food = foodDao.getFoodByVariantId(historyEntity.variantId)
                FoodHistoryWithDetails(
                    historyEntity = historyEntity,
                    foodEntity = food
                )
            }
        }
    }

    private fun buildRatingQuery(variantId: Int, rating: Rating): RatingQuery {
        val ratingValue = when (rating) {
            Rating.UP -> "like"
            Rating.DOWN -> "dislike"
            Rating.NONE -> "none"
        }
        return RatingQuery(variantId, ratingValue)
    }

    private fun buildRecommendationQuery(settings: SettingsState, cartItems: List<CartItem>, additionalNotes: String): RecommendationQuery {
        // Map gender based on the isMale flag.
        val gender = if (settings.personalInfo.isMale) "male" else "female"

        // Get the basic values from PersonalInfo.
        val age = settings.personalInfo.age
        val height = settings.personalInfo.height
        val weight = settings.personalInfo.weight

        // Map the BMR calculation method from your enum.
        val bmrCalculationMethod = when (settings.bmrOption) {
            BmrCalculationOption.DEFAULT -> "default"
            BmrCalculationOption.CUSTOM -> "custom"
            BmrCalculationOption.CALCULATE -> "personal_info"
        }

        // Map the exercise level to an activity string.
        val activityLevel = when (settings.personalInfo.exerciseLevel) {
            ExerciseLevel.SEDENTARY -> "sedentary"
            ExerciseLevel.LIGHT -> "light"
            ExerciseLevel.MODERATE -> "moderate"
            ExerciseLevel.ACTIVE -> "active"
            ExerciseLevel.EXTRA_ACTIVE -> "extra active"
        }

        return RecommendationQuery(
            gender = gender,
            age = age,
            height = height,
            weight = weight,
            cartItems = cartItems.map { it.item.variantId },
            bmrCalculationMethod = bmrCalculationMethod,
            bmr = settings.customBmrValue,
            activityLevel = activityLevel,
            foodPreferences = settings.foodPreferences,
            foodAllergies = settings.foodAllergies,
            additionalNotes = additionalNotes
        )
    }
}
