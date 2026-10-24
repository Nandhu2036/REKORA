package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.NotificationRepository
import com.rekora.app.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for user alerts, order statuses, and campus maker lab updates.
 */
data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * NotificationsViewModel manages institutional alerts and rental/fabrication state updates:
 * - Unread message count badges
 * - Single item and bulk mark-as-read transitions
 * - Real-time notification streams
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotificationsUiState> = authRepository.currentUser
        .flatMapLatest { user ->
            val userId = user?.id ?: "guest-student"
            combine(
                notificationRepository.getNotificationsForUser(userId),
                notificationRepository.getUnreadCount(userId),
                _isLoading,
                _error
            ) { notifications, unread, loading, err ->
                NotificationsUiState(
                    notifications = notifications,
                    unreadCount = unread,
                    isLoading = loading,
                    error = err
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationsUiState(isLoading = true)
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                _error.value = "Failed to update notification: ${e.localizedMessage}"
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"
                notificationRepository.markAllAsRead(userId)
            } catch (e: Exception) {
                _error.value = "Failed to mark all as read: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
