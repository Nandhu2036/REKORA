package com.rekora.app.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.data.local.entity.NotificationEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Notifications",
                onBackClick = onNavigateBack,
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllAsRead) {
                            Text(
                                text = "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RekoraDimens.ScreenPadding, vertical = RekoraDimens.SpacingSm)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                viewModel.filterTabs.forEach { (tabKey, tabLabel) ->
                    val isSelected = uiState.selectedFilter == tabKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(tabKey) },
                        label = {
                            Text(
                                text = tabLabel,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = RekoraCorners.Chip,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Unread count indicator bar
            if (uiState.unreadCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding, vertical = 4.dp),
                    color = RekoraColors.TealLight,
                    shape = RekoraCorners.CardSmall
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RekoraColors.EngineeringTeal)
                        )
                        Text(
                            text = "${uiState.unreadCount} unread campus notifications",
                            style = MaterialTheme.typography.labelSmall,
                            color = RekoraColors.DeepBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Notification List / Loading / Empty
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(RekoraDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        repeat(4) {
                            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 80.dp, cornerRadius = 12.dp)
                        }
                    }
                }
                uiState.filteredNotifications.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "No notifications",
                        message = "You're all caught up! Rental reminders, order pick-up alerts, and 3D print updates will appear here."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = RekoraDimens.ScreenPadding,
                            vertical = RekoraDimens.SpacingSm
                        ),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                    ) {
                        items(
                            items = uiState.filteredNotifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItemCard(
                                notification = notification,
                                onClick = { viewModel.markAsRead(notification.id) },
                                onDismiss = { viewModel.deleteNotification(notification.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(RekoraDimens.SpacingGiant))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, iconColor, bgColor) = when {
        notification.type.startsWith("RENTAL_DUE") -> Triple(
            Icons.Outlined.Schedule,
            RekoraColors.Warning,
            RekoraColors.WarningLight
        )
        notification.type.startsWith("RENTAL") -> Triple(
            Icons.Outlined.Autorenew,
            RekoraColors.EngineeringTeal,
            RekoraColors.TealLight
        )
        notification.type.startsWith("ORDER") -> Triple(
            Icons.Outlined.CheckCircle,
            RekoraColors.Success,
            RekoraColors.SuccessLight
        )
        notification.type.startsWith("FABRICATION") -> Triple(
            Icons.Outlined.PrecisionManufacturing,
            RekoraColors.EngineeringTeal,
            RekoraColors.TealLight
        )
        else -> Triple(
            Icons.Outlined.Campaign,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondaryContainer
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RekoraCorners.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead) 2.dp else 0.dp
        ),
        border = if (!notification.isRead) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.EngineeringTeal.copy(alpha = 0.5f))
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPaddingSmall),
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RekoraColors.EngineeringTeal)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Timestamp
                val timeLabel = formatRelativeTime(notification.createdAt)
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Dismiss Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "${diffMinutes}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays == 1L -> "Yesterday"
        else -> "${diffDays}d ago"
    }
}
