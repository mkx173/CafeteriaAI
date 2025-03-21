package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.util.ToastManager
import kotlinx.coroutines.launch

enum class Rating {
    UP, DOWN, NONE
}

data class RecommendationUiState(
    val recommendation: RecommendationResponse? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val additionalNotes: String? = null,
    val foodRatings: Map<Int, Rating> = emptyMap(),
    val foodSelected: Map<Int, Boolean> = emptyMap()
)

class RecommendationViewModel(
    private val foodRepository: FoodRepository,
    private val application: CafeteriaApplication
) :  ViewModel() {

    private val _uiState = mutableStateOf(RecommendationUiState())
    val uiState: State<RecommendationUiState> = _uiState

    fun fetchRecommendation(cartItems: List<CartItem>) {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                val response = foodRepository.requestRecommendation(
                    cartItems,
                    _uiState.value.additionalNotes ?: ""
                )
                if (!response.isSuccessful) {
                    throw Exception("Request failed with code: ${response.code()}")
                }
                _uiState.value = _uiState.value.copy(
                    recommendation = response.body(),
                    errorMessage = null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred),
                )
                ToastManager.showMessage(
                    e.message ?: application.getString(R.string.unknown_error_occurred),
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun fetchNewRecommendation(cartItems: List<CartItem>) {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                val response = foodRepository.requestNewRecommendation(
                    cartItems,
                    _uiState.value.additionalNotes ?: "",
                    _uiState.value.foodRatings
                )
                if (!response.isSuccessful) {
                    throw Exception("Request failed with code: ${response.code()}")
                }
                _uiState.value = _uiState.value.copy(
                    recommendation = response.body(),
                    errorMessage = null,
                    foodSelected = emptyMap(),
                    foodRatings = emptyMap()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred),
                )
                ToastManager.showMessage(
                    e.message ?: application.getString(R.string.unknown_error_occurred),
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    suspend fun getFoodByVariantId(variantId: Int): FoodEntity? {
        return foodRepository.getFoodByVariantId(variantId)
    }

    fun setAdditionalNotes(additionalNotes: String) {
        _uiState.value = _uiState.value.copy(additionalNotes = additionalNotes)
    }

    fun updateFoodRating(variantId: Int, rating: Rating, skipIfPresent: Boolean = false) {
        if (skipIfPresent && _uiState.value.foodRatings.containsKey(variantId)) {
            return
        }
        val currentRatings = _uiState.value.foodRatings.toMutableMap()
        currentRatings[variantId] = rating
        _uiState.value = _uiState.value.copy(foodRatings = currentRatings.toMap())
    }

    fun updateFoodSelected(variantId: Int, isSelected: Boolean, skipIfPresent: Boolean = false) {
        if (skipIfPresent && _uiState.value.foodSelected.containsKey(variantId)) {
            return
        }
        val currentSelected = _uiState.value.foodSelected.toMutableMap()
        currentSelected[variantId] = isSelected
        _uiState.value = _uiState.value.copy(foodSelected = currentSelected.toMap())
    }

    fun saveSelectedFoodsToHistory(): Int {
        val selectedVariantIds = uiState.value.foodSelected.filter { it.value }.keys.toList()
        viewModelScope.launch {
            foodRepository.saveFoodToHistory(selectedVariantIds)
        }
        return selectedVariantIds.size
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