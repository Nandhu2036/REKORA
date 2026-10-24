package com.rekora.app.data.mapper

import com.rekora.app.data.local.entity.*
import com.rekora.app.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Robust JSON serialization and deserialization helpers with safe fallbacks.
 */
object JsonMapperHelper {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    fun parseStringList(jsonStr: String?): List<String> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(jsonStr)
        } catch (e: Exception) {
            // Fallback: parse bracketed comma-delimited strings
            jsonStr.trim()
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotBlank() }
        }
    }

    fun stringListToJson(list: List<String>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[" + list.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" } + "]"
        }
    }

    fun parseStringMap(jsonStr: String?): Map<String, String> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            json.decodeFromString<Map<String, String>>(jsonStr)
        } catch (e: Exception) {
            // Fallback for simple key:value pairs
            val clean = jsonStr.trim().removeSurrounding("{", "}")
            if (clean.isBlank()) return emptyMap()
            clean.split(",").mapNotNull { pair ->
                val parts = pair.split(":")
                if (parts.size >= 2) {
                    val key = parts[0].trim().removeSurrounding("\"").removeSurrounding("'")
                    val value = parts.subList(1, parts.size).joinToString(":").trim().removeSurrounding("\"").removeSurrounding("'")
                    key to value
                } else null
            }.toMap()
        }
    }

    fun stringMapToJson(map: Map<String, String>): String {
        return try {
            json.encodeToString(map)
        } catch (e: Exception) {
            "{" + map.entries.joinToString(",") { "\"${it.key}\":\"${it.value.replace("\"", "\\\"")}\"" } + "}"
        }
    }

    inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T {
        if (name.isNullOrBlank()) return default
        return try {
            enumValueOf<T>(name)
        } catch (e: Exception) {
            default
        }
    }
}

// ── User Mappers ──

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        role = JsonMapperHelper.enumOrDefault(role, UserRole.STUDENT),
        college = college,
        department = department,
        skills = JsonMapperHelper.parseStringList(skills),
        interests = JsonMapperHelper.parseStringList(interests),
        bio = bio,
        phone = phone,
        isEmailVerified = isEmailVerified,
        isProfileComplete = isProfileComplete,
        createdAt = createdAt
    )
}

fun User.toEntity(passwordHash: String = "", passwordSalt: String = ""): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        role = role.name,
        college = college,
        department = department,
        skills = JsonMapperHelper.stringListToJson(skills),
        interests = JsonMapperHelper.stringListToJson(interests),
        bio = bio,
        phone = phone,
        isEmailVerified = isEmailVerified,
        isProfileComplete = isProfileComplete,
        createdAt = createdAt,
        passwordHash = passwordHash,
        passwordSalt = passwordSalt
    )
}

// ── Project Mappers ──

fun ProjectEntity.toDomain(
    isSaved: Boolean = false,
    contributors: List<ProjectContributor> = emptyList()
): Project {
    return Project(
        id = id,
        title = title,
        shortDescription = shortDescription,
        problemStatement = problemStatement,
        abstractText = abstractText,
        objectives = JsonMapperHelper.parseStringList(objectives),
        domain = JsonMapperHelper.enumOrDefault(domain, EngineeringDomain.GENERAL),
        technologies = JsonMapperHelper.parseStringList(technologies),
        difficulty = JsonMapperHelper.enumOrDefault(difficulty, Difficulty.INTERMEDIATE),
        estimatedCost = PriceRange(min = estimatedCostMin, max = estimatedCostMax, currency = "INR"),
        estimatedTimeline = estimatedTimeline,
        timelineWeeks = timelineWeeks,
        coverImageUrl = coverImageUrl,
        images = JsonMapperHelper.parseStringList(images),
        capabilities = JsonMapperHelper.parseStringList(capabilities),
        hardwareRequirements = JsonMapperHelper.parseStringList(hardwareRequirements),
        softwareRequirements = JsonMapperHelper.parseStringList(softwareRequirements),
        safetyConsiderations = JsonMapperHelper.parseStringList(safetyConsiderations),
        hasCadFiles = hasCadFiles,
        hasFabricationNeeds = hasFabricationNeeds,
        isOpenSource = isOpenSource,
        repositoryLocked = repositoryLocked,
        repositoryPrice = repositoryPrice,
        currentVersion = currentVersion,
        contributors = contributors,
        tags = JsonMapperHelper.parseStringList(tags),
        viewCount = viewCount,
        saveCount = saveCount,
        isSaved = isSaved,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = id,
        title = title,
        shortDescription = shortDescription,
        problemStatement = problemStatement,
        abstractText = abstractText,
        objectives = JsonMapperHelper.stringListToJson(objectives),
        domain = domain.name,
        technologies = JsonMapperHelper.stringListToJson(technologies),
        difficulty = difficulty.name,
        estimatedCostMin = estimatedCost.min,
        estimatedCostMax = estimatedCost.max,
        estimatedTimeline = estimatedTimeline,
        timelineWeeks = timelineWeeks,
        coverImageUrl = coverImageUrl,
        images = JsonMapperHelper.stringListToJson(images),
        capabilities = JsonMapperHelper.stringListToJson(capabilities),
        hardwareRequirements = JsonMapperHelper.stringListToJson(hardwareRequirements),
        softwareRequirements = JsonMapperHelper.stringListToJson(softwareRequirements),
        safetyConsiderations = JsonMapperHelper.stringListToJson(safetyConsiderations),
        hasCadFiles = hasCadFiles,
        hasFabricationNeeds = hasFabricationNeeds,
        isOpenSource = isOpenSource,
        repositoryLocked = repositoryLocked,
        repositoryPrice = repositoryPrice,
        currentVersion = currentVersion,
        tags = JsonMapperHelper.stringListToJson(tags),
        viewCount = viewCount,
        saveCount = saveCount,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ── Project Version Mappers ──

fun ProjectVersionEntity.toDomain(): ProjectVersion {
    return ProjectVersion(
        id = id,
        projectId = projectId,
        version = version,
        title = title,
        changeSummary = changeSummary,
        changes = JsonMapperHelper.parseStringList(changes),
        contributorId = contributorId,
        contributorName = contributorName,
        parentVersionId = parentVersionId,
        bomChanges = bomChanges,
        architectureChanges = architectureChanges,
        isVerified = isVerified,
        createdAt = createdAt
    )
}

fun ProjectVersion.toEntity(): ProjectVersionEntity {
    return ProjectVersionEntity(
        id = id,
        projectId = projectId,
        version = version,
        title = title,
        changeSummary = changeSummary,
        changes = JsonMapperHelper.stringListToJson(changes),
        contributorId = contributorId,
        contributorName = contributorName,
        parentVersionId = parentVersionId,
        bomChanges = bomChanges,
        architectureChanges = architectureChanges,
        isVerified = isVerified,
        createdAt = createdAt
    )
}

// ── Component Mappers ──

fun ComponentEntity.toDomain(): Component {
    return Component(
        id = id,
        name = name,
        description = description,
        category = JsonMapperHelper.enumOrDefault(category, ComponentCategory.OTHER),
        specifications = JsonMapperHelper.parseStringMap(specifications),
        imageUrl = imageUrl,
        images = JsonMapperHelper.parseStringList(images),
        condition = JsonMapperHelper.enumOrDefault(condition, ItemCondition.NEW),
        sellerId = sellerId,
        sellerName = sellerName,
        isInstitutionalInventory = isInstitutionalInventory,
        purchasePrice = purchasePrice,
        rentalPricePerWeek = rentalPricePerWeek,
        securityDeposit = securityDeposit,
        minRentalDays = minRentalDays,
        stockQuantity = stockQuantity,
        availableQuantity = availableQuantity,
        campusAvailability = campusAvailability,
        compatibleProjects = JsonMapperHelper.parseStringList(compatibleProjects),
        isRentable = isRentable,
        isDemo = isDemo,
        createdAt = createdAt
    )
}

fun Component.toEntity(): ComponentEntity {
    return ComponentEntity(
        id = id,
        name = name,
        description = description,
        category = category.name,
        specifications = JsonMapperHelper.stringMapToJson(specifications),
        imageUrl = imageUrl,
        images = JsonMapperHelper.stringListToJson(images),
        condition = condition.name,
        sellerId = sellerId,
        sellerName = sellerName,
        isInstitutionalInventory = isInstitutionalInventory,
        purchasePrice = purchasePrice,
        rentalPricePerWeek = rentalPricePerWeek,
        securityDeposit = securityDeposit,
        minRentalDays = minRentalDays,
        stockQuantity = stockQuantity,
        availableQuantity = availableQuantity,
        campusAvailability = campusAvailability,
        compatibleProjects = JsonMapperHelper.stringListToJson(compatibleProjects),
        isRentable = isRentable,
        isDemo = isDemo,
        createdAt = createdAt
    )
}

// ── BOM Item Mappers ──

fun BomItemEntity.toDomain(
    isOwned: Boolean = false,
    alternatives: List<String> = emptyList()
): BomItem {
    return BomItem(
        id = id,
        projectId = projectId,
        componentId = componentId,
        name = name,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        isOptional = isOptional,
        isOwned = isOwned,
        sourcingChoice = JsonMapperHelper.enumOrDefault(sourcingChoice, SourcingChoice.BUY),
        alternatives = alternatives
    )
}

fun BomItem.toEntity(): BomItemEntity {
    return BomItemEntity(
        id = id,
        projectId = projectId,
        componentId = componentId,
        name = name,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        isOptional = isOptional,
        sourcingChoice = sourcingChoice.name
    )
}

// ── Cart Item Mappers ──

fun CartItemEntity.toDomain(component: Component? = null): CartItem {
    return CartItem(
        id = id,
        componentId = componentId,
        component = component,
        quantity = quantity,
        isRental = isRental,
        rentalDays = rentalDays,
        unitPrice = unitPrice,
        deposit = deposit
    )
}

fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        id = id,
        componentId = componentId,
        quantity = quantity,
        isRental = isRental,
        rentalDays = rentalDays,
        unitPrice = unitPrice,
        deposit = deposit
    )
}

// ── Order & Order Item Mappers ──

fun OrderEntity.toDomain(items: List<OrderItem> = emptyList()): Order {
    return Order(
        id = id,
        userId = userId,
        items = items,
        subtotal = subtotal,
        tax = tax,
        deliveryFee = deliveryFee,
        totalAmount = totalAmount,
        depositTotal = depositTotal,
        status = JsonMapperHelper.enumOrDefault(status, OrderStatus.PENDING),
        paymentStatus = JsonMapperHelper.enumOrDefault(paymentStatus, PaymentStatus.PENDING),
        paymentMethod = paymentMethod,
        deliveryMethod = JsonMapperHelper.enumOrDefault(deliveryMethod, DeliveryMethod.CAMPUS_PICKUP),
        deliveryAddress = deliveryAddress,
        notes = notes,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        userId = userId,
        subtotal = subtotal,
        tax = tax,
        deliveryFee = deliveryFee,
        totalAmount = totalAmount,
        depositTotal = depositTotal,
        status = status.name,
        paymentStatus = paymentStatus.name,
        paymentMethod = paymentMethod,
        deliveryMethod = deliveryMethod.name,
        deliveryAddress = deliveryAddress,
        notes = notes,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun OrderItemEntity.toDomain(): OrderItem {
    return OrderItem(
        id = id,
        componentId = componentId,
        componentName = componentName,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        isRental = isRental,
        rentalDays = rentalDays,
        deposit = deposit
    )
}

fun OrderItem.toEntity(orderId: String): OrderItemEntity {
    return OrderItemEntity(
        id = id,
        orderId = orderId,
        componentId = componentId,
        componentName = componentName,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        isRental = isRental,
        rentalDays = rentalDays,
        deposit = deposit
    )
}

// ── Rental Mappers ──

fun RentalEntity.toDomain(): Rental {
    return Rental(
        id = id,
        userId = userId,
        componentId = componentId,
        componentName = componentName,
        inventoryItemCode = inventoryItemCode,
        status = JsonMapperHelper.enumOrDefault(status, RentalStatus.REQUESTED),
        startDate = startDate,
        endDate = endDate,
        durationDays = durationDays,
        rentalCost = rentalCost,
        securityDeposit = securityDeposit,
        depositRefunded = depositRefunded,
        conditionAtCheckout = conditionAtCheckout,
        conditionAtReturn = conditionAtReturn,
        damageReported = damageReported,
        damageNotes = damageNotes,
        pickupLocation = pickupLocation,
        returnLocation = returnLocation,
        isDemo = isDemo,
        createdAt = createdAt
    )
}

fun Rental.toEntity(): RentalEntity {
    return RentalEntity(
        id = id,
        userId = userId,
        componentId = componentId,
        componentName = componentName,
        inventoryItemCode = inventoryItemCode,
        status = status.name,
        startDate = startDate,
        endDate = endDate,
        durationDays = durationDays,
        rentalCost = rentalCost,
        securityDeposit = securityDeposit,
        depositRefunded = depositRefunded,
        conditionAtCheckout = conditionAtCheckout,
        conditionAtReturn = conditionAtReturn,
        damageReported = damageReported,
        damageNotes = damageNotes,
        pickupLocation = pickupLocation,
        returnLocation = returnLocation,
        isDemo = isDemo,
        createdAt = createdAt
    )
}

// ── Fabrication Job Mappers ──

fun FabricationJobEntity.toDomain(): FabricationJob {
    return FabricationJob(
        id = id,
        userId = userId,
        projectId = projectId,
        fileName = fileName,
        fileUrl = fileUrl,
        fileFormat = fileFormat,
        partDescription = partDescription,
        material = JsonMapperHelper.enumOrDefault(material, FabricationMaterial.PLA),
        color = color,
        quantity = quantity,
        dimensions = dimensions,
        tolerances = tolerances,
        finishing = finishing,
        specialRequirements = specialRequirements,
        deliveryMethod = JsonMapperHelper.enumOrDefault(deliveryMethod, DeliveryMethod.CAMPUS_PICKUP),
        status = JsonMapperHelper.enumOrDefault(status, FabricationStatus.DRAFT),
        quotedPrice = quotedPrice,
        estimatedDays = estimatedDays,
        adminNotes = adminNotes,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun FabricationJob.toEntity(): FabricationJobEntity {
    return FabricationJobEntity(
        id = id,
        userId = userId,
        projectId = projectId,
        fileName = fileName,
        fileUrl = fileUrl,
        fileFormat = fileFormat,
        partDescription = partDescription,
        material = material.name,
        color = color,
        quantity = quantity,
        dimensions = dimensions,
        tolerances = tolerances,
        finishing = finishing,
        specialRequirements = specialRequirements,
        deliveryMethod = deliveryMethod.name,
        status = status.name,
        quotedPrice = quotedPrice,
        estimatedDays = estimatedDays,
        adminNotes = adminNotes,
        isDemo = isDemo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ── Marketplace Listing Mappers ──

fun MarketplaceListingEntity.toDomain(): MarketplaceListing {
    return MarketplaceListing(
        id = id,
        sellerId = sellerId,
        sellerName = sellerName,
        title = title,
        description = description,
        category = JsonMapperHelper.enumOrDefault(category, ComponentCategory.OTHER),
        condition = JsonMapperHelper.enumOrDefault(condition, ItemCondition.GOOD),
        images = JsonMapperHelper.parseStringList(images),
        price = price,
        isRentable = isRentable,
        rentalPricePerWeek = rentalPricePerWeek,
        isBorrowable = isBorrowable,
        campusLocation = campusLocation,
        status = JsonMapperHelper.enumOrDefault(status, ListingStatus.ACTIVE),
        isDemo = isDemo,
        createdAt = createdAt
    )
}

fun MarketplaceListing.toEntity(): MarketplaceListingEntity {
    return MarketplaceListingEntity(
        id = id,
        sellerId = sellerId,
        sellerName = sellerName,
        title = title,
        description = description,
        category = category.name,
        condition = condition.name,
        images = JsonMapperHelper.stringListToJson(images),
        price = price,
        isRentable = isRentable,
        rentalPricePerWeek = rentalPricePerWeek,
        isBorrowable = isBorrowable,
        campusLocation = campusLocation,
        status = status.name,
        isDemo = isDemo,
        createdAt = createdAt
    )
}

// ── AI Conversation & Message Mappers ──

fun AiConversationEntity.toDomain(messages: List<AiMessage> = emptyList()): AiConversation {
    return AiConversation(
        id = id,
        userId = userId,
        title = title,
        messages = messages,
        hasBlueprint = hasBlueprint,
        blueprintId = blueprintId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AiConversation.toEntity(): AiConversationEntity {
    return AiConversationEntity(
        id = id,
        userId = userId,
        title = title,
        hasBlueprint = hasBlueprint,
        blueprintId = blueprintId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AiMessageEntity.toDomain(isStreaming: Boolean = false): AiMessage {
    return AiMessage(
        id = id,
        conversationId = conversationId,
        content = content,
        role = JsonMapperHelper.enumOrDefault(role, AiRole.USER),
        isStreaming = isStreaming,
        timestamp = timestamp
    )
}

fun AiMessage.toEntity(): AiMessageEntity {
    return AiMessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        role = role.name,
        timestamp = timestamp
    )
}

// ── Notification Mappers ──

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = JsonMapperHelper.enumOrDefault(type, NotificationType.GENERAL),
        actionRoute = actionRoute,
        isRead = isRead,
        createdAt = createdAt
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type.name,
        actionRoute = actionRoute,
        isRead = isRead,
        createdAt = createdAt
    )
}

// ── Inventory Item Mappers ──

fun InventoryItemEntity.toDomain(): InventoryItem {
    return InventoryItem(
        id = id,
        itemCode = itemCode,
        componentId = componentId,
        componentName = componentName,
        condition = JsonMapperHelper.enumOrDefault(condition, ItemCondition.NEW),
        currentStatus = JsonMapperHelper.enumOrDefault(currentStatus, InventoryStatus.AVAILABLE),
        currentRentalId = currentRentalId,
        currentUserId = currentUserId,
        location = location,
        maintenanceNotes = maintenanceNotes,
        totalRentals = totalRentals,
        qrCodeData = qrCodeData,
        createdAt = createdAt,
        lastUpdated = lastUpdated
    )
}

fun InventoryItem.toEntity(): InventoryItemEntity {
    return InventoryItemEntity(
        id = id,
        itemCode = itemCode,
        componentId = componentId,
        componentName = componentName,
        condition = condition.name,
        currentStatus = currentStatus.name,
        currentRentalId = currentRentalId,
        currentUserId = currentUserId,
        location = location,
        maintenanceNotes = maintenanceNotes,
        totalRentals = totalRentals,
        qrCodeData = qrCodeData,
        createdAt = createdAt,
        lastUpdated = lastUpdated
    )
}
