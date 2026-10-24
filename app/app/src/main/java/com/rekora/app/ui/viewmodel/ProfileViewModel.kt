package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * UI State container for user profile details, academic department affiliation, and app preferences.
 */
data class ProfileUiState(
    val user: User? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDevMode: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editCollege: String = "",
    val editDepartment: String = "",
    val editBio: String = "",
    val editSkills: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * ProfileViewModel manages user identity, institutional affiliation, skills portfolio,
 * and appearance preferences (Dark, Light, System themes).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: RekoraPreferences
) : ViewModel() {

    private val _isEditing = MutableStateFlow(false)
    private val _editName = MutableStateFlow("")
    private val _editCollege = MutableStateFlow("")
    private val _editDepartment = MutableStateFlow("")
    private val _editBio = MutableStateFlow("")
    private val _editSkills = MutableStateFlow<List<String>>(emptyList())
    private val _isSaving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val themeModeFlow: Flow<AppThemeMode> = combine(
        preferences.useSystemTheme,
        preferences.isDarkTheme
    ) { useSystem, isDark ->
        when {
            useSystem -> AppThemeMode.SYSTEM
            isDark -> AppThemeMode.DARK
            else -> AppThemeMode.LIGHT
        }
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.currentUser,
        themeModeFlow,
        preferences.isDevMode,
        _isEditing,
        _editName,
        _editCollege,
        _editDepartment,
        _editBio,
        _editSkills,
        _isSaving,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val currentUser = args[0] as User?
        val theme = args[1] as AppThemeMode
        val devMode = args[2] as Boolean
        val editing = args[3] as Boolean
        val name = args[4] as String
        val col = args[5] as String
        val dept = args[6] as String
        val bio = args[7] as String
        val skills = args[8] as List<String>
        val saving = args[9] as Boolean
        val err = args[10] as String?

        ProfileUiState(
            user = currentUser,
            themeMode = theme,
            isDevMode = devMode,
            isEditing = editing,
            editName = if (editing) name else (currentUser?.displayName ?: ""),
            editCollege = if (editing) col else (currentUser?.college ?: "St. Joseph's College of Engineering"),
            editDepartment = if (editing) dept else (currentUser?.department ?: "Electronics & Communication"),
            editBio = if (editing) bio else (currentUser?.bio ?: "Prototyping edge AI, IoT sensor networks, and robotic vessels."),
            editSkills = if (editing) skills else (currentUser?.skills ?: listOf("ESP32", "Embedded C", "Python", "3D Printing", "ROS2")),
            isSaving = saving,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun startEditing() {
        val current = uiState.value.user
        _editName.value = current?.displayName ?: ""
        _editCollege.value = current?.college ?: "St. Joseph's College of Engineering"
        _editDepartment.value = current?.department ?: "Electronics & Communication"
        _editBio.value = current?.bio ?: ""
        _editSkills.value = current?.skills ?: listOf("ESP32", "Embedded C", "Python", "3D Printing", "ROS2")
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
        _error.value = null
    }

    fun onNameChange(name: String) {
        _editName.value = name
    }

    fun onCollegeChange(college: String) {
        _editCollege.value = college
    }

    fun onDepartmentChange(department: String) {
        _editDepartment.value = department
    }

    fun onBioChange(bio: String) {
        _editBio.value = bio
    }

    fun addSkill(skill: String) {
        if (skill.isNotBlank() && !_editSkills.value.contains(skill.trim())) {
            _editSkills.value = _editSkills.value + skill.trim()
        }
    }

    fun removeSkill(skill: String) {
        _editSkills.value = _editSkills.value.filterNot { it.equals(skill, ignoreCase = true) }
    }

    fun saveProfile() {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val updated = currentUser.copy(
                    displayName = _editName.value.ifBlank { currentUser.displayName },
                    college = _editCollege.value.ifBlank { currentUser.college },
                    department = _editDepartment.value.ifBlank { currentUser.department },
                    bio = _editBio.value,
                    skills = _editSkills.value
                )
                authRepository.updateProfile(updated)
                _isEditing.value = false
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.localizedMessage}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            when (mode) {
                AppThemeMode.SYSTEM -> {
                    preferences.setUseSystemTheme(true)
                }
                AppThemeMode.DARK -> {
                    preferences.setUseSystemTheme(false)
                    preferences.setDarkTheme(true)
                }
                AppThemeMode.LIGHT -> {
                    preferences.setUseSystemTheme(false)
                    preferences.setDarkTheme(false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
