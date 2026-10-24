package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.ProjectRepository
import com.rekora.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the Guided Project Matcher wizard.
 */
data class ProjectMatcherUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 5,
    val currentCriteria: MatchCriteria = MatchCriteria(
        maxBudget = 10000.0,
        timelineWeeks = 6,
        difficulty = Difficulty.INTERMEDIATE
    ),
    val matches: List<ProjectMatch> = emptyList(),
    val isMatching: Boolean = false,
    val hasExecuted: Boolean = false,
    val newComponentInput: String = ""
)

/**
 * ProjectMatcherViewModel implements the multi-criteria matching algorithm from the REKORA proposal:
 * - Constraint-aware ranking (Budget, Owned hardware reuse, Timeline, Domain, Skill level)
 * - Explainable matching score (0-100%) with transparent justification bullet points
 * - Multi-step guided wizard (1 to 5)
 */
@HiltViewModel
class ProjectMatcherViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectMatcherUiState())
    val uiState: StateFlow<ProjectMatcherUiState> = _uiState.asStateFlow()

    val currentCriteria: StateFlow<MatchCriteria> = _uiState.map { it.currentCriteria }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _uiState.value.currentCriteria
        )

    val matches: StateFlow<List<ProjectMatch>> = _uiState.map { it.matches }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isMatching: StateFlow<Boolean> = _uiState.map { it.isMatching }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun onProblemStatementChange(text: String) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(problemStatement = text))
        }
    }

    fun onMaxBudgetChange(budget: Double) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(maxBudget = budget))
        }
    }

    fun onTimelineWeeksChange(weeks: Int) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(timelineWeeks = weeks))
        }
    }

    fun onDomainSelect(domain: EngineeringDomain?) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(preferredDomain = domain))
        }
    }

    fun onDifficultySelect(difficulty: Difficulty?) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(difficulty = difficulty))
        }
    }

    fun onFabricationAccessChange(hasAccess: Boolean) {
        _uiState.update {
            it.copy(currentCriteria = it.currentCriteria.copy(hasFabricationAccess = hasAccess))
        }
    }

    fun onNewComponentInputChange(input: String) {
        _uiState.update { it.copy(newComponentInput = input) }
    }

    fun addOwnedComponent(componentName: String? = null) {
        val name = componentName ?: _uiState.value.newComponentInput
        if (name.isBlank()) return
        val currentList = _uiState.value.currentCriteria.ownedComponents
        if (!currentList.any { it.equals(name.trim(), ignoreCase = true) }) {
            _uiState.update {
                it.copy(
                    currentCriteria = it.currentCriteria.copy(
                        ownedComponents = currentList + name.trim()
                    ),
                    newComponentInput = ""
                )
            }
        }
    }

    fun removeOwnedComponent(componentName: String) {
        _uiState.update {
            it.copy(
                currentCriteria = it.currentCriteria.copy(
                    ownedComponents = it.currentCriteria.ownedComponents.filterNot { item ->
                        item.equals(componentName, ignoreCase = true)
                    }
                )
            )
        }
    }

    fun nextStep() {
        if (_uiState.value.currentStep < _uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        } else {
            executeMatch()
        }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun reset() {
        _uiState.value = ProjectMatcherUiState()
    }

    fun executeMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMatching = true) }
            delay(500) // Realistic computation delay

            val allProjects = projectRepository.getAllProjects().first()
            val criteria = _uiState.value.currentCriteria

            val results = allProjects.map { project ->
                calculateProjectMatch(project, criteria)
            }.sortedByDescending { it.matchScore }

            _uiState.update {
                it.copy(
                    matches = results,
                    isMatching = false,
                    hasExecuted = true
                )
            }
        }
    }

    /**
     * Calibrated matching algorithm with explainable justification points:
     * - Budget fit: 30% weight
     * - Domain alignment: 25% weight
     * - Timeline fit: 20% weight
     * - Component overlap: 15% weight
     * - Difficulty match: 10% weight
     * - Problem keyword relevance: bonus up to 10%
     */
    private fun calculateProjectMatch(project: Project, criteria: MatchCriteria): ProjectMatch {
        var score = 0
        val reasons = mutableListOf<String>()

        // 1. Budget fit (30% weight)
        val budgetFit = criteria.maxBudget <= 0.0 || project.estimatedCost.min <= criteria.maxBudget
        if (budgetFit) {
            score += 30
            val budgetDisplay = if (criteria.maxBudget > 0) "₹${criteria.maxBudget.toInt()}" else "unconstrained"
            reasons.add("Fits within your budget ($budgetDisplay)")
        } else {
            val delta = (project.estimatedCost.min - criteria.maxBudget).toInt()
            reasons.add("Estimated baseline cost is ₹$delta above specified budget ceiling")
        }

        // 2. Domain alignment (25% weight)
        val domainFit = criteria.preferredDomain == null || criteria.preferredDomain == project.domain
        if (domainFit) {
            score += 25
            if (criteria.preferredDomain != null) {
                reasons.add("Directly aligned with preferred domain: ${project.domain.displayName}")
            } else {
                reasons.add("Domain matches: ${project.domain.displayName}")
            }
        }

        // 3. Timeline feasibility (20% weight)
        val timelineFit = project.timelineWeeks <= criteria.timelineWeeks
        if (timelineFit) {
            score += 20
            reasons.add("Feasible within your ${criteria.timelineWeeks}-week development window")
        } else {
            reasons.add("Typically requires ${project.timelineWeeks} weeks (exceeds your ${criteria.timelineWeeks}-week timeline)")
        }

        // 4. Component overlap (15% weight)
        val matchingComponents = criteria.ownedComponents.filter { owned ->
            project.hardwareRequirements.any { req -> req.contains(owned, ignoreCase = true) }
        }
        val ownedOverlap = matchingComponents.size
        if (ownedOverlap > 0) {
            val componentPoints = (ownedOverlap * 5).coerceAtMost(15)
            score += componentPoints
            val matchedNames = matchingComponents.joinToString(", ")
            reasons.add("Reuses $ownedOverlap of your owned components: ($matchedNames)")
        }

        // 5. Difficulty alignment (10% weight)
        val skillFit = criteria.difficulty == null || criteria.difficulty == project.difficulty
        if (skillFit) {
            score += 10
            reasons.add("Matches your ${project.difficulty.displayName} skill level")
        }

        // 6. Problem statement keyword alignment (Bonus up to 10 points)
        if (criteria.problemStatement.isNotBlank()) {
            val keywords = criteria.problemStatement.lowercase().split(" ", ",", ";").filter { it.length > 3 }
            val matchedKeywords = keywords.filter { kw ->
                project.title.lowercase().contains(kw) ||
                project.shortDescription.lowercase().contains(kw) ||
                project.technologies.any { it.lowercase().contains(kw) } ||
                project.tags.any { it.lowercase().contains(kw) }
            }
            if (matchedKeywords.isNotEmpty()) {
                val bonus = (matchedKeywords.size * 3).coerceAtMost(10)
                score += bonus
                reasons.add("Matches inquiry keywords: ${matchedKeywords.take(3).joinToString(", ")}")
            }
        }

        // 7. Fabrication access check
        if (project.hasFabricationNeeds && !criteria.hasFabricationAccess) {
            reasons.add("Requires 3D printing / CNC fabrication (Campus Maker Lab available)")
        }

        return ProjectMatch(
            project = project,
            matchScore = score.coerceIn(0, 100),
            matchReasons = reasons,
            budgetFit = budgetFit,
            timelineFit = timelineFit,
            skillFit = skillFit,
            componentOverlap = ownedOverlap
        )
    }
}
