package com.rekora.app.data.local.entity

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val college: String?,
    val department: String?,
    val skills: String, // JSON list
    val interests: String, // JSON list
    val bio: String?,
    val phone: String?,
    val isEmailVerified: Boolean,
    val isProfileComplete: Boolean,
    val createdAt: Long,
    val passwordHash: String = "",
    val passwordSalt: String = ""
)

@Entity(
    tableName = "projects",
    indices = [Index(value = ["domain"]), Index(value = ["difficulty"])]
)
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val shortDescription: String,
    val problemStatement: String,
    val abstractText: String,
    val objectives: String, // JSON list
    val domain: String,
    val technologies: String, // JSON list
    val difficulty: String,
    val estimatedCostMin: Double,
    val estimatedCostMax: Double,
    val estimatedTimeline: String,
    val timelineWeeks: Int,
    val coverImageUrl: String?,
    val images: String, // JSON list
    val capabilities: String, // JSON list
    val hardwareRequirements: String, // JSON list
    val softwareRequirements: String, // JSON list
    val safetyConsiderations: String, // JSON list
    val hasCadFiles: Boolean,
    val hasFabricationNeeds: Boolean,
    val isOpenSource: Boolean,
    val repositoryLocked: Boolean,
    val repositoryPrice: Double,
    val currentVersion: String,
    val tags: String, // JSON list
    val viewCount: Int,
    val saveCount: Int,
    val isDemo: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "project_versions")
data class ProjectVersionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val version: String,
    val title: String,
    val changeSummary: String,
    val changes: String, // JSON list
    val contributorId: String,
    val contributorName: String,
    val parentVersionId: String?,
    val bomChanges: String?,
    val architectureChanges: String?,
    val isVerified: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "components",
    indices = [Index(value = ["category"])]
)
data class ComponentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val specifications: String, // JSON map
    val imageUrl: String?,
    val images: String, // JSON list
    val condition: String,
    val sellerId: String?,
    val sellerName: String?,
    val isInstitutionalInventory: Boolean,
    val purchasePrice: Double,
    val rentalPricePerWeek: Double,
    val securityDeposit: Double,
    val minRentalDays: Int,
    val stockQuantity: Int,
    val availableQuantity: Int,
    val campusAvailability: String?,
    val compatibleProjects: String, // JSON list
    val isRentable: Boolean,
    val isDemo: Boolean,
    val createdAt: Long
)

@Entity(tableName = "bom_items")
data class BomItemEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val componentId: String?,
    val name: String,
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val isOptional: Boolean,
    val sourcingChoice: String
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val componentId: String,
    val quantity: Int,
    val isRental: Boolean,
    val rentalDays: Int,
    val unitPrice: Double,
    val deposit: Double
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subtotal: Double,
    val tax: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val depositTotal: Double,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String?,
    val deliveryMethod: String,
    val deliveryAddress: String?,
    val notes: String?,
    val isDemo: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val componentId: String,
    val componentName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val isRental: Boolean,
    val rentalDays: Int,
    val deposit: Double
)

@Entity(tableName = "rentals")
data class RentalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val componentId: String,
    val componentName: String,
    val inventoryItemCode: String?,
    val status: String,
    val startDate: Long?,
    val endDate: Long?,
    val durationDays: Int,
    val rentalCost: Double,
    val securityDeposit: Double,
    val depositRefunded: Boolean,
    val conditionAtCheckout: String?,
    val conditionAtReturn: String?,
    val damageReported: Boolean,
    val damageNotes: String?,
    val pickupLocation: String?,
    val returnLocation: String?,
    val isDemo: Boolean,
    val createdAt: Long
)

@Entity(tableName = "fabrication_jobs")
data class FabricationJobEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val projectId: String?,
    val fileName: String,
    val fileUrl: String?,
    val fileFormat: String,
    val partDescription: String,
    val material: String,
    val color: String,
    val quantity: Int,
    val dimensions: String?,
    val tolerances: String?,
    val finishing: String?,
    val specialRequirements: String?,
    val deliveryMethod: String,
    val status: String,
    val quotedPrice: Double?,
    val estimatedDays: Int?,
    val adminNotes: String?,
    val isDemo: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "marketplace_listings")
data class MarketplaceListingEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val sellerName: String,
    val title: String,
    val description: String,
    val category: String,
    val condition: String,
    val images: String, // JSON list
    val price: Double,
    val isRentable: Boolean,
    val rentalPricePerWeek: Double,
    val isBorrowable: Boolean,
    val campusLocation: String?,
    val status: String,
    val isDemo: Boolean,
    val createdAt: Long
)

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val hasBlueprint: Boolean,
    val blueprintId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val role: String,
    val timestamp: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val actionRoute: String?,
    val isRead: Boolean,
    val createdAt: Long
)

@Entity(tableName = "saved_projects")
data class SavedProjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val projectId: String,
    val savedAt: Long
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val itemCode: String,
    val componentId: String,
    val componentName: String,
    val condition: String,
    val currentStatus: String,
    val currentRentalId: String?,
    val currentUserId: String?,
    val location: String?,
    val maintenanceNotes: String?,
    val totalRentals: Int,
    val qrCodeData: String,
    val createdAt: Long,
    val lastUpdated: Long
)
