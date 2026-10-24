package com.rekora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.domain.repository.FabricationRepository
import com.rekora.app.domain.model.DeliveryMethod
import com.rekora.app.domain.model.FabricationJob
import com.rekora.app.domain.model.FabricationMaterial
import com.rekora.app.domain.model.FabricationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State container for CAD 3D print file submission and active job tracking.
 */
data class FabricationUiState(
    val existingJobs: List<FabricationJob> = emptyList(),
    val fileName: String = "chassis_enclosure_rev3.stl",
    val fileFormat: String = "STL",
    val partDescription: String = "Weatherproof sensor housing with O-ring groove and M3 brass heat-set inserts.",
    val selectedMaterial: FabricationMaterial = FabricationMaterial.PETG,
    val selectedColor: String = "Engineering Teal",
    val quantity: Int = 1,
    val infillPercent: Int = 25,
    val finishing: String = "Deburred & Cleaned",
    val tolerances: String = "±0.2mm Standard FDM",
    val specialRequirements: String = "Water-tight perimeter walls, 4 perimeters recommended.",
    val deliveryMethod: DeliveryMethod = DeliveryMethod.CAMPUS_PICKUP,
    val estimatedQuotedPrice: Double = 320.0,
    val estimatedTurnaroundDays: Int = 2,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submittedJob: FabricationJob? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * FabricationViewModel connects digital CAD design to physical campus manufacturing:
 * - Dynamic price and turnaround estimation based on material density and infill
 * - Multi-material selection calibrated for campus 3D printers (PLA, PETG, ABS, Resin, TPU, Carbon Fiber)
 * - File submission simulation with tracking of historical and active print jobs
 */
@HiltViewModel
class FabricationViewModel @Inject constructor(
    private val fabricationRepository: FabricationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _fileName = MutableStateFlow("chassis_enclosure_rev3.stl")
    private val _fileFormat = MutableStateFlow("STL")
    private val _partDescription = MutableStateFlow("Weatherproof sensor housing with O-ring groove.")
    private val _material = MutableStateFlow(FabricationMaterial.PETG)
    private val _color = MutableStateFlow("Engineering Teal")
    private val _quantity = MutableStateFlow(1)
    private val _infill = MutableStateFlow(25)
    private val _finishing = MutableStateFlow("Deburred & Cleaned")
    private val _tolerances = MutableStateFlow("±0.2mm Standard FDM")
    private val _specialRequirements = MutableStateFlow("Water-tight perimeters")
    private val _deliveryMethod = MutableStateFlow(DeliveryMethod.CAMPUS_PICKUP)

    private val _isSubmitting = MutableStateFlow(false)
    private val _submissionSuccess = MutableStateFlow(false)
    private val _submittedJob = MutableStateFlow<FabricationJob?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val existingJobs: StateFlow<List<FabricationJob>> = authRepository.currentUser
        .flatMapLatest { user ->
            val userId = user?.id ?: "guest-student"
            fabricationRepository.getJobsForUser(userId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<FabricationUiState> = combine(
        existingJobs,
        _fileName,
        _fileFormat,
        _partDescription,
        _material,
        _color,
        _quantity,
        _infill,
        _finishing,
        _tolerances,
        _specialRequirements,
        _deliveryMethod,
        _isSubmitting,
        _submissionSuccess,
        _submittedJob,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val jobs = args[0] as List<FabricationJob>
        val name = args[1] as String
        val format = args[2] as String
        val desc = args[3] as String
        val mat = args[4] as FabricationMaterial
        val col = args[5] as String
        val qty = args[6] as Int
        val infillVal = args[7] as Int
        val finish = args[8] as String
        val tol = args[9] as String
        val spec = args[10] as String
        val method = args[11] as DeliveryMethod
        val submitting = args[12] as Boolean
        val success = args[13] as Boolean
        val jobObj = args[14] as FabricationJob?
        val err = args[15] as String?

        val (estPrice, estDays) = calculateEstimate(
            material = mat,
            quantity = qty,
            infillPercent = infillVal
        )

        FabricationUiState(
            existingJobs = jobs,
            fileName = name,
            fileFormat = format,
            partDescription = desc,
            selectedMaterial = mat,
            selectedColor = col,
            quantity = qty,
            infillPercent = infillVal,
            finishing = finish,
            tolerances = tol,
            specialRequirements = spec,
            deliveryMethod = method,
            estimatedQuotedPrice = estPrice,
            estimatedTurnaroundDays = estDays,
            isSubmitting = submitting,
            submissionSuccess = success,
            submittedJob = jobObj,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FabricationUiState()
    )

    private fun calculateEstimate(
        material: FabricationMaterial,
        quantity: Int,
        infillPercent: Int
    ): Pair<Double, Int> {
        val baseGramPrice = when (material) {
            FabricationMaterial.PLA -> 2.5
            FabricationMaterial.PETG -> 3.2
            FabricationMaterial.ABS -> 3.5
            FabricationMaterial.TPU -> 4.5
            FabricationMaterial.NYLON -> 7.0
            FabricationMaterial.RESIN -> 6.0
            FabricationMaterial.ASA -> 4.0
            FabricationMaterial.CARBON_FIBER -> 9.5
            FabricationMaterial.WOOD -> 5.0
            FabricationMaterial.METAL -> 25.0
        }
        val estimatedGrams = 50 * (infillPercent / 20.0)
        val price = (estimatedGrams * baseGramPrice * quantity).coerceAtLeast(150.0)
        val days = when {
            quantity > 5 -> 4
            infillPercent > 50 -> 3
            else -> 2
        }
        return Pair(price, days)
    }

    fun onFileNameChange(name: String) {
        _fileName.value = name
    }

    fun onFileFormatChange(format: String) {
        _fileFormat.value = format
    }

    fun onPartDescriptionChange(desc: String) {
        _partDescription.value = desc
    }

    fun onMaterialSelect(mat: FabricationMaterial) {
        _material.value = mat
    }

    fun onColorChange(color: String) {
        _color.value = color
    }

    fun onQuantityChange(qty: Int) {
        _quantity.value = qty.coerceIn(1, 50)
    }

    fun onInfillChange(infill: Int) {
        _infill.value = infill.coerceIn(10, 100)
    }

    fun onFinishingChange(finishing: String) {
        _finishing.value = finishing
    }

    fun onTolerancesChange(tolerances: String) {
        _tolerances.value = tolerances
    }

    fun onSpecialRequirementsChange(reqs: String) {
        _specialRequirements.value = reqs
    }

    fun onDeliveryMethodSelect(method: DeliveryMethod) {
        _deliveryMethod.value = method
    }

    fun submitFabricationOrder(onSuccess: (jobId: String) -> Unit) {
        val name = _fileName.value.trim()
        if (name.isBlank()) {
            _error.value = "CAD file name is required."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "guest-student"
                val (estPrice, estDays) = calculateEstimate(
                    material = _material.value,
                    quantity = _quantity.value,
                    infillPercent = _infill.value
                )

                val jobId = "fab-" + UUID.randomUUID().toString().take(8)
                val newJob = FabricationJob(
                    id = jobId,
                    userId = userId,
                    fileName = name,
                    fileUrl = "https://rekora.campus/cad/uploads/$name",
                    fileFormat = _fileFormat.value,
                    partDescription = _partDescription.value,
                    material = _material.value,
                    color = _color.value,
                    quantity = _quantity.value,
                    dimensions = "120 x 85 x 45 mm",
                    tolerances = _tolerances.value,
                    finishing = _finishing.value,
                    specialRequirements = _specialRequirements.value.ifBlank { null },
                    deliveryMethod = _deliveryMethod.value,
                    status = FabricationStatus.SUBMITTED,
                    quotedPrice = estPrice,
                    estimatedDays = estDays,
                    adminNotes = "Received in Campus Maker Lab queue",
                    isDemo = true,
                    createdAt = System.currentTimeMillis()
                )

                delay(600) // Simulation delay
                val finalJobId = fabricationRepository.submitJob(newJob)
                val savedJob = newJob.copy(id = finalJobId)
                _submittedJob.value = savedJob
                _submissionSuccess.value = true
                onSuccess(finalJobId)
            } catch (e: Exception) {
                _error.value = "Submission failed: ${e.localizedMessage}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun resetForm() {
        _fileName.value = ""
        _fileFormat.value = "STL"
        _partDescription.value = ""
        _submissionSuccess.value = false
        _submittedJob.value = null
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
