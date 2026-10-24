package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.CartRepository
import com.rekora.app.domain.repository.OrderRepository
import com.rekora.app.domain.model.CartItem
import com.rekora.app.domain.model.DeliveryMethod
import com.rekora.app.domain.model.Order
import com.rekora.app.domain.model.OrderItem
import com.rekora.app.domain.model.OrderStatus
import com.rekora.app.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State container for checkout, delivery dispatch, and UPI sandbox payment verification.
 */
data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val depositTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.CAMPUS_PICKUP,
    val pickupLocation: String = "St. Joseph's Maker Lab — Station A (ECE Block)",
    val deliveryAddress: String = "",
    val notes: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentStepDescription: String? = null,
    val isProcessing: Boolean = false,
    val confirmedOrder: Order? = null,
    val error: String? = null
)

/**
 * CheckoutViewModel coordinates the complete ordering and payment pipeline:
 * - Delivery method selection (Campus Pickup vs Hostel/Doorstep Delivery)
 * - Safe simulated payment gateway with explicit state transitions (PENDING -> PROCESSING -> COMPLETED)
 * - Security deposit segregation into refundable escrow
 * - Order finalization and automatic cart clearance
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _deliveryMethod = MutableStateFlow(DeliveryMethod.CAMPUS_PICKUP)
    private val _deliveryAddress = MutableStateFlow("")
    private val _notes = MutableStateFlow("")
    private val _isProcessing = MutableStateFlow(false)
    private val _paymentStatus = MutableStateFlow(PaymentStatus.PENDING)
    private val _paymentStepDescription = MutableStateFlow<String?>(null)
    private val _confirmedOrder = MutableStateFlow<Order?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CheckoutUiState> = combine(
        cartRepository.getCartItems(),
        _deliveryMethod,
        _deliveryAddress,
        _notes,
        _isProcessing,
        _paymentStatus,
        _paymentStepDescription,
        _confirmedOrder,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val items = args[0] as List<CartItem>
        val method = args[1] as DeliveryMethod
        val address = args[2] as String
        val notesText = args[3] as String
        val processing = args[4] as Boolean
        val payStatus = args[5] as PaymentStatus
        val stepDesc = args[6] as String?
        val order = args[7] as Order?
        val err = args[8] as String?

        val subtotal = items.sumOf {
            if (it.isRental) {
                val weeklyRate = it.component?.rentalPricePerWeek ?: it.unitPrice
                weeklyRate * (it.rentalDays / 7.0)
            } else {
                it.unitPrice * it.quantity
            }
        }
        val depositTotal = items.sumOf { if (it.isRental) it.deposit else 0.0 }
        val deliveryFee = if (method == DeliveryMethod.DELIVERY) 49.0 else 0.0
        val total = subtotal + depositTotal + deliveryFee

        CheckoutUiState(
            items = items,
            subtotal = subtotal,
            depositTotal = depositTotal,
            deliveryFee = deliveryFee,
            totalAmount = total,
            deliveryMethod = method,
            deliveryAddress = address,
            notes = notesText,
            paymentStatus = payStatus,
            paymentStepDescription = stepDesc,
            isProcessing = processing,
            confirmedOrder = order,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState()
    )

    fun setDeliveryMethod(method: DeliveryMethod) {
        _deliveryMethod.value = method
    }

    fun setDeliveryAddress(address: String) {
        _deliveryAddress.value = address
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun processPaymentAndPlaceOrder(onSuccess: (orderId: String) -> Unit) {
        val currentItems = uiState.value.items
        if (currentItems.isEmpty()) {
            _error.value = "Cart is empty. Please select components first."
            return
        }

        if (_deliveryMethod.value == DeliveryMethod.DELIVERY && _deliveryAddress.value.isBlank()) {
            _error.value = "Campus room / hostel address is required for delivery."
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            _error.value = null

            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"

                // 1. Initiate Gateway
                _paymentStatus.value = PaymentStatus.PROCESSING
                _paymentStepDescription.value = "Initiating Campus Sandbox Gateway..."
                delay(700)

                // 2. Escrow security deposits if any rentals exist
                val hasRentals = currentItems.any { it.isRental }
                if (hasRentals) {
                    _paymentStepDescription.value = "Verifying refundable security deposit escrow..."
                    delay(600)
                }

                // 3. Create persistent Order entity
                val orderId = "ord-" + UUID.randomUUID().toString().take(8)
                val orderItems = currentItems.map { cartItem ->
                    OrderItem(
                        id = UUID.randomUUID().toString(),
                        componentId = cartItem.componentId,
                        componentName = cartItem.component?.name ?: "Component",
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        totalPrice = cartItem.unitPrice * cartItem.quantity,
                        isRental = cartItem.isRental,
                        rentalDays = cartItem.rentalDays,
                        deposit = cartItem.deposit
                    )
                }

                val currentState = uiState.value
                val newOrder = Order(
                    id = orderId,
                    userId = userId,
                    items = orderItems,
                    subtotal = currentState.subtotal,
                    tax = 0.0,
                    deliveryFee = currentState.deliveryFee,
                    totalAmount = currentState.totalAmount,
                    depositTotal = currentState.depositTotal,
                    status = OrderStatus.CONFIRMED,
                    paymentStatus = PaymentStatus.COMPLETED,
                    paymentMethod = "Sandbox Campus Pay",
                    deliveryMethod = _deliveryMethod.value,
                    deliveryAddress = if (_deliveryMethod.value == DeliveryMethod.DELIVERY) _deliveryAddress.value else null,
                    notes = _notes.value.ifBlank { null },
                    isDemo = true,
                    createdAt = System.currentTimeMillis()
                )

                val createdOrderId = orderRepository.createOrder(newOrder, orderItems)

                _paymentStatus.value = PaymentStatus.COMPLETED
                _paymentStepDescription.value = "Payment Verified & Order Confirmed!"
                _confirmedOrder.value = newOrder

                cartRepository.clearCart()
                delay(400)
                onSuccess(createdOrderId)
            } catch (e: Exception) {
                _paymentStatus.value = PaymentStatus.FAILED
                _error.value = "Payment failed: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
