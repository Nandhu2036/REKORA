package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.ProjectRepository
import com.rekora.app.domain.model.Difficulty
import com.rekora.app.domain.model.EngineeringDomain
import com.rekora.app.domain.model.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the Project Catalog exploration screen.
 */
data class ExploreUiState(
    val searchQuery: String = "",
    val selectedDomain: EngineeringDomain? = null,
    val selectedDifficulty: Difficulty? = null,
    val maxBudget: Double? = null,
    val filteredProjects: List<Project> = emptyList(),
    val totalAvailableCount: Int = 0,
    val isLoading: Boolean = false,
    val activeFilterCount: Int = 0
)

/**
 * ExploreViewModel delivers dynamic multi-faceted searching and filtering for the REKORA catalog:
 * - Query text matching across title, descriptions, tags, and technologies
 * - Engineering domain filtering (Robotics, IoT, AI/ML, Renewable Energy, etc.)
 * - Difficulty classification (Beginner to Expert)
 * - Maximum budget constraint threshold
 * - Unidirectional state updates via StateFlow
 */
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDomain = MutableStateFlow<EngineeringDomain?>(null)
    val selectedDomain: StateFlow<EngineeringDomain?> = _selectedDomain.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _maxBudget = MutableStateFlow<Double?>(null)
    val maxBudget: StateFlow<Double?> = _maxBudget.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val filteredProjects: StateFlow<List<Project>> = combine(
        projectRepository.getAllProjects(),
        _searchQuery,
        _selectedDomain,
        _selectedDifficulty,
        _maxBudget
    ) { projects, query, domain, difficulty, budget ->
        var list = projects

        // Filter by text search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { proj ->
                proj.title.lowercase().contains(q) ||
                proj.shortDescription.lowercase().contains(q) ||
                proj.technologies.any { it.lowercase().contains(q) } ||
                proj.tags.any { it.lowercase().contains(q) } ||
                proj.domain.displayName.lowercase().contains(q)
            }
        }

        // Filter by engineering domain
        if (domain != null) {
            list = list.filter { it.domain == domain }
        }

        // Filter by difficulty tier
        if (difficulty != null) {
            list = list.filter { it.difficulty == difficulty }
        }

        // Filter by maximum budget ceiling
        if (budget != null && budget > 0.0) {
            list = list.filter { it.estimatedCost.min <= budget }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<ExploreUiState> = combine(
        filteredProjects,
        _searchQuery,
        _selectedDomain,
        _selectedDifficulty,
        _maxBudget,
        _isLoading
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val filtered = args[0] as List<Project>
        val query = args[1] as String
        val domain = args[2] as? EngineeringDomain
        val difficulty = args[3] as? Difficulty
        val budget = args[4] as? Double
        val loading = args[5] as Boolean

        var filterCount = 0
        if (query.isNotBlank()) filterCount++
        if (domain != null) filterCount++
        if (difficulty != null) filterCount++
        if (budget != null) filterCount++

        ExploreUiState(
            searchQuery = query,
            selectedDomain = domain,
            selectedDifficulty = difficulty,
            maxBudget = budget,
            filteredProjects = filtered,
            totalAvailableCount = filtered.size,
            isLoading = loading,
            activeFilterCount = filterCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDomainSelect(domain: EngineeringDomain?) {
        _selectedDomain.value = if (_selectedDomain.value == domain) null else domain
    }

    fun onDifficultySelect(difficulty: Difficulty?) {
        _selectedDifficulty.value = if (_selectedDifficulty.value == difficulty) null else difficulty
    }

    fun onBudgetChange(budget: Double?) {
        _maxBudget.value = if (budget != null && budget > 0.0) budget else null
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedDomain.value = null
        _selectedDifficulty.value = null
        _maxBudget.value = null
    }

    fun toggleSaveProject(projectId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "guest-student"
            projectRepository.toggleSaveProject(userId, projectId)
        }
    }
}
