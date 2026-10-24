package com.rekora.app.ui.screens.project

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.*
import com.rekora.app.data.local.entity.*
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val bomItemDao: BomItemDao,
    private val projectVersionDao: ProjectVersionDao,
    private val cartDao: CartDao,
    private val savedProjectDao: SavedProjectDao,
    private val prefs: RekoraPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navProjectId: String? = savedStateHandle["projectId"]

    private val _project = MutableStateFlow<ProjectEntity?>(null)
    val project = _project.asStateFlow()

    private val _bomItems = MutableStateFlow<List<BomItemEntity>>(emptyList())
    val bomItems = _bomItems.asStateFlow()

    private val _versions = MutableStateFlow<List<ProjectVersionEntity>>(emptyList())
    val versions = _versions.asStateFlow()

    // Map of bomItemId to choice: "BUY", "RENT", "USE_EXISTING"
    private val _sourcingChoices = MutableStateFlow<Map<String, String>>(emptyMap())
    val sourcingChoices = _sourcingChoices.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    val cartCount: StateFlow<Int> = cartDao.getCartCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    val outOfPocketCost: StateFlow<Double> = combine(_bomItems, _sourcingChoices) { items, choices ->
        items.sumOf { item ->
            val choice = choices[item.id] ?: item.sourcingChoice
            when (choice) {
                "BUY" -> item.totalPrice
                "RENT" -> item.totalPrice * 0.35 // 35% typical student lab rental price
                "USE_EXISTING" -> 0.0
                else -> item.totalPrice
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOriginalBOMCost: StateFlow<Double> = _bomItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        navProjectId?.let { loadProject(it) }
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            // 1. Fetch project from Room or seed fallback
            var currentProject = projectDao.getProjectById(id)
            if (currentProject == null) {
                currentProject = SeedDataProvider.getSeedProjects().find { it.id == id }
                    ?: SeedDataProvider.getSeedProjects().first()
            }
            _project.value = currentProject
            _isUnlocked.value = !currentProject.repositoryLocked

            // 2. Fetch BOM
            val currentBom = bomItemDao.getBomForProject(id).first().ifEmpty {
                SeedDataProvider.getSeedBomItems().filter { it.projectId == id }.ifEmpty {
                    SeedDataProvider.getSeedBomItems().take(4)
                }
            }
            _bomItems.value = currentBom

            // Initialize default sourcing choices
            val initialChoices = currentBom.associate { it.id to it.sourcingChoice }
            _sourcingChoices.value = initialChoices

            // 3. Fetch Versions
            val currentVersions = projectVersionDao.getVersionsForProject(id).first().ifEmpty {
                SeedDataProvider.getSeedProjectVersions().filter { it.projectId == id }.ifEmpty {
                    SeedDataProvider.getSeedProjectVersions().take(2)
                }
            }
            _versions.value = currentVersions

            // 4. Saved status
            val userId = prefs.userId.first() ?: "usr-demo-student-01"
            savedProjectDao.isProjectSaved(userId, id).collect {
                _isSaved.value = it
            }
        }
    }

    fun setSourcingChoice(bomItemId: String, choice: String) {
        _sourcingChoices.value = _sourcingChoices.value.toMutableMap().apply {
            put(bomItemId, choice)
        }
    }

    fun unlockRepository() {
        _isUnlocked.value = true
        _toastMessage.value = "Repository Unlocked! Full technical design files are now accessible."
    }

    fun toggleSave() {
        val proj = _project.value ?: return
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: "usr-demo-student-01"
            if (_isSaved.value) {
                savedProjectDao.unsaveProject(userId, proj.id)
                _isSaved.value = false
                _toastMessage.value = "Project removed from bookmarks"
            } else {
                savedProjectDao.saveProject(
                    SavedProjectEntity(
                        id = "saved-${System.currentTimeMillis()}",
                        userId = userId,
                        projectId = proj.id,
                        savedAt = System.currentTimeMillis()
                    )
                )
                _isSaved.value = true
                _toastMessage.value = "Project saved to workspace"
            }
        }
    }

    fun addSelectedToCart(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val items = _bomItems.value
            val choices = _sourcingChoices.value
            var addedCount = 0

            items.forEach { item ->
                val choice = choices[item.id] ?: item.sourcingChoice
                if (choice == "BUY" || choice == "RENT") {
                    val isRental = choice == "RENT"
                    val unitPrice = if (isRental) item.unitPrice * 0.35 else item.unitPrice
                    val cartItem = CartItemEntity(
                        id = "cart-${System.currentTimeMillis()}-${item.id}",
                        componentId = item.componentId ?: "comp-${item.id}",
                        quantity = item.quantity,
                        isRental = isRental,
                        rentalDays = if (isRental) 14 else 0,
                        unitPrice = unitPrice,
                        deposit = if (isRental) 500.0 else 0.0
                    )
                    cartDao.insertCartItem(cartItem)
                    addedCount++
                }
            }

            _toastMessage.value = "Added $addedCount required items to cart!"
            onSuccess()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToComponent: (String) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val projectState by viewModel.project.collectAsState()
    val bomItems by viewModel.bomItems.collectAsState()
    val versions by viewModel.versions.collectAsState()
    val sourcingChoices by viewModel.sourcingChoices.collectAsState()
    val outOfPocketCost by viewModel.outOfPocketCost.collectAsState()
    val totalOriginalBOMCost by viewModel.totalOriginalBOMCost.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "BOM & Hardware", "CAD & Fab", "Build & Test", "Project DNA")

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project = projectState ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
        return
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = project.title,
                onBackClick = onNavigateBack,
                cartCount = cartCount,
                onCartClick = onNavigateToCart,
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSave() },
                        modifier = Modifier.bouncyClickable()
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) RekoraColors.EngineeringTeal else RekoraColors.MidnightNavy
                        )
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this engineering project on REKORA: ${project.title}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Project"))
                        },
                        modifier = Modifier.bouncyClickable()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = RekoraColors.MidnightNavy
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (selectedTab == 1) { // Sticky BOM Action Bar
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(0.75.dp, RekoraColors.Hairline),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            EditorialEyebrow(text = "DYNAMIC OUT-OF-POCKET")
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${outOfPocketCost.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RekoraColors.EngineeringTeal
                                )
                                if (totalOriginalBOMCost > outOfPocketCost) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LuxuryStatusCapsule(
                                        label = "SAVED ₹${(totalOriginalBOMCost - outOfPocketCost).toInt()}",
                                        dotColor = RekoraColors.Success,
                                        containerColor = RekoraColors.SuccessLight
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .bouncyClickable(targetScale = 0.96f) {
                                    viewModel.addSelectedToCart(onNavigateToCart)
                                },
                            shape = RekoraCorners.Button,
                            color = RekoraColors.MidnightNavy,
                            border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Source via Cart",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color.White
                                )
                            }
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Visual Canvas Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(RekoraColors.MidnightNavy, RekoraColors.DeepBlue)
                        )
                    )
            ) {
                // Vector schematic underlying canvas
                BlueprintSchematicCanvas(
                    modifier = Modifier.fillMaxSize()
                )

                if (!project.coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = project.coverImageUrl,
                        contentDescription = project.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Cupertino Vignette Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        RekoraColors.MidnightNavy.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                }

                // Badges & Title overlay at bottom of hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LuxuryStatusCapsule(
                            label = "v${project.currentVersion} STABLE",
                            dotColor = RekoraColors.ElectricCyan,
                            containerColor = RekoraColors.MidnightNavy.copy(alpha = 0.85f),
                            borderColor = RekoraColors.ElectricCyan.copy(alpha = 0.35f),
                            textColor = Color.White
                        )
                        LuxuryStatusCapsule(
                            label = project.difficulty.uppercase(),
                            dotColor = when (project.difficulty.uppercase()) {
                                "BEGINNER" -> RekoraColors.Success
                                "INTERMEDIATE" -> RekoraColors.Warning
                                else -> RekoraColors.Error
                            },
                            containerColor = RekoraColors.MidnightNavy.copy(alpha = 0.85f),
                            borderColor = Color.White.copy(alpha = 0.2f),
                            textColor = Color.White
                        )
                        if (project.isOpenSource) {
                            LuxuryStatusCapsule(
                                label = "OPEN SOURCE",
                                dotColor = RekoraColors.Success,
                                containerColor = RekoraColors.MidnightNavy.copy(alpha = 0.85f),
                                borderColor = RekoraColors.Success.copy(alpha = 0.35f),
                                textColor = Color.White
                            )
                        }
                    }

                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Ethical Repository Banner
            RepositoryUnlockBanner(
                isUnlocked = isUnlocked,
                price = project.repositoryPrice,
                onUnlock = { viewModel.unlockRepository() }
            )

            // Apple Scrollable Pills Selector
            Surface(
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
            ) {
                AppleScrollablePills(
                    items = tabs,
                    selectedIndex = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }

            // Tab Content
            Box(modifier = Modifier.padding(20.dp)) {
                when (selectedTab) {
                    0 -> OverviewTabContent(project = project)
                    1 -> BomTabContent(
                        bomItems = bomItems,
                        sourcingChoices = sourcingChoices,
                        onChoiceChange = { id, choice -> viewModel.setSourcingChoice(id, choice) },
                        onNavigateToComponent = onNavigateToComponent
                    )
                    2 -> CadTabContent(project = project)
                    3 -> BuildTabContent(project = project)
                    4 -> DnaTabContent(project = project, versions = versions)
                }
            }

            if (toastMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearToast() }) {
                            Text("OK", color = RekoraColors.ElectricCyan)
                        }
                    }
                ) {
                    Text(toastMessage ?: "")
                }
            }
        }
    }
}

// ── Tab 1: Overview ──────────────────────────────────────────

@Composable
private fun OverviewTabContent(project: ProjectEntity) {
    val objectives = remember(project.objectives) { parseJsonList(project.objectives) }
    val capabilities = remember(project.capabilities) { parseJsonList(project.capabilities) }
    val safety = remember(project.safetyConsiderations) { parseJsonList(project.safetyConsiderations) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Quick KPI Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMetricCard(
                title = "Est. Budget",
                value = "₹${project.estimatedCostMin.toInt()} – ₹${project.estimatedCostMax.toInt()}",
                icon = Icons.Outlined.CurrencyRupee,
                modifier = Modifier.weight(1f)
            )
            DetailMetricCard(
                title = "Est. Timeline",
                value = project.estimatedTimeline,
                icon = Icons.Outlined.Schedule,
                modifier = Modifier.weight(1f)
            )
        }

        // Problem Statement
        EngineeringSectionCard(
            eyebrow = "CORE CHALLENGE",
            title = "Problem Statement",
            icon = Icons.Outlined.ReportProblem,
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = project.problemStatement,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Technical Abstract
        EngineeringSectionCard(
            eyebrow = "ARCHITECTURE",
            title = "Technical Abstract",
            icon = Icons.Outlined.Description,
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = project.abstractText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Objectives
        if (objectives.isNotEmpty()) {
            EngineeringSectionCard(
                eyebrow = "DELIVERABLES",
                title = "Project Objectives",
                icon = Icons.Outlined.TrackChanges,
                accentColor = MaterialTheme.colorScheme.secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    objectives.forEach { obj ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                text = obj,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Key Capabilities
        if (capabilities.isNotEmpty()) {
            EngineeringSectionCard(
                eyebrow = "SYSTEM SPECS",
                title = "Key Capabilities",
                icon = Icons.Outlined.Bolt,
                accentColor = MaterialTheme.colorScheme.secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    capabilities.forEach { cap ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SettingsInputComponent,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = cap,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Safety Considerations
        if (safety.isNotEmpty()) {
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(0.75.dp, RekoraColors.Warning)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = "Safety Alert",
                            tint = RekoraColors.Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        EditorialEyebrow(
                            text = "SAFETY & HAZARD PROTOCOLS",
                            color = RekoraColors.Warning
                        )
                    }

                    safety.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = RekoraColors.Warning, fontWeight = FontWeight.Bold)
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 2: BOM & Hardware ────────────────────────────────────

@Composable
private fun BomTabContent(
    bomItems: List<BomItemEntity>,
    sourcingChoices: Map<String, String>,
    onChoiceChange: (String, String) -> Unit,
    onNavigateToComponent: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // High-level BOM Strategy Capsule
        Surface(
            shape = RekoraCorners.Card,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        EditorialEyebrow(text = "DYNAMIC HARDWARE OPTIMIZER")
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bill of Materials (${bomItems.size} items)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    LuxuryStatusCapsule(
                        label = "CAMPUS INVENTORY LINKED",
                        dotColor = MaterialTheme.colorScheme.secondary,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                Text(
                    text = "Select sourcing strategy per item to dynamically optimize your student lab expenditure. 'Use Existing' leverages parts already present in your collegiate locker.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // BOM Items List
        bomItems.forEach { item ->
            val currentChoice = sourcingChoices[item.id] ?: item.sourcingChoice
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .bouncyClickable()
                                    .clickable {
                                        item.componentId?.let { onNavigateToComponent(it) }
                                    }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 2
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${item.unitPrice.toInt()}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Qty: ${item.quantity}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Tactile Segmented Sourcing Toggle [ Buy | Rent | In Lab ]
                    Surface(
                        shape = RekoraCorners.SegmentedControl,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SourcingSegmentButton(
                                title = "Buy",
                                price = "₹${item.totalPrice.toInt()}",
                                isSelected = currentChoice == "BUY",
                                onClick = { onChoiceChange(item.id, "BUY") },
                                modifier = Modifier.weight(1f)
                            )
                            SourcingSegmentButton(
                                title = "Rent Lab",
                                price = "₹${(item.totalPrice * 0.35).toInt()}",
                                isSelected = currentChoice == "RENT",
                                onClick = { onChoiceChange(item.id, "RENT") },
                                modifier = Modifier.weight(1f)
                            )
                            SourcingSegmentButton(
                                title = "Owned",
                                price = "₹0",
                                isSelected = currentChoice == "USE_EXISTING",
                                onClick = { onChoiceChange(item.id, "USE_EXISTING") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourcingSegmentButton(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .bouncyClickable(targetScale = 0.97f) { onClick() },
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = price,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Tab 3: CAD & Fab ─────────────────────────────────────────

@Composable
private fun CadTabContent(project: ProjectEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // High-tech 3D CAD Wireframe Simulation Box
        Surface(
            shape = RekoraCorners.Card,
            color = RekoraColors.MidnightNavy,
            border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                BlueprintSchematicCanvas(modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ViewInAr,
                        contentDescription = "3D CAD Model",
                        modifier = Modifier.size(52.dp),
                        tint = RekoraColors.ElectricCyan
                    )
                    Text(
                        text = "Catamaran Marine Hull 3D Assembly",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Parametric dimensions: 440mm L × 280mm W × 160mm H • Hydrostatic Volume: 4.8 Liters",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LuxuryStatusCapsule(
                            label = "STEP & STL FORMATS",
                            dotColor = RekoraColors.ElectricCyan,
                            containerColor = RekoraColors.DeepBlue.copy(alpha = 0.6f),
                            borderColor = RekoraColors.ElectricCyan.copy(alpha = 0.3f),
                            textColor = Color.White
                        )
                        LuxuryStatusCapsule(
                            label = "MANIFOLD MESH: VERIFIED",
                            dotColor = RekoraColors.Success,
                            containerColor = RekoraColors.DeepBlue.copy(alpha = 0.6f),
                            borderColor = RekoraColors.Success.copy(alpha = 0.3f),
                            textColor = Color.White
                        )
                    }
                }
            }
        }

        // Material Recommendation Banner
        Surface(
            shape = RekoraCorners.Card,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RekoraCorners.Squircle)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EditorialEyebrow(text = "FABRICATION GUIDANCE")
                    Text(
                        text = "PETG Filament (40% Gyroid Infill)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use PETG or ASA for water-immersed hulls and thruster mounts. Avoid standard PLA as it degrades and absorbs moisture under prolonged aquatic immersion.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // STL/STEP Parts List
        EngineeringSectionCard(
            eyebrow = "PARTS DIRECTORY",
            title = "Fabrication Parts List",
            icon = Icons.Outlined.PrecisionManufacturing,
            accentColor = RekoraColors.DeepBlue
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CadPartRow(name = "pontoon_left_hull.stl", weight = "185g", infill = "40% PETG")
                CadPartRow(name = "pontoon_right_hull.stl", weight = "185g", infill = "40% PETG")
                CadPartRow(name = "electronics_enclosure_ip67.step", weight = "140g", infill = "60% PETG (O-ring)")
                CadPartRow(name = "thruster_strut_bracket_pair.stl", weight = "65g", infill = "100% Solid PETG")
                CadPartRow(name = "solar_canopy_riser.stl", weight = "45g", infill = "30% PETG")
            }
        }

        // Order 3D Print CTA Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .bouncyClickable(targetScale = 0.98f) { /* Navigates to fabrication */ },
            shape = RekoraCorners.Button,
            color = RekoraColors.EngineeringTeal
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.Print, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Request 3D Print Quote via Campus Makerspace",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CadPartRow(name: String, weight: String, infill: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = RekoraColors.EngineeringTeal
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    ),
                    color = RekoraColors.MidnightNavy
                )
            }
            Text(
                text = "$weight • $infill",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = RekoraColors.SecondaryText
            )
        }
    }
}

// ── Tab 4: Build & Test ──────────────────────────────────────

@Composable
private fun BuildTabContent(project: ProjectEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Assembly Stepper
        EngineeringSectionCard(
            eyebrow = "STANDARD OPERATING PROCEDURE",
            title = "Assembly Procedure",
            icon = Icons.Outlined.Build,
            accentColor = RekoraColors.MidnightNavy
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AssemblyStepRow(
                    step = 1,
                    title = "Hull Sealing & Structural Spars",
                    instruction = "Fasten twin PETG pontoons using M4 stainless steel threaded rods. Apply marine RTV silicone grease to the enclosure seal channel."
                )
                AssemblyStepRow(
                    step = 2,
                    title = "Powertrain & ESC Harnessing",
                    instruction = "Mount dual underwater thrusters on stern brackets. Route 3-phase silicone motor leads through IP68 cable glands into the central compartment."
                )
                AssemblyStepRow(
                    step = 3,
                    title = "Logic & Vision Payload Integration",
                    instruction = "Secure Raspberry Pi 4 on nylon standoffs. Connect the wide-angle camera to the CSI ribbon connector, ensuring zero bend kinks."
                )
                AssemblyStepRow(
                    step = 4,
                    title = "Firmware Flashing & Thruster Calibration",
                    instruction = "Flash the headless Raspberry Pi OS 64-bit image. Run 'python3 calibrate_esc.py' to center the 1500us neutral PWM throttle."
                )
            }
        }

        // Hardware Pinout Matrix
        EngineeringSectionCard(
            eyebrow = "HARDWARE SCHEMATICS",
            title = "Pinout Matrix & Bus Map",
            icon = Icons.Outlined.Cable,
            accentColor = RekoraColors.DeepBlue
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RekoraColors.MidnightNavy,
                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "// GPIO PINOUT MAPPING — REKORA LABS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = RekoraColors.ElectricCyan
                    )
                    PinoutText("Port Thruster ESC  -> GPIO 18 (PWM0) [Pin 12]")
                    PinoutText("Stbd Thruster ESC  -> GPIO 19 (PWM1) [Pin 35]")
                    PinoutText("Forward Sonar Trig -> GPIO 23        [Pin 16]")
                    PinoutText("Forward Sonar Echo -> GPIO 24        [Pin 18] (via 1k/2k)")
                    PinoutText("BME280 I2C SDA     -> GPIO 2 (SDA)   [Pin 3]")
                    PinoutText("BME280 I2C SCL     -> GPIO 3 (SCL)   [Pin 5]")
                    PinoutText("Kill Switch Relay  -> GPIO 4         [Pin 7]")
                }
            }
        }

        // Validation Criteria
        EngineeringSectionCard(
            eyebrow = "QUALITY ASSURANCE",
            title = "Validation & Acceptance Tests",
            icon = Icons.Outlined.FactCheck,
            accentColor = RekoraColors.Success
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ValidationCheckRow(title = "15 FPS Edge Vision Test", spec = "SSD MobileNet inference <= 65ms per frame on Raspberry Pi 4 CPU")
                ValidationCheckRow(title = "Differential Steering Pivot", spec = "Zero turning radius completed in <= 2.2 seconds in water test basin")
                ValidationCheckRow(title = "IP67 Submersion Seal Test", spec = "0.5m water column immersion for 30 min with 0.0g moisture ingress")
                ValidationCheckRow(title = "Failsafe Geofence Kill Switch", spec = "All PWM thrusters zeroed within 200ms of telemetry disconnect")
            }
        }
    }
}

@Composable
private fun AssemblyStepRow(step: Int, title: String, instruction: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = RekoraColors.MidnightNavy,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$step",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = RekoraColors.MidnightNavy
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RekoraColors.SecondaryText,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
private fun PinoutText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        ),
        color = Color.White.copy(alpha = 0.9f)
    )
}

@Composable
private fun ValidationCheckRow(title: String, spec: String) {
    var checked by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f) { checked = !checked },
        shape = RoundedCornerShape(10.dp),
        color = if (checked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.75.dp, if (checked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = CheckboxDefaults.colors(checkedColor = RekoraColors.Success)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (checked) RekoraColors.Success else RekoraColors.MidnightNavy
                )
                Text(
                    text = spec,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp
                    ),
                    color = RekoraColors.SecondaryText
                )
            }
        }
    }
}

// ── Tab 5: Project DNA ───────────────────────────────────────

@Composable
private fun DnaTabContent(
    project: ProjectEntity,
    versions: List<ProjectVersionEntity>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Version Evolution Tree
        EngineeringSectionCard(
            eyebrow = "PERSISTENT HERITAGE",
            title = "Version Evolution Graph",
            icon = Icons.Outlined.AccountTree,
            accentColor = RekoraColors.EngineeringTeal
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                versions.forEachIndexed { index, v ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = if (index == 0) RekoraColors.EngineeringTeal else RekoraColors.DeepBlue,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            if (index < versions.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(RekoraColors.Hairline)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "v${v.version} — ${v.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RekoraColors.MidnightNavy
                                )
                                if (v.isVerified) {
                                    LuxuryStatusCapsule(label = "VERIFIED PEER REVIEW", dotColor = RekoraColors.Success)
                                }
                            }
                            Text(
                                text = v.changeSummary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RekoraColors.SecondaryText,
                                    lineHeight = 18.sp
                                )
                            )
                            Text(
                                text = "Contributed by ${v.contributorName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = RekoraColors.EngineeringTeal
                            )
                        }
                    }
                }
            }
        }

        // Contributors
        EngineeringSectionCard(
            eyebrow = "RESEARCH TEAM",
            title = "Project Contributors",
            icon = Icons.Outlined.People,
            accentColor = RekoraColors.DeepBlue
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContributorRow(name = "Campus Marine Robotics Lab", role = "Lead Research Lab", dept = "St. Joseph's Engineering")
                ContributorRow(name = "Reshma S", role = "Vision & Edge AI Pipeline", dept = "Dept of ECE")
                ContributorRow(name = "NandhaKumar G", role = "Hydrodynamics & CAD", dept = "Dept of Mech Eng")
            }
        }

        // License
        EngineeringSectionCard(
            eyebrow = "ACADEMIC INTEGRITY",
            title = "Licensing & Ethics Charter",
            icon = Icons.Outlined.Gavel,
            accentColor = RekoraColors.MidnightNavy
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "License: MIT Open Source / Academic Research Non-Commercial",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = RekoraColors.MidnightNavy
                )
                Text(
                    text = "All schematics, CAD files, and firmware are licensed for collegiate research and student prototyping. Attribution to the original student research team is required upon publication or competition entry.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RekoraColors.SecondaryText,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ContributorRow(name: String, role: String, dept: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = RekoraColors.MidnightNavy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = RekoraColors.MidnightNavy
                    )
                    Text(
                        text = dept,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = RekoraColors.SecondaryText
                    )
                }
            }
            LuxuryStatusCapsule(
                label = role.uppercase(),
                dotColor = RekoraColors.EngineeringTeal,
                containerColor = RekoraColors.TealLight
            )
        }
    }
}

// ── Repository Banner ─────────────────────────────────────────

@Composable
private fun RepositoryUnlockBanner(
    isUnlocked: Boolean,
    price: Double,
    onUnlock: () -> Unit
) {
    if (isUnlocked) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(0.75.dp, RekoraColors.Success),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = RekoraColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Unlocked Technical Repository: Source Code, Firmware & CAD Active",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    ),
                    color = RekoraColors.Success
                )
            }
        }
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Repository Locked (Student Grant Eligible)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Access full firmware C++, PCB gerbers, and calibration curves",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .bouncyClickable(targetScale = 0.96f) { onUnlock() },
                    shape = RekoraCorners.ButtonSmall,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (price > 0) "Unlock (₹${price.toInt()})" else "Unlock Free",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// ── Reusable Section Helpers ─────────────────────────────────

@Composable
private fun EngineeringSectionCard(
    eyebrow: String,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                EditorialEyebrow(text = eyebrow, color = accentColor)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun DetailMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.bouncyClickable(targetScale = 0.98f),
        shape = RekoraCorners.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RekoraCorners.Squircle)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                EditorialEyebrow(text = title.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun parseJsonList(json: String): List<String> {
    return json
        .removeSurrounding("[", "]")
        .split("\",\"")
        .map { it.replace("\"", "").trim() }
        .filter { it.isNotBlank() }
}
