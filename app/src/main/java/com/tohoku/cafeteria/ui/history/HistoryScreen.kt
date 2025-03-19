package com.tohoku.cafeteria.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.data.repository.FoodHistoryWithDetails
import com.tohoku.cafeteria.ui.components.FoodEntityBottomSheetComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val historyState by viewModel.historyState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<FoodEntity?>(null) }
    val foodDetailSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val handleItemClick: (FoodEntity) -> Unit = { item ->
        selectedItem = item
        scope.launch { foodDetailSheetState.show() }
    }

    FoodEntityBottomSheetComponent(
        sheetState = foodDetailSheetState,
        selectedItem = selectedItem,
        onDismiss = {
            scope.launch {
                foodDetailSheetState.hide()
                selectedItem = null
            }
        }
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_history)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = historyState) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HistoryState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(R.string.error_loading_history))
                        Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
                is HistoryState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_history_found),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(dimensionResource(R.dimen.padding_medium)),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        HistoryList(dateGroups = state.data, handleItemClick = handleItemClick)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    dateGroups: List<DateGroup>,
    modifier: Modifier = Modifier,
    handleItemClick: (FoodEntity) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
    ) {
        item {
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_small)))
        }
        dateGroups.forEachIndexed { index, dateGroup ->
            item {
                DateHeader(date = dateGroup.date)
            }

            dateGroup.mealGroups.forEach { mealGroup ->
                item {
                    MealHeader(mealType = mealGroup.mealOption)
                }

                items(mealGroup.items) { historyItem ->
                    FoodHistoryItem(historyItem = historyItem, handleItemClick)
                }
            }

            if (index != dateGroups.lastIndex) {
                item {
                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_xsmall)))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_xsmall)))
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_xsmall)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_small)))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color.LightGray
        )
    }
}

@Composable
fun MealHeader(mealType: String) {
    Text(
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
        text = mealType,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FoodHistoryItem(historyItem: FoodHistoryWithDetails, handleItemClick: (FoodEntity) -> Unit) {
    val food = historyItem.foodEntity

    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
            .clickable { food?.let(handleItemClick) },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        if (food != null) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_small)),
                headlineContent = { Text(text = stringResource(R.string.name_variant_name, food.foodName, food.variantName)) },
                supportingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.price, food.price),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        VerticalDivider(
                            modifier = Modifier
                                .padding(dimensionResource(R.dimen.padding_small))
                                .height(dimensionResource(R.dimen.vertical_divider_height)),
                            color = Color.LightGray
                        )
                        Text(
                            text = stringResource(R.string.kcal, food.calories),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.size_food_item_image))
                            .clip(CircleShape),
                        model = food.imageUrl,
                        contentDescription = food.foodName,
                        contentScale = ContentScale.Crop
                    )
                },
                trailingContent = {
                    Icon(
                        modifier = Modifier.size(dimensionResource(R.dimen.size_icon)),
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.add_to_cart)
                    )
                }
            )
        } else {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_small)),
                headlineContent = { Text(text = stringResource(R.string.item_not_found)) }
            )
        }
    }
}