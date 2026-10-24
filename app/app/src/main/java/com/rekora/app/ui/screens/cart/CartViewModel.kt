package com.rekora.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.entity.CartItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItemDisplay(
    val id: String,
    val componentId: String,
    val name: String,
    val category: String,
    val isRental: Boolean,
    val quantity: Int,
    val rentalWeeks: Int,
    val unitPrice: Double,
    val deposit: Double,
    val lineTotal: Double
)

data class CartUiState(
    val items: List<CartItemDisplay> = emptyList(),
    val purchaseSubtotal: Double = 0.0,
    val rentalSubtotal: Double = 0.0,
    val depositTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalPayable: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val componentDao: ComponentDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCartItems()
    }

    private fun observeCartItems() {
        viewModelScope.launch {
            cartDao.getCartItems()
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { entities ->
                    val displayList = entities.map { cartEntity ->
                        val comp = componentDao.getComponentById(cartEntity.componentId)
                            ?: SeedDataProvider.getSeedComponents().find { it.id == cartEntity.componentId }

                        val name = comp?.name ?: "Electronic Component"
                        val category = comp?.category ?: "HARDWARE"
                        val weeks = if (cartEntity.isRental) (cartEntity.rentalDays / 7).coerceAtLeast(1) else 0

                        val lineTotal = if (cartEntity.isRental) {
                            (cartEntity.unitPrice * weeks) + cartEntity.deposit
                        } else {
                            cartEntity.unitPrice * cartEntity.quantity
                        }

                        CartItemDisplay(
                            id = cartEntity.id,
                            componentId = cartEntity.componentId,
                            name = name,
                            category = category.replace("_", " "),
                            isRental = cartEntity.isRental,
                            quantity = cartEntity.quantity,
                            rentalWeeks = weeks,
                            unitPrice = cartEntity.unitPrice,
                            deposit = cartEntity.deposit,
                            lineTotal = lineTotal
                        )
                    }

                    val purchaseSub = displayList
                        .filter { !it.isRental }
                        .sumOf { it.unitPrice * it.quantity }

                    val rentalSub = displayList
                        .filter { it.isRental }
                        .sumOf { it.unitPrice * it.rentalWeeks }

                    val depositSum = displayList
                        .filter { it.isRental }
                        .sumOf { it.deposit }

                    val delivery = 0.0 // Free campus pickup
                    val total = purchaseSub + rentalSub + depositSum + delivery

                    _uiState.update {
                        it.copy(
                            items = displayList,
                            purchaseSubtotal = purchaseSub,
                            rentalSubtotal = rentalSub,
                            depositTotal = depositSum,
                            deliveryFee = delivery,
                            totalPayable = total,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun updateQuantity(cartItemId: String, newQty: Int) {
        if (newQty < 1) return
        viewModelScope.launch {
            val entities = cartDao.getCartItems().first()
            val target = entities.find { it.id == cartItemId }
            if (target != null) {
                cartDao.updateCartItem(target.copy(quantity = newQty))
            }
        }
    }

    fun deleteItem(cartItemId: String) {
        viewModelScope.launch {
            cartDao.deleteCartItemById(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }
}
