package com.rekora.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.screens.auth.AuthViewModel
import com.rekora.app.ui.theme.RekoraColors
import com.rekora.app.ui.theme.RekoraDimens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }

    // Animated scale and fade
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.65f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "logo_fade"
    )

    val taglineAlphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400, easing = LinearOutSlowInEasing),
        label = "tagline_fade"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        // 2-second splash screen duration
        delay(2000)
        if (isLoggedIn || isOnboardingCompleted) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RekoraColors.MidnightNavy,
                        Color(0xFF07121D),
                        Color(0xFF030910)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle engineering grid & circuit glow in background
        EngineeringBackgroundGrid(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RekoraDimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated REKORA Emblem
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating/pulsing glow circle
                RekoraEmblem(
                    modifier = Modifier.size(110.dp),
                    glowColor = RekoraColors.EngineeringTeal
                )
            }

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingXxl))

            // Brand Title
            Text(
                text = "REKORA",
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    fontSize = 34.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingSm))

            // Brand Statement
            Text(
                text = "Engineering Projects. Connected.",
                modifier = Modifier.alpha(taglineAlphaAnim),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                ),
                color = RekoraColors.EngineeringTeal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingMd))

            // Tagline
            Text(
                text = "From Idea to Prototype. From Prototype to Evolution.",
                modifier = Modifier
                    .padding(horizontal = RekoraDimens.SpacingLg)
                    .alpha(taglineAlphaAnim),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                ),
                color = RekoraColors.DarkSecondaryText,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Institutional / Demo watermark
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = RekoraDimens.SpacingMassive)
                .alpha(taglineAlphaAnim)
        ) {
            Surface(
                shape = CircleShape,
                color = RekoraColors.DarkSurface.copy(alpha = 0.7f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = RekoraDimens.SpacingLg,
                        vertical = RekoraDimens.SpacingSm
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RekoraColors.EngineeringTeal, CircleShape)
                    )
                    Text(
                        text = "St. Joseph's College Pilot • v1.0",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = RekoraColors.DarkSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun RekoraEmblem(
    modifier: Modifier = Modifier,
    glowColor: Color = RekoraColors.EngineeringTeal
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 6.dp.toPx()

        // Background subtle circle glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.3f
            ),
            radius = radius * 1.3f,
            center = center
        )

        // Outer hexagon border
        val hexPath = Path()
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val x = center.x + radius * Math.cos(angle).toFloat()
            val y = center.y + radius * Math.sin(angle).toFloat()
            if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
        }
        hexPath.close()

        drawPath(
            path = hexPath,
            color = glowColor.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Inner connected circuit nodes
        val innerRadius = radius * 0.58f
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val x = center.x + innerRadius * Math.cos(angle).toFloat()
            val y = center.y + innerRadius * Math.sin(angle).toFloat()
            drawLine(
                color = glowColor.copy(alpha = 0.4f),
                start = center,
                end = Offset(x, y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = if (i % 2 == 0) glowColor else Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Center core node
        drawCircle(
            color = glowColor,
            radius = 8.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

@Composable
private fun EngineeringBackgroundGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = 44.dp.toPx()
        val gridColor = Color(0xFF162A3D).copy(alpha = 0.35f)

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.75f
            )
            x += step
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.75f
            )
            y += step
        }
    }
}
