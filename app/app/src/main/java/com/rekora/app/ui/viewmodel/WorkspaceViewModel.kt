package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.*
import com.rekora.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the central engineering workspace aggregating physical and digital project assets.
 */
data class WorkspaceUiState(
    val activeProjects: List<Project> = emptyList(),
    val rentedComponents: List<Rental> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val fabricationJobs: List<FabricationJob> = emptyList(),
    val blueprintConversations: List<AiConversation> = emptyList(),
    val savedProjects: List<Project> = emptyList(),
    val selectedTab: Int = 0, // 0: Overview, 1: Projects, 2: Rentals, 3: Fab Jobs, 4: Orders
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * WorkspaceViewModel serves as the central operational cockpit for student engineers:
 * - Active project builds and repository entitlements
 * - Rented hardware tracking with due dates, inspection notes, and deposit status
 * - Order logistics and delivery tracking
 * - 3D print and fabrication production queue monitoring
 * - AI Generated blueprints and saved project bookmarks
 */
@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val rentalRepository: RentalRepository,
    private val orderRepository: OrderRepository,
    private val fabricationRepository: FabricationRepository,
    private val aiRepository: AiRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkspaceUiState> = authRepository.currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "guest-student"

        combine(
            projectRepository.getAllProjects(),
            projectRepository.getSavedProjects(userId),
            rentalRepository.getRentalsForUser(userId),
            orderRepository.getOrdersForUser(userId),
            fabricationRepository.getJobsForUser(userId),
            aiRepository.getConversationsForUser(userId),
            _selectedTab,
            _isRefreshing,
            _error
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val allProjects = args[0] as List<Project>
            val savedProjs = args[1] as List<Project>
            val rentals = args[2] as List<Rental>
            val orders = args[3] as List<Order>
            val fabJobs = args[4] as List<FabricationJob>
            val aiConvs = args[5] as List<AiConversation>
            val tab = args[6] as Int
            val refreshing = args[7] as Boolean
            val err = args[8] as String?

            // Filter projects where repository has been unlocked or saved
            val activeBuilds = allProjects.filter { !it.repositoryLocked }
            val blueprints = aiConvs.filter { it.hasBlueprint }

            WorkspaceUiState(
                activeProjects = activeBuilds,
                rentedComponents = rentals,
                activeOrders = orders,
                fabricationJobs = fabJobs,
                blueprintConversations = blueprints,
                savedProjects = savedProjs,
                selectedTab = tab,
                isLoading = refreshing,
                error = err
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkspaceUiState(isLoading = true)
    )

    fun selectTab(index: Int) {
        _selectedTab.value = index.coerceIn(0, 4)
    }

    fun unsaveProject(projectId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "guest-student"
            projectRepository.toggleSaveProject(userId, projectId)
        }
    }

    fun refreshWorkspace() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            kotlinx.coroutines.delay(400)
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
