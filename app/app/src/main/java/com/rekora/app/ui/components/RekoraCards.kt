package com.rekora.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekora.app.data.local.entity.ProjectEntity
import com.rekora.app.ui.theme.*

/**
 * World-class Apple-grade Project Card with dynamic blueprint schematic,
 * precision hairline border, and spring press feedback.
 */
@Composable
fun ProjectCard(
    title: String,
    description: String,
    domain: String,
    difficulty: String,
    estimatedCost: String,
    estimatedTime: String,
    technologies: List<String> = emptyList(),
    matchScore: Int? = null,
    isSaved: Boolean = false,
    isDemo: Boolean = true,
    onClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.97f, onClick = onClick)
            .shadow(
                elevation = 3.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x0F000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // 1. High-Tech Blueprint Visual Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RekoraColors.MidnightNavy,
                                RekoraColors.DeepBlue,
                                Color(0xFF0E283D)
                            )
                        )
                    )
            ) {
                // Interactive dynamic blueprint circuit grid
                BlueprintSchematicCanvas(
                    modifier = Modifier.fillMaxSize(),
                    domain = domain,
                    accentColor = RekoraColors.EngineeringTeal
                )

                // Top row: Save Bookmark & Match Score / Demo Tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RekoraDimens.SpacingSm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookmark pill
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .bouncyClickable(targetScale = 0.9f, onClick = onSaveClick),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave" else "Save",
                                tint = if (isSaved) RekoraColors.DarkAccent else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (matchScore != null) {
                            LuxuryStatusCapsule(
                                label = "${matchScore}% MATCH",
                                dotColor = RekoraColors.Success,
                                containerColor = Color.Black.copy(alpha = 0.45f),
                                borderColor = RekoraColors.Success.copy(alpha = 0.5f),
                                textColor = Color.White
                            )
                        }
                        if (isDemo) {
                            Surface(
                                shape = RekoraCorners.Tag,
                                color = Color.White.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "DEMO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Bottom row: Domain Eyebrow Capsule
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(RekoraDimens.SpacingSm),
                    shape = RekoraCorners.Pill,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.EngineeringTeal.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(RekoraColors.DarkAccent)
                        )
                        Text(
                            text = domain.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = RekoraColors.DarkAccent
                        )
                    }
                }
            }

            // 2. Editorial Typography & Content
            Column(
                modifier = Modifier.padding(RekoraDimens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Tech tags row
                if (technologies.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        technologies.take(3).forEach { tech ->
                            Surface(
                                shape = RekoraCorners.Tag,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = tech,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                // 3. Technical Metadata Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaInfo(
                            icon = Icons.Outlined.TrendingUp,
                            text = difficulty
                        )
                        MetaInfo(
                            icon = Icons.Outlined.Schedule,
                            text = estimatedTime
                        )
                    }

                    Text(
                        text = estimatedCost,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            letterSpacing = (-0.2).sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }
            }
        }
    }
}

/**
 * World-class Component Sourcing Card with stock capsule and footprint chip
 */
@Composable
fun ComponentCard(
    name: String,
    category: String,
    price: String,
    rentalPrice: String? = null,
    condition: String = "New",
    inStock: Boolean = true,
    isRentable: Boolean = false,
    isDemo: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0C000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hardware Component Thumbnail with Technical Badge
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RekoraCorners.Image)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                RekoraColors.MidnightNavy,
                                RekoraColors.DeepBlue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = RekoraColors.DarkAccent.copy(alpha = 0.85f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )

                    LuxuryStatusCapsule(
                        label = if (inStock) "IN LAB STOCK" else "ALLOCATED",
                        dotColor = if (inStock) RekoraColors.Success else RekoraColors.Warning,
                        containerColor = if (inStock) RekoraColors.SuccessLight else RekoraColors.WarningLight,
                        borderColor = if (inStock) RekoraColors.SuccessBorder else RekoraColors.WarningBorder
                    )
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isRentable && rentalPrice != null) {
                            Text(
                                text = "Rent $rentalPrice/wk",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = RekoraColors.EngineeringTeal
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = RekoraColors.TealLight
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Details",
                            modifier = Modifier
                                .padding(6.dp)
                                .size(14.dp),
                            tint = RekoraColors.EngineeringTeal
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple-grade Luxury Quick Action Card with squircle curvature and tactile spring
 */
@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color = RekoraColors.MidnightNavy,
    contentColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .width(104.dp)
            .height(96.dp)
            .bouncyClickable(targetScale = 0.94f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Squircle,
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x10000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Squircle),
        shape = RekoraCorners.Squircle,
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(RekoraDimens.SpacingMd),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = (-0.1).sp
                ),
                color = contentColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun MetaInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Editorial Lead Project Card (Apple Music inspired)
 * Large immersive engineering schematic visual, confident typography,
 * clear budget and difficulty metadata, and direct action.
 */
@Composable
fun EditorialLeadProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 4.dp,
                shape = RekoraCorners.CardLarge,
                ambientColor = Color(0x0C000000),
                spotColor = Color(0x14000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardLarge),
        shape = RekoraCorners.CardLarge,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Immersive Technical Visual Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                RekoraColors.MidnightNavy,
                                RekoraColors.DeepBlue,
                                Color(0xFF0D253A)
                            )
                        )
                    )
            ) {
                BlueprintSchematicCanvas(
                    modifier = Modifier.matchParentSize(),
                    domain = project.domain,
                    accentColor = RekoraColors.EngineeringTeal
                )

                // Top row tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RekoraCorners.Pill,
                        color = Color.Black.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.DarkAccent.copy(alpha = 0.5f))
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
                                    .background(RekoraColors.DarkAccent)
                            )
                            Text(
                                text = "FEATURED LEAD BLUEPRINT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = RekoraColors.DarkAccent
                            )
                        }
                    }

                    Surface(
                        shape = RekoraCorners.Tag,
                        color = Color.White.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = project.domain.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.6.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Confident Editorial Details
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = project.shortDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RekoraColors.SecondaryText,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = RekoraColors.Border.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaInfo(
                            icon = Icons.Outlined.TrendingUp,
                            text = project.difficulty
                        )
                        MetaInfo(
                            icon = Icons.Outlined.Schedule,
                            text = project.estimatedTimeline
                        )
                    }

                    Text(
                        text = "₹${project.estimatedCostMin.toInt()} - ₹${project.estimatedCostMax.toInt()}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }
            }
        }
    }
}

/**
 * Compact Project Row (Google Play Store inspired)
 * Efficient browsing, clear metadata hierarchy, fast interaction.
 */
@Composable
fun CompactProjectRow(
    project: ProjectEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 1.dp,
                shape = RekoraCorners.CardSmall,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardSmall),
        shape = RekoraCorners.CardSmall,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Thumbnail Squircle
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RekoraCorners.Squircle)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                RekoraColors.MidnightNavy,
                                RekoraColors.DeepBlue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        project.domain.contains("ROBOT", ignoreCase = true) -> Icons.Outlined.PrecisionManufacturing
                        project.domain.contains("IOT", ignoreCase = true) -> Icons.Outlined.Sensors
                        project.domain.contains("ENERGY", ignoreCase = true) -> Icons.Outlined.SolarPower
                        project.domain.contains("AI", ignoreCase = true) -> Icons.Outlined.Psychology
                        project.domain.contains("MECH", ignoreCase = true) -> Icons.Outlined.SettingsSuggest
                        else -> Icons.Outlined.DeveloperBoard
                    },
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = RekoraColors.DarkAccent
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = project.domain.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = RekoraColors.EngineeringTeal
                )

                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.difficulty,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = RekoraColors.SecondaryText
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = RekoraColors.SecondaryText
                    )
                    Text(
                        text = "₹${project.estimatedCostMin.toInt()} - ₹${project.estimatedCostMax.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                modifier = Modifier.size(16.dp),
                tint = RekoraColors.SecondaryText
            )
        }
    }
}

/**
 * Discipline Category Card
 * Restrained engineering vector glyph, discipline name, and blueprint counter.
 */
@Composable
fun DisciplineCard(
    name: String,
    icon: ImageVector,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(136.dp)
            .height(112.dp)
            .bouncyClickable(targetScale = 0.95f, onClick = onClick)
            .shadow(
                elevation = 1.5.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RekoraCorners.Squircle)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count Blueprints",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        color = RekoraColors.SecondaryText
                    )
                )
            }
        }
    }
}

/**
 * Engineering Evolution Card
 * Showcases actual version progression, technical optimizations, and verified outcomes.
 */
@Composable
fun EngineeringEvolutionCard(
    title: String,
    projectRef: String,
    versionFrom: String,
    versionTo: String,
    highlights: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = projectRef.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = RekoraColors.EngineeringTeal
                )

                Surface(
                    shape = RekoraCorners.Tag,
                    color = RekoraColors.TealLight,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.EngineeringTeal.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$versionFrom → $versionTo",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                highlights.forEach { highlight ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(RekoraColors.EngineeringTeal)
                        )
                        Text(
                            text = highlight,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = RekoraColors.SecondaryText
                            )
                        )
                    }
                }
            }
        }
    }
}
