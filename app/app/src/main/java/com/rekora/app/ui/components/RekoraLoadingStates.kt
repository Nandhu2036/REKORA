package com.rekora.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rekora.app.ui.theme.*

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 16.dp,
    cornerRadius: Dp = 4.dp
) {
    val shimmerColors = listOf(
        RekoraColors.Shimmer,
        RekoraColors.ShimmerHighlight,
        RekoraColors.Shimmer
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )
    
    val sizeModifier = modifier
        .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
        .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)

    Box(
        modifier = sizeModifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun ProjectCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(),
                width = Dp.Unspecified,
                height = RekoraDimens.CardImageHeight
            )
            Column(
                modifier = Modifier.padding(RekoraDimens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                ShimmerBox(width = 80.dp, height = 24.dp, cornerRadius = 12.dp)
                ShimmerBox(width = 200.dp, height = 20.dp)
                ShimmerBox(modifier = Modifier.fillMaxWidth(), width = Dp.Unspecified, height = 14.dp)
                ShimmerBox(width = 160.dp, height = 14.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerBox(width = 60.dp, height = 14.dp)
                    ShimmerBox(width = 60.dp, height = 14.dp)
                    ShimmerBox(width = 80.dp, height = 18.dp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Outlined.SearchOff,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RekoraDimens.SpacingXxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(RekoraDimens.SpacingLg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(RekoraDimens.SpacingSm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(RekoraDimens.SpacingXl))
            RekoraSecondaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
fun ErrorStateView(
    message: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RekoraDimens.SpacingXxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(RekoraDimens.SpacingLg))
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(RekoraDimens.SpacingSm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(RekoraDimens.SpacingXl))
            RekoraSecondaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RekoraDimens.SpacingSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
