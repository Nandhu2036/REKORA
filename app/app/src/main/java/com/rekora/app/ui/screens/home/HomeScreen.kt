package com.rekora.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.data.local.entity.ComponentEntity
import com.rekora.app.data.local.entity.ProjectEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

/**
 * REKORA Home Screen — Editorial × Discovery Redesign
 * Immersive editorial hero, prominent featured lead project,
 * 7-discipline navigation rail, active workspace progress,
 * component marketplace showcase, and authentic engineering evolution stories.
 */
@Composable
fun HomeScreen(
    onNavigateToProject: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToAiEngineer: () -> Unit,
    onNavigateToProjectMatcher: () -> Unit,
    onNavigateToComponents: () -> Unit,
    onNavigateToFabrication: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val recommendedProjects by viewModel.recommendedProjects.collectAsState()
    val rentableComponents by viewModel.rentableComponents.collectAsState()
    val activePrototype by viewModel.activePrototype.collectAsState()

    val disciplines = listOf(
        Triple("Robotics", Icons.Outlined.PrecisionManufacturing, 8),
        Triple("Internet of Things", Icons.Outlined.Sensors, 14),
        Triple("Mechanical Design", Icons.Outlined.SettingsSuggest, 7),
        Triple("Renewable Energy", Icons.Outlined.SolarPower, 5),
        Triple("Electronics", Icons.Outlined.DeveloperBoard, 19),
        Triple("Artificial Intelligence", Icons.Outlined.Psychology, 11),
        Triple("Manufacturing & Fab", Icons.Outlined.Layers, 9)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RekoraHomeTopBar(
                userName = userName,
                avatarUrl = null,
                cartCount = cartCount,
                onAvatarClick = { /* Profile handled by bottom bar */ },
                onNotificationClick = onNavigateToNotifications,
                onCartClick = onNavigateToCart
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            // 1. Search Spotlight Field (Google Play / Apple Spotlight)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding)
                ) {
                    SearchSpotlightBar(onClick = onNavigateToExplore)
                }
            }

            // 2. Editorial Hero: "Build what comes next."
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding)
                ) {
                    EditorialHeroSection(
                        onExploreProjectsClick = onNavigateToExplore,
                        onAskAiClick = onNavigateToAiEngineer
                    )
                }
            }

            // 3. Featured Projects: Prominent Lead Project + Browsing Rail
            if (recommendedProjects.isNotEmpty()) {
                val leadProject = recommendedProjects.first()
                val remainingProjects = recommendedProjects.drop(1)

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            title = "Featured Projects",
                            actionText = "Explore All ›",
                            onAction = onNavigateToExplore,
                            modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                        )

                        // Lead Project Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = RekoraDimens.ScreenPadding)
                        ) {
                            EditorialLeadProjectCard(
                                project = leadProject,
                                onClick = { onNavigateToProject(leadProject.id) }
                            )
                        }

                        // Horizontal Browsing Rail of remaining projects
                        if (remainingProjects.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(remainingProjects, key = { it.id }) { project ->
                                    HomeProjectCard(
                                        project = project,
                                        onClick = { onNavigateToProject(project.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Discover by Discipline
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "Discover by Discipline",
                        actionText = "All Domains ›",
                        onAction = onNavigateToExplore,
                        modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(disciplines) { (name, icon, count) ->
                            DisciplineCard(
                                name = name,
                                icon = icon,
                                count = count,
                                onClick = onNavigateToExplore
                            )
                        }
                    }
                }
            }

            // 5. Continue Building (Active Projects & Saved Blueprints)
            activePrototype?.let { prototype ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = RekoraDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            title = "Continue Building",
                            actionText = "Open Blueprint ›",
                            onAction = { onNavigateToProject(prototype.projectId) }
                        )
                        ActivePrototypeCard(
                            prototype = prototype,
                            onResumeClick = { onNavigateToProject(prototype.projectId) }
                        )
                    }
                }
            }

            // 6. Component Marketplace Showcase (Buy & Rent Options)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "Component Marketplace",
                        actionText = "Browse Hardware ›",
                        onAction = onNavigateToComponents,
                        modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rentableComponents, key = { it.id }) { component ->
                            HomeRentableComponentCard(
                                component = component,
                                onClick = onNavigateToComponents
                            )
                        }
                    }
                }
            }

            // 7. Engineering Stories & Evolution (Authentic Project Iterations)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "Engineering Stories & Evolution",
                        actionText = null
                    )

                    EngineeringEvolutionCard(
                        title = "Edge TPU Inference Acceleration",
                        projectRef = "Autonomous Floating Waste Collector",
                        versionFrom = "v1.0",
                        versionTo = "v2.1",
                        highlights = listOf(
                            "Inference latency reduced from 142ms to 28ms with quantized MobileNetV2",
                            "Reduced power draw to 4.2W on 12V LiFePO4 propulsion bus",
                            "Added dual hydro-thruster ESC telemetry monitoring over UART"
                        ),
                        onClick = { onNavigateToProject("proj-floating-waste-collector") }
                    )

                    EngineeringEvolutionCard(
                        title = "Dual-Axis Worm Drive Reinforcement",
                        projectRef = "Solar Tracking Demonstrator",
                        versionFrom = "v1.2",
                        versionTo = "v2.0",
                        highlights = listOf(
                            "3D-printed PETG worm gear increases holding torque by 45%",
                            "Firmware PID closed-loop calibrated with dual LDR differential feedback"
                        ),
                        onClick = { onNavigateToProject("proj-solar-tracker") }
                    )
                }
            }

            // 8. Campus Lab Bulletins
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(
                        title = "Lab Bulletins",
                        actionText = null
                    )

                    viewModel.announcements.forEach { ann ->
                        AnnouncementCard(announcement = ann)
                    }
                }
            }

            // 9. Academic Integrity & Open Research Note
            item {
                EthicalEcosystemNote(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding)
                )
            }
        }
    }
}

/**
 * Clean Search Spotlight Bar
 */
@Composable
private fun SearchSpotlightBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.SearchBar,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.SearchBar),
        shape = RekoraCorners.SearchBar,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = RekoraColors.EngineeringTeal,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Search engineering blueprints, parts, tools...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Filter",
                    modifier = Modifier
                        .padding(6.dp)
                        .size(15.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Editorial Hero Section
 * "Build what comes next."
 * Supporting copy, CAD render schematic canvas, primary CTA & secondary CTA.
 */
@Composable
private fun EditorialHeroSection(
    onExploreProjectsClick: () -> Unit,
    onAskAiClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RekoraCorners.CardLarge,
                ambientColor = Color(0x10000000),
                spotColor = Color(0x18000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardLarge),
        shape = RekoraCorners.CardLarge,
        color = RekoraColors.MidnightNavy
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            RekoraColors.MidnightNavy,
                            RekoraColors.DeepBlue,
                            Color(0xFF0F324D)
                        )
                    )
                )
        ) {
            // Blueprint CAD schematic canvas
            BlueprintSchematicCanvas(
                modifier = Modifier.matchParentSize(),
                domain = "Robotics",
                accentColor = RekoraColors.EngineeringTeal
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Eyebrow badge
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.DarkAccent.copy(alpha = 0.4f))
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
                            text = "ENGINEERING BLUEPRINTS • CONNECTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = RekoraColors.DarkAccent
                        )
                    }
                }

                // Large Editorial Typography
                Text(
                    text = "Build what comes next.",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.6).sp
                    ),
                    color = Color.White
                )

                // Supporting Copy
                Text(
                    text = "Discover engineering projects, access the resources you need, and turn ideas into working prototypes.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.5.sp,
                        lineHeight = 19.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Primary "Explore projects", Secondary "Ask the AI Project Engineer"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onExploreProjectsClick,
                        modifier = Modifier
                            .height(44.dp)
                            .bouncyClickable(targetScale = 0.95f, onClick = onExploreProjectsClick),
                        shape = RekoraCorners.Button,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RekoraColors.EngineeringTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Explore projects",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onAskAiClick,
                        modifier = Modifier
                            .height(44.dp)
                            .bouncyClickable(targetScale = 0.95f, onClick = onAskAiClick),
                        shape = RekoraCorners.Button,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = RekoraColors.DarkAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask AI Engineer",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Active Prototype Card
 */
@Composable
private fun ActivePrototypeCard(
    prototype: ActivePrototype,
    onResumeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onResumeClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LuxuryStatusCapsule(
                    label = prototype.domain.uppercase(),
                    dotColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderColor = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = RekoraColors.SecondaryText
                    )
                    Text(
                        text = "${prototype.daysRemaining} days to demo",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = RekoraColors.SecondaryText
                    )
                }
            }

            Text(
                text = prototype.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = prototype.stage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = RekoraColors.SecondaryText
                )
            )

            // Progress Bar with Animated Fill
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sprint Milestones",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = RekoraColors.SecondaryText
                    )
                    Text(
                        text = "${prototype.progressPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }
                LinearProgressIndicator(
                    progress = { prototype.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = RekoraColors.EngineeringTeal,
                    trackColor = RekoraColors.NavyLight
                )
            }

            // Next step callout
            Surface(
                shape = RekoraCorners.CardSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = prototype.nextAction,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Resume",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Horizontal Carousel Project Card
 */
@Composable
private fun HomeProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .bouncyClickable(targetScale = 0.97f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(RekoraColors.MidnightNavy, RekoraColors.DeepBlue)
                        )
                    )
            ) {
                BlueprintSchematicCanvas(
                    modifier = Modifier.matchParentSize(),
                    domain = project.domain,
                    accentColor = RekoraColors.EngineeringTeal
                )

                Surface(
                    shape = RekoraCorners.Tag,
                    color = RekoraColors.MidnightNavy.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = project.domain.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = RekoraColors.DarkAccent
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = project.shortDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = RekoraColors.SecondaryText
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetaInfo(
                        icon = Icons.Outlined.TrendingUp,
                        text = project.difficulty
                    )
                    Text(
                        text = "₹${project.estimatedCostMin.toInt()} - ₹${project.estimatedCostMax.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }
            }
        }
    }
}

/**
 * Rentable Hardware Component Card
 */
@Composable
private fun HomeRentableComponentCard(
    component: ComponentEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .bouncyClickable(targetScale = 0.97f, onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RekoraCorners.Card,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RekoraCorners.Image)
                    .background(
                        Brush.linearGradient(
                            listOf(RekoraColors.MidnightNavy, RekoraColors.DeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = RekoraColors.DarkAccent.copy(alpha = 0.8f)
                )
            }

            Text(
                text = component.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
                        text = "₹${component.rentalPricePerWeek.toInt()}/wk",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                    Text(
                        text = "Buy ₹${component.purchasePrice.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            color = RekoraColors.SecondaryText
                        )
                    )
                }

                LuxuryStatusCapsule(
                    label = "AVAILABLE",
                    dotColor = RekoraColors.Success,
                    containerColor = RekoraColors.SuccessLight,
                    borderColor = RekoraColors.SuccessBorder
                )
            }
        }
    }
}

/**
 * Announcement Bulletin Card
 */
@Composable
private fun AnnouncementCard(announcement: CampusAnnouncement) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RekoraCorners.CardSmall,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.CardSmall),
        shape = RekoraCorners.CardSmall,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RekoraColors.TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = RekoraColors.EngineeringTeal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = announcement.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = RekoraColors.SecondaryText
                    )
                )
            }
        }
    }
}

/**
 * IEEE Academic Integrity Charter Note
 */
@Composable
private fun EthicalEcosystemNote(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RekoraCorners.CardSmall,
        color = RekoraColors.TealLight.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.EngineeringTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = RekoraColors.EngineeringTeal
            )
            Text(
                text = "IEEE Academic Integrity: All blueprints and CAD repos preserve student provenance, version trees, and ethical open research standards.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
