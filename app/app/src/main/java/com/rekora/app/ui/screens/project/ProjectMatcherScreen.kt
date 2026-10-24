package com.rekora.app.ui.screens.project

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.BomItemDao
import com.rekora.app.data.local.dao.ProjectDao
import com.rekora.app.data.local.entity.ProjectEntity
import com.rekora.app.domain.model.ProjectMatch
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class MatchResultItem(
    val project: ProjectEntity,
    val score: Int,
    val reasons: List<String>,
    val reusedComponentCount: Int,
    val estimatedSavings: Double
)

@HiltViewModel
class ProjectMatcherViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val bomItemDao: BomItemDao
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    private val _keywords = MutableStateFlow("")
    val keywords = _keywords.asStateFlow()

    private val _maxBudget = MutableStateFlow(12000.0)
    val maxBudget = _maxBudget.asStateFlow()

    private val _ownedComponents = MutableStateFlow<Set<String>>(emptySet())
    val ownedComponents = _ownedComponents.asStateFlow()

    private val _difficulty = MutableStateFlow("INTERMEDIATE")
    val difficulty = _difficulty.asStateFlow()

    private val _timelineWeeks = MutableStateFlow(6)
    val timelineWeeks = _timelineWeeks.asStateFlow()

    private val _hasFabricationAccess = MutableStateFlow(true)
    val hasFabricationAccess = _hasFabricationAccess.asStateFlow()

    private val _matchResults = MutableStateFlow<List<MatchResultItem>>(emptyList())
    val matchResults = _matchResults.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating = _isCalculating.asStateFlow()

    fun setKeywords(value: String) { _keywords.value = value }
    fun setMaxBudget(value: Double) { _maxBudget.value = value }
    fun toggleOwnedComponent(component: String) {
        _ownedComponents.value = if (_ownedComponents.value.contains(component)) {
            _ownedComponents.value - component
        } else {
            _ownedComponents.value + component
        }
    }
    fun setDifficulty(value: String) { _difficulty.value = value }
    fun setTimelineWeeks(weeks: Int) { _timelineWeeks.value = weeks }
    fun setFabricationAccess(hasAccess: Boolean) { _hasFabricationAccess.value = hasAccess }

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value += 1
        } else if (_currentStep.value == 4) {
            calculateMatches()
            _currentStep.value = 5
        }
    }

    fun prevStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun goToStep(step: Int) {
        _currentStep.value = step.coerceIn(1, 5)
    }

    fun calculateMatches() {
        viewModelScope.launch {
            _isCalculating.value = true

            // Fetch all projects from DB or Seed
            var allProjects = projectDao.getAllProjects().first()
            if (allProjects.isEmpty()) {
                allProjects = SeedDataProvider.getSeedProjects()
            }

            val kw = _keywords.value.lowercase().trim()
            val budget = _maxBudget.value
            val owned = _ownedComponents.value
            val diff = _difficulty.value
            val weeks = _timelineWeeks.value

            val results = allProjects.map { project ->
                var score = 40 // Baseline score
                val reasons = mutableListOf<String>()
                var reusedCount = 0
                var savings = 0.0

                // 1. Budget Fit Analysis (up to 30 pts)
                if (project.estimatedCostMax <= budget) {
                    score += 30
                    reasons.add("Fits comfortably within ₹${budget.toInt()} budget")
                } else if (project.estimatedCostMin <= budget) {
                    score += 20
                    reasons.add("Within entry budget of ₹${budget.toInt()}")
                } else {
                    score -= 10
                }

                // 2. Owned Component Reusability (up to 25 pts)
                val hwReq = project.hardwareRequirements.lowercase()
                owned.forEach { item ->
                    if (hwReq.contains(item.lowercase())) {
                        reusedCount++
                        score += 8
                        savings += 1200.0 // Estimated hardware component savings
                    }
                }
                if (reusedCount > 0) {
                    reasons.add("Reuses $reusedCount of your lab components (Saves ~₹${savings.toInt()})")
                }

                // 3. Difficulty Level Match (up to 20 pts)
                if (project.difficulty.equals(diff, ignoreCase = true)) {
                    score += 20
                    reasons.add("Matches your ${diff.lowercase().replaceFirstChar { it.uppercase() }} skill level")
                } else {
                    score += 10
                }

                // 4. Timeline Fit
                if (project.timelineWeeks <= weeks + 2) {
                    score += 10
                    reasons.add("Fits your $weeks-week development timeline")
                }

                // 5. Keyword Relevance (up to 15 pts)
                if (kw.isNotBlank()) {
                    if (project.title.lowercase().contains(kw) ||
                        project.abstractText.lowercase().contains(kw) ||
                        project.tags.lowercase().contains(kw)) {
                        score += 15
                        reasons.add("Directly addresses '$kw' keywords")
                    }
                }

                val finalScore = score.coerceIn(55, 98)
                MatchResultItem(
                    project = project,
                    score = finalScore,
                    reasons = reasons,
                    reusedComponentCount = reusedCount,
                    estimatedSavings = savings
                )
            }.sortedByDescending { it.score }

            _matchResults.value = results
            _isCalculating.value = false
        }
    }
}

@Composable
fun ProjectMatcherScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    viewModel: ProjectMatcherViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val keywords by viewModel.keywords.collectAsState()
    val maxBudget by viewModel.maxBudget.collectAsState()
    val ownedComponents by viewModel.ownedComponents.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val timelineWeeks by viewModel.timelineWeeks.collectAsState()
    val hasFabAccess by viewModel.hasFabricationAccess.collectAsState()
    val matchResults by viewModel.matchResults.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Project Matcher",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            if (currentStep < 5) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    border = BorderStroke(RekoraDimens.DividerThickness, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(RekoraDimens.CardPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { viewModel.prevStep() },
                                shape = RekoraCorners.ButtonSmall
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = { viewModel.nextStep() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RekoraColors.EngineeringTeal,
                                contentColor = Color.White
                            ),
                            shape = RekoraCorners.ButtonSmall
                        ) {
                            Text(if (currentStep == 4) "Find Projects" else "Continue", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RekoraDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
        ) {
            // Wizard Step Indicator
            WizardStepIndicator(
                currentStep = currentStep,
                totalSteps = 5,
                stepLabels = listOf("Problem", "Budget", "Components", "Skills", "Results"),
                onStepClick = { if (it < currentStep) viewModel.goToStep(it) }
            )

            // Step Content
            when (currentStep) {
                1 -> Step1ProblemStatement(
                    keywords = keywords,
                    onKeywordsChange = { viewModel.setKeywords(it) }
                )
                2 -> Step2BudgetSelector(
                    budget = maxBudget,
                    onBudgetChange = { viewModel.setMaxBudget(it) }
                )
                3 -> Step3OwnedComponents(
                    ownedComponents = ownedComponents,
                    onToggleComponent = { viewModel.toggleOwnedComponent(it) }
                )
                4 -> Step4SkillsAndTimeline(
                    difficulty = difficulty,
                    timelineWeeks = timelineWeeks,
                    hasFabAccess = hasFabAccess,
                    onDifficultyChange = { viewModel.setDifficulty(it) },
                    onTimelineChange = { viewModel.setTimelineWeeks(it) },
                    onFabAccessChange = { viewModel.setFabricationAccess(it) }
                )
                5 -> Step5MatchingResults(
                    results = matchResults,
                    isCalculating = isCalculating,
                    onNavigateToProject = onNavigateToProject,
                    onRecalculate = { viewModel.goToStep(1) }
                )
            }
        }
    }
}

// ── Step 1: Problem / Keyword ────────────────────────────────

@Composable
private fun Step1ProblemStatement(
    keywords: String,
    onKeywordsChange: (String) -> Unit
) {
    val quickTopics = listOf(
        "Water Cleaning Robot",
        "Air Quality Station",
        "Smart Irrigation",
        "Vibration TinyML",
        "Autonomous Rover",
        "Solar Energy Harvester"
    )

    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)) {
        Text(
            text = "Step 1: What engineering problem do you want to solve?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter a keyword or technical theme for your capstone project, or choose one of our suggested research areas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RekoraSearchBar(
            query = keywords,
            onQueryChange = onKeywordsChange,
            placeholder = "e.g. marine debris, microclimate, computer vision...",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Suggested Research Topics",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
        ) {
            quickTopics.forEach { topic ->
                val isSelected = keywords.equals(topic, ignoreCase = true)
                RekoraFilterChip(
                    text = topic,
                    selected = isSelected,
                    onClick = { onKeywordsChange(if (isSelected) "" else topic) },
                    leadingIcon = Icons.Outlined.Lightbulb
                )
            }
        }
    }
}

// ── Step 2: Budget Selector ──────────────────────────────────

@Composable
private fun Step2BudgetSelector(
    budget: Double,
    onBudgetChange: (Double) -> Unit
) {
    val presets = listOf(
        3000.0 to "₹3,000 (Lab Micro-Grant)",
        8000.0 to "₹8,000 (Standard Capstone)",
        15000.0 to "₹15,000 (Multi-Sensor Drone)",
        25000.0 to "₹25,000+ (High-Power Robotic)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)) {
        Text(
            text = "Step 2: What is your maximum budget limit?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "REKORA cross-references verified component pricing and campus rental rates to keep your project strictly within this threshold.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Large Budget Display
        Card(
            shape = RekoraCorners.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RekoraDimens.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Maximum Budget Ceiling",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "₹${budget.toInt()}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Slider (₹2,000 to ₹30,000)
        Column {
            Slider(
                value = budget.toFloat(),
                onValueChange = { onBudgetChange(it.toDouble().roundToInt().toDouble()) },
                valueRange = 2000f..30000f,
                steps = 27,
                colors = SliderDefaults.colors(
                    thumbColor = RekoraColors.EngineeringTeal,
                    activeTrackColor = RekoraColors.EngineeringTeal
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("₹2,000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹30,000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Quick Preset Chips
        Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
            Text("Or select typical student tiers:", style = MaterialTheme.typography.labelMedium)
            presets.forEach { (amt, label) ->
                val selected = budget == amt
                Surface(
                    shape = RekoraCorners.CardSmall,
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RekoraCorners.CardSmall)
                        .clickable { onBudgetChange(amt) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        RadioButton(selected = selected, onClick = { onBudgetChange(amt) })
                    }
                }
            }
        }
    }
}

// ── Step 3: Owned Components ─────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3OwnedComponents(
    ownedComponents: Set<String>,
    onToggleComponent: (String) -> Unit
) {
    val commonLabHardware = listOf(
        "ESP32-WROOM",
        "Arduino Uno",
        "Raspberry Pi 4",
        "OV2640 Camera",
        "BME280 Sensor",
        "MG996R Servos",
        "18650 Li-ion Cells",
        "Ultrasonic Sonar",
        "3S LiPo Battery",
        "L298N Motor Driver",
        "Solenoid Water Valve",
        "10W Solar Panel"
    )

    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)) {
        Text(
            text = "Step 3: What components do you already own?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select any modules or controllers you have in your lab drawer. The Matcher factors these in as ₹0 cost to minimize out-of-pocket budget!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RekoraCorners.CardSmall,
            color = RekoraColors.SuccessLight
        ) {
            Row(
                modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Savings, contentDescription = null, tint = RekoraColors.Success)
                Text(
                    text = "${ownedComponents.size} components selected for hardware reuse",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RekoraColors.Success
                )
            }
        }

        // Chip Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonLabHardware.forEach { comp ->
                val isSelected = ownedComponents.contains(comp)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleComponent(comp) },
                    label = { Text(comp) },
                    leadingIcon = if (isSelected) {
                        { Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

// ── Step 4: Skills & Timeline ────────────────────────────────

@Composable
private fun Step4SkillsAndTimeline(
    difficulty: String,
    timelineWeeks: Int,
    hasFabAccess: Boolean,
    onDifficultyChange: (String) -> Unit,
    onTimelineChange: (Int) -> Unit,
    onFabAccessChange: (Boolean) -> Unit
) {
    val levels = listOf(
        "BEGINNER" to "Beginner (First-year prototyping)",
        "INTERMEDIATE" to "Intermediate (Capstone / Semester)",
        "ADVANCED" to "Advanced (Edge AI / Multithreaded)"
    )

    val timelines = listOf(
        3 to "3-4 Weeks (Mini-Project)",
        6 to "6-8 Weeks (Full Semester Capstone)",
        12 to "12+ Weeks (Research Competition)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)) {
        Text(
            text = "Step 4: Skills, Timeline & Fabrication",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Difficulty Selection
        Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
            Text("Your Team's Technical Skill Level:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            levels.forEach { (level, desc) ->
                val selected = difficulty == level
                Surface(
                    shape = RekoraCorners.CardSmall,
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RekoraCorners.CardSmall)
                        .clickable { onDifficultyChange(level) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(desc, style = MaterialTheme.typography.bodyMedium)
                        RadioButton(selected = selected, onClick = { onDifficultyChange(level) })
                    }
                }
            }
        }

        // Timeline Selection
        Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
            Text("Available Timeline:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            timelines.forEach { (w, desc) ->
                val selected = timelineWeeks == w
                Surface(
                    shape = RekoraCorners.CardSmall,
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RekoraCorners.CardSmall)
                        .clickable { onTimelineChange(w) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(desc, style = MaterialTheme.typography.bodyMedium)
                        RadioButton(selected = selected, onClick = { onTimelineChange(w) })
                    }
                }
            }
        }

        // 3D Print / Fab Access Toggle
        Card(
            shape = RekoraCorners.CardSmall,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Campus Makerspace / 3D Printer Access", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Can produce custom chassis brackets and waterproof housings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = hasFabAccess, onCheckedChange = onFabAccessChange)
            }
        }
    }
}

// ── Step 5: Matching Results ─────────────────────────────────

@Composable
private fun Step5MatchingResults(
    results: List<MatchResultItem>,
    isCalculating: Boolean,
    onNavigateToProject: (String) -> Unit,
    onRecalculate: () -> Unit
) {
    if (isCalculating) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                CircularProgressIndicator(color = RekoraColors.EngineeringTeal)
                Text(
                    text = "Running multi-dimensional hardware matching...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Matching Results (${results.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ranked by component reuse & budget compatibility",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onRecalculate) {
                Icon(imageVector = Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adjust")
            }
        }

        results.forEach { match ->
            Card(
                shape = RekoraCorners.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(RekoraDimens.DividerThickness, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProject(match.project.id) }
            ) {
                Column(
                    modifier = Modifier.padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    // Header with title and Match Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = match.project.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.MidnightNavy
                            )
                            Text(
                                text = match.project.domain.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Surface(
                            shape = RekoraCorners.Chip,
                            color = if (match.score >= 85) RekoraColors.EngineeringTeal else RekoraColors.DeepBlue
                        ) {
                            Text(
                                text = "${match.score}% MATCH",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = match.project.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Explainable Badges
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        match.reasons.forEach { reason ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = RekoraColors.Success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = RekoraDimens.DividerThickness,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Bottom info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Est. ₹${match.project.estimatedCostMin.toInt()} – ₹${match.project.estimatedCostMax.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RekoraColors.DeepBlue
                        )

                        TextButton(onClick = { onNavigateToProject(match.project.id) }) {
                            Text("View Project")
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Wizard Indicator ─────────────────────────────────────────

@Composable
private fun WizardStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>,
    onStepClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..totalSteps).forEach { step ->
                val isActive = step == currentStep
                val isCompleted = step < currentStep

                val bgColor = when {
                    isCompleted -> RekoraColors.Success
                    isActive -> RekoraColors.EngineeringTeal
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = if (isActive || isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onStepClick(step) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(text = "$step", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                }

                if (step < totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (step < currentStep) RekoraColors.Success else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }

        // Active Step Title
        Text(
            text = "Step $currentStep of $totalSteps: ${stepLabels.getOrElse(currentStep - 1) { "" }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
