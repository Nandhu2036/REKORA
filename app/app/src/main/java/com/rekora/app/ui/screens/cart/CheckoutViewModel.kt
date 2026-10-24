package com.rekora.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.dao.OrderDao
import com.rekora.app.data.local.dao.RentalDao
import com.rekora.app.data.local.entity.OrderEntity
import com.rekora.app.data.local.entity.OrderItemEntity
import com.rekora.app.data.local.entity.RentalEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class DeliveryChoice {
    CAMPUS_PICKUP, LAB_DELIVERY
}

enum class PaymentChoice {
    UPI_SANDBOX, CASH_ON_PICKUP
}

data class CheckoutUiState(
    val deliveryMethod: DeliveryChoice = DeliveryChoice.CAMPUS_PICKUP,
    val campusDeliveryAddress: String = "Robotics Lab, Block B Room 104",
    val paymentMethod: PaymentChoice = PaymentChoice.UPI_SANDBOX,
    val upiId: String = "student@sjc.ac.in",
    val items: List<CartItemDisplay> = emptyList(),
    val purchaseSubtotal: Double = 0.0,
    val rentalSubtotal: Double = 0.0,
    val depositTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val isProcessing: Boolean = false,
    val completedOrderId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val rentalDao: RentalDao,
    private val componentDao: ComponentDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCartSummary()
    }

    private fun loadCartSummary() {
        viewModelScope.launch {
            cartDao.getCartItems()
                .firstOrNull()?.let { entities ->
                    val displayList = entities.map { cartEntity ->
                        val comp = componentDao.getComponentById(cartEntity.componentId)
                            ?: SeedDataProvider.getSeedComponents().find { it.id == cartEntity.componentId }

                        val name = comp?.name ?: "Component"
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

                    val purchaseSub = displayList.filter { !it.isRental }.sumOf { it.unitPrice * it.quantity }
                    val rentalSub = displayList.filter { it.isRental }.sumOf { it.unitPrice * it.rentalWeeks }
                    val depositSum = displayList.filter { it.isRental }.sumOf { it.deposit }
                    val total = purchaseSub + rentalSub + depositSum

                    _uiState.update {
                        it.copy(
                            items = displayList,
                            purchaseSubtotal = purchaseSub,
                            rentalSubtotal = rentalSub,
                            depositTotal = depositSum,
                            totalAmount = total
                        )
                    }
                }
        }
    }

    fun setDeliveryChoice(choice: DeliveryChoice) {
        _uiState.update { it.copy(deliveryMethod = choice) }
    }

    fun setCampusAddress(address: String) {
        _uiState.update { it.copy(campusDeliveryAddress = address) }
    }

    fun setPaymentChoice(choice: PaymentChoice) {
        _uiState.update { it.copy(paymentMethod = choice) }
    }

    fun setUpiId(id: String) {
        _uiState.update { it.copy(upiId = id) }
    }

    fun processOrder(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            // Realistic simulation delay for sandbox payment / order registration
            delay(1500)

            val orderId = "ORD-SJC-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}"
            val currentState = _uiState.value

            val orderEntity = OrderEntity(
                id = orderId,
                userId = "usr-student-pilot",
                subtotal = currentState.purchaseSubtotal + currentState.rentalSubtotal,
                tax = 0.0,
                deliveryFee = 0.0,
                totalAmount = currentState.totalAmount,
                depositTotal = currentState.depositTotal,
                status = "CONFIRMED",
                paymentStatus = if (currentState.paymentMethod == PaymentChoice.UPI_SANDBOX) "SANDBOX" else "PENDING",
                paymentMethod = if (currentState.paymentMethod == PaymentChoice.UPI_SANDBOX) "UPI Sandbox (${currentState.upiId})" else "Cash on Lab Desk",
                deliveryMethod = if (currentState.deliveryMethod == DeliveryChoice.CAMPUS_PICKUP) "CAMPUS_PICKUP" else "LAB_DELIVERY",
                deliveryAddress = if (currentState.deliveryMethod == DeliveryChoice.CAMPUS_PICKUP) "Engineering Block B, Room 204" else currentState.campusDeliveryAddress,
                notes = "St. Joseph's College Pilot Sandbox Order",
                isDemo = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save order
            orderDao.insertOrder(orderEntity)

            // Save order items
            val orderItems = currentState.items.map { item ->
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    componentId = item.componentId,
                    componentName = item.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.lineTotal,
                    isRental = item.isRental,
                    rentalDays = item.rentalWeeks * 7,
                    deposit = item.deposit
                )
            }
            orderDao.insertOrderItems(orderItems)

            // Save rental records if any
            currentState.items.filter { it.isRental }.forEach { rentalItem ->
                val rentalEntity = RentalEntity(
                    id = "RNT-SJC-${UUID.randomUUID().toString().take(6).uppercase()}",
                    userId = "usr-student-pilot",
                    componentId = rentalItem.componentId,
                    componentName = rentalItem.name,
                    inventoryItemCode = "RK-SJC-${(100..999).random()}",
                    status = "AWAITING_PICKUP",
                    startDate = System.currentTimeMillis(),
                    endDate = System.currentTimeMillis() + (rentalItem.rentalWeeks * 7L * 86400000L),
                    durationDays = rentalItem.rentalWeeks * 7,
                    rentalCost = rentalItem.unitPrice * rentalItem.rentalWeeks,
                    securityDeposit = rentalItem.deposit,
                    depositRefunded = false,
                    conditionAtCheckout = "GOOD",
                    conditionAtReturn = null,
                    damageReported = false,
                    damageNotes = null,
                    pickupLocation = "Engineering Block B, Room 204",
                    returnLocation = "Engineering Block B, Room 204",
                    isDemo = true,
                    createdAt = System.currentTimeMillis()
                )
                rentalDao.insertRental(rentalEntity)
            }

            // Clear Cart
            cartDao.clearCart()

            _uiState.update { it.copy(isProcessing = false, completedOrderId = orderId) }
            onComplete(orderId)
        }
    }
}
