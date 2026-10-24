package com.rekora.app.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ProjectDao
import com.rekora.app.data.local.dao.SavedProjectDao
import com.rekora.app.data.local.entity.ProjectEntity
import com.rekora.app.data.local.entity.SavedProjectEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExploreSortOption(val label: String) {
    RELEVANCE("Relevance"),
    COST_LOW_TO_HIGH("Lowest Budget"),
    COST_HIGH_TO_LOW("Highest Budget"),
    NEWEST("Newest First")
}

private data class ExploreFilters(
    val query: String,
    val domain: String?,
    val difficulty: String?,
    val budgetMax: Double?,
    val sort: ExploreSortOption
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val cartDao: CartDao,
    private val savedProjectDao: SavedProjectDao,
    private val prefs: RekoraPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDomain = MutableStateFlow<String?>(null)
    val selectedDomain = _selectedDomain.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<String?>(null)
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    private val _selectedBudgetMax = MutableStateFlow<Double?>(null)
    val selectedBudgetMax = _selectedBudgetMax.asStateFlow()

    private val _sortOption = MutableStateFlow(ExploreSortOption.RELEVANCE)
    val sortOption = _sortOption.asStateFlow()

    val cartCount: StateFlow<Int> = cartDao.getCartCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val savedProjectIds: StateFlow<Set<String>> = prefs.userId
        .flatMapLatest { userId ->
            savedProjectDao.getSavedProjects(userId ?: "usr-demo-student-01")
        }
        .map { list -> list.map { it.projectId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allProjects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
        .map { list ->
            if (list.isNotEmpty()) list else SeedDataProvider.getSeedProjects()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedDataProvider.getSeedProjects())

    private val filtersFlow = combine(
        combine(_searchQuery, _selectedDomain, _selectedDifficulty) { q, d, diff ->
            Triple(q, d, diff)
        },
        combine(_selectedBudgetMax, _sortOption) { b, s ->
            b to s
        }
    ) { (q, d, diff), (b, s) ->
        ExploreFilters(q, d, diff, b, s)
    }

    val filteredProjects: StateFlow<List<ProjectEntity>> = combine(allProjects, filtersFlow) { projects, filters ->
        var result = projects
        if (filters.query.isNotBlank()) {
            val q = filters.query.trim().lowercase()
            result = result.filter { p ->
                p.title.lowercase().contains(q) ||
                p.shortDescription.lowercase().contains(q) ||
                p.domain.lowercase().contains(q) ||
                p.tags.lowercase().contains(q) ||
                p.technologies.lowercase().contains(q)
            }
        }
        if (!filters.domain.isNullOrBlank() && filters.domain != "ALL") {
            result = result.filter { it.domain.equals(filters.domain, ignoreCase = true) }
        }
        if (!filters.difficulty.isNullOrBlank() && filters.difficulty != "ALL") {
            result = result.filter { it.difficulty.equals(filters.difficulty, ignoreCase = true) }
        }
        if (filters.budgetMax != null) {
            result = result.filter { it.estimatedCostMin <= filters.budgetMax }
        }

        when (filters.sort) {
            ExploreSortOption.COST_LOW_TO_HIGH -> result.sortedBy { it.estimatedCostMin }
            ExploreSortOption.COST_HIGH_TO_LOW -> result.sortedByDescending { it.estimatedCostMax }
            ExploreSortOption.NEWEST -> result.sortedByDescending { it.createdAt }
            ExploreSortOption.RELEVANCE -> result
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            projectDao.getAllProjects().first().let { current ->
                if (current.isEmpty()) {
                    projectDao.insertProjects(SeedDataProvider.getSeedProjects())
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectDomain(domain: String?) {
        _selectedDomain.value = if (_selectedDomain.value == domain) null else domain
    }

    fun selectDifficulty(difficulty: String?) {
        _selectedDifficulty.value = if (_selectedDifficulty.value == difficulty) null else difficulty
    }

    fun selectBudgetMax(budget: Double?) {
        _selectedBudgetMax.value = if (_selectedBudgetMax.value == budget) null else budget
    }

    fun setSortOption(sort: ExploreSortOption) {
        _sortOption.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedDomain.value = null
        _selectedDifficulty.value = null
        _selectedBudgetMax.value = null
        _sortOption.value = ExploreSortOption.RELEVANCE
    }

    fun toggleSave(projectId: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: "usr-demo-student-01"
            if (savedProjectIds.value.contains(projectId)) {
                savedProjectDao.unsaveProject(userId, projectId)
            } else {
                savedProjectDao.saveProject(
                    SavedProjectEntity(
                        id = "saved-${System.currentTimeMillis()}",
                        userId = userId,
                        projectId = projectId,
                        savedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}

/**
 * REKORA Dedicated Explore Screen — Google Play Inspired Content Discovery
 * Prominent search, quick suggestions, domain tabs, structured collections,
 * polished filter bottom sheet, and varied card layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateToProject: (String) -> Unit,
    onNavigateToProjectMatcher: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val selectedBudgetMax by viewModel.selectedBudgetMax.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val projects by viewModel.filteredProjects.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val savedIds by viewModel.savedProjectIds.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val activeFilterCount = (if (selectedDomain != null) 1 else 0) +
            (if (selectedDifficulty != null) 1 else 0) +
            (if (selectedBudgetMax != null) 1 else 0) +
            (if (sortOption != ExploreSortOption.RELEVANCE) 1 else 0)

    val isSearchOrFilterActive = searchQuery.isNotBlank() ||
            selectedDomain != null ||
            selectedDifficulty != null ||
            selectedBudgetMax != null

    val domains = listOf(
        "ALL" to "All Domains",
        "ROBOTICS" to "Robotics",
        "IOT" to "IoT & Embedded",
        "AI_ML" to "AI & ML",
        "RENEWABLE_ENERGY" to "Renewable Energy",
        "MECHANICAL" to "Mechanical",
        "ELECTRONICS" to "Electronics"
    )

    val searchSuggestions = listOf(
        "Autonomous Rover",
        "ESP32 LoRa Sensor",
        "Under ₹5,000",
        "Solar Tracking MPPT",
        "Mechanical Gripper CAD",
        "Edge TPU AI"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RekoraTopBar(
                title = "Explore Blueprints",
                cartCount = cartCount,
                onCartClick = onNavigateToCart
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Prominent Search Bar with Integrated Filter Trigger
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RekoraSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = "Search blueprints, components, sensors...",
                        modifier = Modifier.weight(1f)
                    )

                    // Filter Action Button with Active Badge
                    Surface(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RekoraCorners.SearchBar,
                                ambientColor = Color(0x06000000),
                                spotColor = Color(0x0A000000)
                            )
                            .hairlineBorder(width = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RekoraCorners.SearchBar),
                        shape = RekoraCorners.SearchBar,
                        color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Filters",
                                tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            if (activeFilterCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(16.dp),
                                    shape = CircleShape,
                                    color = RekoraColors.EngineeringTeal
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Google Play-style Domain Navigation Rail
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(domains) { (key, label) ->
                        val isSelected = (selectedDomain == null && key == "ALL") || selectedDomain == key
                        Surface(
                            onClick = { viewModel.selectDomain(if (key == "ALL") null else key) },
                            shape = RekoraCorners.Pill,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 0.75.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. Search Suggestions Rail (Shown when no search query is typed)
            if (searchQuery.isBlank() && !isSearchOrFilterActive) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SUGGESTED DISCOVERY",
                            modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = RekoraColors.SecondaryText
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchSuggestions) { suggestion ->
                                Surface(
                                    onClick = { viewModel.onSearchQueryChange(suggestion) },
                                    shape = RekoraCorners.Pill,
                                    color = RekoraColors.NavyLight,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RekoraColors.Border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = RekoraColors.SecondaryText
                                        )
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Guided Project Matcher Card
            if (!isSearchOrFilterActive) {
                item {
                    Box(modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)) {
                        ProjectMatcherPromoCard(onClick = onNavigateToProjectMatcher)
                    }
                }
            }

            // 5. CONTENT AREA: Either Structured Collections OR Filtered Search Results
            if (isSearchOrFilterActive) {
                // ACTIVE FILTER / SEARCH RESULTS MODE
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = RekoraDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${projects.size} Engineering ${if (projects.size == 1) "Blueprint" else "Blueprints"}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Reset All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.EngineeringTeal
                            ),
                            modifier = Modifier
                                .bouncyClickable { viewModel.clearFilters() }
                                .padding(4.dp)
                        )
                    }
                }

                if (projects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.ScreenPadding)
                        ) {
                            EmptyStateView(
                                icon = Icons.Outlined.SearchOff,
                                title = "No Blueprints Match Your Query",
                                message = "Try clearing specific constraints or searching for broader terms like 'sensor', 'robot', or 'motor'.",
                                actionText = "Reset All Filters",
                                onAction = { viewModel.clearFilters() }
                            )
                        }
                    }
                } else {
                    items(projects, key = { it.id }) { project ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = RekoraDimens.ScreenPadding)
                        ) {
                            CompactProjectRow(
                                project = project,
                                onClick = { onNavigateToProject(project.id) }
                            )
                        }
                    }
                }
            } else {
                // GOOGLE PLAY-INSPIRED STRUCTURED COLLECTIONS

                // Collection 1: Featured Staff & Lab Recommendations
                if (allProjects.isNotEmpty()) {
                    val featured = allProjects.first()
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SectionHeader(
                                title = "Staff & Lab Recommendations",
                                actionText = null,
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                            )
                            Box(modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)) {
                                EditorialLeadProjectCard(
                                    project = featured,
                                    onClick = { onNavigateToProject(featured.id) }
                                )
                            }
                        }
                    }
                }

                // Collection 2: Under ₹5,000 High-Impact Prototypes (Compact Rows)
                val budgetProjects = allProjects.filter { it.estimatedCostMin <= 5000 }
                if (budgetProjects.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SectionHeader(
                                title = "Under ₹5,000 Student Grants",
                                actionText = "See All ›",
                                onAction = { viewModel.selectBudgetMax(5000.0) },
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                            )
                            Column(
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                budgetProjects.take(3).forEach { proj ->
                                    CompactProjectRow(
                                        project = proj,
                                        onClick = { onNavigateToProject(proj.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Collection 3: Autonomous Systems & Advanced AI
                val advancedProjects = allProjects.filter {
                    it.domain.contains("ROBOT", ignoreCase = true) ||
                    it.domain.contains("AI", ignoreCase = true)
                }
                if (advancedProjects.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionHeader(
                                title = "Autonomous & Edge AI Blueprints",
                                actionText = null,
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = RekoraDimens.ScreenPadding),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(advancedProjects, key = { "adv-${it.id}" }) { proj ->
                                    val isSaved = savedIds.contains(proj.id)
                                    val techList = remember(proj.technologies) {
                                        proj.technologies
                                            .removeSurrounding("[", "]")
                                            .split(",")
                                            .map { it.trim().removeSurrounding("\"") }
                                            .filter { it.isNotBlank() }
                                    }
                                    Box(modifier = Modifier.width(300.dp)) {
                                        ProjectCard(
                                            title = proj.title,
                                            description = proj.shortDescription,
                                            domain = proj.domain.replace("_", " "),
                                            difficulty = proj.difficulty,
                                            estimatedCost = "₹${proj.estimatedCostMin.toInt()} - ₹${proj.estimatedCostMax.toInt()}",
                                            estimatedTime = proj.estimatedTimeline,
                                            technologies = techList,
                                            matchScore = 95,
                                            isSaved = isSaved,
                                            isDemo = proj.isDemo,
                                            onClick = { onNavigateToProject(proj.id) },
                                            onSaveClick = { viewModel.toggleSave(proj.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Collection 4: Beginner Hardware Foundations
                val beginnerProjects = allProjects.filter { it.difficulty.equals("BEGINNER", ignoreCase = true) }
                if (beginnerProjects.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SectionHeader(
                                title = "Hardware Foundations for Beginners",
                                actionText = "See All ›",
                                onAction = { viewModel.selectDifficulty("BEGINNER") },
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding)
                            )
                            Column(
                                modifier = Modifier.padding(horizontal = RekoraDimens.ScreenPadding),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                beginnerProjects.forEach { proj ->
                                    CompactProjectRow(
                                        project = proj,
                                        onClick = { onNavigateToProject(proj.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Google Play Structured Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RekoraCorners.BottomSheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Blueprints",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.SecondaryText
                            )
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = RekoraColors.Border)

                // Budget constraint
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "MAXIMUM BUDGET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = RekoraColors.SecondaryText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            null to "Any",
                            5000.0 to "< ₹5,000",
                            10000.0 to "< ₹10,000",
                            20000.0 to "< ₹20,000"
                        ).forEach { (amount, label) ->
                            val isSelected = selectedBudgetMax == amount
                            Surface(
                                onClick = { viewModel.selectBudgetMax(amount) },
                                modifier = Modifier.weight(1f),
                                shape = RekoraCorners.Tag,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Difficulty constraint
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DIFFICULTY LEVEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = RekoraColors.SecondaryText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            null to "All Levels",
                            "BEGINNER" to "Beginner",
                            "INTERMEDIATE" to "Inter.",
                            "ADVANCED" to "Advanced"
                        ).forEach { (diff, label) ->
                            val isSelected = selectedDifficulty == diff
                            Surface(
                                onClick = { viewModel.selectDifficulty(diff) },
                                modifier = Modifier.weight(1f),
                                shape = RekoraCorners.Tag,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Sorting constraint
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SORT BLUEPRINTS BY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = RekoraColors.SecondaryText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExploreSortOption.entries.forEach { option ->
                            val isSelected = sortOption == option
                            Surface(
                                onClick = { viewModel.setSortOption(option) },
                                modifier = Modifier.weight(1f),
                                shape = RekoraCorners.Tag,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Apply button
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RekoraCorners.Button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Show Blueprints (${projects.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Guided Project Matcher Card
 */
@Composable
fun ProjectMatcherPromoCard(
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
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0C000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.Card),
        shape = RekoraCorners.Card,
        color = RekoraColors.MidnightNavy
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RekoraCorners.Tag,
                    color = RekoraColors.EngineeringTeal
                ) {
                    Text(
                        text = "GUIDED MATCHER",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = Color.White
                    )
                }

                Text(
                    text = "Not sure where to begin?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Match your budget, owned hardware components, and timeline with validated blueprints.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = RekoraColors.EngineeringTeal
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Match",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(16.dp)
                )
            }
        }
    }
}
