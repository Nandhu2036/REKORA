package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.RentalDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.Rental
import com.rekora.app.domain.model.RentalStatus
import com.rekora.app.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RentalRepositoryImpl @Inject constructor(
    private val rentalDao: RentalDao
) : RentalRepository {

    override fun getRentalsForUser(userId: String): Flow<List<Rental>> {
        return rentalDao.getRentalsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRentalById(rentalId: String): Flow<Rental?> {
        return rentalDao.observeRentalById(rentalId).map { it?.toDomain() }
    }

    override suspend fun createRental(rental: Rental): String {
        val rentalId = rental.id.ifBlank { UUID.randomUUID().toString() }
        val finalizedRental = rental.copy(id = rentalId)
        rentalDao.insertRental(finalizedRental.toEntity())
        return rentalId
    }

    override suspend fun updateRentalStatus(rentalId: String, status: RentalStatus) {
        val existing = rentalDao.getRentalById(rentalId)
        if (existing != null) {
            rentalDao.updateRental(existing.copy(status = status.name))
        }
    }
}
