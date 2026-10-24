package com.rekora.app.ui.screens.workspace

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.FabricationJobDao
import com.rekora.app.data.local.dao.ProjectDao
import com.rekora.app.data.local.dao.RentalDao
import com.rekora.app.data.local.dao.SavedProjectDao
import com.rekora.app.data.local.entity.FabricationJobEntity
import com.rekora.app.data.local.entity.ProjectEntity
import com.rekora.app.data.local.entity.RentalEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ActiveProjectItem(
    val project: ProjectEntity,
    val progressPercent: Int,
    val milestones: List<Pair<String, Boolean>>
)

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val rentalDao: RentalDao,
    private val fabricationJobDao: FabricationJobDao,
    private val savedProjectDao: SavedProjectDao,
    private val prefs: RekoraPreferences
) : ViewModel() {

    private val _activeProjects = MutableStateFlow<List<ActiveProjectItem>>(emptyList())
    val activeProjects = _activeProjects.asStateFlow()

    private val _rentals = MutableStateFlow<List<RentalEntity>>(emptyList())
    val rentals = _rentals.asStateFlow()

    private val _fabricationJobs = MutableStateFlow<List<FabricationJobEntity>>(emptyList())
    val fabricationJobs = _fabricationJobs.asStateFlow()

    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects = _savedProjects.asStateFlow()

    private val _actionToast = MutableStateFlow<String?>(null)
    val actionToast = _actionToast.asStateFlow()

    init {
        loadWorkspace()
    }

    private fun loadWorkspace() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: "usr-demo-student-01"

            // 1. Load active projects
            var seedProjects = projectDao.getAllProjects().first()
            if (seedProjects.isEmpty()) {
                seedProjects = SeedDataProvider.getSeedProjects()
            }
            if (seedProjects.isNotEmpty()) {
                val p1 = seedProjects.first()
                val p2 = seedProjects.getOrNull(1) ?: p1
                _activeProjects.value = listOf(
                    ActiveProjectItem(
                        project = p1,
                        progressPercent = 65,
                        milestones = listOf(
                            "3D Print Pontoon Catamaran Hull" to true,
                            "Wire Dual 12V Underwater Thrusters" to true,
                            "Deploy MobileNet SSD on Raspberry Pi 4" to false,
                            "Water Basin Buoyancy & Leak Test" to false
                        )
                    ),
                    ActiveProjectItem(
                        project = p2,
                        progressPercent = 30,
                        milestones = listOf(
                            "Bench Test BME280 & MQ-135 Sensors" to true,
                            "Configure ESP32 Deep Sleep Duty Cycle" to false,
                            "Fabricate Weatherproof Stevenson Screen" to false
                        )
                    )
                )
            }

            // 2. Load rentals from DB or seed
            rentalDao.getRentalsForUser(userId).collect { rentalList ->
                if (rentalList.isEmpty()) {
                    val demoRentals = listOf(
                        RentalEntity(
                            id = "rent-001",
                            userId = userId,
                            componentId = "comp-rpi-4b",
                            componentName = "Raspberry Pi 4 Model B (4GB)",
                            inventoryItemCode = "RK-RPI4-001",
                            status = "ACTIVE",
                            startDate = System.currentTimeMillis() - 86400000L * 4,
                            endDate = System.currentTimeMillis() + 86400000L * 10,
                            durationDays = 14,
                            rentalCost = 450.0,
                            securityDeposit = 1500.0,
                            depositRefunded = false,
                            conditionAtCheckout = "LIKE_NEW",
                            conditionAtReturn = null,
                            damageReported = false,
                            damageNotes = null,
                            pickupLocation = "Central Engineering Lab - Shelf B",
                            returnLocation = "Central Engineering Lab - Drop Box #2",
                            isDemo = true,
                            createdAt = System.currentTimeMillis() - 86400000L * 4
                        ),
                        RentalEntity(
                            id = "rent-002",
                            userId = userId,
                            componentId = "comp-esp32-wroom",
                            componentName = "ESP32-S3 Dual Type-C Dev Board",
                            inventoryItemCode = "RK-ESP32-001",
                            status = "DUE_SOON",
                            startDate = System.currentTimeMillis() - 86400000L * 5,
                            endDate = System.currentTimeMillis() + 86400000L * 2,
                            durationDays = 7,
                            rentalCost = 120.0,
                            securityDeposit = 400.0,
                            depositRefunded = false,
                            conditionAtCheckout = "NEW",
                            conditionAtReturn = null,
                            damageReported = false,
                            damageNotes = null,
                            pickupLocation = "IoT Embedded Lab - Locker 4",
                            returnLocation = "IoT Embedded Lab - Locker 4",
                            isDemo = true,
                            createdAt = System.currentTimeMillis() - 86400000L * 5
                        )
                    )
                    demoRentals.forEach { rentalDao.insertRental(it) }
                    _rentals.value = demoRentals
                } else {
                    _rentals.value = rentalList
                }
            }

            // 3. Load Fabrication Jobs
            fabricationJobDao.getJobsForUser(userId).collect { fabList ->
                if (fabList.isEmpty()) {
                    val demoJobs = listOf(
                        FabricationJobEntity(
                            id = "fab-001",
                            userId = userId,
                            projectId = "proj-floating-waste-collector",
                            fileName = "hull_strut_mount_v2.stl",
                            fileUrl = null,
                            fileFormat = "STL",
                            partDescription = "Reinforced marine bracket for starboard underwater thruster",
                            material = "PETG",
                            color = "Black",
                            quantity = 2,
                            dimensions = "85 x 45 x 30 mm",
                            tolerances = "±0.2mm",
                            finishing = "Raw print",
                            specialRequirements = "40% gyroid infill for maximum shear strength",
                            deliveryMethod = "CAMPUS_PICKUP",
                            status = "IN_PRODUCTION",
                            quotedPrice = 420.0,
                            estimatedDays = 2,
                            adminNotes = "Assigned to Station #2 (Bambu X1-Carbon)",
                            isDemo = true,
                            createdAt = System.currentTimeMillis() - 86400000L,
                            updatedAt = System.currentTimeMillis()
                        ),
                        FabricationJobEntity(
                            id = "fab-002",
                            userId = userId,
                            projectId = "proj-env-monitoring-station",
                            fileName = "stevenson_weather_louver.step",
                            fileUrl = null,
                            fileFormat = "STEP",
                            partDescription = "Multi-tier ambient temperature radiation shield",
                            material = "ASA",
                            color = "White",
                            quantity = 1,
                            dimensions = "120 x 120 x 160 mm",
                            tolerances = "±0.3mm",
                            finishing = "UV resistant",
                            specialRequirements = "White ASA to reflect solar radiation",
                            deliveryMethod = "CAMPUS_PICKUP",
                            status = "QUOTATION_READY",
                            quotedPrice = 780.0,
                            estimatedDays = 3,
                            adminNotes = "Approved by Lab Manager. Ready for payment.",
                            isDemo = true,
                            createdAt = System.currentTimeMillis() - 86400000L * 3,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    demoJobs.forEach { fabricationJobDao.insertJob(it) }
                    _fabricationJobs.value = demoJobs
                } else {
                    _fabricationJobs.value = fabList
                }
            }

            // 4. Load Saved Projects
            savedProjectDao.getSavedProjects(userId).collect { savedEntities ->
                val projectIds = savedEntities.map { it.projectId }
                val projects = seedProjects.filter { projectIds.contains(it.id) }
                _savedProjects.value = if (projects.isEmpty()) seedProjects.take(2) else projects
            }
        }
    }

    fun toggleMilestone(projectIndex: Int, milestoneIndex: Int) {
        val currentList = _activeProjects.value.toMutableList()
        if (projectIndex in currentList.indices) {
            val item = currentList[projectIndex]
            val milestones = item.milestones.toMutableList()
            if (milestoneIndex in milestones.indices) {
                val (label, checked) = milestones[milestoneIndex]
                milestones[milestoneIndex] = label to !checked
                val newProgress = (milestones.count { it.second } * 100) / milestones.size
                currentList[projectIndex] = item.copy(progressPercent = newProgress, milestones = milestones)
                _activeProjects.value = currentList
            }
        }
    }

    fun initiateRentalReturn(rentalId: String, code: String?) {
        viewModelScope.launch {
            _actionToast.value = "Return Request submitted for ${code ?: rentalId}! Place in Lab Drop Box #2 for QR inspection."
        }
    }

    fun clearToast() {
        _actionToast.value = null
    }
}

@Composable
fun WorkspaceScreen(
    onNavigateToProject: (String) -> Unit,
    onNavigateToOrder: (String) -> Unit = {},
    onNavigateToRental: (String) -> Unit = {},
    onNavigateToFabricationJob: (String) -> Unit = {},
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val activeProjects by viewModel.activeProjects.collectAsState()
    val rentals by viewModel.rentals.collectAsState()
    val fabJobs by viewModel.fabricationJobs.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val toastMessage by viewModel.actionToast.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Projects", "Lab Rentals", "3D Fabrication", "Saved Blueprints")

    Scaffold(
        topBar = {
            RekoraTopBar(title = "Engineering Workspace")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                when (selectedTab) {
                    0 -> ActiveProjectsView(
                        projects = activeProjects,
                        onToggleMilestone = { pIdx, mIdx -> viewModel.toggleMilestone(pIdx, mIdx) },
                        onNavigateToProject = onNavigateToProject
                    )
                    1 -> MyRentalsView(
                        rentals = rentals,
                        onInitiateReturn = { id, code -> viewModel.initiateRentalReturn(id, code) }
                    )
                    2 -> FabricationJobsView(
                        jobs = fabJobs,
                        onNavigateToJob = onNavigateToFabricationJob
                    )
                    3 -> SavedBlueprintsView(
                        savedProjects = savedProjects,
                        onNavigateToProject = onNavigateToProject
                    )
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

// ── Tab 1: Active Projects ───────────────────────────────────

@Composable
private fun ActiveProjectsView(
    projects: List<ActiveProjectItem>,
    onToggleMilestone: (Int, Int) -> Unit,
    onNavigateToProject: (String) -> Unit
) {
    if (projects.isEmpty()) {
        EmptyStateView(
            title = "No Active Projects",
            message = "Explore the engineering repository or use the Project Matcher to start a hardware project!"
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(projects.size) { pIndex ->
            val item = projects[pIndex]
            Surface(
                shape = RekoraCorners.Card,
                color = Color.White,
                border = BorderStroke(0.75.dp, RekoraColors.Hairline),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            EditorialEyebrow(text = "CURRENT SPRINT NODE")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.project.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = RekoraColors.MidnightNavy,
                                modifier = Modifier
                                    .bouncyClickable()
                                    .clickable { onNavigateToProject(item.project.id) }
                            )
                            Text(
                                text = "Estimated: ${item.project.estimatedTimeline}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RekoraColors.SecondaryText
                                )
                            )
                        }

                        LuxuryStatusCapsule(
                            label = "${item.progressPercent}% DONE",
                            dotColor = if (item.progressPercent >= 60) RekoraColors.Success else RekoraColors.EngineeringTeal,
                            containerColor = if (item.progressPercent >= 60) RekoraColors.SuccessLight else RekoraColors.TealLight
                        )
                    }

                    // Progress Track
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Sprint Milestones Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorialEyebrow(text = "HARDWARE MILESTONES", color = MaterialTheme.colorScheme.onSurfaceVariant)

                        item.milestones.forEachIndexed { mIndex, (name, checked) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable(targetScale = 0.98f) { onToggleMilestone(pIndex, mIndex) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (checked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.5.dp, if (checked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { onToggleMilestone(pIndex, mIndex) },
                                        colors = CheckboxDefaults.colors(checkedColor = RekoraColors.Success)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (checked) RekoraColors.Success else RekoraColors.MidnightNavy
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            modifier = Modifier
                                .bouncyClickable(targetScale = 0.96f) { onNavigateToProject(item.project.id) },
                            shape = RekoraCorners.ButtonSmall,
                            color = RekoraColors.MidnightNavy
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Open Blueprint",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 2: My Rentals ────────────────────────────────────────

@Composable
private fun MyRentalsView(
    rentals: List<RentalEntity>,
    onInitiateReturn: (String, String?) -> Unit
) {
    if (rentals.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Memory,
            title = "No Active Rentals",
            message = "Need microcontrollers or thrusters for your project? Borrow or rent from St. Joseph's Lab Inventory."
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(
                shape = RekoraCorners.Card,
                color = Color.White,
                border = BorderStroke(0.75.dp, RekoraColors.Hairline)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RekoraCorners.Squircle)
                            .background(RekoraColors.TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode,
                            contentDescription = null,
                            tint = RekoraColors.EngineeringTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Scan QR codes at lab stations for instant return inspection & automated deposit clearance.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RekoraColors.SecondaryText,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        items(rentals, key = { it.id }) { item ->
            val isDueSoon = item.status == "DUE_SOON"
            Surface(
                shape = RekoraCorners.Card,
                color = Color.White,
                border = BorderStroke(
                    if (isDueSoon) 1.25.dp else 0.75.dp,
                    if (isDueSoon) RekoraColors.WarningBorder else RekoraColors.Hairline
                ),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            EditorialEyebrow(text = "LAB CUSTODY ASSET")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.componentName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RekoraColors.MidnightNavy
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = RekoraColors.EngineeringTeal
                                )
                                Text(
                                    text = item.inventoryItemCode ?: "RK-ASSET",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = RekoraColors.EngineeringTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        LuxuryStatusCapsule(
                            label = item.status.replace("_", " "),
                            dotColor = if (isDueSoon) RekoraColors.Warning else RekoraColors.Success,
                            containerColor = if (isDueSoon) RekoraColors.WarningLight else RekoraColors.SuccessLight
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = RekoraColors.Hairline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            EditorialEyebrow(text = "RETURN DUE", color = RekoraColors.SecondaryText)
                            val endDateStr = item.endDate?.let {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "In 7 Days"
                            Text(
                                text = endDateStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RekoraColors.MidnightNavy
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            EditorialEyebrow(text = "SECURITY DEPOSIT", color = RekoraColors.SecondaryText)
                            Text(
                                text = "₹${item.securityDeposit.toInt()} (Refundable)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RekoraColors.Success
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable(targetScale = 0.98f) {
                                onInitiateReturn(item.id, item.inventoryItemCode)
                            },
                        shape = RekoraCorners.ButtonSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AssignmentReturn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RekoraColors.MidnightNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Initiate Return Pass",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RekoraColors.MidnightNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 3: Fabrication Jobs ──────────────────────────────────

@Composable
private fun FabricationJobsView(
    jobs: List<FabricationJobEntity>,
    onNavigateToJob: (String) -> Unit
) {
    if (jobs.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Print,
            title = "No Fabrication Jobs",
            message = "Upload STL/STEP 3D models to request printing at the St. Joseph's Makerspace Lab."
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(jobs, key = { it.id }) { job ->
            Surface(
                shape = RekoraCorners.Card,
                color = Color.White,
                border = BorderStroke(0.75.dp, RekoraColors.Hairline),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            EditorialEyebrow(text = "RAPID PROTOTYPING TICKET")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = job.fileName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RekoraColors.MidnightNavy
                            )
                            Text(
                                text = job.partDescription,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RekoraColors.SecondaryText
                                )
                            )
                        }

                        LuxuryStatusCapsule(
                            label = job.status.replace("_", " "),
                            dotColor = when (job.status) {
                                "IN_PRODUCTION" -> RekoraColors.ElectricCyan
                                "READY_FOR_PICKUP" -> RekoraColors.Success
                                "QUOTATION_READY" -> RekoraColors.Warning
                                else -> RekoraColors.SecondaryText
                            },
                            containerColor = when (job.status) {
                                "IN_PRODUCTION" -> MaterialTheme.colorScheme.secondaryContainer
                                "READY_FOR_PICKUP" -> RekoraColors.SuccessLight
                                "QUOTATION_READY" -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    }

                    // Stepper pipeline
                    FabricationStatusPipeline(status = job.status)

                    // Job specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Material: ${job.material} (${job.color})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "Quoted: ₹${job.quotedPrice?.toInt() ?: 0}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (!job.adminNotes.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "Station: ${job.adminNotes}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = RekoraColors.MidnightNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FabricationStatusPipeline(status: String) {
    val steps = listOf("Submitted", "Quoted", "In Production", "Ready")
    val currentIndex = when (status) {
        "SUBMITTED" -> 0
        "QUOTATION_READY" -> 1
        "IN_PRODUCTION" -> 2
        "READY_FOR_PICKUP" -> 3
        else -> 1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isPassed = index <= currentIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (isPassed) RekoraColors.EngineeringTeal else Color(0xFFE2E8EE),
                    modifier = Modifier.size(16.dp)
                ) {
                    if (isPassed) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isPassed) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isPassed) RekoraColors.EngineeringTeal else RekoraColors.SecondaryText
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentIndex) RekoraColors.EngineeringTeal else Color(0xFFE2E8EE))
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// ── Tab 4: Saved Blueprints & Projects ───────────────────────

@Composable
private fun SavedBlueprintsView(
    savedProjects: List<ProjectEntity>,
    onNavigateToProject: (String) -> Unit
) {
    if (savedProjects.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.BookmarkBorder,
            title = "No Bookmarked Blueprints",
            message = "Save projects from the Explore screen or AI Consultations to keep them here."
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(savedProjects, key = { it.id }) { project ->
            ProjectCard(
                title = project.title,
                description = project.shortDescription,
                domain = project.domain,
                difficulty = project.difficulty,
                estimatedCost = "₹${project.estimatedCostMin.toInt()} - ₹${project.estimatedCostMax.toInt()}",
                estimatedTime = project.estimatedTimeline,
                matchScore = 96,
                isSaved = true,
                isDemo = project.isDemo,
                onClick = { onNavigateToProject(project.id) },
                onSaveClick = { /* unsave */ }
            )
        }
    }
}
