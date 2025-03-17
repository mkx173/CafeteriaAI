package com.tohoku.cafeteria.ui.cart

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.ui.navigation.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CartViewModel(
    private val application: CafeteriaApplication
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    val totalPrice = cartItems.map { items ->
        items.sumOf { it.item.price * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addToCart(item: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.variantId == item.item.variantId }

        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            currentItems.add(item)
        }

        _cartItems.value = currentItems
        // Show confirmation message
        SnackbarManager.showMessage(
            application.getString(
                R.string.added_to_cart,
                item.name,
                item.item.variantName
            ))
    }

    fun removeFromCart(itemId: Int) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeIf { it.item.variantId == itemId }
        _cartItems.value = currentItems
    }

    fun updateQuantity(itemId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(itemId)
            return
        }

        _cartItems.value = _cartItems.value.map { cartItem ->
            if (cartItem.item.variantId == itemId) {
                cartItem.copy(quantity = newQuantity)
            } else {
                cartItem
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                CartViewModel(application = application)
            }
        }
    }
}

@Composable
fun rememberCartViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): CartViewModel {
    return viewModel(viewModelStoreOwner, factory = CartViewModel.Factory)
}
