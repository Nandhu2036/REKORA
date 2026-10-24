package com.rekora.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object RekoraMotion {
    // Apple-grade durations
    const val DURATION_FAST = 160
    const val DURATION_NORMAL = 280
    const val DURATION_SLOW = 420
    const val DURATION_EMPHASIS = 480
    
    // Apple fluid cubic beziers
    val EaseOut = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f) // Apple spring-like decelerate
    val EaseIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    
    // Spring specs
    val AppleSpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val AppleSpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Screen transitions
    val screenEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    ) + slideInHorizontally(
        initialOffsetX = { it / 6 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )
    
    val screenExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / 6 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseIn)
    )
    
    val screenPopEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    ) + slideInHorizontally(
        initialOffsetX = { -it / 6 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )
    
    val screenPopExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    ) + slideOutHorizontally(
        targetOffsetX = { it / 6 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseIn)
    )
    
    val bottomSheetEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    ) + slideInVertically(
        initialOffsetY = { it / 3 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )
    
    val bottomSheetExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    ) + slideOutVertically(
        targetOffsetY = { it / 3 },
        animationSpec = tween(DURATION_NORMAL, easing = EaseIn)
    )
    
    val scaleEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )
    
    val scaleExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    ) + scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    )
    
    val fadeEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )
    val fadeExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = EaseIn)
    )
}

/**
 * Tactile Apple-grade click modifier with spring physics compression and haptic feedback
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    targetScale: Float = 0.96f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Custom scale provides tactile feedback
            enabled = enabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        )
}

/**
 * Precision hairline border for Apple-level card finishes
 */
fun Modifier.hairlineBorder(
    width: Dp = 0.75.dp,
    color: Color = RekoraColors.Hairline,
    shape: Shape = RekoraCorners.Card
): Modifier = this.border(width = width, color = color, shape = shape)
