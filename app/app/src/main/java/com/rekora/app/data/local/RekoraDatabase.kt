package com.rekora.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rekora.app.data.local.dao.*
import com.rekora.app.data.local.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        ProjectVersionEntity::class,
        ComponentEntity::class,
        BomItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        RentalEntity::class,
        FabricationJobEntity::class,
        MarketplaceListingEntity::class,
        AiConversationEntity::class,
        AiMessageEntity::class,
        NotificationEntity::class,
        SavedProjectEntity::class,
        InventoryItemEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RekoraDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun componentDao(): ComponentDao
    abstract fun bomItemDao(): BomItemDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun rentalDao(): RentalDao
    abstract fun fabricationJobDao(): FabricationJobDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun savedProjectDao(): SavedProjectDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun projectVersionDao(): ProjectVersionDao
    abstract fun userDao(): UserDao
}
