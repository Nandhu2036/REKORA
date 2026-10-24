package com.rekora.app.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderComplete: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Campus Checkout",
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
                                text = "Total to Authorize",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${uiState.totalAmount.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        RekoraPrimaryButton(
                            text = if (uiState.isProcessing) "Authorizing..." else "Confirm & Pay ₹${uiState.totalAmount.toInt()}",
                            onClick = {
                                viewModel.processOrder(onComplete = onOrderComplete)
                            },
                            isLoading = uiState.isProcessing,
                            icon = Icons.Outlined.Lock,
                            modifier = Modifier.width(220.dp)
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
            // Demo Mode Disclosure Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.CardSmall,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.Warning)
                )
            ) {
                Row(
                    modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = "Demo Mode",
                        tint = RekoraColors.Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Institutional Pilot Demonstration Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RekoraColors.Warning
                        )
                        Text(
                            text = "No actual monetary transaction will be charged to your real bank account. This transaction executes in the St. Joseph's College campus testbed environment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Step 1: Delivery / Pickup Method Selection
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
                        text = "1. Delivery / Handover Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Option A: Campus Pickup
                    val isPickup = uiState.deliveryMethod == DeliveryChoice.CAMPUS_PICKUP
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDeliveryChoice(DeliveryChoice.CAMPUS_PICKUP) },
                        shape = RekoraCorners.CardSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPickup) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isPickup) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.CardPaddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            RadioButton(
                                selected = isPickup,
                                onClick = { viewModel.setDeliveryChoice(DeliveryChoice.CAMPUS_PICKUP) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "St. Joseph's Campus Pickup",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "FREE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RekoraColors.Success
                                    )
                                }
                                Text(
                                    text = "Engineering Block B, Room 204 (Central Depot Desk)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Collect on working days 9:00 AM – 5:00 PM with your Student ID QR code.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Option B: Lab Delivery
                    val isLabDelivery = uiState.deliveryMethod == DeliveryChoice.LAB_DELIVERY
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDeliveryChoice(DeliveryChoice.LAB_DELIVERY) },
                        shape = RekoraCorners.CardSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLabDelivery) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isLabDelivery) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                        ) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.CardPaddingSmall),
                            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                            ) {
                                RadioButton(
                                    selected = isLabDelivery,
                                    onClick = { viewModel.setDeliveryChoice(DeliveryChoice.LAB_DELIVERY) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Direct Hostel / Campus Lab Delivery",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Internal campus runner delivery directly to your research lab or hostel desk.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isLabDelivery) {
                                OutlinedTextField(
                                    value = uiState.campusDeliveryAddress,
                                    onValueChange = viewModel::setCampusAddress,
                                    label = { Text("Campus Room / Lab Location") },
                                    placeholder = { Text("e.g. Mechanical CAD Lab, Block A Room 102") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Payment Option Selection
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
                        text = "2. Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Option 1: Simulated UPI Sandbox
                    val isUpi = uiState.paymentMethod == PaymentChoice.UPI_SANDBOX
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPaymentChoice(PaymentChoice.UPI_SANDBOX) },
                        shape = RekoraCorners.CardSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUpi) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isUpi) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
                        ) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.CardPaddingSmall),
                            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                            ) {
                                RadioButton(
                                    selected = isUpi,
                                    onClick = { viewModel.setPaymentChoice(PaymentChoice.UPI_SANDBOX) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "UPI / Net Banking (Sandbox Simulator)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RekoraCorners.Tag,
                                            color = MaterialTheme.colorScheme.secondary
                                        ) {
                                            Text(
                                                text = "DEV MODE",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Instant simulated settlement with realistic authentication latency.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isUpi) {
                                OutlinedTextField(
                                    value = uiState.upiId,
                                    onValueChange = viewModel::setUpiId,
                                    label = { Text("Virtual Payment Address / UPI ID") },
                                    placeholder = { Text("student@sjc.ac.in") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }

                    // Option 2: Cash on Campus Pickup
                    val isCash = uiState.paymentMethod == PaymentChoice.CASH_ON_PICKUP
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPaymentChoice(PaymentChoice.CASH_ON_PICKUP) },
                        shape = RekoraCorners.CardSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCash) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isCash) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(RekoraDimens.CardPaddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            RadioButton(
                                selected = isCash,
                                onClick = { viewModel.setPaymentChoice(PaymentChoice.CASH_ON_PICKUP) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cash on Campus Pickup / Lab Desk",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pay in cash at Engineering Block B Room 204 desk when collecting components.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Order Review Summary Card
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
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Text(
                        text = "3. Order Items Summary (${uiState.items.size} items)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    uiState.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (item.isRental) "Rental: ${item.rentalWeeks} Wks + ₹${item.deposit.toInt()} Deposit" else "Qty: ${item.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₹${item.lineTotal.toInt()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Refundable Deposit Subtotal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹${uiState.depositTotal.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delivery / Handover Fee",
                            style = MaterialTheme.typography.bodySmall,
                            color = RekoraColors.Success,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹0 (Free)",
                            style = MaterialTheme.typography.bodySmall,
                            color = RekoraColors.Success,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingGiant))
        }
    }
}
