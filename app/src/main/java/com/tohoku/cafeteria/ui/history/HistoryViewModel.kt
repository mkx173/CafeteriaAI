package com.tohoku.cafeteria.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.data.repository.FoodHistoryWithDetails
import com.tohoku.cafeteria.data.repository.FoodRepository
import com.tohoku.cafeteria.data.repository.MealOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val data: List<DateGroup>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

data class DateGroup(
    val date: String,
    val mealGroups: List<MealGroup>
)

data class MealGroup(
    val mealOption: String,
    val items: List<FoodHistoryWithDetails>
)

class HistoryViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            foodRepository.getFoodHistoryWithDetails()
                .catch { e ->
                    _historyState.value = HistoryState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { historyList ->
                    val groupedHistory = processHistoryData(historyList)
                    _historyState.value = HistoryState.Success(groupedHistory)
                }
        }
    }

    private fun processHistoryData(historyList: List<FoodHistoryWithDetails>): List<DateGroup> {
        // Group history items by date (yyyy/MM/dd)
        return historyList
            .groupBy { historyWithDetails ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = historyWithDetails.historyEntity.timestamp
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // Format: yyyy/MM/dd
                String.format("%04d/%02d/%02d", year, month, day)
            }
            .map { (date, historyItems) ->
                // Group items by meal option within each date
                val mealGroups = historyItems
                    .groupBy { it.historyEntity.mealOption }
                    .map { (mealOption, items) ->
                        MealGroup(
                            mealOption = mealOption,
                            items = items.sortedByDescending { it.historyEntity.timestamp }
                        )
                    }
                    .sortedBy { mealGroup ->
                        // Sort meal groups by: Breakfast, Lunch, Dinner
                        when (mealGroup.mealOption) {
                            MealOption.BREAKFAST.key -> 0
                            MealOption.LUNCH.key -> 1
                            MealOption.DINNER.key -> 2
                            else -> 3
                        }
                    }

                DateGroup(
                    date = date,
                    mealGroups = mealGroups
                )
            }
            .sortedByDescending { it.date } // Most recent dates first
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val foodRepository = application.appContainer.foodRepository
                HistoryViewModel(foodRepository = foodRepository)
            }
        }
    }
}
