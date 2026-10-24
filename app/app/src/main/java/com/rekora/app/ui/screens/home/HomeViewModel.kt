package com.rekora.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.RekoraDatabase
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.entity.ComponentEntity
import com.rekora.app.data.local.entity.ProjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CampusAnnouncement(
    val id: String,
    val title: String,
    val description: String,
    val tag: String,
    val timeAgo: String,
    val isUrgent: Boolean = false
)

data class ActivePrototype(
    val projectId: String,
    val title: String,
    val domain: String,
    val stage: String,
    val progressPercent: Int,
    val nextAction: String,
    val daysRemaining: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: RekoraPreferences,
    private val database: RekoraDatabase
) : ViewModel() {

    val userName: StateFlow<String> = preferences.userName
        .map { it.ifBlank { "Engineer" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Engineer")

    val userRole: StateFlow<String> = preferences.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STUDENT")

    val cartCount: StateFlow<Int> = database.cartDao().getCartCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val notificationCount: StateFlow<Int> = flow {
        // Fallback or count from DB
        database.notificationDao().getUnreadCount("current_user").collect {
            emit(if (it > 0) it else 3)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    // Recommended Projects from Room or SeedData
    val recommendedProjects: StateFlow<List<ProjectEntity>> = database.projectDao().getAllProjects()
        .map { list ->
            if (list.isNotEmpty()) list else SeedDataProvider.getSeedProjects()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedDataProvider.getSeedProjects())

    // Rentable Components from Room or SeedData
    val rentableComponents: StateFlow<List<ComponentEntity>> = database.componentDao().getRentableComponents()
        .map { list ->
            if (list.isNotEmpty()) list
            else SeedDataProvider.getSeedComponents().filter { it.isRentable }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SeedDataProvider.getSeedComponents().filter { it.isRentable }
        )

    // Current active student prototype showcase
    val activePrototype: StateFlow<ActivePrototype?> = MutableStateFlow(
        ActivePrototype(
            projectId = "proj-floating-waste-collector",
            title = "Autonomous Floating Waste Collector",
            domain = "Robotics & CV",
            stage = "Phase 3: Telemetry & Sonar Calibration",
            progressPercent = 68,
            nextAction = "Mount HC-SR04 array & test ESP-NOW link",
            daysRemaining = 12
        )
    ).asStateFlow()

    // Campus Announcements
    val announcements = listOf(
        CampusAnnouncement(
            id = "ann-1",
            title = "St. Joseph's Fab Lab: PETG & TPU Spools Restocked",
            description = "High-tensile PETG and flexible TPU spools are now available for rapid additive prototyping at Room 104.",
            tag = "Makerspace",
            timeAgo = "2h ago",
            isUrgent = false
        ),
        CampusAnnouncement(
            id = "ann-2",
            title = "Component Rental Returns Due Friday 5:00 PM",
            description = "All 14-day microcontroller loans (Raspberry Pi & STM32) must be renewed or inspected at Central Depot.",
            tag = "Depot Alert",
            timeAgo = "5h ago",
            isUrgent = true
        ),
        CampusAnnouncement(
            id = "ann-3",
            title = "Autonomous Systems Hackathon 2026",
            description = "Registration is live for the inter-collegiate autonomous robotics challenge. Blueprints eligible for grants.",
            tag = "Competition",
            timeAgo = "1d ago",
            isUrgent = false
        )
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }
}
