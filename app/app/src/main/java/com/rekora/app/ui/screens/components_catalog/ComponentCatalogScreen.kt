package com.rekora.app.ui.screens.components_catalog

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
fun ComponentCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComponent: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: ComponentCatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Component Depot",
                onBackClick = onNavigateBack,
                cartCount = uiState.cartCount,
                onCartClick = onNavigateToCart
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RekoraDimens.ScreenPadding, vertical = RekoraDimens.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                RekoraSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Search sensors, MCUs, drivers, motors..."
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    viewModel.categories.forEach { category ->
                        val isSelected = uiState.selectedCategory == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelect(category.id) },
                            label = {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            shape = RekoraCorners.Chip,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                // Rentable Only Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RekoraCorners.CardSmall,
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.showRentableOnly) {
                            RekoraColors.TealLight
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (uiState.showRentableOnly) RekoraColors.EngineeringTeal else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = RekoraDimens.CardPaddingSmall, vertical = RekoraDimens.SpacingSm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Autorenew,
                                contentDescription = null,
                                tint = if (uiState.showRentableOnly) RekoraColors.EngineeringTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(RekoraDimens.IconSize)
                            )
                            Column {
                                Text(
                                    text = "Show Rentable Only",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.showRentableOnly) RekoraColors.DeepBlue else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Save money on student prototypes by renting campus lab inventory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = uiState.showRentableOnly,
                            onCheckedChange = viewModel::onToggleRentableOnly,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Results count and Campus Depot tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RekoraDimens.ScreenPadding, vertical = RekoraDimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredComponents.size} Components Available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = RekoraColors.EngineeringTeal
                    )
                    Text(
                        text = "St. Joseph's Central Depot",
                        style = MaterialTheme.typography.labelSmall,
                        color = RekoraColors.EngineeringTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Content List / Loading / Empty
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(RekoraDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        repeat(4) {
                            ComponentCardSkeleton()
                        }
                    }
                }
                uiState.filteredComponents.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Inventory2,
                        title = "No components found",
                        message = if (uiState.showRentableOnly) {
                            "No rentable components match your current filters. Try turning off 'Show Rentable Only' or clearing the search query."
                        } else {
                            "Try searching with a different keyword or selecting a different category."
                        },
                        actionText = "Clear Filters",
                        onAction = {
                            viewModel.onSearchQueryChange("")
                            viewModel.onCategorySelect("ALL")
                            viewModel.onToggleRentableOnly(false)
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = RekoraDimens.ScreenPadding,
                            vertical = RekoraDimens.SpacingSm
                        ),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.ListItemSpacing)
                    ) {
                        items(
                            items = uiState.filteredComponents,
                            key = { it.id }
                        ) { comp ->
                            val inStock = comp.availableQuantity > 0
                            val formattedPrice = "₹${comp.purchasePrice.toInt()}"
                            val formattedRental = if (comp.isRentable) "₹${comp.rentalPricePerWeek.toInt()}" else null

                            ComponentCatalogCard(
                                name = comp.name,
                                category = comp.category.replace("_", " "),
                                price = formattedPrice,
                                rentalPrice = formattedRental,
                                condition = comp.condition.replace("_", " "),
                                inStock = inStock,
                                availableCount = comp.availableQuantity,
                                isRentable = comp.isRentable,
                                isDemo = comp.isDemo,
                                campusLocation = comp.campusAvailability ?: "Block B, Room 204",
                                onClick = { onNavigateToComponent(comp.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(RekoraDimens.SpacingXxl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentCatalogCard(
    name: String,
    category: String,
    price: String,
    rentalPrice: String? = null,
    condition: String = "NEW",
    inStock: Boolean = true,
    availableCount: Int = 0,
    isRentable: Boolean = false,
    isDemo: Boolean = true,
    campusLocation: String = "Block B, Room 204",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = RekoraDimens.CardElevation
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd),
                verticalAlignment = Alignment.Top
            ) {
                // Icon / Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RekoraCorners.Image)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            category.contains("MICROCONTROLLER", ignoreCase = true) -> Icons.Outlined.Memory
                            category.contains("SENSOR", ignoreCase = true) -> Icons.Outlined.Sensors
                            category.contains("MOTOR", ignoreCase = true) -> Icons.Outlined.ElectricBolt
                            category.contains("BATTERY", ignoreCase = true) -> Icons.Outlined.BatteryChargingFull
                            category.contains("TOOL", ignoreCase = true) -> Icons.Outlined.Build
                            else -> Icons.Outlined.PrecisionManufacturing
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Details column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )

                        if (isDemo) {
                            Surface(
                                shape = RekoraCorners.Tag,
                                color = RekoraColors.WarningLight
                            ) {
                                Text(
                                    text = "DEMO PILOT",
                                    modifier = Modifier.padding(
                                        horizontal = RekoraDimens.SpacingXs,
                                        vertical = RekoraDimens.SpacingXxs
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RekoraColors.Warning
                                )
                            }
                        }
                    }

                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Location tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Room,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = campusLocation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = RekoraDimens.SpacingSm),
                thickness = RekoraDimens.DividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Pricing and Condition row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges: Stock and Condition
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        status = if (inStock) "$availableCount In Stock" else "Out of Stock",
                        type = if (inStock) StatusType.SUCCESS else StatusType.ERROR
                    )
                    StatusBadge(
                        status = condition,
                        type = StatusType.INFO
                    )
                }

                // Pricing column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$price Buy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isRentable && rentalPrice != null) {
                        Text(
                            text = "$rentalPrice/week Rent",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
        ) {
            ShimmerBox(width = 64.dp, height = 64.dp, cornerRadius = 12.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                ShimmerBox(width = 80.dp, height = 14.dp)
                ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 18.dp)
                ShimmerBox(width = 140.dp, height = 14.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerBox(width = 90.dp, height = 20.dp)
                    ShimmerBox(width = 70.dp, height = 20.dp)
                }
            }
        }
    }
}
