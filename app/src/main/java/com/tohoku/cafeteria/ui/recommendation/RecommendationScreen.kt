package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.ui.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_recommendation)) },
            )
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.cart_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChanged = { newQuantity ->
                            cartViewModel.updateQuantity(item.item.variantId, newQuantity)
                        },
                        onRemoveClick = {
                            cartViewModel.removeFromCart(item.item.variantId)
                        }
                    )
                    HorizontalDivider()
                }

                // Total and checkout
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = stringResource(R.string.price, totalPrice),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

                        Button(
                            onClick = { /* Implement checkout */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.get_recommendation))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.name_variant_name, item.name, item.item.variantName),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.price, item.item.price),
                style = MaterialTheme.typography.bodyMedium
            )
        }

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
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
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
