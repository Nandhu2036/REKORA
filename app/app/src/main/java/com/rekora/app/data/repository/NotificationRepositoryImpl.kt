package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.NotificationDao
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.Notification
import com.rekora.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    override suspend fun insertNotification(notification: Notification) {
        notificationDao.insertNotification(notification.toEntity())
    }
}
