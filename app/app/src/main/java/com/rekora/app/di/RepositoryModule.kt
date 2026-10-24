package com.rekora.app.di

import com.rekora.app.data.repository.ProjectRepositoryImpl
import com.rekora.app.data.repository.ComponentRepositoryImpl
import com.rekora.app.data.repository.CartRepositoryImpl
import com.rekora.app.data.repository.OrderRepositoryImpl
import com.rekora.app.data.repository.RentalRepositoryImpl
import com.rekora.app.data.repository.FabricationRepositoryImpl
import com.rekora.app.data.repository.MarketplaceRepositoryImpl
import com.rekora.app.data.repository.AiRepositoryImpl
import com.rekora.app.data.repository.InventoryRepositoryImpl
import com.rekora.app.data.repository.AuthRepositoryImpl
import com.rekora.app.data.repository.NotificationRepositoryImpl
import com.rekora.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        impl: ProjectRepositoryImpl
    ): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindComponentRepository(
        impl: ComponentRepositoryImpl
    ): ComponentRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindRentalRepository(
        impl: RentalRepositoryImpl
    ): RentalRepository

    @Binds
    @Singleton
    abstract fun bindFabricationRepository(
        impl: FabricationRepositoryImpl
    ): FabricationRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        impl: MarketplaceRepositoryImpl
    ): MarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        impl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
