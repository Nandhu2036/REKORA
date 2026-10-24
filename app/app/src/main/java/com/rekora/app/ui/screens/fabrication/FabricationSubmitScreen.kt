package com.rekora.app.ui.screens.fabrication

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabricationSubmitScreen(
    onNavigateBack: () -> Unit,
    viewModel: FabricationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCadPickerSheet by remember { mutableStateOf(false) }

    // Submission Confirmation Dialog / Modal
    if (uiState.isConfirmed && uiState.submissionTrackingId != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetSubmission()
                onNavigateBack()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RekoraColors.SuccessLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = RekoraColors.Success,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Fabrication Job Dispatched",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your 3D CAD mesh has been queued at the St. Joseph's Campus 3D Fabrication Hub.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RekoraCorners.CardSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(RekoraDimens.SpacingSm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FABRICATION TRACKING ID",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = uiState.submissionTrackingId ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Text(
                        text = "Pickup Location: Engineering Block B, Room 204\nEstimated Turnaround: 24 - 48 Hours",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSubmission()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        )
    }

    // CAD File Picker Bottom Sheet Simulator
    if (showCadPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCadPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RekoraDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
            ) {
                Text(
                    text = "Select 3D Mesh / CAD Model",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Simulating file system browser with verified student project CAD models (STL, STEP, 3MF):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                viewModel.availableCadFiles.forEach { fileOption ->
                    val isSelected = uiState.selectedFile?.fileName == fileOption.fileName
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCadFile(fileOption)
                                showCadPickerSheet = false
                            },
                        shape = RekoraCorners.CardSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RekoraColors.TealLight else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.EngineeringTeal)
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.CardPaddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilePresent,
                                contentDescription = null,
                                tint = if (isSelected) RekoraColors.EngineeringTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileOption.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${fileOption.format} • ${fileOption.fileSizeMb} MB • ${fileOption.triangleCount} Triangles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (fileOption.isWatertight) {
                                StatusBadge(status = "WATERTIGHT", type = StatusType.SUCCESS)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(RekoraDimens.SpacingLg))
            }
        }
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "3D Fabrication Lab Hub",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RekoraDimens.ScreenPadding, vertical = RekoraDimens.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Fabrication Cost",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${uiState.estimatedCost.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        RekoraPrimaryButton(
                            text = if (uiState.isSubmitting) "Queueing..." else "Submit Print Job",
                            onClick = viewModel::submitJob,
                            isLoading = uiState.isSubmitting,
                            icon = Icons.Outlined.Send,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(RekoraDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
        ) {
            // Header Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.CardSmall,
                colors = CardDefaults.cardColors(
                    containerColor = RekoraColors.TealLight
                )
            ) {
                Row(
                    modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PrecisionManufacturing,
                        contentDescription = null,
                        tint = RekoraColors.EngineeringTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "St. Joseph's Rapid Prototyping Hub",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RekoraColors.DeepBlue
                        )
                        Text(
                            text = "Industrial Bambu Lab X1-Carbon & Formlabs SLA printers with subsidized student spool rates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RekoraColors.DeepBlue.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Step 1: CAD File Upload Simulator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 1: CAD Model Upload",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showCadPickerSheet = true }) {
                            Text("Change File")
                        }
                    }

                    if (uiState.selectedFile != null) {
                        val file = uiState.selectedFile!!
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RekoraCorners.CardSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RekoraCorners.Image)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ViewInAr,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${file.fileSizeMb} MB • ${file.triangleCount} Triangles • Water-tight verified",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = file.format, type = StatusType.INFO)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showCadPickerSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RekoraCorners.CardSmall
                        ) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse CAD Files (.STL, .STEP, .3MF)")
                        }
                    }
                }
            }

            // Step 2: Part Specifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                ) {
                    Text(
                        text = "Step 2: Fabrication Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Part Description input
                    OutlinedTextField(
                        value = uiState.partDescription,
                        onValueChange = viewModel::setPartDescription,
                        label = { Text("Part Description & Engineering Purpose") },
                        placeholder = { Text("e.g. Waterproof chassis for aquatic debris collector") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // Material Selection
                    Text(
                        text = "Filament / Material Choice:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)) {
                        viewModel.availableMaterials.forEach { material ->
                            val isSelected = uiState.selectedMaterial.code == material.code
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setMaterial(material) },
                                shape = RekoraCorners.CardSmall,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) RekoraColors.TealLight else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.EngineeringTeal)
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.setMaterial(material) }
                                        )
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = material.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                StatusBadge(status = material.densityTag, type = StatusType.DEFAULT)
                                            }
                                            Text(
                                                text = material.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = "₹${material.ratePerGram.toInt()}/g",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RekoraColors.EngineeringTeal
                                    )
                                }
                            }
                        }
                    }

                    // Color Selector
                    Text(
                        text = "Part Color: ${uiState.selectedColor}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                    ) {
                        viewModel.availableColors.forEach { colorName ->
                            val isSelected = uiState.selectedColor == colorName
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setColor(colorName) },
                                label = { Text(colorName) },
                                shape = RekoraCorners.Chip
                            )
                        }
                    }

                    // Infill Percentage Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Infill Percentage:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.infillPercentage}% (${
                                when {
                                    uiState.infillPercentage < 20 -> "Lightweight"
                                    uiState.infillPercentage <= 40 -> "Standard Prototype"
                                    uiState.infillPercentage <= 70 -> "High Structural"
                                    else -> "Solid Solid"
                                }
                            })",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = RekoraColors.EngineeringTeal
                        )
                    }

                    Slider(
                        value = uiState.infillPercentage.toFloat(),
                        onValueChange = { viewModel.setInfill(it.toInt()) },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    // Quantity Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantity:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = { viewModel.setQuantity(uiState.quantity - 1) },
                                enabled = uiState.quantity > 1
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                text = "${uiState.quantity}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedIconButton(
                                onClick = { viewModel.setQuantity(uiState.quantity + 1) },
                                enabled = uiState.quantity < 10
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }

            // Step 3: Instant Turnaround & Quote Estimator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Text(
                        text = "Step 3: Instant Turnaround & Cost Estimation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Material Weight Estimate")
                        Text(
                            text = "${uiState.estimatedWeightGrams} grams",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Machine Print Time Estimate")
                        val hours = uiState.estimatedPrintTimeHours.toInt()
                        val minutes = ((uiState.estimatedPrintTimeHours - hours) * 60).toInt()
                        Text(
                            text = "${hours}h ${minutes}m",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Filament Cost (at ₹${uiState.selectedMaterial.ratePerGram.toInt()}/g)")
                        Text(
                            text = "₹${(uiState.estimatedWeightGrams * uiState.selectedMaterial.ratePerGram).toInt()}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Machine Bed Setup Fee")
                        Text(
                            text = "₹50",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Estimated Charge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${uiState.estimatedCost.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingGiant))
        }
    }
}
