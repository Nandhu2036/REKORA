package com.rekora.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.RekoraPrimaryButton
import com.rekora.app.ui.components.RekoraSecondaryButton
import com.rekora.app.ui.screens.auth.AuthViewModel
import com.rekora.app.ui.theme.RekoraColors
import com.rekora.app.ui.theme.RekoraCorners
import com.rekora.app.ui.theme.RekoraDimens
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val phaseTag: String,
    val icon: ImageVector,
    val highlights: List<String>,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    // User preferences selection on final step
    var selectedRole by remember { mutableStateOf("Student") }
    val availableRoles = listOf("Student", "Contributor", "Faculty", "Hobbyist")
    val availableInterests = listOf(
        "Robotics",
        "IoT & Embedded",
        "AI & Machine Learning",
        "3D Printing & CAD",
        "Clean Energy",
        "Computer Vision"
    )
    val selectedInterests = remember { mutableStateListOf("Robotics", "IoT & Embedded") }

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Discover & Blueprint",
                subtitle = "Find projects matching budget, components, and skills. AI Project Engineer consultation.",
                phaseTag = "PHASE 01 • BLUEPRINTING",
                icon = Icons.Outlined.AutoAwesome,
                highlights = listOf(
                    "Intelligent project matching by budget & hardware",
                    "AI Project Engineer for 24/7 technical consultation",
                    "Verified schematics, pinout guides & complete BOM"
                ),
                gradientColors = listOf(RekoraColors.MidnightNavy, RekoraColors.DeepBlue)
            ),
            OnboardingPageData(
                title = "Source & Fabricate",
                subtitle = "Rent microcontrollers, buy parts, request 3D printing & laser fabrication directly on campus.",
                phaseTag = "PHASE 02 • MAKERSPACE & HARDWARE",
                icon = Icons.Outlined.PrecisionManufacturing,
                highlights = listOf(
                    "Rent microcontrollers & sensors at student rates",
                    "Instant campus 3D printing (PLA, PETG, TPU)",
                    "Peer-to-peer student marketplace for verified components"
                ),
                gradientColors = listOf(RekoraColors.DeepBlue, RekoraColors.EngineeringTeal)
            ),
            OnboardingPageData(
                title = "Build & Document",
                subtitle = "Real-time assembly instructions, testing validation, and automated academic report drafting.",
                phaseTag = "PHASE 03 • BUILD & ACADEMICS",
                icon = Icons.Outlined.FactCheck,
                highlights = listOf(
                    "Step-by-step interactive hardware assembly guide",
                    "Sensor test runs with real-time telemetry validation",
                    "Automated academic report drafting in IEEE format"
                ),
                gradientColors = listOf(RekoraColors.EngineeringTeal, Color(0xFF1E6068))
            ),
            OnboardingPageData(
                title = "Preserve & Evolve",
                subtitle = "Project DNA persistent identity. Today's project becomes tomorrow's foundation.",
                phaseTag = "PHASE 04 • PERSISTENT DNA",
                icon = Icons.Outlined.AccountTree,
                highlights = listOf(
                    "Cryptographically signed Project DNA genealogy",
                    "Fork & build upon previous academic milestones",
                    "Verified engineering portfolio recognized by faculty"
                ),
                gradientColors = listOf(Color(0xFF102638), RekoraColors.MidnightNavy)
            )
        )
    }

    val isLastPage = pagerState.currentPage == 3

    Scaffold(
        containerColor = RekoraColors.LightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = RekoraDimens.ScreenPadding,
                        vertical = RekoraDimens.SpacingMd
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RekoraColors.EngineeringTeal)
                    )
                    Text(
                        text = "REKORA",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = RekoraColors.MidnightNavy
                    )
                }

                // Skip button
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = RekoraColors.SecondaryText
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(RekoraColors.LightBackground)
                    .padding(RekoraDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                // Dots / Pill Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) RekoraColors.EngineeringTeal
                                    else RekoraColors.Border
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(RekoraDimens.SpacingXs))

                // Action Buttons
                if (!isLastPage) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        // Continue as Guest button
                        OutlinedButton(
                            onClick = {
                                viewModel.continueAsGuest {
                                    onNavigateToHome()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(RekoraDimens.ButtonHeight),
                            shape = RekoraCorners.Button,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RekoraColors.MidnightNavy
                            )
                        ) {
                            Text(
                                text = "Guest Mode",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Next button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1,
                                        animationSpec = tween(400)
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(RekoraDimens.ButtonHeight),
                            shape = RekoraCorners.Button,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RekoraColors.MidnightNavy,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(RekoraDimens.SpacingSm))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(RekoraDimens.IconSizeSmall)
                            )
                        }
                    }
                } else {
                    // Final page: Get Started
                    RekoraPrimaryButton(
                        text = "Get Started",
                        onClick = {
                            viewModel.completeOnboarding(
                                role = selectedRole.uppercase(),
                                interests = selectedInterests.toList()
                            ) {
                                onNavigateToHome()
                            }
                        }
                    )

                    // Secondary: Sign in or Continue as Guest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.continueAsGuest {
                                    onNavigateToHome()
                                }
                            }
                        ) {
                            Text(
                                text = "Browse as Guest",
                                style = MaterialTheme.typography.labelMedium,
                                color = RekoraColors.SecondaryText
                            )
                        }

                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                text = "Have an account? Sign In",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RekoraColors.EngineeringTeal
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RekoraDimens.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
            ) {
                Spacer(modifier = Modifier.height(RekoraDimens.SpacingSm))

                // Phase tag badge
                Surface(
                    shape = CircleShape,
                    color = RekoraColors.TealLight
                ) {
                    Text(
                        text = page.phaseTag,
                        modifier = Modifier.padding(
                            horizontal = RekoraDimens.SpacingMd,
                            vertical = RekoraDimens.SpacingXs
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = RekoraColors.EngineeringTeal
                    )
                }

                // Illustration Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RekoraCorners.CardLarge,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(page.gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Watermark / Accent Circles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                radius = size.minDimension * 0.7f,
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f)
                            )
                        }

                        // Central Icon
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = page.title,
                                modifier = Modifier.size(46.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Title and Subtitle
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RekoraColors.MidnightNavy
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = RekoraColors.SecondaryText,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                // Key Value Highlights list
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RekoraCorners.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = RekoraColors.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(RekoraDimens.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        page.highlights.forEach { highlight ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(RekoraColors.TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = RekoraColors.EngineeringTeal
                                    )
                                }
                                Text(
                                    text = highlight,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = RekoraColors.PrimaryText,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // If on final page, show Role & Interest selector
                if (pageIndex == 3) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = RekoraDimens.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        Text(
                            text = "Primary Role",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = RekoraColors.MidnightNavy
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            availableRoles.forEach { role ->
                                val selected = selectedRole == role
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRole = role },
                                    shape = RekoraCorners.CardSmall,
                                    color = if (selected) RekoraColors.MidnightNavy else RekoraColors.White,
                                    shadowElevation = if (selected) 2.dp else 1.dp
                                ) {
                                    Text(
                                        text = role,
                                        modifier = Modifier.padding(vertical = RekoraDimens.SpacingSm),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) Color.White else RekoraColors.PrimaryText,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Engineering Interests",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = RekoraColors.MidnightNavy,
                            modifier = Modifier.padding(top = RekoraDimens.SpacingSm)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm),
                            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            availableInterests.forEach { interest ->
                                val isChecked = selectedInterests.contains(interest)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = {
                                        if (isChecked) selectedInterests.remove(interest)
                                        else selectedInterests.add(interest)
                                    },
                                    label = {
                                        Text(
                                            text = interest,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RekoraColors.TealLight,
                                        selectedLabelColor = RekoraColors.EngineeringTeal
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(RekoraDimens.SpacingXxl))
            }
        }
    }
}
