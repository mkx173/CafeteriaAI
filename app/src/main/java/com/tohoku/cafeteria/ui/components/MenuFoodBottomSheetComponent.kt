package com.tohoku.cafeteria.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.MenuItem
import com.tohoku.cafeteria.domain.model.NutritionData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFoodBottomSheetComponent(
    modifier: Modifier = Modifier,
    selectedItem: MenuItem? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem, NutritionData) -> Unit
) {
    if (sheetState.isVisible && selectedItem != null) {
        var selectedVariant by remember { mutableStateOf(selectedItem.nutritionDataList.firstOrNull()) }

        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_medium))
            ) {
                // Food image
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large),
                    painter = painterResource(R.drawable.sample_food),
                    contentDescription = selectedItem.name,
                    contentScale = ContentScale.Crop
                )

                // Food name
                Text(
                    text = selectedItem.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_medium))
                )

                // Variant selection with radio buttons (only if there's more than one variant)
                if (selectedItem.nutritionDataList.size > 1) {
                    Text(
                        text = stringResource(R.string.select_size),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_xsmall))
                    )

                    selectedItem.nutritionDataList.forEach { variant ->
                        Surface(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                                .clickable { selectedVariant = variant },
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = 1.dp
                        ) {
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                leadingContent = {
                                    RadioButton(
                                        selected = selectedVariant == variant,
                                        onClick = { selectedVariant = variant }
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        text = variant.variantName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = stringResource(R.string.kcal, variant.calories),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        text = stringResource(R.string.price, variant.price),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                }
                            )
                        }
                    }
                }

                // Nutrition details for selected variant
                selectedVariant?.let { variant ->
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(
                                top = dimensionResource(R.dimen.padding_medium),
                                bottom = dimensionResource(R.dimen.padding_small)
                            )
                    )

                    Text(
                        text = stringResource(R.string.nutrition_information),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                    )

                    NutritionDetailRow(label = stringResource(R.string.calories), value = stringResource(R.string.kcal, variant.calories))
                    NutritionDetailRow(label = stringResource(R.string.protein), value = stringResource(
                        R.string.gram, variant.protein
                    ))
                    NutritionDetailRow(label = stringResource(R.string.fat), value = stringResource(
                        R.string.gram, variant.fat
                    ))
                    NutritionDetailRow(label = stringResource(R.string.carbohydrates), value = stringResource(
                        R.string.gram, variant.carbohydrates
                    ))
                }
            }

            // Add to cart button
            selectedVariant?.let { variant ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                ) {
                    Button(
                        onClick = { onAddToCart(selectedItem, variant) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.add_to_cart_price, variant.price),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MenuFoodDisplayBottomSheetPreview() {
    val selectedItem by remember { mutableStateOf<MenuItem?>(
        MenuItem(
            foodId = 1,
            name = "Sample Burger",
            url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4=",
            nutritionDataList = listOf(
                NutritionData(
                    variantName = "S",
                    variantId = 101,
                    price = 500,
                    calories = 500,
                    protein = 25,
                    fat = 20,
                    carbohydrates = 50
                ),
                NutritionData(
                    variantName = "M",
                    variantId = 102,
                    price = 600,
                    calories = 600,
                    protein = 30,
                    fat = 25,
                    carbohydrates = 60
                )
            )
        )
    ) }

    MenuFoodBottomSheetComponent(
        sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded
        ),
        selectedItem = selectedItem,
        onDismiss = { },
        onAddToCart = {_, _ -> }
    )
}