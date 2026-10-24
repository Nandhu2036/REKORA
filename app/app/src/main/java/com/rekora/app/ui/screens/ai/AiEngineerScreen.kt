package com.rekora.app.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.dao.AiConversationDao
import com.rekora.app.data.local.entity.AiConversationEntity
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AiEngineerViewModel @Inject constructor(
    private val conversationDao: AiConversationDao,
    private val prefs: RekoraPreferences
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<AiConversationEntity>>(emptyList())
    val conversations = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: "usr-demo-student-01"
            conversationDao.getConversationsForUser(userId).collect { list ->
                if (list.isEmpty()) {
                    val demoList = listOf(
                        AiConversationEntity(
                            id = "conv-marine-drone-01",
                            userId = userId,
                            title = "Autonomous Water Skimmer Architecture",
                            hasBlueprint = true,
                            blueprintId = "bp-001",
                            createdAt = System.currentTimeMillis() - 86400000L * 2,
                            updatedAt = System.currentTimeMillis() - 86400000L * 2
                        ),
                        AiConversationEntity(
                            id = "conv-solar-mppt-02",
                            userId = userId,
                            title = "ESP32 MPPT Solar Charger Circuit Review",
                            hasBlueprint = false,
                            blueprintId = null,
                            createdAt = System.currentTimeMillis() - 86400000L * 5,
                            updatedAt = System.currentTimeMillis() - 86400000L * 5
                        )
                    )
                    demoList.forEach { conversationDao.insertConversation(it) }
                    _conversations.value = demoList
                } else {
                    _conversations.value = list
                }
            }
        }
    }
}

@Composable
fun AiEngineerScreen(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToNewConversation: () -> Unit,
    viewModel: AiEngineerViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()

    val promptStarters = listOf(
        "Design an autonomous solar pond skimmer with edge vision",
        "Analyze thermal limits of 18650 battery pack at 3C discharge",
        "BOM optimization for sub-₹3,000 smart irrigation controller",
        "Compare ESP32-S3 vs Raspberry Pi Zero 2W for wearable TinyML"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RekoraTopBar(title = "AI Systems Engineer")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(RekoraDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Apple Intelligence Glowing Hero Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RekoraCorners.CardLarge,
                            ambientColor = Color(0x10000000),
                            spotColor = Color(0x18000000)
                        )
                        .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardLarge),
                    shape = RekoraCorners.CardLarge,
                    color = RekoraColors.MidnightNavy
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        RekoraColors.MidnightNavy,
                                        RekoraColors.DeepBlue,
                                        Color(0xFF0F324D)
                                    )
                                )
                            )
                    ) {
                        BlueprintSchematicCanvas(
                            modifier = Modifier.matchParentSize(),
                            domain = "AI",
                            accentColor = RekoraColors.ElectricCyan
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = RekoraColors.EngineeringTeal,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.SmartToy,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "REKORA AI ARCHITECT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = RekoraColors.DarkAccent
                                        )
                                        Text(
                                            text = "Hardware Intelligence",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                letterSpacing = (-0.3).sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                LuxuryStatusCapsule(
                                    label = "ACTIVE PILOT",
                                    dotColor = RekoraColors.ElectricCyan,
                                    containerColor = Color.Black.copy(alpha = 0.4f),
                                    borderColor = RekoraColors.ElectricCyan.copy(alpha = 0.4f),
                                    textColor = Color.White
                                )
                            }

                            Text(
                                text = "Formulate end-to-end hardware blueprints, evaluate component tradeoffs, generate optimized BOMs, and review safety hazards before lab fabrication.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )

                            Button(
                                onClick = onNavigateToNewConversation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .bouncyClickable(targetScale = 0.96f, onClick = onNavigateToNewConversation),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RekoraColors.EngineeringTeal,
                                    contentColor = Color.White
                                ),
                                shape = RekoraCorners.Button
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "New Project Consultation",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Core Engineering Capabilities
            item {
                Text(
                    text = "Core Engineering Capabilities",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CapabilityRow(
                        icon = Icons.Outlined.Architecture,
                        title = "Blueprint Generation",
                        description = "Full system architecture, functional blocks, and pinout tables."
                    )
                    CapabilityRow(
                        icon = Icons.Outlined.Balance,
                        title = "Component Tradeoff Analysis",
                        description = "Evaluate compute, power budget, cost, and pinout compromises."
                    )
                    CapabilityRow(
                        icon = Icons.Outlined.AttachMoney,
                        title = "BOM Optimization",
                        description = "Identify drop-in alternatives and institutional inventory reusability."
                    )
                    CapabilityRow(
                        icon = Icons.Outlined.Shield,
                        title = "Safety & Hazard Analysis",
                        description = "Thermal runaway, inductive spikes, over-current, and isolation checks."
                    )
                }
            }

            // Quick Prompt Starters
            item {
                Text(
                    text = "Quick Consultation Starters",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    promptStarters.forEach { prompt ->
                        Surface(
                            shape = RekoraCorners.CardSmall,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClickable(targetScale = 0.98f) { onNavigateToNewConversation() }
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RekoraCorners.CardSmall,
                                    ambientColor = Color(0x06000000),
                                    spotColor = Color(0x0A000000)
                                )
                                .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardSmall)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(RekoraColors.TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.TipsAndUpdates,
                                        contentDescription = null,
                                        tint = RekoraColors.EngineeringTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = RekoraColors.SecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Past Consultations
            if (conversations.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Blueprint Consultations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.3).sp
                        )
                    )
                }

                items(conversations, key = { it.id }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = { onNavigateToConversation(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RekoraCorners.CardSmall,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RekoraCorners.CardSmall,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardSmall)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RekoraColors.TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RekoraColors.EngineeringTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = RekoraColors.SecondaryText
                    )
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: AiConversationEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Surface(
        shape = RekoraCorners.CardSmall,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(targetScale = 0.98f, onClick = onClick)
            .shadow(
                elevation = 1.dp,
                shape = RekoraCorners.CardSmall,
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000)
            )
            .hairlineBorder(width = 0.75.dp, color = RekoraColors.Hairline, shape = RekoraCorners.CardSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RekoraColors.MidnightNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (conversation.hasBlueprint) Icons.Outlined.Description else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = RekoraColors.DarkAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateFormat.format(Date(conversation.createdAt)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = RekoraColors.SecondaryText
                        )
                    )
                }
            }

            if (conversation.hasBlueprint) {
                LuxuryStatusCapsule(
                    label = "BLUEPRINT",
                    dotColor = RekoraColors.EngineeringTeal,
                    containerColor = RekoraColors.TealLight,
                    borderColor = RekoraColors.TealLight,
                    textColor = RekoraColors.EngineeringTeal
                )
            }
        }
    }
}
