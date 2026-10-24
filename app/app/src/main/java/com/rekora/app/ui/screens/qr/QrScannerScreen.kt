package com.rekora.app.ui.screens.qr

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onItemCodeScanned: (String) -> Unit
) {
    var manualCodeInput by remember { mutableStateOf("") }
    var showManualDialog by remember { mutableStateOf(false) }
    var simulatedScanSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Scan Item QR Code",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(RekoraDimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                Text(
                    text = "REKORA Inventory Lifecycle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Align the hardware QR code (e.g. RK-ESP32-001) within the scanning viewfinder to verify condition, checkout, or initiate a return.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Viewfinder box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(
                        width = 3.dp,
                        color = if (simulatedScanSuccess) RekoraColors.Success else RekoraColors.EngineeringTeal,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                ) {
                    Icon(
                        imageVector = if (simulatedScanSuccess) Icons.Filled.CheckCircle else Icons.Outlined.QrCodeScanner,
                        contentDescription = "Scanner",
                        modifier = Modifier.size(72.dp),
                        tint = if (simulatedScanSuccess) RekoraColors.Success else Color.White
                    )
                    Text(
                        text = if (simulatedScanSuccess) "QR Code Detected!" else "Scanning active...",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            // Quick actions & Fallback options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                // Test scan buttons for development simulation
                Text(
                    text = "Quick Test QR Codes (Demo)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    RekoraChip(
                        text = "RK-ESP32-001",
                        onClick = {
                            simulatedScanSuccess = true
                            onItemCodeScanned("RK-ESP32-001")
                        }
                    )
                    RekoraChip(
                        text = "RK-RPI4-001",
                        onClick = {
                            simulatedScanSuccess = true
                            onItemCodeScanned("RK-RPI4-001")
                        }
                    )
                    RekoraChip(
                        text = "RK-THRUST-001",
                        onClick = {
                            simulatedScanSuccess = true
                            onItemCodeScanned("RK-THRUST-001")
                        }
                    )
                }

                RekoraSecondaryButton(
                    text = "Enter Code Manually",
                    icon = Icons.Outlined.Keyboard,
                    onClick = { showManualDialog = true }
                )
            }
        }
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("Manual Asset Lookup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                    Text(
                        text = "Enter the 10-character inventory asset ID printed on the physical item label.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it.uppercase() },
                        label = { Text("Asset Code (e.g. RK-ESP32-001)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCodeInput.isNotBlank()) {
                            showManualDialog = false
                            onItemCodeScanned(manualCodeInput.trim())
                        }
                    }
                ) {
                    Text("Lookup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
