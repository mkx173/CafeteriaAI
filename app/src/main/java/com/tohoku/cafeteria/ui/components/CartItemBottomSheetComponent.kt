package com.tohoku.cafeteria.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemBottomSheetComponent(
    modifier: Modifier = Modifier,
    selectedItem: CartItem? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    if (sheetState.isVisible && selectedItem != null) {
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
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large),
                    model = selectedItem.url,
                    contentDescription = selectedItem.name,
                    contentScale = ContentScale.Crop
                )

                // Food name
                Text(
                    text =
                        if (selectedItem.item.variantName != stringResource(R.string.single)) {
                            stringResource(R.string.name_variant_name, selectedItem.name, selectedItem.item.variantName)
                        } else {
                            selectedItem.name
                        },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_medium))
                )

                // Nutrition details for selected variant
                selectedItem.let { cartItem ->
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small)),
                        color = Color.LightGray
                    )

                    Text(
                        text = stringResource(R.string.nutrition_information),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                    )

                    NutritionDetailRow(label = stringResource(R.string.calories), value = stringResource(R.string.kcal, cartItem.item.calories))
                    NutritionDetailRow(label = stringResource(R.string.protein), value = stringResource(
                        R.string.gram, cartItem.item.protein
                    ))
                    NutritionDetailRow(label = stringResource(R.string.fat), value = stringResource(
                        R.string.gram, cartItem.item.fat
                    ))
                    NutritionDetailRow(label = stringResource(R.string.carbohydrates), value = stringResource(
                        R.string.gram, cartItem.item.carbohydrates
                    ))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                    .padding(bottom = dimensionResource(R.dimen.padding_medium))
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}