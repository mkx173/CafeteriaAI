package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.entity.FoodEntity
import com.tohoku.cafeteria.data.response.RecommendationResponse
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.components.FoodEntityBottomSheetComponent
import com.tohoku.cafeteria.ui.components.FoodSelectorBottomSheetComponent
import com.tohoku.cafeteria.ui.navigation.Screen
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme
import com.tohoku.cafeteria.util.ToastManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationResultDisplay (
    modifier: Modifier = Modifier,
    viewModel: RecommendationViewModel = viewModel(factory = RecommendationViewModel.Factory),
    cartViewModel: CartViewModel = viewModel(),
    navController: NavHostController? = null,
    recommendationResponse: RecommendationResponse,
    getFoodByVariantId: suspend (Int) -> FoodEntity?,
    onTotalPriceCalculated: (Int) -> Unit,
    setSaveToHistoryClick: (() -> Unit) -> Unit
) {
    val uiState = viewModel.uiState.value
    val recommendedFoods = remember { mutableStateListOf<FoodEntity?>() }
    val context = LocalContext.current

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

    var showBottomSheet by remember { mutableStateOf(false) }
    val addToHistorySheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    FoodSelectorBottomSheetComponent(
        showBottomSheet = showBottomSheet,
        addToHistorySheetState = addToHistorySheetState,
        recommendedFoods = recommendedFoods,
        viewModel = viewModel,
        onDismiss = {
            scope.launch {
                addToHistorySheetState.hide()
                showBottomSheet = false
            }
        },
        onSaveToHistory = {
            val numSelected = viewModel.saveSelectedFoodsToHistory()
            if (numSelected == 0) {
                ToastManager.showMessage(context.getString(R.string.please_select_at_least_one_item))
                false
            } else {
                ToastManager.showMessage(context.getString(
                    if (numSelected == 1) R.string.item_saved_to_history else R.string.items_saved_to_history,
                    numSelected
                ))
                cartViewModel.clearCart()
                navController?.navigate(Screen.History.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = false }
                    launchSingleTop = true
                    restoreState = false
                }
                scope.launch {
                    addToHistorySheetState.hide()
                    showBottomSheet = false
                }
                true
            }
        }
    )

    LaunchedEffect(recommendationResponse.recommendedMeals) {
        recommendedFoods.clear() // Clear the list if the recommended meals change
        recommendationResponse.recommendedMeals.forEach { variantId ->
            val foodItem = getFoodByVariantId(variantId)
            recommendedFoods.add(foodItem)
            foodItem?.let {
                viewModel.updateFoodRating(foodItem.variantId, Rating.NONE, skipIfPresent = true)
                viewModel.updateFoodSelected(foodItem.variantId, true, skipIfPresent = true)
            }
        }
        val computedPrice = recommendedFoods.filterNotNull().sumOf { it.price }
        onTotalPriceCalculated(computedPrice)
        setSaveToHistoryClick {
            scope.launch {
                showBottomSheet = true
                addToHistorySheetState.show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
    ) {
        item {
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_small)))
        }
        item {
            Surface(
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_xsmall)),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                ) {
                    Text(
                        text = stringResource(R.string.your_recommended_meals),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = recommendationResponse.recommendedMealDetail,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                    )
                }
            }
        }

        items(recommendedFoods) { foodItem ->
            Surface (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                    .clickable { foodItem?.let(handleItemClick) },
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                if (foodItem != null) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(R.dimen.padding_small)),
                        headlineContent = { Text(
                            text = if (foodItem.variantName != stringResource(R.string.single)) {
                                stringResource(R.string.name_variant_name, foodItem.foodName, foodItem.variantName)
                            } else {
                                foodItem.foodName
                            })
                        },
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
                        leadingContent = {
                            AsyncImage(
                                modifier = Modifier
                                    .size(dimensionResource(R.dimen.size_food_item_image))
                                    .clip(CircleShape),
                                model = foodItem.imageUrl,
                                contentDescription = foodItem.foodName,
                                contentScale = ContentScale.Crop
                            )
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val currentRating = uiState.foodRatings[foodItem.variantId] ?: Rating.NONE
                                    val newRating = if (currentRating == Rating.UP) Rating.NONE else Rating.UP
                                    viewModel.updateFoodRating(foodItem.variantId, newRating)
                                }) {
                                    val isSelected = uiState.foodRatings[foodItem.variantId] == Rating.UP
                                    val icon = if (isSelected) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp
                                    Icon(
                                        icon,
                                        modifier = Modifier.size(dimensionResource(R.dimen.size_icon_large)),
                                        contentDescription = stringResource(R.string.thumbs_up),
                                        tint = if (uiState.foodRatings[foodItem.variantId] == Rating.UP) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                    )
                                }
                                IconButton(onClick = {
                                    val currentRating = uiState.foodRatings[foodItem.variantId] ?: Rating.NONE
                                    val newRating = if (currentRating == Rating.DOWN) Rating.NONE else Rating.DOWN
                                    viewModel.updateFoodRating(foodItem.variantId, newRating)
                                }) {
                                    val isSelected = uiState.foodRatings[foodItem.variantId] == Rating.DOWN
                                    val icon = if (isSelected) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown
                                    Icon(
                                        icon,
                                        modifier = Modifier.size(dimensionResource(R.dimen.size_icon_large)),
                                        contentDescription = stringResource(R.string.thumbs_down),
                                        tint = if (uiState.foodRatings[foodItem.variantId] == Rating.DOWN) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                    )
                                }
                            }
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
        item {
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_xsmall)))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecommendationResultDisplayPreview() {
    val recommendationResponse = RecommendationResponse(
        additionalNotes = "24 years old, 50 kg, 165 cm, lactose intolerant.",
        detailNutritions = listOf(
            "Based on the user's age, weight, and height, the minimum energy intake is estimated using the Harris-Benedict equation and considering a sedentary lifestyle (Roza & Shizgal, 1984).",
            "According to current guidelines, the minimum protein intake for adults is 0.8 grams per kilogram of body weight (WHO, 2007).",
            "Based on a 24-year-old female, with a weight of 50 kg, the recommended minimum fat intake is approximately 0.8-1.0 grams per kilogram of body weight according to current nutritional guidelines [WARNING] (https://www.calculator.net/fat-calculator.html).",
            "According to current dietary guidelines and considering the user's age, weight, and height, a minimum carbohydrate intake of 130 grams per day is recommended to support basic metabolic functions (National Academies of Sciences, Engineering, and Medicine, 2005).",
            "Based on general dietary guidelines and considering the user's age, the recommended minimum daily fiber intake is approximately 25 grams (Anderson et al., 1998).",
            "According to the National Institutes of Health, adults aged 19-50 years need 1000 mg of calcium daily to maintain bone health, which is especially important for lactose-intolerant individuals who may have limited dairy intake (NIH, n.d.).",
            "Given the user's age, weight, height, and lactose intolerance, coupled with the need to determine a minimum vegetable intake, I will use the tavily_search_tool to find guidelines since I don't have specific journal data [WARNING]."
        ),
        minNutritions = listOf(1320, 40, 40, 130, 25, 1000, 400),
        recommendedMealDetail = "Enjoy a balanced meal featuring \"Hamburger steak with grated Japanese radish sauce\" and \"Rice (small)\". The hamburger steak offers a delightful umami flavor, perfectly complemented by the refreshing radish. This combination provides approximately 460 kcal of energy, 17.6g of protein, and 15.2g of fat, catering to your dietary requirements. Please note that this meal is somewhat low in fiber, calcium, and veggies compared to your target nutritional goals.",
        listMeals = listOf("hamburger steak with grated japanese radish sauce", "rice (small)"),
        verboseInFunction = true,
        recommendedMeals = listOf(101, 102),
        id = "f64299e3-2985-44f6-a6ce-eedaec54c502"
    )
    CafeteriaAITheme {
        RecommendationResultDisplay(
            recommendationResponse = recommendationResponse,
            getFoodByVariantId = { variantId -> // Provide a mock implementation here
                // Create mock FoodEntity objects as needed for your preview
                when (variantId) {
                    101 -> FoodEntity(101, 1, "small", "rice", 100, 150, 3, 1, 30, "side", "rice_small.jpg")
                    102 -> FoodEntity(102, 2, "grated", "hamburger steak", 400, 310, 15, 12, 10, "main", "hamburger_steak.jpg")
                    else -> null
                }
            },
            onTotalPriceCalculated = { },
            setSaveToHistoryClick = { }
        )
    }
}