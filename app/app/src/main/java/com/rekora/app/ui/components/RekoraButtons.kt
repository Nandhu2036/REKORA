package com.rekora.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.rekora.app.ui.theme.*

@Composable
fun RekoraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(RekoraDimens.ButtonHeight),
        enabled = enabled && !isLoading,
        shape = RekoraCorners.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(RekoraDimens.IconSize),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = RekoraDimens.BorderWidthThick
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(RekoraDimens.IconSizeSmall)
                )
                Spacer(modifier = Modifier.width(RekoraDimens.SpacingSm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RekoraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(RekoraDimens.ButtonHeight),
        enabled = enabled,
        shape = RekoraCorners.Button,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(RekoraDimens.IconSizeSmall)
            )
            Spacer(modifier = Modifier.width(RekoraDimens.SpacingSm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RekoraTealButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(RekoraDimens.ButtonHeight),
        enabled = enabled && !isLoading,
        shape = RekoraCorners.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = RekoraColors.EngineeringTeal,
            contentColor = RekoraColors.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(RekoraDimens.IconSize),
                color = RekoraColors.White,
                strokeWidth = RekoraDimens.BorderWidthThick
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(RekoraDimens.IconSizeSmall)
                )
                Spacer(modifier = Modifier.width(RekoraDimens.SpacingSm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
