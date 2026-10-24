package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.MarketplaceDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.MarketplaceListing
import com.rekora.app.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepositoryImpl @Inject constructor(
    private val marketplaceDao: MarketplaceDao
) : MarketplaceRepository {

    override fun getActiveListings(): Flow<List<MarketplaceListing>> {
        return marketplaceDao.getActiveListings().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getListingsForUser(userId: String): Flow<List<MarketplaceListing>> {
        return marketplaceDao.getListingsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getListingById(id: String): Flow<MarketplaceListing?> {
        return marketplaceDao.observeListingById(id).map { it?.toDomain() }
    }

    override suspend fun createListing(listing: MarketplaceListing): String {
        val listingId = listing.id.ifBlank { UUID.randomUUID().toString() }
        val finalizedListing = listing.copy(id = listingId)
        marketplaceDao.insertListing(finalizedListing.toEntity())
        return listingId
    }

    override fun searchListings(query: String): Flow<List<MarketplaceListing>> {
        return marketplaceDao.searchListings(query.trim()).map { list ->
            list.map { it.toDomain() }
        }
    }
}
