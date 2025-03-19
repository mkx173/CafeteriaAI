package com.tohoku.cafeteria.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.domain.model.FoodItem
import com.tohoku.cafeteria.domain.model.FoodVariant
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.components.MenuCarouselComponent
import com.tohoku.cafeteria.ui.components.MenuFoodBottomSheetComponent
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFoodDisplay(
    modifier: Modifier = Modifier,
    categoryData: List<FoodCategory>?,
    cartViewModel: CartViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val context = LocalContext.current

    MenuFoodBottomSheetComponent(
        sheetState = sheetState,
        selectedItem = selectedItem,
        onDismiss = {
            scope.launch { sheetState.hide() }
            selectedItem = null
        },
        onAddToCart = { menuItem, variant ->
            cartViewModel.addToCart(
                CartItem(item = variant, url = menuItem.url, name = menuItem.name),
                context.getString(R.string.added_to_cart),
                context.getString(R.string.item_already_in_cart)
            )
            scope.launch { sheetState.hide() }
        }
    )

    val handleItemClick: (FoodItem) -> Unit = { item ->
        selectedItem = item
        scope.launch { sheetState.show() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
    ) {
        categoryData?.let {
            if (categoryData.isNotEmpty()) {
                item {
                    MenuCarouselComponent(
                        title = "Today's Menu",
                        items = categoryData[0].items,
                        onItemClick = handleItemClick
                    )
                }
            }
            items(categoryData) { foodCategory ->
                Column(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                        text = foodCategory.category,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    foodCategory.items.forEach { foodItem ->
                        Surface (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { handleItemClick(foodItem) }
                                .padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp
                        ) {
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = dimensionResource(R.dimen.padding_small)),
                                headlineContent = {
                                    Text(
                                        text = foodItem.name
                                    )
                                },
                                leadingContent = {
                                    AsyncImage(
                                        modifier = Modifier
                                            .size(dimensionResource(R.dimen.size_food_item_image))
                                            .clip(CircleShape),
                                        model = foodItem.url,
                                        contentDescription = foodItem.name,
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                supportingContent = {
                                    val calorieValues = foodItem.foodVariantsList.map { it.calories }
                                    val calorieText = if (calorieValues.size > 1) {
                                        stringResource(
                                            R.string.kcal_range,
                                            calorieValues.min(),
                                            calorieValues.max()
                                        )
                                    } else {
                                        stringResource(R.string.kcal, calorieValues.firstOrNull() ?: 0)
                                    }
                                    Text(text = calorieText)
                                },
                                trailingContent = {
                                    val priceValues = foodItem.foodVariantsList.map { it.price }
                                    val priceText = if (priceValues.size > 1) {
                                        stringResource(
                                            R.string.price_range,
                                            priceValues.min(),
                                            priceValues.max()
                                        )
                                    } else {
                                        stringResource(R.string.price, priceValues.firstOrNull() ?: 0)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = priceText,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Spacer(
                                            modifier = Modifier.size(dimensionResource(R.dimen.padding_xsmall))
                                        )
                                        Icon(
                                            modifier = Modifier.size(dimensionResource(R.dimen.size_icon)),
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = stringResource(R.string.add_to_cart)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_xsmall)))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuFoodDisplayPreview() {
    CafeteriaAITheme {
        MenuFoodDisplay(
            categoryData = listOf(
                FoodCategory(
                    category = "Burgers",
                    items = List(5) {
                        FoodItem(
                            foodId = 1,
                            name = "Sample Burger",
                            url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4=",
                            foodVariantsList = listOf(
                                FoodVariant(
                                    variantName = "S",
                                    variantId = 101,
                                    price = 500,
                                    calories = 500,
                                    protein = 25,
                                    fat = 20,
                                    carbohydrates = 50
                                ),
                                FoodVariant(
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
                    }
                )
            )
        )
    }
}
