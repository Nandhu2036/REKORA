package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.MarketplaceRepository
import com.rekora.app.domain.model.ComponentCategory
import com.rekora.app.domain.model.ItemCondition
import com.rekora.app.domain.model.ListingStatus
import com.rekora.app.domain.model.MarketplaceListing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State container for peer-to-peer campus hardware listings.
 */
data class MarketplaceUiState(
    val listings: List<MarketplaceListing> = emptyList(),
    val userListings: List<MarketplaceListing> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ComponentCategory? = null,
    val selectedCondition: ItemCondition? = null,
    val rentableOnly: Boolean = false,
    val borrowableOnly: Boolean = false,
    // Create new listing form state
    val newTitle: String = "",
    val newDescription: String = "",
    val newCategory: ComponentCategory = ComponentCategory.OTHER,
    val newCondition: ItemCondition = ItemCondition.GOOD,
    val newPrice: Double = 0.0,
    val isNewRentable: Boolean = false,
    val newRentalPricePerWeek: Double = 0.0,
    val isNewBorrowable: Boolean = false,
    val campusLocation: String = "St. Joseph's Campus - Main Library / ECE Block",
    val isSubmitting: Boolean = false,
    val creationSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * MarketplaceViewModel powers the student peer-to-peer hardware circulation network:
 * - Search and category/condition filters
 * - Buy, Rent, or Borrow acquisition modes
 * - Form submission for senior/alumni hardware listings
 */
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<ComponentCategory?>(null)
    private val _selectedCondition = MutableStateFlow<ItemCondition?>(null)
    private val _rentableOnly = MutableStateFlow(false)
    private val _borrowableOnly = MutableStateFlow(false)

    // Creation form flows
    private val _newTitle = MutableStateFlow("")
    private val _newDescription = MutableStateFlow("")
    private val _newCategory = MutableStateFlow(ComponentCategory.MICROCONTROLLER)
    private val _newCondition = MutableStateFlow(ItemCondition.GOOD)
    private val _newPrice = MutableStateFlow(0.0)
    private val _isNewRentable = MutableStateFlow(false)
    private val _newRentalPricePerWeek = MutableStateFlow(0.0)
    private val _isNewBorrowable = MutableStateFlow(false)
    private val _campusLocation = MutableStateFlow("St. Joseph's Campus - Main Library / ECE Block")
    private val _isSubmitting = MutableStateFlow(false)
    private val _creationSuccess = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val activeListings: StateFlow<List<MarketplaceListing>> = combine(
        marketplaceRepository.getActiveListings(),
        _searchQuery,
        _selectedCategory,
        _selectedCondition,
        _rentableOnly,
        _borrowableOnly
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val all = args[0] as List<MarketplaceListing>
        val query = args[1] as String
        val category = args[2] as com.rekora.app.domain.model.ComponentCategory?
        val condition = args[3] as com.rekora.app.domain.model.ItemCondition?
        val rentable = args[4] as Boolean
        val borrowable = args[5] as Boolean

        var list = all

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.displayName.lowercase().contains(q)
            }
        }

        if (category != null) {
            list = list.filter { it.category == category }
        }

        if (condition != null) {
            list = list.filter { it.condition == condition }
        }

        if (rentable) {
            list = list.filter { it.isRentable }
        }

        if (borrowable) {
            list = list.filter { it.isBorrowable }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userListings: StateFlow<List<MarketplaceListing>> = authRepository.currentUser
        .flatMapLatest { user ->
            val userId = user?.id ?: "guest-student"
            marketplaceRepository.getListingsForUser(userId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<MarketplaceUiState> = combine(
        activeListings,
        userListings,
        _searchQuery,
        _selectedCategory,
        _selectedCondition,
        _rentableOnly,
        _borrowableOnly,
        _newTitle,
        _newDescription,
        _newCategory,
        _newCondition,
        _newPrice,
        _isNewRentable,
        _newRentalPricePerWeek,
        _isNewBorrowable,
        _campusLocation,
        _isSubmitting,
        _creationSuccess,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val active = args[0] as List<MarketplaceListing>
        val myItems = args[1] as List<MarketplaceListing>
        val q = args[2] as String
        val cat = args[3] as ComponentCategory?
        val cond = args[4] as ItemCondition?
        val rent = args[5] as Boolean
        val borrow = args[6] as Boolean
        val title = args[7] as String
        val desc = args[8] as String
        val newCat = args[9] as ComponentCategory
        val newCond = args[10] as ItemCondition
        val price = args[11] as Double
        val isRent = args[12] as Boolean
        val rentPrice = args[13] as Double
        val isBorrow = args[14] as Boolean
        val loc = args[15] as String
        val sub = args[16] as Boolean
        val succ = args[17] as Boolean
        val err = args[18] as String?

        MarketplaceUiState(
            listings = active,
            userListings = myItems,
            searchQuery = q,
            selectedCategory = cat,
            selectedCondition = cond,
            rentableOnly = rent,
            borrowableOnly = borrow,
            newTitle = title,
            newDescription = desc,
            newCategory = newCat,
            newCondition = newCond,
            newPrice = price,
            isNewRentable = isRent,
            newRentalPricePerWeek = rentPrice,
            isNewBorrowable = isBorrow,
            campusLocation = loc,
            isSubmitting = sub,
            creationSuccess = succ,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MarketplaceUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: ComponentCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun onConditionSelect(condition: ItemCondition?) {
        _selectedCondition.value = if (_selectedCondition.value == condition) null else condition
    }

    fun toggleRentableFilter() {
        _rentableOnly.value = !_rentableOnly.value
    }

    fun toggleBorrowableFilter() {
        _borrowableOnly.value = !_borrowableOnly.value
    }

    fun setNewTitle(title: String) {
        _newTitle.value = title
    }

    fun setNewDescription(description: String) {
        _newDescription.value = description
    }

    fun setNewCategory(category: ComponentCategory) {
        _newCategory.value = category
    }

    fun setNewCondition(condition: ItemCondition) {
        _newCondition.value = condition
    }

    fun setNewPrice(price: Double) {
        _newPrice.value = price.coerceAtLeast(0.0)
    }

    fun setNewRentable(rentable: Boolean, rentalRate: Double = 0.0) {
        _isNewRentable.value = rentable
        if (rentalRate > 0.0) _newRentalPricePerWeek.value = rentalRate
    }

    fun setNewRentalPricePerWeek(price: Double) {
        _newRentalPricePerWeek.value = price.coerceAtLeast(0.0)
    }

    fun setNewBorrowable(borrowable: Boolean) {
        _isNewBorrowable.value = borrowable
    }

    fun setCampusLocation(location: String) {
        _campusLocation.value = location
    }

    fun submitListing(onSuccess: (String) -> Unit) {
        val title = _newTitle.value.trim()
        val desc = _newDescription.value.trim()

        if (title.isBlank()) {
            _error.value = "Hardware listing title is required."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"
                val userName = user?.displayName ?: "Student Maker"

                val newListing = MarketplaceListing(
                    id = "mkt-" + UUID.randomUUID().toString().take(8),
                    sellerId = userId,
                    sellerName = userName,
                    title = title,
                    description = desc,
                    category = _newCategory.value,
                    condition = _newCondition.value,
                    price = _newPrice.value,
                    isRentable = _isNewRentable.value,
                    rentalPricePerWeek = _newRentalPricePerWeek.value,
                    isBorrowable = _isNewBorrowable.value,
                    campusLocation = _campusLocation.value,
                    status = ListingStatus.ACTIVE,
                    isDemo = true,
                    createdAt = System.currentTimeMillis()
                )

                val createdId = marketplaceRepository.createListing(newListing)
                _creationSuccess.value = true
                resetCreateForm()
                onSuccess(createdId)
            } catch (e: Exception) {
                _error.value = "Submission error: ${e.localizedMessage}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun resetCreateForm() {
        _newTitle.value = ""
        _newDescription.value = ""
        _newPrice.value = 0.0
        _isNewRentable.value = false
        _newRentalPricePerWeek.value = 0.0
        _isNewBorrowable.value = false
        _creationSuccess.value = false
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
