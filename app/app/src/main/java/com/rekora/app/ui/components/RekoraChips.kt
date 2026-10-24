package com.rekora.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rekora.app.ui.theme.*

@Composable
fun RekoraChip(
    text: String,
    icon: ImageVector? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RekoraCorners.Chip,
        color = containerColor,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RekoraDimens.SpacingMd,
                vertical = RekoraDimens.SpacingXs
            ),
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(RekoraDimens.IconSizeXs),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekoraFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        shape = RekoraCorners.Chip,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun StatusBadge(
    status: String,
    type: StatusType = StatusType.DEFAULT
) {
    val (bgColor, textColor) = when (type) {
        StatusType.SUCCESS -> RekoraColors.SuccessLight to RekoraColors.Success
        StatusType.WARNING -> RekoraColors.WarningLight to RekoraColors.Warning
        StatusType.ERROR -> RekoraColors.ErrorLight to RekoraColors.Error
        StatusType.INFO -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatusType.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RekoraCorners.Tag,
        color = bgColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(
                horizontal = RekoraDimens.SpacingSm,
                vertical = RekoraDimens.SpacingXxs
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

enum class StatusType {
    DEFAULT, SUCCESS, WARNING, ERROR, INFO
}
