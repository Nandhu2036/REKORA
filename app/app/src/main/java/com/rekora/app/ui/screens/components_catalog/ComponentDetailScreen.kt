package com.rekora.app.ui.screens.components_catalog

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun ComponentDetailScreen(
    componentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: ComponentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(componentId) {
        viewModel.loadComponent(componentId)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "View Cart",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateToCart()
            }
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Component Details",
                onBackClick = onNavigateBack,
                cartCount = uiState.cartCount,
                onCartClick = onNavigateToCart
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.component != null) {
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
                                    text = if (uiState.selectedMode == AcquisitionMode.RENT) "Rental Fee + Deposit" else "Total Purchase Price",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val calculatedTotal = if (uiState.selectedMode == AcquisitionMode.RENT) {
                                    val rent = (uiState.component?.rentalPricePerWeek ?: 0.0) * uiState.rentalWeeks
                                    val deposit = uiState.component?.securityDeposit ?: 0.0
                                    rent + deposit
                                } else {
                                    (uiState.component?.purchasePrice ?: 0.0) * uiState.quantity
                                }
                                Text(
                                    text = "₹${calculatedTotal.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            RekoraPrimaryButton(
                                text = if (uiState.selectedMode == AcquisitionMode.RENT) "Rent for ${uiState.rentalWeeks} Wks" else "Add to Cart",
                                onClick = viewModel::addToCart,
                                isLoading = uiState.isAddingToCart,
                                icon = Icons.Outlined.AddShoppingCart,
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
        } else if (uiState.component == null) {
            EmptyStateView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                title = "Component not found",
                message = "The requested hardware item could not be retrieved from the campus inventory depot.",
                actionText = "Back to Catalog",
                onAction = onNavigateBack
            )
        } else {
            val comp = uiState.component!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(RekoraDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
            ) {
                // Hero Image Card with Badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RekoraCorners.Card)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    RekoraColors.MidnightNavy,
                                    RekoraColors.DeepBlue
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when {
                                comp.category.contains("MICROCONTROLLER", ignoreCase = true) -> Icons.Outlined.Memory
                                comp.category.contains("SENSOR", ignoreCase = true) -> Icons.Outlined.Sensors
                                comp.category.contains("MOTOR", ignoreCase = true) -> Icons.Outlined.ElectricBolt
                                else -> Icons.Outlined.PrecisionManufacturing
                            },
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = RekoraColors.EngineeringTeal
                        )
                        Spacer(modifier = Modifier.height(RekoraDimens.SpacingSm))
                        Text(
                            text = comp.category.replace("_", " "),
                            style = MaterialTheme.typography.labelMedium,
                            color = RekoraColors.White.copy(alpha = 0.8f)
                        )
                    }

                    // Top Left Condition Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(RekoraDimens.SpacingMd),
                        shape = RekoraCorners.Tag,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = comp.condition.replace("_", " "),
                            modifier = Modifier.padding(
                                horizontal = RekoraDimens.SpacingSm,
                                vertical = RekoraDimens.SpacingXxs
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    // Top Right Institutional Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(RekoraDimens.SpacingMd),
                        shape = RekoraCorners.Tag,
                        color = RekoraColors.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = RekoraDimens.SpacingSm,
                                vertical = RekoraDimens.SpacingXxs
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = RekoraColors.White
                            )
                            Text(
                                text = "Institutional Inventory",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RekoraColors.White
                            )
                        }
                    }

                    // Bottom Tracking ID label
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(RekoraDimens.SpacingMd),
                        shape = RekoraCorners.Tag,
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = RekoraDimens.SpacingSm,
                                vertical = RekoraDimens.SpacingXxs
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = RekoraColors.DarkAccent
                            )
                            Text(
                                text = uiState.inventoryCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.DarkAccent
                            )
                        }
                    }
                }

                // Component Title & Location
                Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)) {
                    Text(
                        text = comp.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RekoraColors.EngineeringTeal
                            )
                            Text(
                                text = comp.campusAvailability ?: "Central Engineering Block - Room 204",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatusBadge(
                            status = "${comp.availableQuantity} in Stock",
                            type = if (comp.availableQuantity > 0) StatusType.SUCCESS else StatusType.ERROR
                        )
                    }

                    Text(
                        text = comp.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = RekoraDimens.SpacingSm)
                    )
                }

                // Acquisition Options: Buy vs Rent
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
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        Text(
                            text = "Choose Acquisition Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                        ) {
                            // Buy Tab Card
                            val isBuy = uiState.selectedMode == AcquisitionMode.BUY
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAcquisitionMode(AcquisitionMode.BUY) },
                                shape = RekoraCorners.CardSmall,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isBuy) RekoraColors.NavyLight else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isBuy) CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                ) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Buy Item",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBuy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        RadioButton(
                                            selected = isBuy,
                                            onClick = { viewModel.setAcquisitionMode(AcquisitionMode.BUY) }
                                        )
                                    }
                                    Text(
                                        text = "₹${comp.purchasePrice.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Permanent lab ownership",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Rent Tab Card
                            if (comp.isRentable) {
                                val isRent = uiState.selectedMode == AcquisitionMode.RENT
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setAcquisitionMode(AcquisitionMode.RENT) },
                                    shape = RekoraCorners.CardSmall,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isRent) RekoraColors.TealLight else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = if (isRent) CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.EngineeringTeal)
                                    ) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Rent Item",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRent) RekoraColors.DeepBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            RadioButton(
                                                selected = isRent,
                                                onClick = { viewModel.setAcquisitionMode(AcquisitionMode.RENT) }
                                            )
                                        }
                                        Text(
                                            text = "₹${comp.rentalPricePerWeek.toInt()}/wk",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RekoraColors.EngineeringTeal
                                        )
                                        Text(
                                            text = "+ ₹${comp.securityDeposit.toInt()} deposit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Buy Options: Quantity Stepper
                        if (uiState.selectedMode == AcquisitionMode.BUY) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Purchase Quantity:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
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
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    OutlinedIconButton(
                                        onClick = { viewModel.setQuantity(uiState.quantity + 1) },
                                        enabled = uiState.quantity < comp.availableQuantity.coerceAtMost(10)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        } else {
                            // Rental Options: Duration Slider & Deposit Breakdown
                            Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rental Duration:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${uiState.rentalWeeks} Weeks (${uiState.rentalWeeks * 7} Days)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RekoraColors.EngineeringTeal
                                    )
                                }

                                Slider(
                                    value = uiState.rentalWeeks.toFloat(),
                                    onValueChange = { viewModel.setRentalWeeks(it.toInt()) },
                                    valueRange = 1f..8f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary
                                    )
                                )

                                // Deposit explanation card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RekoraCorners.CardSmall,
                                    color = RekoraColors.TealLight
                                ) {
                                    Row(
                                        modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Shield,
                                            contentDescription = null,
                                            tint = RekoraColors.EngineeringTeal,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "100% Refundable Security Deposit",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RekoraColors.DeepBlue
                                            )
                                            Text(
                                                text = "₹${comp.securityDeposit.toInt()} is held separately and refunded immediately upon physical inspection at Engineering Block B, Room 204 desk.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = RekoraColors.DeepBlue.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Specifications Table Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RekoraCorners.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(RekoraDimens.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        Text(
                            text = "Technical Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.specifications.isEmpty()) {
                            Text(
                                text = "Standard campus prototyping specification",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RekoraCorners.CardSmall)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RekoraCorners.CardSmall
                                    )
                            ) {
                                uiState.specifications.entries.forEachIndexed { index, (key, value) ->
                                    val isEven = index % 2 == 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .padding(
                                                horizontal = RekoraDimens.SpacingMd,
                                                vertical = RekoraDimens.SpacingSm
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.45f)
                                        )
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(0.55f)
                                        )
                                    }
                                    if (index < uiState.specifications.size - 1) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Compatible Projects Recommendation Section
                if (uiState.compatibleProjects.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                        Text(
                            text = "Compatible Student Projects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This component is part of the verified Bill of Materials (BOM) in these blueprints:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                            uiState.compatibleProjects.forEach { proj ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RekoraCorners.CardSmall,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(RekoraDimens.CardPaddingSmall),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = RekoraColors.Success,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = proj.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${proj.domain} • ${proj.difficulty}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(RekoraDimens.SpacingHuge))
            }
        }
    }
}
