package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.domain.model.FoodItem
import com.tohoku.cafeteria.domain.model.FoodVariant
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.cart.rememberCartViewModel
import com.tohoku.cafeteria.ui.components.CartFoodBottomSheetComponent
import com.tohoku.cafeteria.ui.components.MenuFoodBottomSheetComponent
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel(),
    recommendationViewModel: RecommendationViewModel = viewModel(factory = RecommendationViewModel.Factory),
    onGetRecommendationClick: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    var additionalNotes by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<CartItem?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val handleItemClick: (CartItem) -> Unit = { item ->
        selectedItem = item
        scope.launch { sheetState.show() }
    }

    CartFoodBottomSheetComponent(
        sheetState = sheetState,
        selectedItem = selectedItem,
        onDismiss = {
            scope.launch {
                sheetState.hide()
                selectedItem = null
            }
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_recommendation)) },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_medium))
            ) {
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { newText -> additionalNotes = newText },
                    label = { Text(stringResource(R.string.additional_instructions)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        .padding(bottom = dimensionResource(R.dimen.padding_xsmall)),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                )
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.total),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    trailingContent = {
                        Text(
                            text = stringResource(R.string.price, totalPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                Button(
                    onClick = {
                        recommendationViewModel.setAdditionalNotes(additionalNotes)
                        onGetRecommendationClick()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium))
                ) {
                    Text(stringResource(R.string.get_recommendation))
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.cart_empty),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(dimensionResource(R.dimen.padding_medium))
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onClick = handleItemClick,
                        onQuantityChanged = { newQuantity ->
                            cartViewModel.updateQuantity(item.item.variantId, newQuantity)
                        },
                        onRemoveClick = {
                            cartViewModel.removeFromCart(item.item.variantId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    modifier: Modifier = Modifier,
    item: CartItem,
    onClick: (CartItem) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onRemoveClick: () -> Unit
) {
    // Local state for confirmation dialog
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmMessage by remember { mutableStateOf("") }
    var onConfirmAction by remember { mutableStateOf({}) }

    val context = LocalContext.current

    // Helper function to show the dialog with the provided message and confirm action.
    fun showConfirmation(message: String, onConfirm: () -> Unit) {
        confirmMessage = message
        onConfirmAction = onConfirm
        showConfirmDialog = true
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
            .clickable { onClick(item) },
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.name_variant_name, item.name, item.item.variantName),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.price, item.item.price),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.padding_small))
                            .height(dimensionResource(R.dimen.vertical_divider_height)),
                        color = Color.LightGray
                    )
                    Text(
                        text = stringResource(R.string.kcal, item.item.calories),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (item.quantity - 1 <= 0) {
                                showConfirmation(context.getString(R.string.confirm_remove_cart, item.name)) {
                                    onQuantityChanged(0)
                                }
                            } else {
                                onQuantityChanged(item.quantity - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.decrease))
                    }

                    Text(
                        text = "${item.quantity}",
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                        style = MaterialTheme.typography.labelLarge
                    )

                    IconButton(
                        onClick = { onQuantityChanged(item.quantity + 1) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.increase))
                    }

                    IconButton(onClick = {
                        showConfirmation(context.getString(R.string.confirm_remove_cart, item.name)) {
                            onRemoveClick()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
                    }
                }
            }
        )
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(confirmMessage) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmAction()
                    showConfirmDialog = false
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartItemRowPreview() {
    CafeteriaAITheme {
        CartItemRow(
            item = CartItem(
                item = FoodVariant(
                    variantName = "M",
                    variantId = 102,
                    price = 600,
                    calories = 600,
                    protein = 30,
                    fat = 25,
                    carbohydrates = 60
                ),
                name = "Sample Burger",
                url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4="
            ),
            onClick = { },
            onQuantityChanged = { },
            onRemoveClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecommendationScreenPreview() {
    val cartViewModel = rememberCartViewModel()
    cartViewModel.addToCart(
        CartItem(
            item = FoodVariant(
                variantName = "S",
                variantId = 101,
                price = 500,
                calories = 500,
                protein = 25,
                fat = 20,
                carbohydrates = 50
            ),
            name = "Sample Burger",
            url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4="
        ),
        message = stringResource(R.string.added_to_cart)
    )
    cartViewModel.addToCart(
        CartItem(
            item = FoodVariant(
                variantName = "M",
                variantId = 102,
                price = 600,
                calories = 600,
                protein = 30,
                fat = 25,
                carbohydrates = 60
            ),
            name = "Sample Burger",
            url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4="
        ),
        message = stringResource(R.string.added_to_cart)
    )
    CafeteriaAITheme {
        RecommendationScreen(
            cartViewModel = cartViewModel,
            onGetRecommendationClick = { }
        )
    }
}