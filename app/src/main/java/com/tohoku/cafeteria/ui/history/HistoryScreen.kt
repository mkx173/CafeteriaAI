package com.tohoku.cafeteria.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.tohoku.cafeteria.data.repository.FoodHistoryWithDetails
import com.tohoku.cafeteria.ui.recommendation.Rating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val historyState by viewModel.historyState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error loading history")
                        Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
                is HistoryState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(
                            text = "No history found",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        HistoryList(dateGroups = state.data)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    dateGroups: List<DateGroup>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
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
                    FoodHistoryItem(historyItem = historyItem)
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_xsmall))
        )
        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_small)))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().weight(1f),
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
fun FoodHistoryItem(historyItem: FoodHistoryWithDetails) {
    val food = historyItem.foodEntity

    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
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