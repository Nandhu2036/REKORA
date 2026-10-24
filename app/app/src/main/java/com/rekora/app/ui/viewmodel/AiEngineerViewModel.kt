package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AiRepository
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.model.AiConversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for the AI Engineer hub displaying conversation history and starter prompts.
 */
data class AiEngineerUiState(
    val conversations: List<AiConversation> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val newlyCreatedConversationId: String? = null,
    val starterPrompts: List<AiPromptTemplate> = listOf(
        AiPromptTemplate(
            title = "Autonomous Precision Agriculture",
            subtitle = "Design a solar-powered soil monitoring and dynamic irrigation node.",
            initialMessage = "I want to design an automated solar-powered irrigation node using soil moisture sensors and an ESP32. My budget is under ₹4,000."
        ),
        AiPromptTemplate(
            title = "Computer Vision Trash Skimmer",
            subtitle = "Aquatic surface vessel using edge inference to detect floating plastics.",
            initialMessage = "I want to build a floating trash skimmer using a Raspberry Pi camera for object detection and differential thruster steering."
        ),
        AiPromptTemplate(
            title = "Industrial Vibration Sentinel",
            subtitle = "Edge impulse TinyML anomaly detector for campus lab machinery.",
            initialMessage = "I want to develop an edge TinyML vibration monitoring sensor for rotating lab machinery with wireless BLE anomaly alerts."
        ),
        AiPromptTemplate(
            title = "Dual-Axis Solar Tracker",
            subtitle = "Maximize solar collection efficiency with quadrant light sensors.",
            initialMessage = "Help me design a closed-loop dual-axis solar tracker with wind-stow protection and battery charging management."
        )
    ),
    val error: String? = null
)

data class AiPromptTemplate(
    val title: String,
    val subtitle: String,
    val initialMessage: String
)

/**
 * AiEngineerViewModel manages student conversations with the REKORA AI Engineering Consultant.
 * Provides starting templates, session history, and fast conversation creation.
 */
@HiltViewModel
class AiEngineerViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isCreating = MutableStateFlow(false)
    private val _newlyCreatedConversationId = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val conversations: StateFlow<List<AiConversation>> = authRepository.currentUser
        .flatMapLatest { user ->
            val userId = user?.id ?: "guest-student"
            aiRepository.getConversationsForUser(userId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<AiEngineerUiState> = combine(
        conversations,
        _isLoading,
        _isCreating,
        _newlyCreatedConversationId,
        _error
    ) { convs, loading, creating, newId, err ->
        AiEngineerUiState(
            conversations = convs,
            isLoading = loading,
            isCreating = creating,
            newlyCreatedConversationId = newId,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiEngineerUiState(isLoading = true)
    )

    fun startNewConversation(title: String = "New Engineering Consultation", onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _isCreating.value = true
            _error.value = null
            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"
                val convId = aiRepository.createConversation(title, userId)
                _newlyCreatedConversationId.value = convId
                onCreated(convId)
            } catch (e: Exception) {
                _error.value = "Failed to start conversation: ${e.localizedMessage}"
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun startFromPrompt(prompt: AiPromptTemplate, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _isCreating.value = true
            _error.value = null
            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"
                val convId = aiRepository.createConversation(prompt.title, userId)
                aiRepository.sendMessage(convId, prompt.initialMessage).collect()
                _newlyCreatedConversationId.value = convId
                onCreated(convId)
            } catch (e: Exception) {
                _error.value = "Failed to launch prompt consultation: ${e.localizedMessage}"
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun clearNewConversationId() {
        _newlyCreatedConversationId.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
