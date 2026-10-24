package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.entity.CartItemEntity
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.domain.model.CartItem
import com.rekora.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val componentDao: ComponentDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { cartEntity ->
                val component = componentDao.getComponentById(cartEntity.componentId)?.toDomain()
                cartEntity.toDomain(component)
            }
        }
    }

    override fun getCartCount(): Flow<Int> {
        return cartDao.getCartCount()
    }

    override suspend fun addToCart(
        componentId: String,
        quantity: Int,
        isRental: Boolean,
        rentalDays: Int
    ) {
        val existing = cartDao.getCartItemByComponent(componentId)
        if (existing != null) {
            cartDao.updateCartItem(
                existing.copy(
                    quantity = existing.quantity + quantity,
                    isRental = isRental,
                    rentalDays = rentalDays
                )
            )
        } else {
            val component = componentDao.getComponentById(componentId)
            val unitPrice = if (isRental) {
                component?.rentalPricePerWeek ?: 0.0
            } else {
                component?.purchasePrice ?: 0.0
            }
            val deposit = if (isRental) {
                component?.securityDeposit ?: 0.0
            } else {
                0.0
            }
            cartDao.insertCartItem(
                CartItemEntity(
                    id = UUID.randomUUID().toString(),
                    componentId = componentId,
                    quantity = quantity,
                    isRental = isRental,
                    rentalDays = rentalDays,
                    unitPrice = unitPrice,
                    deposit = deposit
                )
            )
        }
    }

    override suspend fun updateQuantity(cartItemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItemById(cartItemId)
        } else {
            val items = cartDao.getCartItems().first()
            val item = items.find { it.id == cartItemId }
            if (item != null) {
                cartDao.updateCartItem(item.copy(quantity = quantity))
            }
        }
    }

    override suspend fun removeFromCart(cartItemId: String) {
        cartDao.deleteCartItemById(cartItemId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
