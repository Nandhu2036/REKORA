package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.OrderDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.Order
import com.rekora.app.domain.model.OrderItem
import com.rekora.app.domain.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getOrdersForUser(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersForUser(userId).flatMapLatest { orderEntities ->
            if (orderEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                val orderFlows = orderEntities.map { orderEntity ->
                    orderDao.getOrderItems(orderEntity.id).map { itemEntities ->
                        orderEntity.toDomain(items = itemEntities.map { it.toDomain() })
                    }
                }
                combine(orderFlows) { it.toList() }
            }
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> {
        return combine(
            orderDao.observeOrderById(orderId),
            orderDao.getOrderItems(orderId)
        ) { orderEntity, itemEntities ->
            orderEntity?.toDomain(items = itemEntities.map { it.toDomain() })
        }
    }

    override suspend fun createOrder(order: Order, items: List<OrderItem>): String {
        val orderId = order.id.ifBlank { UUID.randomUUID().toString() }
        val finalizedOrder = order.copy(id = orderId)
        orderDao.insertOrder(finalizedOrder.toEntity())
        orderDao.insertOrderItems(items.map { it.toEntity(orderId) })
        return orderId
    }
}
