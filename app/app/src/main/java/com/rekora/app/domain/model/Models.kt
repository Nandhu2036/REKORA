package com.rekora.app.domain.model

import java.util.UUID

/**
 * Core domain models for the REKORA application.
 * These represent the business entities independent of any data layer.
 */

// ── User & Auth ──

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.STUDENT,
    val college: String? = null,
    val department: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val bio: String? = null,
    val phone: String? = null,
    val isEmailVerified: Boolean = false,
    val isProfileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    STUDENT,
    CONTRIBUTOR,
    FACULTY,
    INVENTORY_OPERATOR,
    TESTER,
    ADMIN
}

// ── Project ──

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val shortDescription: String = "",
    val problemStatement: String = "",
    val abstractText: String = "",
    val objectives: List<String> = emptyList(),
    val domain: EngineeringDomain = EngineeringDomain.GENERAL,
    val technologies: List<String> = emptyList(),
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val estimatedCost: PriceRange = PriceRange(),
    val estimatedTimeline: String = "",
    val timelineWeeks: Int = 4,
    val coverImageUrl: String? = null,
    val images: List<String> = emptyList(),
    val capabilities: List<String> = emptyList(),
    val hardwareRequirements: List<String> = emptyList(),
    val softwareRequirements: List<String> = emptyList(),
    val safetyConsiderations: List<String> = emptyList(),
    val hasCadFiles: Boolean = false,
    val hasFabricationNeeds: Boolean = false,
    val isOpenSource: Boolean = false,
    val repositoryLocked: Boolean = true,
    val repositoryPrice: Double = 0.0,
    val currentVersion: String = "1.0",
    val contributors: List<ProjectContributor> = emptyList(),
    val tags: List<String> = emptyList(),
    val viewCount: Int = 0,
    val saveCount: Int = 0,
    val isSaved: Boolean = false,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProjectContributor(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val avatarUrl: String? = null
)

data class PriceRange(
    val min: Double = 0.0,
    val max: Double = 0.0,
    val currency: String = "INR"
) {
    fun display(): String {
        return if (min == max) "₹${min.toInt()}"
        else "₹${min.toInt()} – ₹${max.toInt()}"
    }
}

enum class EngineeringDomain(val displayName: String) {
    ROBOTICS("Robotics & Automation"),
    IOT("IoT & Embedded Systems"),
    AI_ML("AI & Machine Learning"),
    COMPUTER_VISION("Computer Vision"),
    RENEWABLE_ENERGY("Renewable Energy"),
    ENVIRONMENTAL("Environmental Engineering"),
    MECHANICAL("Mechanical Engineering"),
    ELECTRONICS("Electronics & Circuits"),
    BIOMEDICAL("Biomedical Engineering"),
    AEROSPACE("Aerospace Engineering"),
    CIVIL("Civil & Structural"),
    AUTOMOTIVE("Automotive Engineering"),
    COMMUNICATION("Communication Systems"),
    GENERAL("General Engineering")
}

enum class Difficulty(val displayName: String, val level: Int) {
    BEGINNER("Beginner", 1),
    INTERMEDIATE("Intermediate", 2),
    ADVANCED("Advanced", 3),
    EXPERT("Expert", 4)
}

// ── Project DNA & Versions ──

data class ProjectVersion(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val version: String = "1.0",
    val title: String = "",
    val changeSummary: String = "",
    val changes: List<String> = emptyList(),
    val contributorId: String = "",
    val contributorName: String = "",
    val parentVersionId: String? = null,
    val bomChanges: String? = null,
    val architectureChanges: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ── Repository ──

data class RepositoryAsset(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val versionId: String = "",
    val name: String = "",
    val description: String = "",
    val category: RepositoryCategory = RepositoryCategory.DOCUMENTATION,
    val fileUrl: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val isPublicPreview: Boolean = false
)

enum class RepositoryCategory(val displayName: String) {
    OVERVIEW("Project Overview"),
    ARCHITECTURE("Architecture"),
    SOURCE_CODE("Source Code"),
    CAD_FILES("CAD Files"),
    BOM("Bill of Materials"),
    CIRCUIT_DIAGRAM("Circuit Diagrams"),
    BUILD_INSTRUCTIONS("Build Instructions"),
    TESTING("Testing & Validation"),
    DOCUMENTATION("Documentation"),
    OTHER("Other")
}

data class RepositoryEntitlement(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val projectId: String = "",
    val versionId: String? = null,
    val grantedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val isActive: Boolean = true
)

// ── Components & Products ──

data class Component(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: ComponentCategory = ComponentCategory.OTHER,
    val specifications: Map<String, String> = emptyMap(),
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val condition: ItemCondition = ItemCondition.NEW,
    val sellerId: String? = null,
    val sellerName: String? = null,
    val isInstitutionalInventory: Boolean = false,
    val purchasePrice: Double = 0.0,
    val rentalPricePerWeek: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val minRentalDays: Int = 7,
    val stockQuantity: Int = 0,
    val availableQuantity: Int = 0,
    val campusAvailability: String? = null,
    val compatibleProjects: List<String> = emptyList(),
    val isRentable: Boolean = false,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ComponentCategory(val displayName: String) {
    MICROCONTROLLER("Microcontrollers"),
    SENSOR("Sensors"),
    MOTOR_DRIVER("Motor Drivers"),
    MOTOR("Motors & Actuators"),
    BATTERY("Batteries & Power"),
    ROBOTICS_KIT("Robotics Kits"),
    MECHANICAL("Mechanical Components"),
    FASTENER("Bearings, Fasteners & Couplings"),
    TOOL("Tools"),
    FILAMENT("Filaments & Materials"),
    ELECTRONICS("Electronics Accessories"),
    OTHER("Other")
}

enum class ItemCondition(val displayName: String) {
    NEW("New"),
    LIKE_NEW("Like New"),
    GOOD("Good"),
    FAIR("Fair"),
    USED("Used")
}

// ── BOM ──

data class BomItem(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val componentId: String? = null,
    val name: String = "",
    val description: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val isOptional: Boolean = false,
    val isOwned: Boolean = false,
    val sourcingChoice: SourcingChoice = SourcingChoice.BUY,
    val alternatives: List<String> = emptyList()
)

enum class SourcingChoice(val displayName: String) {
    BUY("Buy"),
    RENT("Rent"),
    BORROW("Borrow"),
    USE_EXISTING("Use Existing"),
    FABRICATE("Fabricate / 3D Print")
}

// ── Cart & Orders ──

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val componentId: String = "",
    val component: Component? = null,
    val quantity: Int = 1,
    val isRental: Boolean = false,
    val rentalDays: Int = 7,
    val unitPrice: Double = 0.0,
    val deposit: Double = 0.0
) {
    val lineTotal: Double get() = if (isRental) {
        (component?.rentalPricePerWeek ?: unitPrice) * (rentalDays / 7.0) + deposit
    } else {
        unitPrice * quantity
    }
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val depositTotal: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String? = null,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.CAMPUS_PICKUP,
    val deliveryAddress: String? = null,
    val notes: String? = null,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val componentId: String = "",
    val componentName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val isRental: Boolean = false,
    val rentalDays: Int = 0,
    val deposit: Double = 0.0
)

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    READY_FOR_PICKUP("Ready for Pickup"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    SANDBOX("Sandbox / Dev Mode")
}

enum class DeliveryMethod(val displayName: String) {
    CAMPUS_PICKUP("Campus Pickup"),
    DELIVERY("Delivery")
}

// ── Rental ──

data class Rental(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val componentId: String = "",
    val componentName: String = "",
    val inventoryItemCode: String? = null,
    val status: RentalStatus = RentalStatus.REQUESTED,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val durationDays: Int = 7,
    val rentalCost: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val depositRefunded: Boolean = false,
    val conditionAtCheckout: String? = null,
    val conditionAtReturn: String? = null,
    val damageReported: Boolean = false,
    val damageNotes: String? = null,
    val pickupLocation: String? = null,
    val returnLocation: String? = null,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RentalStatus(val displayName: String) {
    REQUESTED("Requested"),
    APPROVED("Approved"),
    AWAITING_PICKUP("Awaiting Pickup"),
    ACTIVE("Active"),
    DUE_SOON("Due Soon"),
    OVERDUE("Overdue"),
    RETURN_INITIATED("Return Initiated"),
    UNDER_INSPECTION("Under Inspection"),
    COMPLETED("Completed"),
    DISPUTED("Disputed"),
    CANCELLED("Cancelled")
}

// ── Fabrication ──

data class FabricationJob(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val projectId: String? = null,
    val fileName: String = "",
    val fileUrl: String? = null,
    val fileFormat: String = "",
    val partDescription: String = "",
    val material: FabricationMaterial = FabricationMaterial.PLA,
    val color: String = "White",
    val quantity: Int = 1,
    val dimensions: String? = null,
    val tolerances: String? = null,
    val finishing: String? = null,
    val specialRequirements: String? = null,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.CAMPUS_PICKUP,
    val status: FabricationStatus = FabricationStatus.DRAFT,
    val quotedPrice: Double? = null,
    val estimatedDays: Int? = null,
    val adminNotes: String? = null,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FabricationStatus(val displayName: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    AWAITING_CLARIFICATION("Awaiting Clarification"),
    QUOTATION_READY("Quotation Ready"),
    AWAITING_APPROVAL("Awaiting Approval"),
    APPROVED("Approved"),
    IN_PRODUCTION("In Production"),
    QUALITY_INSPECTION("Quality Inspection"),
    READY_FOR_PICKUP("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class FabricationMaterial(val displayName: String) {
    PLA("PLA"),
    ABS("ABS"),
    PETG("PETG"),
    TPU("TPU"),
    NYLON("Nylon"),
    RESIN("Resin"),
    ASA("ASA"),
    CARBON_FIBER("Carbon Fiber Composite"),
    WOOD("Wood Composite"),
    METAL("Metal (DMLS)")
}

// ── Marketplace ──

data class MarketplaceListing(
    val id: String = UUID.randomUUID().toString(),
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: ComponentCategory = ComponentCategory.OTHER,
    val condition: ItemCondition = ItemCondition.GOOD,
    val images: List<String> = emptyList(),
    val price: Double = 0.0,
    val isRentable: Boolean = false,
    val rentalPricePerWeek: Double = 0.0,
    val isBorrowable: Boolean = false,
    val campusLocation: String? = null,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val isDemo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ListingStatus(val displayName: String) {
    DRAFT("Draft"),
    ACTIVE("Active"),
    RESERVED("Reserved"),
    SOLD("Sold"),
    EXPIRED("Expired"),
    REMOVED("Removed"),
    FLAGGED("Flagged")
}

// ── AI Engineer ──

data class AiConversation(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "New Conversation",
    val messages: List<AiMessage> = emptyList(),
    val hasBlueprint: Boolean = false,
    val blueprintId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val content: String = "",
    val role: AiRole = AiRole.USER,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AiRole {
    USER, ASSISTANT, SYSTEM
}

data class ProjectBlueprint(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val userId: String = "",
    val title: String = "",
    val objective: String = "",
    val assumptions: List<String> = emptyList(),
    val functionalRequirements: List<String> = emptyList(),
    val proposedArchitecture: String = "",
    val components: List<BomItem> = emptyList(),
    val estimatedCost: PriceRange = PriceRange(),
    val dependencies: List<String> = emptyList(),
    val milestones: List<String> = emptyList(),
    val validationCriteria: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
    val openQuestions: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// ── Inventory (QR) ──

data class InventoryItem(
    val id: String = UUID.randomUUID().toString(),
    val itemCode: String = "", // e.g., "RK-ESP32-001"
    val componentId: String = "",
    val componentName: String = "",
    val condition: ItemCondition = ItemCondition.NEW,
    val currentStatus: InventoryStatus = InventoryStatus.AVAILABLE,
    val currentRentalId: String? = null,
    val currentUserId: String? = null,
    val location: String? = null,
    val maintenanceNotes: String? = null,
    val totalRentals: Int = 0,
    val qrCodeData: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class InventoryStatus(val displayName: String) {
    AVAILABLE("Available"),
    RENTED("Rented"),
    RESERVED("Reserved"),
    MAINTENANCE("Under Maintenance"),
    DAMAGED("Damaged"),
    RETIRED("Retired")
}

// ── Notifications ──

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val actionRoute: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType(val displayName: String) {
    GENERAL("General"),
    ORDER_UPDATE("Order Update"),
    RENTAL_APPROVAL("Rental Approval"),
    RENTAL_DUE("Rental Due"),
    RENTAL_OVERDUE("Rental Overdue"),
    FABRICATION_UPDATE("Fabrication Update"),
    REPOSITORY_UPDATE("Repository Update"),
    MARKETPLACE("Marketplace"),
    ANNOUNCEMENT("Announcement")
}

// ── Project Matching ──

data class MatchCriteria(
    val problemStatement: String = "",
    val maxBudget: Double = 0.0,
    val ownedComponents: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val timelineWeeks: Int = 4,
    val preferredDomain: EngineeringDomain? = null,
    val difficulty: Difficulty? = null,
    val hasFabricationAccess: Boolean = false
)

data class ProjectMatch(
    val project: Project,
    val matchScore: Int = 0, // 0-100
    val matchReasons: List<String> = emptyList(),
    val budgetFit: Boolean = true,
    val timelineFit: Boolean = true,
    val skillFit: Boolean = true,
    val componentOverlap: Int = 0
)

// ── Documentation ──

data class DocumentationDraft(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val userId: String = "",
    val title: String = "",
    val abstractText: String = "",
    val problemStatement: String = "",
    val objectives: String = "",
    val methodology: String = "",
    val systemArchitecture: String = "",
    val implementation: String = "",
    val testing: String = "",
    val results: String = "",
    val limitations: String = "",
    val futureImprovements: String = "",
    val references: String = "",
    val lastSavedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
