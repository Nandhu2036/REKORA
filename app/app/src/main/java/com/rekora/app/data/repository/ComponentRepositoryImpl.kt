package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.domain.model.Component
import com.rekora.app.domain.model.ComponentCategory
import com.rekora.app.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponentRepositoryImpl @Inject constructor(
    private val componentDao: ComponentDao
) : ComponentRepository {

    override fun getAllComponents(): Flow<List<Component>> {
        return componentDao.getAllComponents().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getComponentById(id: String): Flow<Component?> {
        return componentDao.observeComponent(id).map { it?.toDomain() }
    }

    override fun getComponentsByCategory(category: ComponentCategory): Flow<List<Component>> {
        return componentDao.getComponentsByCategory(category.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchComponents(query: String): Flow<List<Component>> {
        return componentDao.searchComponents(query.trim()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRentableComponents(): Flow<List<Component>> {
        return componentDao.getRentableComponents().map { list ->
            list.map { it.toDomain() }
        }
    }
}
