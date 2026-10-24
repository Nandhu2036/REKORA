package com.rekora.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.domain.model.User
import com.rekora.app.domain.model.UserRole
import com.rekora.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAccount(
    val name: String,
    val email: String,
    val role: String,
    val label: String,
    val department: String = "ECE"
)

val PREDEFINED_ACCOUNTS = listOf(
    QuickAccount(
        name = "Arun Kumar",
        email = "student@stjosephs.edu",
        role = "STUDENT",
        label = "Student",
        department = "ECE"
    ),
    QuickAccount(
        name = "Divya Ramesh",
        email = "contributor@stjosephs.edu",
        role = "CONTRIBUTOR",
        label = "Contributor",
        department = "MECH"
    ),
    QuickAccount(
        name = "Dr. K. Senthil",
        email = "faculty@stjosephs.edu",
        role = "FACULTY",
        label = "Faculty",
        department = "CSE"
    ),
    QuickAccount(
        name = "M. Rajesh",
        email = "operator@stjosephs.edu",
        role = "INVENTORY_OPERATOR",
        label = "Lab Tech",
        department = "Fab Lab"
    )
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferences: RekoraPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isOnboardingCompleted: StateFlow<Boolean> = preferences.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userName: StateFlow<String> = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userEmail: StateFlow<String> = preferences.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userRole: StateFlow<String> = preferences.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STUDENT")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.login(email, pass).fold(
                onSuccess = {
                    preferences.setOnboardingCompleted(true)
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Sign in failed"
                }
            )
        }
    }

    fun loginWithPredefined(account: QuickAccount, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.login(account.email, "password123").fold(
                onSuccess = {
                    preferences.setOnboardingCompleted(true)
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Failed to sign in"
                }
            )
        }
    }

    fun loginWithTesterCode(code: String, email: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.loginWithTesterCode(code, email).fold(
                onSuccess = {
                    preferences.setOnboardingCompleted(true)
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Invalid authorization code"
                }
            )
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.loginAsGuest().fold(
                onSuccess = {
                    preferences.setOnboardingCompleted(true)
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Guest login failed"
                }
            )
        }
    }

    fun register(
        fullName: String,
        email: String,
        pass: String,
        confirmPass: String,
        college: String,
        department: String,
        role: String,
        acceptedTerms: Boolean,
        onSuccess: () -> Unit
    ) {
        if (pass != confirmPass) {
            _errorMessage.value = "Passwords do not match"
            return
        }
        if (!acceptedTerms) {
            _errorMessage.value = "Please agree to the Terms and Academic Integrity code"
            return
        }

        val roleEnum = try {
            UserRole.valueOf(role)
        } catch (e: Exception) {
            UserRole.STUDENT
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.register(
                email = email,
                pass = pass,
                name = fullName,
                role = roleEnum,
                college = college
            ).fold(
                onSuccess = {
                    preferences.setOnboardingCompleted(true)
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Registration failed"
                }
            )
        }
    }

    fun completeOnboarding(
        role: String = "STUDENT",
        interests: List<String> = emptyList(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            preferences.setOnboardingCompleted(true)
            authRepository.loginAsGuest().fold(
                onSuccess = { onSuccess() },
                onFailure = { onSuccess() }
            )
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _errorMessage.value = null
            authRepository.requestPasswordReset(email).fold(
                onSuccess = { onComplete(true) },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage
                    onComplete(false)
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
