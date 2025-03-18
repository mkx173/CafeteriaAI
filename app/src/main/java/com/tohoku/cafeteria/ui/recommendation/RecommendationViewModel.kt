package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.data.repository.FoodRepository
import com.tohoku.cafeteria.data.response.RecommendationResponse
import kotlinx.coroutines.launch

enum class Rating {
    UP, DOWN, NONE
}

data class RecommendationUiState(
    val recommendation: RecommendationResponse? = null,
    val isRefreshing: Boolean = false,
    val isErrorNew: Boolean = false,
    val errorMessage: String? = null,
    val additionalNotes: String? = null,
    val foodRatings: Map<Int, Rating> = emptyMap()
)

class RecommendationViewModel(
    private val foodRepository: FoodRepository,
    private val application: CafeteriaApplication
) :  ViewModel() {

    private val _uiState = mutableStateOf(RecommendationUiState())
    val uiState: State<RecommendationUiState> = _uiState

    fun fetchRecommendation() {
        // Clear any previous error and set refreshing state.
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                val response = foodRepository.requestRecommendation(_uiState.value.additionalNotes ?: "")
                if (!response.isSuccessful) {
                    throw Exception("Request failed with code: ${response.code()}")
                }
                _uiState.value = _uiState.value.copy(
                    recommendation = response.body(),
                    errorMessage = null,
                    isErrorNew = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred),
                    isErrorNew = true
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun clearNewErrorFlag() {
        _uiState.value = _uiState.value.copy(isErrorNew = false)
    }

    suspend fun getFoodByVariantId(variantId: Int): FoodEntity? {
        return foodRepository.getFoodByVariantId(variantId)
    }

    fun setAdditionalNotes(additionalNotes: String) {
        _uiState.value = _uiState.value.copy(additionalNotes = additionalNotes)
    }

    fun updateFoodRating(variantId: Int, rating: Rating) {
        val currentRatings = _uiState.value.foodRatings.toMutableMap()
        currentRatings[variantId] = rating
        _uiState.value = _uiState.value.copy(foodRatings = currentRatings.toMap())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val foodRepository = application.appContainer.foodRepository
                RecommendationViewModel(foodRepository = foodRepository, application = application)
            }
        }
    }
}