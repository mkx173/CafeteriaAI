package com.tohoku.cafeteria.ui.menu

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.data.repository.MenuRepository
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.domain.model.MenuItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MenuUiState(
    val menuData: List<FoodCategory> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class MenuViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _uiState = mutableStateOf(MenuUiState())
    val uiState: State<MenuUiState> = _uiState

    init {
        refreshMenu()
    }

    fun refreshMenu()  {
        // Clear any previous error and set refreshing state.
        _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Simulate network delay.
                delay(2000)
                // Simulate a refresh failure 30% of the time.
                if (Math.random() < 0.3) {
                    throw Exception("Failed to refresh data. Please try again.")
                }
                // Update the state with fetched data.
                _uiState.value = _uiState.value.copy(menuData = menuRepository.getMenu())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Unknown error occurred.")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Retrieve items by category name.
    fun getItemsByCategory(categoryName: String): List<MenuItem> {
        return _uiState.value.menuData.find { it.category == categoryName }?.items ?: emptyList()
    }

    // Retrieve a menu item by its foodId.
    fun getItemById(id: Int): MenuItem? {
        return _uiState.value.menuData.flatMap { it.items }.firstOrNull { it.foodId == id }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val menuRepository = application.appContainer.menuRepository
                MenuViewModel(menuRepository = menuRepository)
            }
        }
    }
}