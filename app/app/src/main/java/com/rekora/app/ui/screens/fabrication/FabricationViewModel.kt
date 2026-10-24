package com.rekora.app.ui.screens.fabrication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.local.dao.FabricationJobDao
import com.rekora.app.data.local.entity.FabricationJobEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CadFileOption(
    val fileName: String,
    val format: String,
    val fileSizeMb: Double,
    val triangleCount: Int,
    val isWatertight: Boolean,
    val defaultDescription: String
)

data class FabricationMaterialOption(
    val name: String,
    val code: String,
    val ratePerGram: Double,
    val description: String,
    val densityTag: String
)

data class FabricationUiState(
    val selectedFile: CadFileOption? = null,
    val partDescription: String = "Waterproof chassis for waste collector with O-ring groove",
    val selectedMaterial: FabricationMaterialOption = FabricationMaterialOption(
        name = "PLA",
        code = "PLA",
        ratePerGram = 3.0,
        description = "Standard dimensionally stable prototyping plastic",
        densityTag = "Easiest • Eco-friendly"
    ),
    val selectedColor: String = "Engineering Teal",
    val infillPercentage: Int = 25,
    val quantity: Int = 1,
    val estimatedWeightGrams: Int = 140,
    val estimatedPrintTimeHours: Double = 6.5,
    val estimatedCost: Double = 470.0,
    val isSubmitting: Boolean = false,
    val submissionTrackingId: String? = null,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FabricationViewModel @Inject constructor(
    private val fabricationJobDao: FabricationJobDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FabricationUiState())
    val uiState: StateFlow<FabricationUiState> = _uiState.asStateFlow()

    val availableCadFiles = listOf(
        CadFileOption(
            fileName = "chassis_waterproof_afwc_v2.stl",
            format = "STL",
            fileSizeMb = 14.2,
            triangleCount = 124580,
            isWatertight = true,
            defaultDescription = "Waterproof chassis for waste collector with O-ring groove"
        ),
        CadFileOption(
            fileName = "propeller_nozzle_guard.step",
            format = "STEP",
            fileSizeMb = 8.6,
            triangleCount = 68400,
            isWatertight = true,
            defaultDescription = "Dual thruster duct protection cage for aquatic debris avoidance"
        ),
        CadFileOption(
            fileName = "sensor_housing_ip67.3mf",
            format = "3MF",
            fileSizeMb = 5.1,
            triangleCount = 45210,
            isWatertight = true,
            defaultDescription = "Stevenson screen micro-weather station environmental node housing"
        ),
        CadFileOption(
            fileName = "gripper_finger_tpu_flexible.stl",
            format = "STL",
            fileSizeMb = 3.8,
            triangleCount = 32150,
            isWatertight = true,
            defaultDescription = "Compliant compliant flexure finger for robotic trash grasping"
        )
    )

    val availableMaterials = listOf(
        FabricationMaterialOption("PLA", "PLA", 3.0, "Standard prototyping plastic with excellent dimensional accuracy", "Rigid • Easy"),
        FabricationMaterialOption("ABS", "ABS", 3.5, "High temperature, impact resistant engineering thermoplastic", "High Heat"),
        FabricationMaterialOption("PETG", "PETG", 3.5, "UV and weather resistant with outstanding layer adhesion", "Outdoor / Waterproof"),
        FabricationMaterialOption("TPU", "TPU", 4.5, "Flexible elastomeric filament for seals, bumpers, and grippers", "Flexible 95A"),
        FabricationMaterialOption("Resin", "RESIN", 6.0, "Stereolithography (SLA) ultra-high resolution resin", "Micron Precision"),
        FabricationMaterialOption("Carbon Fiber", "CARBON_FIBER", 8.0, "Chopped carbon fiber reinforced structural composite", "Maximum Rigidity")
    )

    val availableColors = listOf(
        "White",
        "Black",
        "Industrial Gray",
        "Safety Orange",
        "Engineering Teal"
    )

    init {
        // Pre-select first file for convenience
        selectCadFile(availableCadFiles[0])
    }

    fun selectCadFile(cadFile: CadFileOption) {
        _uiState.update {
            it.copy(
                selectedFile = cadFile,
                partDescription = cadFile.defaultDescription
            )
        }
        recalculateQuote()
    }

    fun setPartDescription(desc: String) {
        _uiState.update { it.copy(partDescription = desc) }
    }

    fun setMaterial(material: FabricationMaterialOption) {
        _uiState.update { it.copy(selectedMaterial = material) }
        recalculateQuote()
    }

    fun setColor(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun setInfill(infill: Int) {
        _uiState.update { it.copy(infillPercentage = infill) }
        recalculateQuote()
    }

    fun setQuantity(qty: Int) {
        if (qty in 1..10) {
            _uiState.update { it.copy(quantity = qty) }
            recalculateQuote()
        }
    }

    private fun recalculateQuote() {
        val state = _uiState.value
        val infillFraction = state.infillPercentage / 100.0
        val baseGramWeight = (40.0 + (infillFraction * 120.0)).toInt()
        val totalGrams = baseGramWeight * state.quantity

        val printHours = ((totalGrams / 25.0) + 1.0).coerceAtLeast(1.5)
        val rate = state.selectedMaterial.ratePerGram
        val machineSetupFee = 50.0
        val cost = (totalGrams * rate) + machineSetupFee

        _uiState.update {
            it.copy(
                estimatedWeightGrams = totalGrams,
                estimatedPrintTimeHours = printHours,
                estimatedCost = cost
            )
        }
    }

    fun submitJob() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            delay(1200)

            val trackingId = "FAB-SJC-${(1000..9999).random()}"
            val state = _uiState.value

            val job = FabricationJobEntity(
                id = UUID.randomUUID().toString(),
                userId = "usr-student-pilot",
                projectId = null,
                fileName = state.selectedFile?.fileName ?: "part.stl",
                fileUrl = "content://cad/${state.selectedFile?.fileName}",
                fileFormat = state.selectedFile?.format ?: "STL",
                partDescription = state.partDescription,
                material = state.selectedMaterial.code,
                color = state.selectedColor,
                quantity = state.quantity,
                dimensions = "140 x 95 x 65 mm",
                tolerances = "±0.2mm standard FDM",
                finishing = "${state.infillPercentage}% infill",
                specialRequirements = "Print for St. Joseph's Campus Project Hub",
                deliveryMethod = "CAMPUS_PICKUP",
                status = "SUBMITTED",
                quotedPrice = state.estimatedCost,
                estimatedDays = 2,
                adminNotes = "Auto-estimated via Campus FabLab Engine",
                isDemo = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            fabricationJobDao.insertJob(job)

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    submissionTrackingId = trackingId,
                    isConfirmed = true
                )
            }
        }
    }

    fun resetSubmission() {
        _uiState.update {
            it.copy(
                isConfirmed = false,
                submissionTrackingId = null
            )
        }
    }
}
