package com.rekora.app.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Campus Operations Console",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RekoraDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
        ) {
            // Operator badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RekoraCorners.Card
            ) {
                Column(
                    modifier = Modifier.padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "St. Joseph's College Pilot Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        StatusBadge(
                            status = "ROLE: OPERATOR / ADMIN",
                            type = StatusType.INFO
                        )
                    }
                    Text(
                        text = "Authorized access for Component Inventory, Rental Lifecycle, and Fabrication Lab management.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                MetricCard(
                    title = "Active Rentals",
                    value = "8",
                    icon = Icons.Outlined.Memory,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Fab Jobs",
                    value = "3",
                    icon = Icons.Outlined.Print,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Live Inventory",
                    value = "24 items",
                    icon = Icons.Outlined.Inventory,
                    modifier = Modifier.weight(1f)
                )
            }

            // Operational Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Rental Approvals") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fabrication Queue") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("QR Assets") }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Rentals Section
                    SectionHeader(title = "Pending Rental Dispatches")
                    AdminActionCard(
                        title = "Raspberry Pi 4 Model B (4GB)",
                        subtitle = "Student: Reshma S (ECE) • Asset: RK-RPI4-001",
                        badge = "DUE: 14 DAYS",
                        onApprove = { actionMessage = "Dispatched RK-RPI4-001 to Reshma S" },
                        onReject = { actionMessage = "Rejected request" }
                    )
                    AdminActionCard(
                        title = "ESP32-S3 Dual Type-C Dev Board",
                        subtitle = "Student: NandhaKumar G (Mech) • Asset: RK-ESP32-001",
                        badge = "DUE: 7 DAYS",
                        onApprove = { actionMessage = "Dispatched RK-ESP32-001 to NandhaKumar G" },
                        onReject = { actionMessage = "Rejected request" }
                    )
                }
                1 -> {
                    // Fabrication Section
                    SectionHeader(title = "Fabrication Job Review")
                    AdminActionCard(
                        title = "Hull Strut Bracket (STL)",
                        subtitle = "Material: PETG Black • 40% Infill • Estimated 140g",
                        badge = "QUOTED ₹420",
                        approveText = "Accept Print Job",
                        onApprove = { actionMessage = "Job routed to 3D Print Farm: Station #2" },
                        onReject = { actionMessage = "Quotation declined or clarification requested" }
                    )
                }
                2 -> {
                    // QR Inventory section
                    SectionHeader(title = "QR Hardware Items")
                    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                        QrItemRow(code = "RK-ESP32-001", name = "ESP32-S3 Dev Board", status = "AVAILABLE")
                        QrItemRow(code = "RK-RPI4-001", name = "Raspberry Pi 4 (4GB)", status = "RENTED (Reshma S)")
                        QrItemRow(code = "RK-THRUST-001", name = "12V Underwater Thruster", status = "AVAILABLE")
                    }
                }
            }

            if (actionMessage != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { actionMessage = null }) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                ) {
                    Text(actionMessage ?: "")
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RekoraCorners.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminActionCard(
    title: String,
    subtitle: String,
    badge: String,
    approveText: String = "Approve Dispatch",
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = RekoraDimens.CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(RekoraDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(status = badge, type = StatusType.WARNING)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(approveText)
                }
            }
        }
    }
}

@Composable
private fun QrItemRow(
    code: String,
    name: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = RekoraDimens.SpacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall
            )
        }
        StatusBadge(
            status = status,
            type = if (status.contains("AVAILABLE")) StatusType.SUCCESS else StatusType.INFO
        )
    }
}
