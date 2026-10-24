package com.rekora.app.domain.repository

import com.rekora.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interfaces defining core business data operations for REKORA.
 */

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    fun getProjectById(id: String): Flow<Project?>
    fun getProjectsByDomain(domain: EngineeringDomain): Flow<List<Project>>
    fun searchProjects(query: String): Flow<List<Project>>
    fun getProjectsWithinBudget(maxBudget: Double): Flow<List<Project>>
    fun getBomForProject(projectId: String): Flow<List<BomItem>>
    fun getVersionsForProject(projectId: String): Flow<List<ProjectVersion>>
    fun isProjectSaved(userId: String, projectId: String): Flow<Boolean>
    suspend fun toggleSaveProject(userId: String, projectId: String)
    fun getSavedProjects(userId: String): Flow<List<Project>>
    suspend fun unlockRepository(userId: String, projectId: String)
}

interface ComponentRepository {
    fun getAllComponents(): Flow<List<Component>>
    fun getComponentById(id: String): Flow<Component?>
    fun getComponentsByCategory(category: ComponentCategory): Flow<List<Component>>
    fun searchComponents(query: String): Flow<List<Component>>
    fun getRentableComponents(): Flow<List<Component>>
}

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartCount(): Flow<Int>
    suspend fun addToCart(componentId: String, quantity: Int = 1, isRental: Boolean = false, rentalDays: Int = 7)
    suspend fun updateQuantity(cartItemId: String, quantity: Int)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun clearCart()
}

interface OrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    suspend fun createOrder(order: Order, items: List<OrderItem>): String
}

interface RentalRepository {
    fun getRentalsForUser(userId: String): Flow<List<Rental>>
    fun getRentalById(rentalId: String): Flow<Rental?>
    suspend fun createRental(rental: Rental): String
    suspend fun updateRentalStatus(rentalId: String, status: RentalStatus)
}

interface FabricationRepository {
    fun getJobsForUser(userId: String): Flow<List<FabricationJob>>
    fun getJobById(jobId: String): Flow<FabricationJob?>
    suspend fun submitJob(job: FabricationJob): String
}

interface MarketplaceRepository {
    fun getActiveListings(): Flow<List<MarketplaceListing>>
    fun getListingsForUser(userId: String): Flow<List<MarketplaceListing>>
    fun getListingById(id: String): Flow<MarketplaceListing?>
    suspend fun createListing(listing: MarketplaceListing): String
    fun searchListings(query: String): Flow<List<MarketplaceListing>>
}

interface AiRepository {
    fun getConversationsForUser(userId: String): Flow<List<AiConversation>>
    fun getConversation(conversationId: String): Flow<AiConversation?>
    fun getMessages(conversationId: String): Flow<List<AiMessage>>
    fun sendMessage(conversationId: String, userMessage: String): Flow<AiMessage>
    suspend fun createConversation(title: String, userId: String): String
    suspend fun generateBlueprint(conversationId: String): ProjectBlueprint
}

interface InventoryRepository {
    fun getAllItems(): Flow<List<InventoryItem>>
    fun getItemByCode(code: String): Flow<InventoryItem?>
    suspend fun updateStatus(itemCode: String, status: InventoryStatus, notes: String? = null)
}

interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(
        email: String,
        pass: String,
        name: String,
        role: UserRole = UserRole.STUDENT,
        college: String? = null
    ): Result<User>
    suspend fun loginAsGuest(): Result<User>
    suspend fun loginWithTesterCode(code: String, email: String? = null): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun logout()
    suspend fun deleteAccount(): Result<Unit>
    suspend fun requestPasswordReset(email: String): Result<String>
}

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun insertNotification(notification: Notification)
}

