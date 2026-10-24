package com.rekora.app.ui.screens.components_catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.CartDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.dao.InventoryItemDao
import com.rekora.app.data.local.dao.ProjectDao
import com.rekora.app.data.local.entity.CartItemEntity
import com.rekora.app.data.local.entity.ComponentEntity
import com.rekora.app.data.local.entity.ProjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

enum class AcquisitionMode {
    BUY, RENT
}

data class ComponentDetailUiState(
    val component: ComponentEntity? = null,
    val specifications: Map<String, String> = emptyMap(),
    val compatibleProjects: List<ProjectEntity> = emptyList(),
    val inventoryCode: String = "RK-DEPOT-001",
    val selectedMode: AcquisitionMode = AcquisitionMode.BUY,
    val quantity: Int = 1,
    val rentalWeeks: Int = 2,
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val isAddingToCart: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class ComponentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val componentDao: ComponentDao,
    private val cartDao: CartDao,
    private val inventoryItemDao: InventoryItemDao,
    private val projectDao: ProjectDao
) : ViewModel() {

    private val componentIdFromArgs: String? = savedStateHandle["componentId"]

    private val _uiState = MutableStateFlow(ComponentDetailUiState())
    val uiState: StateFlow<ComponentDetailUiState> = _uiState.asStateFlow()

    init {
        observeCartCount()
        componentIdFromArgs?.let { loadComponent(it) }
    }

    fun loadComponent(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val comp = componentDao.getComponentById(id)
                ?: SeedDataProvider.getSeedComponents().find { it.id == id }

            if (comp != null) {
                // Parse specifications JSON
                val specMap = mutableMapOf<String, String>()
                try {
                    val jsonObj = JSONObject(comp.specifications)
                    val keys = jsonObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        specMap[key] = jsonObj.optString(key, "")
                    }
                } catch (e: Exception) {
                    specMap["Specification"] = "Standard institutional educational component"
                }

                // Parse compatible project IDs
                val compatibleList = mutableListOf<ProjectEntity>()
                try {
                    val jsonArr = JSONArray(comp.compatibleProjects)
                    for (i in 0 until jsonArr.length()) {
                        val projId = jsonArr.getString(i)
                        val proj = projectDao.getProjectById(projId)
                            ?: SeedDataProvider.getSeedProjects().find { it.id == projId }
                        if (proj != null) {
                            compatibleList.add(proj)
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to empty
                }

                // Inventory tracking QR label code
                val code = "RK-${comp.category.take(3).uppercase()}-${comp.id.takeLast(4).uppercase()}"

                _uiState.update {
                    it.copy(
                        component = comp,
                        specifications = specMap,
                        compatibleProjects = compatibleList,
                        inventoryCode = code,
                        selectedMode = if (comp.isRentable) AcquisitionMode.BUY else AcquisitionMode.BUY,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, userMessage = "Component not found") }
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

    fun setAcquisitionMode(mode: AcquisitionMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setQuantity(qty: Int) {
        if (qty in 1..10) {
            _uiState.update { it.copy(quantity = qty) }
        }
    }

    fun setRentalWeeks(weeks: Int) {
        if (weeks in 1..8) {
            _uiState.update { it.copy(rentalWeeks = weeks) }
        }
    }

    fun addToCart() {
        val comp = _uiState.value.component ?: return
        val mode = _uiState.value.selectedMode
        val isRental = mode == AcquisitionMode.RENT
        val qty = if (isRental) 1 else _uiState.value.quantity
        val weeks = _uiState.value.rentalWeeks

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true) }

            val cartItem = CartItemEntity(
                id = UUID.randomUUID().toString(),
                componentId = comp.id,
                quantity = qty,
                isRental = isRental,
                rentalDays = if (isRental) weeks * 7 else 0,
                unitPrice = if (isRental) comp.rentalPricePerWeek else comp.purchasePrice,
                deposit = if (isRental) comp.securityDeposit else 0.0
            )

            cartDao.insertCartItem(cartItem)

            _uiState.update {
                it.copy(
                    isAddingToCart = false,
                    userMessage = if (isRental) {
                        "Added ${comp.name} ($weeks wk rental) to cart"
                    } else {
                        "Added $qty x ${comp.name} to cart"
                    }
                )
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
