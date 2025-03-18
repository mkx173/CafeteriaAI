package com.tohoku.cafeteria.ui.menu

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
import com.tohoku.cafeteria.data.repository.FoodRepository
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.domain.model.FoodItem
import kotlinx.coroutines.launch

data class MenuUiState(
    val menuData: List<FoodCategory>? = null,
    val isRefreshing: Boolean = false,
    val isErrorNew: Boolean = false,
    val errorMessage: String? = null
)

class MenuViewModel(
    private val foodRepository: FoodRepository,
    private val application: CafeteriaApplication
) : ViewModel() {
    private val _uiState = mutableStateOf(MenuUiState())
    val uiState: State<MenuUiState> = _uiState

    init {
        refreshMenu()
    }

    fun refreshMenu()  {
        // Clear any previous error and set refreshing state.
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    menuData = foodRepository.getMenu(),
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val foodRepository = application.appContainer.foodRepository
                MenuViewModel(foodRepository = foodRepository, application = application)
            }
        }
    }
}