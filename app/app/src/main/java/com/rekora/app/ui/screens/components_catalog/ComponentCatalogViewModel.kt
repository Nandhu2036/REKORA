package com.rekora.app.ui.screens.components_catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.entity.ComponentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComponentCategoryFilter(
    val id: String,
    val displayName: String
)

data class ComponentCatalogUiState(
    val allComponents: List<ComponentEntity> = emptyList(),
    val filteredComponents: List<ComponentEntity> = emptyList(),
    val selectedCategory: String = "ALL",
    val showRentableOnly: Boolean = false,
    val searchQuery: String = "",
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ComponentCatalogViewModel @Inject constructor(
    private val componentDao: ComponentDao,
    private val cartDao: CartDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComponentCatalogUiState())
    val uiState: StateFlow<ComponentCatalogUiState> = _uiState.asStateFlow()

    val categories = listOf(
        ComponentCategoryFilter("ALL", "All"),
        ComponentCategoryFilter("MICROCONTROLLER", "Microcontrollers"),
        ComponentCategoryFilter("SENSOR", "Sensors"),
        ComponentCategoryFilter("MOTOR_DRIVER", "Motor Drivers"),
        ComponentCategoryFilter("MOTOR", "Motors"),
        ComponentCategoryFilter("BATTERY", "Batteries"),
        ComponentCategoryFilter("MECHANICAL", "Mechanical"),
        ComponentCategoryFilter("FASTENER", "Fasteners"),
        ComponentCategoryFilter("FILAMENT", "Filaments"),
        ComponentCategoryFilter("TOOL", "Tools")
    )

    init {
        ensureSeededData()
        observeComponents()
        observeCartCount()
    }

    private fun ensureSeededData() {
        viewModelScope.launch {
            try {
                // If database has no components, pre-seed with rich demo components
                val firstBatch = componentDao.getAllComponents().first()
                if (firstBatch.isEmpty()) {
                    val seeds = SeedDataProvider.getSeedComponents()
                    componentDao.insertComponents(seeds)
                }
            } catch (e: Exception) {
                // Seed fallback handled
            }
        }
    }

    private fun observeComponents() {
        viewModelScope.launch {
            componentDao.getAllComponents()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { list ->
                    val components = if (list.isEmpty()) {
                        SeedDataProvider.getSeedComponents()
                    } else {
                        list
                    }
                    _uiState.update { state ->
                        state.copy(
                            allComponents = components,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    applyFilters()
                }
        }
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            cartDao.getCartCount()
                .catch { emit(0) }
                .collect { count ->
                    _uiState.update { it.copy(cartCount = count) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onCategorySelect(categoryId: String) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
        applyFilters()
    }

    fun onToggleRentableOnly(enabled: Boolean) {
        _uiState.update { it.copy(showRentableOnly = enabled) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val cat = state.selectedCategory
        val rentableOnly = state.showRentableOnly

        val filtered = state.allComponents.filter { comp ->
            // Category filter
            val matchesCategory = if (cat == "ALL") true else {
                comp.category.equals(cat, ignoreCase = true)
            }

            // Rentable filter
            val matchesRentable = if (rentableOnly) comp.isRentable else true

            // Search query filter
            val matchesQuery = if (query.isEmpty()) true else {
                comp.name.lowercase().contains(query) ||
                        comp.description.lowercase().contains(query) ||
                        comp.category.lowercase().contains(query)
            }

            matchesCategory && matchesRentable && matchesQuery
        }

        _uiState.update { it.copy(filteredComponents = filtered) }
    }
}
