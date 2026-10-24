package com.rekora.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.dao.NotificationDao
import com.rekora.app.data.local.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val filteredNotifications: List<NotificationEntity> = emptyList(),
    val selectedFilter: String = "ALL", // "ALL", "RENTAL", "ORDER", "FABRICATION", "ANNOUNCEMENT"
    val unreadCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val currentUserId = "usr-student-pilot"

    val filterTabs = listOf(
        "ALL" to "All",
        "RENTAL" to "Rentals",
        "ORDER" to "Orders",
        "FABRICATION" to "3D Prints",
        "ANNOUNCEMENT" to "Announcements"
    )

    init {
        ensureSeededNotifications()
        observeNotifications()
    }

    private fun ensureSeededNotifications() {
        viewModelScope.launch {
            try {
                val existing = notificationDao.getNotificationsForUser(currentUserId).first()
                if (existing.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val sampleList = listOf(
                        NotificationEntity(
                            id = "notif-rental-due-1",
                            userId = currentUserId,
                            title = "Rental Return Due in 2 Days",
                            message = "RK-ESP32-001 rental return is due in 2 days. Return to Engineering Block B, Room 204 to ensure 100% deposit refund (₹200).",
                            type = "RENTAL_DUE",
                            actionRoute = "rentals",
                            isRead = false,
                            createdAt = now - 35 * 60 * 1000L // 35 mins ago
                        ),
                        NotificationEntity(
                            id = "notif-order-pickup-1",
                            userId = currentUserId,
                            title = "Component Pickup Ready at Lab Desk",
                            message = "Your hardware pickup (Order #ORD-SJC-48912) is prepared at Engineering Block B Room 204. Collect anytime 9:00 AM – 5:00 PM.",
                            type = "ORDER_UPDATE",
                            actionRoute = "orders",
                            isRead = false,
                            createdAt = now - 3 * 3600 * 1000L // 3 hours ago
                        ),
                        NotificationEntity(
                            id = "notif-fab-complete-1",
                            userId = currentUserId,
                            title = "3D Print Completed Quality Inspection",
                            message = "Your waterproof chassis print has passed dimensional tolerance inspection (±0.15mm). Ready for pickup at 3D Print Lab Desk 3.",
                            type = "FABRICATION_UPDATE",
                            actionRoute = "fabrication/history",
                            isRead = false,
                            createdAt = now - 8 * 3600 * 1000L // 8 hours ago
                        ),
                        NotificationEntity(
                            id = "notif-announcement-1",
                            userId = currentUserId,
                            title = "Institutional Hardware Depot Restocked",
                            message = "New batch of Raspberry Pi 4B (4GB) and BME280 precision environmental sensors received at Central Engineering Block depot.",
                            type = "ANNOUNCEMENT",
                            actionRoute = "components",
                            isRead = true,
                            createdAt = now - 24 * 3600 * 1000L // 1 day ago
                        ),
                        NotificationEntity(
                            id = "notif-rental-approved-1",
                            userId = currentUserId,
                            title = "Rental Request Approved",
                            message = "Your rental request for STM32 Blue Pill Board (RK-STM-042) was approved by the inventory operator. Security deposit recorded.",
                            type = "RENTAL_APPROVAL",
                            actionRoute = "rentals",
                            isRead = true,
                            createdAt = now - 48 * 3600 * 1000L // 2 days ago
                        )
                    )

                    sampleList.forEach { notificationDao.insertNotification(it) }
                }
            } catch (e: Exception) {
                // Ignore seed error
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationDao.getNotificationsForUser(currentUserId)
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { list ->
                    val unread = list.count { !it.isRead }
                    _uiState.update {
                        it.copy(
                            notifications = list,
                            unreadCount = unread,
                            isLoading = false
                        )
                    }
                    applyFilter()
                }
        }
    }

    fun setFilter(filterKey: String) {
        _uiState.update { it.copy(selectedFilter = filterKey) }
        applyFilter()
    }

    private fun applyFilter() {
        val state = _uiState.value
        val filter = state.selectedFilter

        val filtered = if (filter == "ALL") {
            state.notifications
        } else {
            state.notifications.filter { it.type.startsWith(filter) }
        }

        _uiState.update { it.copy(filteredNotifications = filtered) }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationDao.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead(currentUserId)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            // Mark as read or remove from current list
            notificationDao.markAsRead(id)
            _uiState.update { state ->
                val remaining = state.notifications.filter { it.id != id }
                state.copy(notifications = remaining)
            }
            applyFilter()
        }
    }
}
