package com.rekora.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekoraTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    cartCount: Int = 0,
    onCartClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = {
                actions()
                if (onNotificationClick != null) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onNotificationClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (onCartClick != null) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = RekoraColors.SignatureTeal,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = cartCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onCartClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Editorial Home Header: Brand Wordmark + Search + Notification + Avatar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekoraHomeTopBar(
    userName: String,
    avatarUrl: String?,
    cartCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        TopAppBar(
            title = {
                RekoraWordmarkHeader(
                    symbolSize = 24.dp,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    showSubtext = false
                )
            },
            actions = {
                // Search Action
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onSearchClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Notification Action with Badge
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onNotificationClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cart Action
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = RekoraColors.SignatureTeal,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.bouncyClickable(targetScale = 0.92f) { onCartClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Profile Avatar with Subtle Ring
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .bouncyClickable(targetScale = 0.92f) { onAvatarClick() }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "R",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}
