package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.CartRepository
import com.rekora.app.domain.repository.ComponentRepository
import com.rekora.app.domain.model.Component
import com.rekora.app.domain.model.ComponentCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State container for Component Catalog browsing and filtering.
 */
data class ComponentCatalogUiState(
    val components: List<Component> = emptyList(),
    val activeCategory: ComponentCategory? = null,
    val searchQuery: String = "",
    val rentableOnly: Boolean = false,
    val institutionalOnly: Boolean = false,
    val cartCount: Int = 0,
    val isLoading: Boolean = false,
    val totalCount: Int = 0
)

/**
 * ComponentCatalogViewModel provides searchable, category-filtered hardware access:
 * - Search by component name, description, or specification
 * - Category tabs (Microcontrollers, Sensors, Motor Drivers, etc.)
 * - Rental vs Purchase filtering
 * - Direct quick-add to cart
 */
@HiltViewModel
class ComponentCatalogViewModel @Inject constructor(
    private val componentRepository: ComponentRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeCategory = MutableStateFlow<ComponentCategory?>(null)
    val activeCategory: StateFlow<ComponentCategory?> = _activeCategory.asStateFlow()

    private val _rentableOnly = MutableStateFlow(false)
    val rentableOnly: StateFlow<Boolean> = _rentableOnly.asStateFlow()

    private val _institutionalOnly = MutableStateFlow(false)
    val institutionalOnly: StateFlow<Boolean> = _institutionalOnly.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val cartCount: StateFlow<Int> = cartRepository.getCartCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val components: StateFlow<List<Component>> = combine(
        componentRepository.getAllComponents(),
        _searchQuery,
        _activeCategory,
        _rentableOnly,
        _institutionalOnly
    ) { all, query, category, rentable, institutional ->
        var list = all

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { comp ->
                comp.name.lowercase().contains(q) ||
                comp.description.lowercase().contains(q) ||
                comp.category.displayName.lowercase().contains(q)
            }
        }

        if (category != null) {
            list = list.filter { it.category == category }
        }

        if (rentable) {
            list = list.filter { it.isRentable && it.availableQuantity > 0 }
        }

        if (institutional) {
            list = list.filter { it.isInstitutionalInventory }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<ComponentCatalogUiState> = combine(
        components,
        _activeCategory,
        _searchQuery,
        _rentableOnly,
        _institutionalOnly,
        cartCount,
        _isLoading
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val compList = args[0] as List<Component>
        val category = args[1] as? ComponentCategory
        val query = args[2] as String
        val rentable = args[3] as Boolean
        val institutional = args[4] as Boolean
        val count = args[5] as Int
        val loading = args[6] as Boolean

        ComponentCatalogUiState(
            components = compList,
            activeCategory = category,
            searchQuery = query,
            rentableOnly = rentable,
            institutionalOnly = institutional,
            cartCount = count,
            isLoading = loading,
            totalCount = compList.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComponentCatalogUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: ComponentCategory?) {
        _activeCategory.value = if (_activeCategory.value == category) null else category
    }

    fun toggleRentableFilter() {
        _rentableOnly.value = !_rentableOnly.value
    }

    fun toggleInstitutionalFilter() {
        _institutionalOnly.value = !_institutionalOnly.value
    }

    fun quickAddToCart(component: Component, isRental: Boolean = false) {
        viewModelScope.launch {
            cartRepository.addToCart(
                componentId = component.id,
                quantity = 1,
                isRental = isRental,
                rentalDays = if (isRental) 14 else 7
            )
        }
    }
}
