package com.tohoku.cafeteria.ui.cart

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.domain.model.CartItem
import com.tohoku.cafeteria.util.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    val totalPrice = cartItems.map { items ->
        items.sumOf { it.item.price }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addToCart(item: CartItem, message: String, alreadyInCartMessage: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.variantId == item.item.variantId }

        if (existingItem != null) {
            ToastManager.showMessage(alreadyInCartMessage)
            return
        } else {
            currentItems.add(item)
        }

        _cartItems.value = currentItems
        // Show confirmation message
        ToastManager.showMessage(
            String.format(message, item.name, item.item.variantName)
        )
    }

    fun getCartItems(): List<CartItem> {
        return _cartItems.value
    }

    fun removeFromCart(itemId: Int) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeIf { it.item.variantId == itemId }
        _cartItems.value = currentItems
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

@Composable
fun rememberCartViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): CartViewModel {
    return viewModel(viewModelStoreOwner)
}
