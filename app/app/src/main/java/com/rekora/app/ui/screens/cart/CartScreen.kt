package com.rekora.app.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToComponent: (String) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Hardware Cart?") },
            text = { Text("Are you sure you want to remove all components and rentals from your cart?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCart()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Hardware Cart",
                onBackClick = onNavigateBack,
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear Cart",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
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
                                    text = "Total Payable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${uiState.totalPayable.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            RekoraPrimaryButton(
                                text = "Proceed to Checkout",
                                onClick = onNavigateToCheckout,
                                icon = Icons.Outlined.ArrowForward,
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }
            uiState.items.isEmpty() -> {
                EmptyStateView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    icon = Icons.Outlined.ShoppingCart,
                    title = "Your cart is empty",
                    message = "Browse the campus component depot or rent lab microcontrollers and sensors for your engineering project.",
                    actionText = "Browse Depot",
                    onAction = onNavigateBack
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(
                        horizontal = RekoraDimens.ScreenPadding,
                        vertical = RekoraDimens.SpacingMd
                    ),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                ) {
                    // Header reminder
                    item {
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
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = RekoraColors.EngineeringTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "St. Joseph's Campus Hub: Free pickup at Engineering Block B, Room 204. Rental security deposits are 100% refundable upon return.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RekoraColors.DeepBlue
                                )
                            }
                        }
                    }

                    // Itemized list
                    items(
                        items = uiState.items,
                        key = { it.id }
                    ) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChange = { newQty -> viewModel.updateQuantity(item.id, newQty) },
                            onDelete = { viewModel.deleteItem(item.id) },
                            onClick = { onNavigateToComponent(item.componentId) }
                        )
                    }

                    // Price Breakdown Card
                    item {
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
                                    text = "Payment Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = RekoraDimens.DividerThickness
                                )

                                // Purchase Subtotal
                                BreakdownRow(
                                    label = "Component Purchase Subtotal",
                                    value = "₹${uiState.purchaseSubtotal.toInt()}"
                                )

                                // Rental Subtotal
                                BreakdownRow(
                                    label = "Component Rental Subtotal",
                                    value = "₹${uiState.rentalSubtotal.toInt()}"
                                )

                                // Refundable Security Deposit
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Refundable Security Deposits Total",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RekoraColors.EngineeringTeal
                                        )
                                        Text(
                                            text = "Separated from sales revenue • 100% refundable on return",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "₹${uiState.depositTotal.toInt()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RekoraColors.EngineeringTeal
                                    )
                                }

                                // Delivery / Pickup Fee
                                BreakdownRow(
                                    label = "Campus Pickup (Block B, Room 204)",
                                    value = "FREE (₹0)",
                                    isHighlight = true
                                )

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = RekoraDimens.DividerThickness
                                )

                                // Total Payable
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Payable Amount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${uiState.totalPayable.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(RekoraDimens.SpacingGiant))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemDisplay,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Acquisition Type Badge
                Surface(
                    shape = RekoraCorners.Tag,
                    color = if (item.isRental) RekoraColors.TealLight else RekoraColors.NavyLight
                ) {
                    Text(
                        text = if (item.isRental) "RENTAL ITEM" else "PURCHASE ITEM",
                        modifier = Modifier.padding(
                            horizontal = RekoraDimens.SpacingSm,
                            vertical = RekoraDimens.SpacingXxs
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isRental) RekoraColors.DeepBlue else MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Name & Category
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onClick() }
            )

            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rental specifics
            if (item.isRental) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RekoraCorners.CardSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(RekoraDimens.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Duration: ${item.rentalWeeks} Weeks",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${item.unitPrice.toInt()}/week",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Refundable Deposit:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${item.deposit.toInt()} (Refundable)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RekoraColors.Success
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            // Quantity & Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.isRental) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)
                    ) {
                        OutlinedIconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            modifier = Modifier.size(32.dp),
                            enabled = item.quantity > 1
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        OutlinedIconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(32.dp),
                            enabled = item.quantity < 10
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Text(
                        text = "1 Unit on Loan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Line Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${item.lineTotal.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) RekoraColors.Success else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) RekoraColors.Success else MaterialTheme.colorScheme.onSurface
        )
    }
}
