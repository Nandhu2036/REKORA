package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.FabricationJobDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.FabricationJob
import com.rekora.app.domain.repository.FabricationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FabricationRepositoryImpl @Inject constructor(
    private val fabricationJobDao: FabricationJobDao
) : FabricationRepository {

    override fun getJobsForUser(userId: String): Flow<List<FabricationJob>> {
        return fabricationJobDao.getJobsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getJobById(jobId: String): Flow<FabricationJob?> {
        return fabricationJobDao.observeJobById(jobId).map { it?.toDomain() }
    }

    override suspend fun submitJob(job: FabricationJob): String {
        val jobId = job.id.ifBlank { UUID.randomUUID().toString() }
        val finalizedJob = job.copy(id = jobId)
        fabricationJobDao.insertJob(finalizedJob.toEntity())
        return jobId
    }
}
