package com.tohoku.cafeteria.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.ui.recommendation.RecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSelectorBottomSheetComponent(
    modifier: Modifier = Modifier,
    showBottomSheet: Boolean,
    addToHistorySheetState: SheetState,
    recommendedFoods: List<FoodEntity?>,
    viewModel: RecommendationViewModel,
    onDismiss: () -> Unit,
    onSaveToHistory: () -> Boolean
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismiss,
            sheetState = addToHistorySheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            ) {
                Text(
                    text = "Select Food",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))
                )

                recommendedFoods.forEach { foodItem ->
                    foodItem?.let {
                        Surface(
                            modifier = Modifier
                                .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                                .clickable{
                                    viewModel.updateFoodSelected(
                                        foodItem.variantId,
                                        !(viewModel.uiState.value.foodSelected[foodItem.variantId] ?: true)
                                    )
                                },
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp
                        ) {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                headlineContent = { Text(text =
                                    if (foodItem.variantName != stringResource(R.string.single)) {
                                        stringResource(R.string.name_variant_name, foodItem.foodName, foodItem.variantName)
                                    } else {
                                        foodItem.foodName
                                    }
                                ) },
                                supportingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.price, foodItem.price),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        VerticalDivider(
                                            modifier = Modifier
                                                .padding(dimensionResource(R.dimen.padding_small))
                                                .height(dimensionResource(R.dimen.vertical_divider_height)),
                                            color = Color.LightGray
                                        )
                                        Text(
                                            text = stringResource(R.string.kcal, foodItem.calories),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                trailingContent = {
                                    Checkbox(
                                        checked = viewModel.uiState.value.foodSelected[foodItem.variantId] ?: true,
                                        onCheckedChange = { viewModel.updateFoodSelected(foodItem.variantId, it) }
                                    )
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_small))
                ) {
                    Button(
                        onClick = {
                            if (onSaveToHistory()) onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.save_to_history),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}