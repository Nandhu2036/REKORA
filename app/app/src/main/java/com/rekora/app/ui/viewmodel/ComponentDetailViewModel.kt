package com.rekora.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.CartRepository
import com.rekora.app.domain.repository.ComponentRepository
import com.rekora.app.domain.model.Component
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for Component Detail page with Buy vs Rent pricing calculators.
 */
data class ComponentDetailUiState(
    val component: Component? = null,
    val isRentalSelected: Boolean = false,
    val rentalWeeks: Int = 2,
    val quantity: Int = 1,
    val calculatedPrice: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val totalWithDeposit: Double = 0.0,
    val purchaseCostSavings: Double = 0.0,
    val cartCount: Int = 0,
    val isAddingToCart: Boolean = false,
    val addedSuccess: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ComponentDetailViewModel manages detailed specifications and acquisition trade-offs:
 * - Dynamic price calculation comparing purchase vs rental duration
 * - Security deposit tracking (segregated from operational charges)
 * - Quantity selection with inventory stock bounds
 * - Cart dispatch
 */
@HiltViewModel
class ComponentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val componentRepository: ComponentRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val componentId: String = checkNotNull(savedStateHandle["componentId"]) {
        "componentId parameter is required for ComponentDetailViewModel"
    }

    private val _isRentalSelected = MutableStateFlow(false)
    private val _rentalWeeks = MutableStateFlow(2)
    private val _quantity = MutableStateFlow(1)
    private val _isAddingToCart = MutableStateFlow(false)
    private val _addedSuccess = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val componentFlow = componentRepository.getComponentById(componentId)
    private val cartCountFlow = cartRepository.getCartCount()

    val uiState: StateFlow<ComponentDetailUiState> = combine(
        componentFlow,
        _isRentalSelected,
        _rentalWeeks,
        _quantity,
        cartCountFlow,
        _isAddingToCart,
        _addedSuccess,
        _error
    ) { args: Array<Any?> ->
        val comp = args[0] as? Component
        val isRental = args[1] as Boolean
        val weeks = args[2] as Int
        val qty = args[3] as Int
        val cartCount = args[4] as Int
        val adding = args[5] as Boolean
        val success = args[6] as Boolean
        val err = args[7] as? String

        if (comp == null) {
            ComponentDetailUiState(isLoading = true, cartCount = cartCount, error = err)
        } else {
            val calcPrice: Double
            val deposit: Double

            if (isRental && comp.isRentable) {
                calcPrice = comp.rentalPricePerWeek * weeks * qty
                deposit = comp.securityDeposit * qty
            } else {
                calcPrice = comp.purchasePrice * qty
                deposit = 0.0
            }

            val total = calcPrice + deposit
            val savings = if (isRental && comp.isRentable) {
                ((comp.purchasePrice * qty) - calcPrice).coerceAtLeast(0.0)
            } else 0.0

            ComponentDetailUiState(
                component = comp,
                isRentalSelected = isRental && comp.isRentable,
                rentalWeeks = weeks,
                quantity = qty,
                calculatedPrice = calcPrice,
                securityDeposit = deposit,
                totalWithDeposit = total,
                purchaseCostSavings = savings,
                cartCount = cartCount,
                isAddingToCart = adding,
                addedSuccess = success,
                isLoading = false,
                error = err
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComponentDetailUiState(isLoading = true)
    )

    fun selectBuyOrRent(isRental: Boolean) {
        _isRentalSelected.value = isRental
    }

    fun updateRentalWeeks(weeks: Int) {
        _rentalWeeks.value = weeks.coerceIn(1, 16)
    }

    fun updateQuantity(quantity: Int) {
        val maxStock = uiState.value.component?.availableQuantity ?: 10
        _quantity.value = quantity.coerceIn(1, maxStock.coerceAtLeast(1))
    }

    fun addToCart() {
        val currentComponent = uiState.value.component ?: return
        val isRental = uiState.value.isRentalSelected
        val qty = uiState.value.quantity
        val rentalDays = uiState.value.rentalWeeks * 7

        viewModelScope.launch {
            _isAddingToCart.value = true
            _error.value = null
            try {
                cartRepository.addToCart(
                    componentId = currentComponent.id,
                    quantity = qty,
                    isRental = isRental,
                    rentalDays = rentalDays
                )
                _addedSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to add to cart: ${e.localizedMessage}"
            } finally {
                _isAddingToCart.value = false
            }
        }
    }

    fun resetAddedNotification() {
        _addedSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
