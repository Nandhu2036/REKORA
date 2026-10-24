package com.rekora.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekora.app.ui.theme.*

/**
 * High-precision Apple & Google engineering schematic canvas.
 * Draws a subtle blueprint Cartesian grid, IC footprint traces, and circuit nodes.
 */
@Composable
fun BlueprintSchematicCanvas(
    modifier: Modifier = Modifier,
    domain: String = "Robotics",
    accentColor: Color = RekoraColors.EngineeringTeal
) {
    val infiniteTransition = rememberInfiniteTransition(label = "schematic_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Technical background grid
        val step = 28.dp.toPx()
        var x = 0f
        while (x < width) {
            drawLine(
                color = RekoraColors.BlueprintGrid,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < height) {
            drawLine(
                color = RekoraColors.BlueprintGrid,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }

        // 2. Microchip / Sensor Footprint Schematic in Center
        val centerX = width * 0.78f
        val centerY = height * 0.5f
        val icRadius = 38.dp.toPx()

        // IC core square
        drawRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(centerX - icRadius, centerY - icRadius),
            size = androidx.compose.ui.geometry.Size(icRadius * 2, icRadius * 2)
        )
        drawRect(
            color = accentColor.copy(alpha = 0.45f),
            topLeft = Offset(centerX - icRadius, centerY - icRadius),
            size = androidx.compose.ui.geometry.Size(icRadius * 2, icRadius * 2),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // IC Pins
        val pinCount = 4
        val pinSpacing = (icRadius * 1.5f) / pinCount
        for (i in 0 until pinCount) {
            val pinY = (centerY - icRadius * 0.75f) + i * pinSpacing
            // Left pin traces
            drawLine(
                color = accentColor.copy(alpha = 0.5f),
                start = Offset(centerX - icRadius, pinY),
                end = Offset(centerX - icRadius - 20.dp.toPx(), pinY),
                strokeWidth = 1.5f
            )
            // Pin solder via
            drawCircle(
                color = RekoraColors.CircuitVia.copy(alpha = pulseAlpha),
                radius = 2.5.dp.toPx(),
                center = Offset(centerX - icRadius - 20.dp.toPx(), pinY)
            )

            // Right pin traces
            drawLine(
                color = accentColor.copy(alpha = 0.5f),
                start = Offset(centerX + icRadius, pinY),
                end = Offset(centerX + icRadius + 20.dp.toPx(), pinY),
                strokeWidth = 1.5f
            )
            drawCircle(
                color = RekoraColors.CircuitVia.copy(alpha = pulseAlpha),
                radius = 2.5.dp.toPx(),
                center = Offset(centerX + icRadius + 20.dp.toPx(), pinY)
            )
        }

        // 3. Diagonal Circuit Bus Line
        val path = Path().apply {
            moveTo(24.dp.toPx(), height * 0.85f)
            lineTo(width * 0.45f, height * 0.85f)
            lineTo(width * 0.6f, height * 0.65f)
            lineTo(centerX - icRadius - 20.dp.toPx(), centerY)
        }
        drawPath(
            path = path,
            color = RekoraColors.CircuitTrace.copy(alpha = pulseAlpha),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Bus via points
        drawCircle(
            color = RekoraColors.ElectricCyan,
            radius = 3.5.dp.toPx(),
            center = Offset(24.dp.toPx(), height * 0.85f)
        )
        drawCircle(
            color = RekoraColors.ElectricCyan,
            radius = 3.5.dp.toPx(),
            center = Offset(width * 0.45f, height * 0.85f)
        )

        // Coordinate crosshairs in top-left
        val crossX = 24.dp.toPx()
        val crossY = 24.dp.toPx()
        val crossLen = 6.dp.toPx()
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(crossX - crossLen, crossY),
            end = Offset(crossX + crossLen, crossY),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(crossX, crossY - crossLen),
            end = Offset(crossX, crossY + crossLen),
            strokeWidth = 1f
        )
    }
}

/**
 * Apple-grade Luxury Status Capsule with pulsating state dot
 */
@Composable
fun LuxuryStatusCapsule(
    label: String,
    dotColor: Color = RekoraColors.Success,
    containerColor: Color = RekoraColors.SuccessLight,
    borderColor: Color = RekoraColors.SuccessBorder,
    textColor: Color = dotColor,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RekoraCorners.Pill,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(0.75.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.2.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * Editorial Eyebrow Tag: High-contrast Apple-style micro-label
 */
@Composable
fun EditorialEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = RekoraColors.EngineeringTeal
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            letterSpacing = 1.3.sp,
            fontFamily = FontFamily.Default
        ),
        color = color
    )
}

/**
 * Apple UISegmentedControl style animated pill toggle
 */
@Composable
fun AppleSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RekoraCorners.SegmentedControl,
        color = Color(0xFFE5E7EB).copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x14000000))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bouncyClickable(targetScale = 0.98f) { onItemSelected(index) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) Color.White else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) RekoraColors.MidnightNavy else RekoraColors.SecondaryText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Cupertino style horizontally scrollable pills with tactile spring compression
 */
@Composable
fun AppleScrollablePills(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .bouncyClickable(targetScale = 0.96f) { onItemSelected(index) },
                shape = RekoraCorners.Pill,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    0.75.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 0.1.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
