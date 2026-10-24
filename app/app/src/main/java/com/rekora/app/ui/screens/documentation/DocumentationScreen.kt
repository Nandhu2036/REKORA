package com.rekora.app.ui.screens.documentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun DocumentationScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("Autonomous Floating Waste Collector — Technical Documentation") }
    var abstractText by remember { mutableStateOf("The Autonomous Floating Waste Collector (AFWC) addresses plastic accumulation in campus water retention ponds through an edge-AI powered unmanned surface vessel...") }
    var methodology by remember { mutableStateOf("1. Image capture via OV2640 camera.\n2. Lightweight SSD MobileNet model running at 15 FPS.\n3. Differential PWM propulsion for trajectory correction.\n4. Debris collection into an open mesh hopper.") }
    var testingLogs by remember { mutableStateOf("Test Run 1: Static buoyancy test passed with 3.5kg payload capacity.\nTest Run 2: Object detection reached 88% precision in daylight conditions.\nTest Run 3: Return-to-home battery failsafe engaged successfully at 15% voltage threshold.") }
    var showExportDialog by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Documentation Workspace",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = "Export")
                    }
                }
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
            // Academic Integrity Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RekoraCorners.CardSmall
            ) {
                Row(
                    modifier = Modifier.padding(RekoraDimens.CardPadding),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "REKORA provides engineering frameworks and structured templates. All technical implementations, real test data, and final submissions remain the authentic work of the student.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Project Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Report Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Abstract section
            OutlinedTextField(
                value = abstractText,
                onValueChange = { abstractText = it },
                label = { Text("Abstract & Problem Formulation") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Methodology
            OutlinedTextField(
                value = methodology,
                onValueChange = { methodology = it },
                label = { Text("System Methodology & Architecture") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Testing & Validation Logs
            OutlinedTextField(
                value = testingLogs,
                onValueChange = { testingLogs = it },
                label = { Text("Empirical Validation & Test Logs") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                RekoraSecondaryButton(
                    text = if (isSaved) "Draft Saved" else "Save Draft",
                    onClick = { isSaved = true },
                    modifier = Modifier.weight(1f)
                )
                RekoraTealButton(
                    text = "Export Report",
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Project Report") },
            text = {
                Text("Select your preferred format for academic project documentation. REKORA formats your report with standard IEEE/college submission headers.")
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false }
                ) {
                    Text("Download PDF")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) {
                    Text("Export Markdown")
                }
            }
        )
    }
}
