package com.rekora.app.ui.screens.marketplace

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToListing: (String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    if (showCreateSheet) {
        CreateListingBottomSheet(
            onDismiss = { showCreateSheet = false },
            onSubmit = { title, desc, cat, cond, price, isRent, rentPrice, isBorrow, loc ->
                viewModel.createListing(
                    title = title,
                    description = desc,
                    category = cat,
                    condition = cond,
                    price = price,
                    isRentable = isRent,
                    rentalPrice = rentPrice,
                    isBorrowable = isBorrow,
                    campusLocation = loc
                )
                showCreateSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            RekoraTopBar(
                title = "Student Hardware P2P",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = {
                    Text(
                        text = "Sell / Share Hardware",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search and Banner Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RekoraDimens.ScreenPadding, vertical = RekoraDimens.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                // Academic Integrity & Safety Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RekoraCorners.CardSmall,
                    colors = CardDefaults.cardColors(
                        containerColor = RekoraColors.TealLight
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RekoraColors.EngineeringTeal)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(RekoraDimens.CardPaddingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = RekoraColors.EngineeringTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Academic Integrity & Campus P2P Network",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.DeepBlue
                            )
                            Text(
                                text = "St. Joseph's verified peer-to-peer exchange. Always inspect items in person at the campus project center.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RekoraColors.DeepBlue.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Search Bar
                RekoraSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Search kits, sensors, tools from students..."
                )

                // Transaction Type Selector (Sale, Rental, Free Borrow)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    viewModel.transactionTypes.forEach { (typeKey, typeLabel) ->
                        val isSelected = uiState.selectedType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectType(typeKey) },
                            label = { Text(typeLabel) },
                            shape = RekoraCorners.Chip,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    viewModel.categories.forEach { (catKey, catLabel) ->
                        val isSelected = uiState.selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(catKey) },
                            label = { Text(catLabel) },
                            shape = RekoraCorners.Chip
                        )
                    }
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RekoraDimens.ScreenPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredListings.size} Listings Available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "St. Joseph's Verified Students",
                    style = MaterialTheme.typography.labelSmall,
                    color = RekoraColors.EngineeringTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Listings List / Empty
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(RekoraDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                    ) {
                        repeat(4) { MarketplaceCardSkeleton() }
                    }
                }
                uiState.filteredListings.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Storefront,
                        title = "No hardware listings found",
                        message = "No student listings match your current filters. Post the first unused sensor or kit on the campus network!",
                        actionText = "Post Hardware",
                        onAction = { showCreateSheet = true }
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
                            items = uiState.filteredListings,
                            key = { it.id }
                        ) { listing ->
                            MarketplaceItemCard(
                                title = listing.title,
                                description = listing.description,
                                category = listing.category.replace("_", " "),
                                condition = listing.condition.replace("_", " "),
                                price = listing.price,
                                isRentable = listing.isRentable,
                                rentalPrice = listing.rentalPricePerWeek,
                                isBorrowable = listing.isBorrowable,
                                sellerName = listing.sellerName,
                                location = listing.campusLocation ?: "Main Campus",
                                onClick = { onNavigateToListing(listing.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceItemCard(
    title: String,
    description: String,
    category: String,
    condition: String,
    price: Double,
    isRentable: Boolean,
    rentalPrice: Double,
    isBorrowable: Boolean,
    sellerName: String,
    location: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = RekoraDimens.CardElevation),
        onClick = onClick
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
                // Transaction Type Badge
                val (badgeText, badgeColor, badgeTextColor) = when {
                    isBorrowable -> Triple("FREE BORROW", RekoraColors.SuccessLight, RekoraColors.Success)
                    isRentable -> Triple("RENTAL AVAILABLE", RekoraColors.TealLight, RekoraColors.DeepBlue)
                    else -> Triple("FOR SALE", RekoraColors.NavyLight, MaterialTheme.colorScheme.primary)
                }

                Surface(
                    shape = RekoraCorners.Tag,
                    color = badgeColor
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }

                StatusBadge(status = condition, type = StatusType.INFO)
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Seller Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = sellerName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Room,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(horizontalAlignment = Alignment.End) {
                    when {
                        isBorrowable -> {
                            Text(
                                text = "FREE BORROW",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.Success
                            )
                        }
                        isRentable && price > 0 -> {
                            Text(
                                text = "₹${price.toInt()} Buy / ₹${rentalPrice.toInt()}/wk Rent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        isRentable -> {
                            Text(
                                text = "₹${rentalPrice.toInt()}/week",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RekoraColors.EngineeringTeal
                            )
                        }
                        else -> {
                            Text(
                                text = "₹${price.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        category: String,
        condition: String,
        price: Double,
        isRentable: Boolean,
        rentalPrice: Double,
        isBorrowable: Boolean,
        campusLocation: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("MICROCONTROLLER") }
    var selectedCondition by remember { mutableStateOf("LIKE_NEW") }
    var priceText by remember { mutableStateOf("450") }
    var rentalPriceText by remember { mutableStateOf("50") }
    var isRentable by remember { mutableStateOf(false) }
    var isBorrowable by remember { mutableStateOf(false) }
    var campusLocation by remember { mutableStateOf("Hostel Block B, Room 304") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
        ) {
            Text(
                text = "Sell or Share Student Hardware",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "List your unused microcontroller, sensors, kits, or tools for campus classmates at St. Joseph's.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Item Title") },
                placeholder = { Text("e.g. Raspberry Pi Zero 2W with Header Pins") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Notes") },
                placeholder = { Text("Used for 1 semester IoT project. Works perfectly.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Category Chips
            Text(
                text = "Category:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "MICROCONTROLLER" to "Microcontrollers",
                    "SENSOR" to "Sensors",
                    "ROBOTICS_KIT" to "Kits",
                    "TOOL" to "Tools",
                    "FILAMENT" to "Filament"
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = selectedCategory == code,
                        onClick = { selectedCategory = code },
                        label = { Text(label) }
                    )
                }
            }

            // Condition Chips
            Text(
                text = "Condition:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("NEW", "LIKE_NEW", "GOOD", "FAIR").forEach { cond ->
                    FilterChip(
                        selected = selectedCondition == cond,
                        onClick = { selectedCondition = cond },
                        label = { Text(cond.replace("_", " ")) }
                    )
                }
            }

            // Mode checkboxes: Borrowable or Rentable
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Offer as Free Borrow (Campus Goodwill)")
                Switch(
                    checked = isBorrowable,
                    onCheckedChange = { isBorrowable = it }
                )
            }

            if (!isBorrowable) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Outright Selling Price (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Allow Weekly Peer Rental")
                    Switch(
                        checked = isRentable,
                        onCheckedChange = { isRentable = it }
                    )
                }

                if (isRentable) {
                    OutlinedTextField(
                        value = rentalPriceText,
                        onValueChange = { rentalPriceText = it },
                        label = { Text("Rental Price per Week (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = campusLocation,
                onValueChange = { campusLocation = it },
                label = { Text("Handover Location on Campus") },
                placeholder = { Text("Hostel Block B, Room 304 or Central Library") },
                modifier = Modifier.fillMaxWidth()
            )

            RekoraPrimaryButton(
                text = "Publish Campus Listing",
                onClick = {
                    if (title.isNotBlank()) {
                        onSubmit(
                            title,
                            description,
                            selectedCategory,
                            selectedCondition,
                            priceText.toDoubleOrNull() ?: 0.0,
                            isRentable,
                            rentalPriceText.toDoubleOrNull() ?: 0.0,
                            isBorrowable,
                            campusLocation
                        )
                    }
                },
                enabled = title.isNotBlank()
            )

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingXxl))
        }
    }
}

@Composable
fun MarketplaceCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RekoraCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RekoraDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(width = 90.dp, height = 20.dp, cornerRadius = 6.dp)
                ShimmerBox(width = 60.dp, height = 20.dp, cornerRadius = 6.dp)
            }
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 20.dp)
            ShimmerBox(width = 200.dp, height = 14.dp)
            ShimmerBox(width = 140.dp, height = 14.dp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(width = 80.dp, height = 16.dp)
                ShimmerBox(width = 90.dp, height = 20.dp)
            }
        }
    }
}

