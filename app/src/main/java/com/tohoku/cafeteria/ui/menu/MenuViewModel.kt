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
import com.tohoku.cafeteria.data.repository.MenuRepository
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.domain.model.FoodItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MenuUiState(
    val menuData: List<FoodCategory>? = null,
    val isRefreshing: Boolean = false,
    val isErrorNew: Boolean = false,
    val errorMessage: String? = null
)

class MenuViewModel(
    private val menuRepository: MenuRepository,
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
                    menuData = menuRepository.getMenu(),
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

    // Retrieve items by category name.
    fun getItemsByCategory(categoryName: String): List<FoodItem> {
        return _uiState.value.menuData?.find { it.category == categoryName }?.items ?: emptyList()
    }

    // Retrieve a menu item by its foodId.
    fun getItemById(id: Int): FoodItem? {
        return _uiState.value.menuData?.flatMap { it.items }?.firstOrNull { it.foodId == id }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val menuRepository = application.appContainer.menuRepository
                MenuViewModel(menuRepository = menuRepository, application = application)
            }
        }
    }
}