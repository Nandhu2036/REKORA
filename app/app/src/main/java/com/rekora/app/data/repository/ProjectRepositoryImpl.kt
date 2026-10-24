package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.BomItemDao
import com.rekora.app.data.local.dao.ProjectDao
import com.rekora.app.data.local.dao.ProjectVersionDao
import com.rekora.app.data.local.dao.SavedProjectDao
import com.rekora.app.data.local.entity.SavedProjectEntity
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.domain.model.BomItem
import com.rekora.app.domain.model.EngineeringDomain
import com.rekora.app.domain.model.Project
import com.rekora.app.domain.model.ProjectVersion
import com.rekora.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val bomItemDao: BomItemDao,
    private val projectVersionDao: ProjectVersionDao,
    private val savedProjectDao: SavedProjectDao
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getProjectById(id: String): Flow<Project?> {
        return projectDao.observeProject(id).map { it?.toDomain() }
    }

    override fun getProjectsByDomain(domain: EngineeringDomain): Flow<List<Project>> {
        return projectDao.getProjectsByDomain(domain.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchProjects(query: String): Flow<List<Project>> {
        return projectDao.searchProjects(query.trim()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getProjectsWithinBudget(maxBudget: Double): Flow<List<Project>> {
        return projectDao.getProjectsWithinBudget(maxBudget).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getBomForProject(projectId: String): Flow<List<BomItem>> {
        return bomItemDao.getBomForProject(projectId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getVersionsForProject(projectId: String): Flow<List<ProjectVersion>> {
        return projectVersionDao.getVersionsForProject(projectId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun isProjectSaved(userId: String, projectId: String): Flow<Boolean> {
        return savedProjectDao.isProjectSaved(userId, projectId)
    }

    override suspend fun toggleSaveProject(userId: String, projectId: String) {
        val isSaved = savedProjectDao.isProjectSaved(userId, projectId).first()
        val project = projectDao.getProjectById(projectId)

        if (isSaved) {
            savedProjectDao.unsaveProject(userId, projectId)
            if (project != null && project.saveCount > 0) {
                projectDao.updateProject(project.copy(saveCount = project.saveCount - 1))
            }
        } else {
            savedProjectDao.saveProject(
                SavedProjectEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    projectId = projectId,
                    savedAt = System.currentTimeMillis()
                )
            )
            if (project != null) {
                projectDao.updateProject(project.copy(saveCount = project.saveCount + 1))
            }
        }
    }

    override fun getSavedProjects(userId: String): Flow<List<Project>> {
        return savedProjectDao.getSavedProjectsFull(userId).map { list ->
            list.map { it.toDomain(isSaved = true) }
        }
    }

    override suspend fun unlockRepository(userId: String, projectId: String) {
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(repositoryLocked = false))
        }
    }
}
