package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.CartRepository
import com.rekora.app.domain.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the hardware shopping cart.
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val depositTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val itemCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * CartViewModel manages the student procurement cart:
 * - Line total calculations combining buy and rent line items
 * - Segregated security deposit totals for transparency
 * - Quantity adjustments and item removal
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        _isLoading,
        _error
    ) { items, loading, err ->
        val subtotal = items.sumOf {
            if (it.isRental) {
                val weeklyRate = it.component?.rentalPricePerWeek ?: it.unitPrice
                weeklyRate * (it.rentalDays / 7.0)
            } else {
                it.unitPrice * it.quantity
            }
        }
        val depositTotal = items.sumOf { if (it.isRental) it.deposit else 0.0 }
        val grandTotal = subtotal + depositTotal
        val totalUnits = items.sumOf { it.quantity }

        CartUiState(
            items = items,
            subtotal = subtotal,
            depositTotal = depositTotal,
            grandTotal = grandTotal,
            itemCount = totalUnits,
            isLoading = loading,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartUiState(isLoading = true)
    )

    fun updateQuantity(cartItemId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.updateQuantity(cartItemId, quantity)
            } catch (e: Exception) {
                _error.value = "Failed to update item quantity: ${e.localizedMessage}"
            }
        }
    }

    fun removeItem(cartItemId: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(cartItemId)
            } catch (e: Exception) {
                _error.value = "Failed to remove item: ${e.localizedMessage}"
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
            } catch (e: Exception) {
                _error.value = "Failed to clear cart: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
