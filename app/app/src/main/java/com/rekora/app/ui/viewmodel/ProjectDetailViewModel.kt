package com.rekora.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.ProjectRepository
import com.rekora.app.domain.model.BomItem
import com.rekora.app.domain.model.Project
import com.rekora.app.domain.model.ProjectVersion
import com.rekora.app.domain.model.SourcingChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for Project Detail screen representing preview, BOM, CAD, and evolution DNA.
 */
data class ProjectDetailUiState(
    val project: Project? = null,
    val bomItems: List<BomItem> = emptyList(),
    val versions: List<ProjectVersion> = emptyList(),
    val isSaved: Boolean = false,
    val isUnlocked: Boolean = false,
    val activeTab: Int = 0, // 0: Overview, 1: BOM, 2: CAD, 3: Build/Test, 4: Versions
    val isLoading: Boolean = true,
    val isUnlocking: Boolean = false,
    val error: String? = null,
    val outOfPocketCost: Double = 0.0,
    val totalOriginalCost: Double = 0.0,
    val studentSavings: Double = 0.0
)

/**
 * ProjectDetailViewModel coordinates the complete engineering project dossier:
 * - Preview vs Unlocked repository state
 * - Itemized Bill of Materials with Buy/Rent/Reuse status
 * - CAD model listings and physical fabrication indicators
 * - Persistent project version timeline (Project DNA)
 * - Saved bookmark status
 */
@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle["projectId"]) {
        "projectId parameter must be supplied to ProjectDetailViewModel"
    }

    private val _activeTab = MutableStateFlow(0)
    private val _isUnlocking = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val projectFlow: Flow<Project?> = projectRepository.getProjectById(projectId)
    private val bomFlow: Flow<List<BomItem>> = projectRepository.getBomForProject(projectId)
    private val versionsFlow: Flow<List<ProjectVersion>> = projectRepository.getVersionsForProject(projectId)

    private val isSavedFlow: Flow<Boolean> = authRepository.currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "guest-student"
        projectRepository.isProjectSaved(userId, projectId)
    }

    val uiState: StateFlow<ProjectDetailUiState> = combine(
        projectFlow,
        bomFlow,
        versionsFlow,
        isSavedFlow,
        _activeTab,
        _isUnlocking,
        _errorMessage
    ) { args ->
        val project = args[0] as Project?
        @Suppress("UNCHECKED_CAST")
        val bom = args[1] as List<BomItem>
        @Suppress("UNCHECKED_CAST")
        val versions = args[2] as List<ProjectVersion>
        val isSaved = args[3] as Boolean
        val tab = args[4] as Int
        val unlocking = args[5] as Boolean
        val error = args[6] as String?

        val isUnlocked = project?.let { !it.repositoryLocked } ?: false

        val totalCost = bom.sumOf { it.unitPrice * it.quantity }
        val outOfPocket = bom
            .filter { it.sourcingChoice != com.rekora.app.domain.model.SourcingChoice.USE_EXISTING }
            .sumOf { it.unitPrice * it.quantity }
        val savings = (totalCost - outOfPocket).coerceAtLeast(0.0)

        ProjectDetailUiState(
            project = project,
            bomItems = bom,
            versions = versions,
            isSaved = isSaved,
            isUnlocked = isUnlocked,
            activeTab = tab,
            isLoading = project == null,
            isUnlocking = unlocking,
            error = error,
            totalOriginalCost = totalCost,
            outOfPocketCost = outOfPocket,
            studentSavings = savings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectDetailUiState(isLoading = true)
    )

    fun selectTab(index: Int) {
        _activeTab.value = index.coerceIn(0, 4)
    }

    fun toggleSave() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "guest-student"
            try {
                projectRepository.toggleSaveProject(userId, projectId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to bookmark project: ${e.localizedMessage}"
            }
        }
    }

    fun unlockRepository() {
        viewModelScope.launch {
            _isUnlocking.value = true
            _errorMessage.value = null
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "guest-student"

            try {
                projectRepository.unlockRepository(userId, projectId)
                _isUnlocking.value = false
            } catch (e: Exception) {
                _isUnlocking.value = false
                _errorMessage.value = "Failed to unlock repository: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
