package com.rekora.app.ui.screens.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.MarketplaceDao
import com.rekora.app.data.local.entity.MarketplaceListingEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MarketplaceUiState(
    val allListings: List<MarketplaceListingEntity> = emptyList(),
    val filteredListings: List<MarketplaceListingEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "ALL",
    val selectedType: String = "ALL", // "ALL", "SALE", "RENTAL", "BORROW"
    val isLoading: Boolean = true,
    val isPosting: Boolean = false,
    val userNotification: String? = null
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceDao: MarketplaceDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    val categories = listOf(
        "ALL" to "All Categories",
        "MICROCONTROLLER" to "Microcontrollers",
        "SENSOR" to "Sensors",
        "ROBOTICS_KIT" to "Kits & Starter Packs",
        "TOOL" to "Tools & Equipment",
        "FILAMENT" to "3D Filaments",
        "MECHANICAL" to "Mechanical Parts"
    )

    val transactionTypes = listOf(
        "ALL" to "All Deals",
        "SALE" to "Direct Sale",
        "RENTAL" to "Student Rental",
        "BORROW" to "Free Borrow"
    )

    init {
        ensureSeededListings()
        observeListings()
    }

    private fun ensureSeededListings() {
        viewModelScope.launch {
            try {
                val existing = marketplaceDao.getActiveListings().first()
                if (existing.isEmpty()) {
                    val seeds = SeedDataProvider.getSeedMarketplaceListings()
                    seeds.forEach { marketplaceDao.insertListing(it) }
                }
            } catch (e: Exception) {
                // Ignore seed error
            }
        }
    }

    private fun observeListings() {
        viewModelScope.launch {
            marketplaceDao.getActiveListings()
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { list ->
                    val finalItems = if (list.isEmpty()) {
                        SeedDataProvider.getSeedMarketplaceListings()
                    } else {
                        list
                    }
                    _uiState.update {
                        it.copy(
                            allListings = finalItems,
                            isLoading = false
                        )
                    }
                    applyFilters()
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun selectCategory(cat: String) {
        _uiState.update { it.copy(selectedCategory = cat) }
        applyFilters()
    }

    fun selectType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val cat = state.selectedCategory
        val type = state.selectedType

        val filtered = state.allListings.filter { listing ->
            val matchesCategory = if (cat == "ALL") true else listing.category.equals(cat, ignoreCase = true)

            val matchesType = when (type) {
                "SALE" -> listing.price > 0 && !listing.isBorrowable
                "RENTAL" -> listing.isRentable
                "BORROW" -> listing.isBorrowable
                else -> true
            }

            val matchesQuery = if (query.isEmpty()) true else {
                listing.title.lowercase().contains(query) ||
                        listing.description.lowercase().contains(query) ||
                        listing.sellerName.lowercase().contains(query) ||
                        (listing.campusLocation?.lowercase()?.contains(query) ?: false)
            }

            matchesCategory && matchesType && matchesQuery
        }

        _uiState.update { it.copy(filteredListings = filtered) }
    }

    fun createListing(
        title: String,
        description: String,
        category: String,
        condition: String,
        price: Double,
        isRentable: Boolean,
        rentalPrice: Double,
        isBorrowable: Boolean,
        campusLocation: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPosting = true) }

            val newListing = MarketplaceListingEntity(
                id = "list-${UUID.randomUUID().toString().take(8)}",
                sellerId = "usr-current-student",
                sellerName = "Rogith Kumar R (Final Year ECE)",
                title = title,
                description = description,
                category = category,
                condition = condition,
                images = """["https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80"]""",
                price = if (isBorrowable) 0.0 else price,
                isRentable = isRentable,
                rentalPricePerWeek = if (isRentable) rentalPrice else 0.0,
                isBorrowable = isBorrowable,
                campusLocation = campusLocation.ifBlank { "St. Joseph's Engineering Block B" },
                status = "ACTIVE",
                isDemo = true,
                createdAt = System.currentTimeMillis()
            )

            marketplaceDao.insertListing(newListing)

            _uiState.update {
                it.copy(
                    isPosting = false,
                    userNotification = "Listing published successfully on campus network!"
                )
            }
        }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }
}
