package com.rekora.app.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.dao.AiConversationDao
import com.rekora.app.data.local.entity.AiConversationEntity
import com.rekora.app.data.local.entity.AiMessageEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class StructuredBlueprintView(
    val title: String,
    val objective: String,
    val architecture: String,
    val functionalRequirements: List<String>,
    val pinouts: List<Pair<String, String>>,
    val bomEstimate: List<Triple<String, Int, Double>>,
    val safetyAdvisories: List<String>
)

@HiltViewModel
class AiConversationViewModel @Inject constructor(
    private val conversationDao: AiConversationDao,
    private val prefs: RekoraPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navConvId: String? = savedStateHandle["conversationId"]

    private val _conversationId = MutableStateFlow(navConvId ?: "conv-${UUID.randomUUID()}")
    val conversationId = _conversationId.asStateFlow()

    private val _title = MutableStateFlow("Hardware Blueprint Consultation")
    val title = _title.asStateFlow()

    private val _messages = MutableStateFlow<List<AiMessageEntity>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _currentStep = MutableStateFlow(3)
    val currentStep = _currentStep.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _activeBlueprint = MutableStateFlow<StructuredBlueprintView?>(null)
    val activeBlueprint = _activeBlueprint.asStateFlow()

    init {
        initSession(_conversationId.value)
    }

    private fun initSession(convId: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: "usr-demo-student-01"

            // Check if conversation exists
            val existing = conversationDao.getConversationById(convId)
            if (existing != null) {
                _title.value = existing.title
                conversationDao.getMessagesForConversation(convId).collect { msgList ->
                    _messages.value = msgList
                }
            } else {
                // Create conversation with engineering initial briefing
                val newEntity = AiConversationEntity(
                    id = convId,
                    userId = userId,
                    title = "Autonomous Water Skimmer Architecture",
                    hasBlueprint = true,
                    blueprintId = "bp-${convId}",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                conversationDao.insertConversation(newEntity)

                val initialMsgs = listOf(
                    AiMessageEntity(
                        id = "msg-${UUID.randomUUID()}",
                        conversationId = convId,
                        content = "Hello! I am your REKORA AI Systems Architect. I assist student engineering teams with system modeling, component tradeoff analysis, and bill of materials synthesis.",
                        role = "ASSISTANT",
                        timestamp = System.currentTimeMillis() - 20000L
                    ),
                    AiMessageEntity(
                        id = "msg-${UUID.randomUUID()}",
                        conversationId = convId,
                        content = "We are designing an Autonomous Marine Surface Skimmer for our final year capstone. We need differential thruster control with edge camera classification under ₹15,000 budget.",
                        role = "USER",
                        timestamp = System.currentTimeMillis() - 15000L
                    ),
                    AiMessageEntity(
                        id = "msg-${UUID.randomUUID()}",
                        conversationId = convId,
                        content = "Excellent problem statement! Here is my component tradeoff recommendation:\n\n1. **Edge Brain**: Raspberry Pi 4 Model B (4GB) is optimal for 15 FPS MobileNet SSD inference.\n2. **Propulsion**: Dual 12V underwater thrusters with 30A Bi-directional ESCs.\n3. **Power Architecture**: 3S 11.1V 2200mAh LiPo for propulsion + LM2596 buck converter (5V 3A) for logic.",
                        role = "ASSISTANT",
                        timestamp = System.currentTimeMillis() - 10000L
                    )
                )

                initialMsgs.forEach { conversationDao.insertMessage(it) }
                _messages.value = initialMsgs

                // Pre-generate sample blueprint for instant preview
                _activeBlueprint.value = createSampleBlueprint()
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val convId = _conversationId.value

        viewModelScope.launch {
            val userMsg = AiMessageEntity(
                id = "msg-${UUID.randomUUID()}",
                conversationId = convId,
                content = userText.trim(),
                role = "USER",
                timestamp = System.currentTimeMillis()
            )
            conversationDao.insertMessage(userMsg)
            _messages.value = _messages.value + userMsg

            _isGenerating.value = true
            delay(1200) // Simulated AI reasoning delay

            val aiResponse = generateSmartResponse(userText)
            val assistantMsg = AiMessageEntity(
                id = "msg-${UUID.randomUUID()}",
                conversationId = convId,
                content = aiResponse,
                role = "ASSISTANT",
                timestamp = System.currentTimeMillis()
            )
            conversationDao.insertMessage(assistantMsg)
            _messages.value = _messages.value + assistantMsg
            _isGenerating.value = false
        }
    }

    fun triggerGenerateBlueprint() {
        _activeBlueprint.value = createSampleBlueprint()
        _currentStep.value = 4
    }

    private fun generateSmartResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("sleep") || q.contains("power") -> {
                "For low-power duty cycling with ESP32:\n\n```cpp\n// Enter deep sleep for 10 minutes\nesp_sleep_enable_timer_wakeup(10 * 60 * 1000000ULL);\nesp_deep_sleep_start();\n```\nAverage current draw drops to ~10μA during deep sleep, extending 18650 cell endurance to over 4 months."
            }
            q.contains("pinout") || q.contains("wire") -> {
                "Here is the recommended GPIO Pinout assignment:\n\n• GPIO 18: PWM0 Left ESC Signal (50Hz servo refresh)\n• GPIO 19: PWM1 Right ESC Signal\n• GPIO 2 & 3: I2C Bus for BME280 & IMU\n• GPIO 4: Failsafe Relay Kill Gate\n\n*Reminder: Connect ESC signal ground back to Raspberry Pi Ground to avoid floating reference jitter.*"
            }
            q.contains("pi zero") || q.contains("esp32-s3") -> {
                "**Tradeoff: Raspberry Pi Zero 2W vs ESP32-S3**\n\n• **ESP32-S3**: Instant boot (50ms), ultra low sleep power (14μA), dual hardware USB-C, built-in vector instructions for TinyML.\n• **Pi Zero 2W**: Full Linux OS, easy OpenCV/Python libraries, but higher idle power (120mA) and SD card corruption risks on sudden power cut.\n\n*Recommendation*: For pure telemetry and sonar, ESP32-S3 is superior. For 15 FPS OpenCV object classification, Pi Zero 2W or Pi 4 is required."
            }
            else -> {
                "Analysis complete. The requested hardware subsystem has been verified against institutional safety margins. Would you like me to synthesize the final Bill of Materials and CAD fabrication constraints?"
            }
        }
    }

    private fun createSampleBlueprint(): StructuredBlueprintView {
        return StructuredBlueprintView(
            title = "Autonomous Marine Waste Collector (v1.1 Blueprint)",
            objective = "Autonomous surface debris interception using edge MobileNet SSD on Raspberry Pi 4 and differential PWM propulsion.",
            architecture = "Catamaran dual pontoon (PETG 40% infill) with central IP67 sealed electronics bay and mesh collection hopper.",
            functionalRequirements = listOf(
                "Detect floating plastics at >= 15 FPS within 4m radius",
                "Differential thrust vectoring with zero turn radius",
                "Failsafe automated return-to-launch upon LiPo voltage < 10.5V",
                "Geofenced boundary perimeter adherence over LoRaWAN"
            ),
            pinouts = listOf(
                "PWM Thruster Left" to "GPIO 18 (PWM0)",
                "PWM Thruster Right" to "GPIO 19 (PWM1)",
                "Sonar Trig / Echo" to "GPIO 23 / 24",
                "Telemetry LoRa TX/RX" to "UART0 (GPIO 14/15)"
            ),
            bomEstimate = listOf(
                Triple("Raspberry Pi 4 Model B (4GB)", 1, 6500.0),
                Triple("Underwater 12V Brushless Thruster Pair", 2, 4400.0),
                Triple("3S 11.1V 2200mAh LiPo Pack", 1, 1450.0),
                Triple("OV2640 2MP Wide Angle Camera", 1, 350.0)
            ),
            safetyAdvisories = listOf(
                "Propeller guards mandatory during shore handling",
                "Charge LiPo battery exclusively inside fireproof LiPo safe bag",
                "Isolate motor ESC power rails with physical emergency kill switch"
            )
        )
    }
}

@Composable
fun AiConversationScreen(
    conversationId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AiConversationViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val activeBlueprint by viewModel.activeBlueprint.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val promptSuggestions = listOf(
        "Suggest low-power deep sleep mode",
        "Compare ESP32-S3 vs Raspberry Pi Zero 2W",
        "Generate wiring diagram pinouts",
        "Estimate battery endurance at 2C draw"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = title,
                onBackClick = onNavigateBack,
                actions = {
                    Surface(
                        shape = RekoraCorners.Tag,
                        color = RekoraColors.TealLight,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "AI BOUNDARY: DEV",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RekoraColors.DeepBlue
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
            ) {
                // Suggestions Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = RekoraDimens.ScreenPadding, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    promptSuggestions.forEach { suggestion ->
                        Surface(
                            shape = RekoraCorners.Chip,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RekoraCorners.Chip)
                                .clickable {
                                    viewModel.sendMessage(suggestion)
                                }
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = RekoraDimens.DividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask technical architecture, pinouts, BOM...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true,
                        shape = RekoraCorners.SearchBar,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(RekoraColors.EngineeringTeal, CircleShape)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = RekoraDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd),
            contentPadding = PaddingValues(vertical = RekoraDimens.SpacingMd)
        ) {
            // Step Progress Pill
            item {
                StepProgressPill(step = currentStep, totalSteps = 4, stepLabel = "Defining Architecture & Hardware Pinouts")
            }

            // Physical Validation Safety Banner
            item {
                SafetyAdvisoryBanner()
            }

            // Chat Messages
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "USER"
                ChatBubble(
                    content = msg.content,
                    isUser = isUser
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = RekoraColors.EngineeringTeal,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Synthesizing hardware architecture...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Structured Blueprint Section (if available)
            if (activeBlueprint != null) {
                item {
                    BlueprintStructuredCard(
                        blueprint = activeBlueprint!!,
                        onAddBomToCart = { /* Added */ }
                    )
                }
            } else {
                item {
                    Button(
                        onClick = { viewModel.triggerGenerateBlueprint() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RekoraColors.MidnightNavy,
                            contentColor = Color.White
                        ),
                        shape = RekoraCorners.Button
                    ) {
                        Icon(imageVector = Icons.Outlined.DesignServices, contentDescription = null)
                        Spacer(modifier = Modifier.width(RekoraDimens.SpacingSm))
                        Text("Generate Full Project Blueprint", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Chat Bubble ──────────────────────────────────────────────

@Composable
private fun ChatBubble(content: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = RekoraColors.EngineeringTeal,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 6.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) RekoraColors.MidnightNavy else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (content.contains("```")) {
                    CodeFormattedText(text = content, isUser = isUser)
                } else {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeFormattedText(text: String, isUser: Boolean) {
    val parts = text.split("```")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) { // Code block
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RekoraColors.DarkBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = part.trim(),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = RekoraColors.DarkAccent,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            if (part.isNotBlank()) {
                Text(
                    text = part.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ── Blueprint Structured Card ────────────────────────────────

@Composable
private fun BlueprintStructuredCard(
    blueprint: StructuredBlueprintView,
    onAddBomToCart: () -> Unit
) {
    Card(
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, RekoraColors.EngineeringTeal),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(RekoraDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Architecture, contentDescription = null, tint = RekoraColors.EngineeringTeal)
                    Text(
                        text = "Synthesized Project Blueprint",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = "VALIDATED", type = StatusType.SUCCESS)
            }

            Text(text = blueprint.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RekoraColors.MidnightNavy)
            Text(text = blueprint.objective, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(thickness = RekoraDimens.DividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            // Pinout Matrix
            Text("Hardware Pinout Matrix:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RekoraColors.MidnightNavy, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                blueprint.pinouts.forEach { (func, pin) ->
                    Text(
                        text = "$func -> $pin",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // BOM Items Estimate
            Text("Recommended Bill of Materials:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                blueprint.bomEstimate.forEach { (name, qty, cost) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• $name (x$qty)", style = MaterialTheme.typography.bodySmall)
                        Text(text = "₹${(cost * qty).toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = onAddBomToCart,
                colors = ButtonDefaults.buttonColors(containerColor = RekoraColors.EngineeringTeal, contentColor = Color.White),
                shape = RekoraCorners.ButtonSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Source Blueprint Components", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StepProgressPill(step: Int, totalSteps: Int, stepLabel: String) {
    Surface(
        shape = RekoraCorners.Chip,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Step $step of $totalSteps: $stepLabel",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${(step * 100 / totalSteps)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SafetyAdvisoryBanner() {
    Card(
        shape = RekoraCorners.CardSmall,
        colors = CardDefaults.cardColors(containerColor = RekoraColors.WarningLight),
        border = BorderStroke(1.dp, RekoraColors.Warning.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.WarningAmber, contentDescription = null, tint = RekoraColors.Warning)
            Text(
                text = "Physical Validation Advisory: AI-generated schematics & power calculations must be verified against hardware datasheets before live soldering.",
                style = MaterialTheme.typography.labelSmall,
                color = RekoraColors.Warning
            )
        }
    }
}
