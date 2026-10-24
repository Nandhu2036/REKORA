package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.InventoryItemDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.domain.model.InventoryItem
import com.rekora.app.domain.model.InventoryStatus
import com.rekora.app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryItemDao: InventoryItemDao
) : InventoryRepository {

    override fun getAllItems(): Flow<List<InventoryItem>> {
        return inventoryItemDao.getAllItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getItemByCode(code: String): Flow<InventoryItem?> {
        return inventoryItemDao.observeItemByCode(code).map { it?.toDomain() }
    }

    override suspend fun updateStatus(
        itemCode: String,
        status: InventoryStatus,
        notes: String?
    ) {
        val item = inventoryItemDao.getItemByCode(itemCode)
        if (item != null) {
            val updated = item.copy(
                currentStatus = status.name,
                maintenanceNotes = notes ?: item.maintenanceNotes,
                lastUpdated = System.currentTimeMillis()
            )
            inventoryItemDao.updateItem(updated)
        }
    }
}
