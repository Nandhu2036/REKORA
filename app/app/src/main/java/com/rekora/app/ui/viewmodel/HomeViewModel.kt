package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.DatabaseSeeder
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.CartRepository
import com.rekora.app.domain.repository.ComponentRepository
import com.rekora.app.domain.repository.ProjectRepository
import com.rekora.app.domain.model.Component
import com.rekora.app.domain.model.Project
import com.rekora.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the REKORA Home dashboard.
 */
data class HomeUiState(
    val userName: String = "Engineering Student",
    val userRole: String = "STUDENT",
    val cartCount: Int = 0,
    val recommendedProjects: List<Project> = emptyList(),
    val rentableComponents: List<Component> = emptyList(),
    val recentAnnouncements: List<String> = emptyList(),
    val savedProjectsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * HomeViewModel coordinates the primary landing hub:
 * - Ensures local Room database is seeded with verified demonstration projects
 * - Streams user profile, live cart count, recommended hardware projects, and rentable components
 * - Provides unidirectional state flow for project bookmarks and pull-to-refresh
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val componentRepository: ComponentRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val databaseSeeder: DatabaseSeeder
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val defaultAnnouncements = listOf(
        "Campus Maker Lab: 3D printing queue currently open with 48h turnaround for capstone submissions.",
        "Component Pool: High-demand Jetson Nano and LiDAR modules available for 2-week semester rentals.",
        "St. Joseph's Engineering Expo: Prototype verification deadline set for November 15th."
    )

    private val _announcements = MutableStateFlow(defaultAnnouncements)

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        cartRepository.getCartCount(),
        projectRepository.getAllProjects(),
        componentRepository.getRentableComponents(),
        _isRefreshing,
        _error,
        _announcements
    ) { args: Array<Any?> ->
        val user = args[0] as? User
        val cartCount = args[1] as Int
        @Suppress("UNCHECKED_CAST")
        val projects = args[2] as List<Project>
        @Suppress("UNCHECKED_CAST")
        val components = args[3] as List<Component>
        val isRefreshing = args[4] as Boolean
        val error = args[5] as String?
        @Suppress("UNCHECKED_CAST")
        val announcements = args[6] as List<String>

        val savedCount = projects.count { it.isSaved }

        HomeUiState(
            userName = user?.displayName?.ifBlank { "Engineering Student" } ?: "Engineering Student",
            userRole = user?.role?.name ?: "STUDENT",
            cartCount = cartCount,
            recommendedProjects = projects.take(5),
            rentableComponents = components.take(6),
            recentAnnouncements = announcements,
            savedProjectsCount = savedCount,
            isLoading = isRefreshing,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                databaseSeeder.seedDatabaseIfNeeded()
            } catch (e: Exception) {
                _error.value = "Failed to load database catalog: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                // Ensure seed data integrity
                databaseSeeder.resetToSeedData()
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleSaveProject(projectId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "guest-student"
            try {
                projectRepository.toggleSaveProject(userId, projectId)
            } catch (e: Exception) {
                _error.value = "Could not update bookmark: ${e.localizedMessage}"
            }
        }
    }
}
