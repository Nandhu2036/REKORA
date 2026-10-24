package com.rekora.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekora.app.ui.theme.RekoraColors

/**
 * REKORA Vector Geometric Brand Emblem
 * Inspired by modular engineering nodes, connected systems, and iterative prototype evolution.
 */
@Composable
fun RekoraBrandSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    primaryColor: Color = RekoraColors.MidnightNavy,
    accentColor: Color = RekoraColors.SignatureTeal
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val strokeWidth = w * 0.09f

        // Outer interconnected delta / engineering hexagon
        val p1 = Offset(w * 0.5f, h * 0.08f)    // Top apex
        val p2 = Offset(w * 0.90f, h * 0.32f)   // Upper right
        val p3 = Offset(w * 0.90f, h * 0.76f)   // Lower right
        val p4 = Offset(w * 0.5f, h * 0.96f)    // Bottom vertex
        val p5 = Offset(w * 0.10f, h * 0.76f)   // Lower left
        val p6 = Offset(w * 0.10f, h * 0.32f)   // Upper left
        val center = Offset(w * 0.5f, h * 0.54f) // Central hub

        // Draw structural engineering frame
        val outerPath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            lineTo(p5.x, p5.y)
            lineTo(p6.x, p6.y)
            close()
        }
        drawPath(
            path = outerPath,
            color = primaryColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw internal node connecting trusses (Evolution / Connected Systems)
        drawLine(
            color = primaryColor.copy(alpha = 0.5f),
            start = p1,
            end = center,
            strokeWidth = strokeWidth * 0.75f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.5f),
            start = p3,
            end = center,
            strokeWidth = strokeWidth * 0.75f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.5f),
            start = p5,
            end = center,
            strokeWidth = strokeWidth * 0.75f,
            cap = StrokeCap.Round
        )

        // Core active engineering node (Teal accent)
        drawCircle(
            color = accentColor,
            radius = w * 0.13f,
            center = center
        )

        // Micro-node via points at vertices
        listOf(p1, p3, p5).forEach { node ->
            drawCircle(
                color = primaryColor,
                radius = w * 0.055f,
                center = node
            )
        }
    }
}

/**
 * REKORA Brand Header: Vector Emblem + Precision Tracked Wordmark
 */
@Composable
fun RekoraWordmarkHeader(
    modifier: Modifier = Modifier,
    symbolSize: Dp = 26.dp,
    textColor: Color = RekoraColors.MidnightNavy,
    showSubtext: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RekoraBrandSymbol(
            size = symbolSize,
            primaryColor = textColor,
            accentColor = RekoraColors.SignatureTeal
        )
        Column {
            Text(
                text = "REKORA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = 2.4.sp,
                    fontFamily = FontFamily.Default
                ),
                color = textColor
            )
            if (showSubtext) {
                Text(
                    text = "ENGINEERING PROJECTS. CONNECTED.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.sp,
                        letterSpacing = 1.1.sp
                    ),
                    color = RekoraColors.SecondaryText
                )
            }
        }
    }
}
