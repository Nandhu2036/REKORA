package com.rekora.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AiRepository
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.model.AiConversation
import com.rekora.app.domain.model.AiMessage
import com.rekora.app.domain.model.ProjectBlueprint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Diagnostic stages in the REKORA AI Engineering consultative workflow.
 */
enum class DiagnosticStage(val title: String, val stepNumber: Int) {
    CLARIFICATION("1. Clarification", 1),
    CONSTRAINTS("2. Constraints", 2),
    ARCHITECTURE("3. Architecture", 3),
    BOM("4. BOM & Sourcing", 4),
    CAD("5. CAD & Enclosure", 5),
    VALIDATION("6. Validation", 6),
    BLUEPRINT("7. Blueprint", 7)
}

/**
 * UI State container for an active AI Engineering conversation.
 */
data class AiConversationUiState(
    val conversationId: String = "",
    val conversation: AiConversation? = null,
    val messages: List<AiMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentStage: DiagnosticStage = DiagnosticStage.CLARIFICATION,
    val blueprint: ProjectBlueprint? = null,
    val isGeneratingBlueprint: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

/**
 * AiConversationViewModel coordinates a structured engineering consultation:
 * - Diagnostic conversation flow (Clarification -> Constraints -> Architecture -> BOM -> CAD -> Validation -> Blueprint)
 * - Real-time conversational messaging with asynchronous typing simulation
 * - Generation of complete, verified ProjectBlueprint artifacts
 */
@HiltViewModel
class AiConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiRepository: AiRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val conversationId: String = checkNotNull(savedStateHandle["conversationId"]) {
        "conversationId parameter is required for AiConversationViewModel"
    }

    private val _isTyping = MutableStateFlow(false)
    private val _currentStage = MutableStateFlow(DiagnosticStage.CLARIFICATION)
    private val _blueprint = MutableStateFlow<ProjectBlueprint?>(null)
    private val _isGeneratingBlueprint = MutableStateFlow(false)
    private val _inputText = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    private val conversationFlow: Flow<AiConversation?> = aiRepository.getConversation(conversationId)
    private val messagesFlow: Flow<List<AiMessage>> = aiRepository.getMessages(conversationId)

    val uiState: StateFlow<AiConversationUiState> = combine(
        conversationFlow,
        messagesFlow,
        _isTyping,
        _currentStage,
        _blueprint,
        _isGeneratingBlueprint,
        _inputText,
        _error
    ) { args: Array<Any?> ->
        val conversation = args[0] as? AiConversation
        @Suppress("UNCHECKED_CAST")
        val messages = args[1] as List<AiMessage>
        val isTyping = args[2] as Boolean
        val stage = args[3] as DiagnosticStage
        val blueprint = args[4] as? ProjectBlueprint
        val generating = args[5] as Boolean
        val input = args[6] as String
        val err = args[7] as? String

        AiConversationUiState(
            conversationId = conversationId,
            conversation = conversation,
            messages = messages,
            isTyping = isTyping,
            currentStage = stage,
            blueprint = blueprint,
            isGeneratingBlueprint = generating,
            inputText = input,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiConversationUiState(
            conversationId = conversationId,
            isTyping = false
        )
    )

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun selectStage(stage: DiagnosticStage) {
        _currentStage.value = stage
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isTyping.value) return

        _inputText.value = ""
        viewModelScope.launch {
            try {
                _isTyping.value = true
                advanceStageHeuristically(text)
                aiRepository.sendMessage(conversationId, text).collect()
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.localizedMessage}"
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun generateBlueprint(projectTitle: String? = null, objective: String? = null) {
        viewModelScope.launch {
            _isGeneratingBlueprint.value = true
            _error.value = null
            try {
                val bp = aiRepository.generateBlueprint(conversationId)
                _blueprint.value = bp
                _currentStage.value = DiagnosticStage.BLUEPRINT
            } catch (e: Exception) {
                _error.value = "Blueprint generation failed: ${e.localizedMessage}"
            } finally {
                _isGeneratingBlueprint.value = false
            }
        }
    }

    private fun advanceStageHeuristically(userPrompt: String) {
        val lower = userPrompt.lowercase()
        val current = _currentStage.value

        when {
            lower.contains("budget") || lower.contains("timeline") || lower.contains("environment") ->
                if (current.stepNumber < DiagnosticStage.CONSTRAINTS.stepNumber) _currentStage.value = DiagnosticStage.CONSTRAINTS
            lower.contains("architecture") || lower.contains("esp32") || lower.contains("subsystem") ->
                if (current.stepNumber < DiagnosticStage.ARCHITECTURE.stepNumber) _currentStage.value = DiagnosticStage.ARCHITECTURE
            lower.contains("bom") || lower.contains("component") || lower.contains("buy") || lower.contains("rent") ->
                if (current.stepNumber < DiagnosticStage.BOM.stepNumber) _currentStage.value = DiagnosticStage.BOM
            lower.contains("cad") || lower.contains("enclosure") || lower.contains("3d print") ->
                if (current.stepNumber < DiagnosticStage.CAD.stepNumber) _currentStage.value = DiagnosticStage.CAD
            lower.contains("testing") || lower.contains("validation") || lower.contains("metrics") ->
                if (current.stepNumber < DiagnosticStage.VALIDATION.stepNumber) _currentStage.value = DiagnosticStage.VALIDATION
        }
    }

    fun clearError() {
        _error.value = null
    }
}
