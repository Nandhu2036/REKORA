package com.rekora.app.di

import android.content.Context
import androidx.room.Room
import com.rekora.app.data.local.RekoraDatabase
import com.rekora.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RekoraDatabase {
        return Room.databaseBuilder(
            context,
            RekoraDatabase::class.java,
            "rekora_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProjectDao(db: RekoraDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideComponentDao(db: RekoraDatabase): ComponentDao = db.componentDao()

    @Provides
    fun provideBomItemDao(db: RekoraDatabase): BomItemDao = db.bomItemDao()

    @Provides
    fun provideCartDao(db: RekoraDatabase): CartDao = db.cartDao()

    @Provides
    fun provideOrderDao(db: RekoraDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideRentalDao(db: RekoraDatabase): RentalDao = db.rentalDao()

    @Provides
    fun provideFabricationJobDao(db: RekoraDatabase): FabricationJobDao = db.fabricationJobDao()

    @Provides
    fun provideMarketplaceDao(db: RekoraDatabase): MarketplaceDao = db.marketplaceDao()

    @Provides
    fun provideAiConversationDao(db: RekoraDatabase): AiConversationDao = db.aiConversationDao()

    @Provides
    fun provideNotificationDao(db: RekoraDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideSavedProjectDao(db: RekoraDatabase): SavedProjectDao = db.savedProjectDao()

    @Provides
    fun provideInventoryItemDao(db: RekoraDatabase): InventoryItemDao = db.inventoryItemDao()

    @Provides
    fun provideProjectVersionDao(db: RekoraDatabase): ProjectVersionDao = db.projectVersionDao()

    @Provides
    fun provideUserDao(db: RekoraDatabase): UserDao = db.userDao()
}
