package com.rekora.app.data.local.dao

import androidx.room.*
import com.rekora.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeProject(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE domain = :domain ORDER BY createdAt DESC")
    fun getProjectsByDomain(domain: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE title LIKE '%' || :query || '%' OR shortDescription LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR domain LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE difficulty = :difficulty")
    fun getProjectsByDifficulty(difficulty: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE estimatedCostMax <= :maxBudget ORDER BY estimatedCostMin ASC")
    fun getProjectsWithinBudget(maxBudget: Double): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}

@Dao
interface ComponentDao {
    @Query("SELECT * FROM components ORDER BY name ASC")
    fun getAllComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getComponentById(id: String): ComponentEntity?

    @Query("SELECT * FROM components WHERE id = :id")
    fun observeComponent(id: String): Flow<ComponentEntity?>

    @Query("SELECT * FROM components WHERE category = :category ORDER BY name ASC")
    fun getComponentsByCategory(category: String): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchComponents(query: String): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE isRentable = 1 AND availableQuantity > 0")
    fun getRentableComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE availableQuantity > 0 ORDER BY name ASC")
    fun getAvailableComponents(): Flow<List<ComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<ComponentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: ComponentEntity)

    @Update
    suspend fun updateComponent(component: ComponentEntity)

    @Query("DELETE FROM components")
    suspend fun deleteAll()
}

@Dao
interface BomItemDao {
    @Query("SELECT * FROM bom_items WHERE projectId = :projectId")
    fun getBomForProject(projectId: String): Flow<List<BomItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBomItems(items: List<BomItemEntity>)

    @Query("DELETE FROM bom_items WHERE projectId = :projectId")
    suspend fun deleteBomForProject(projectId: String)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartCount(): Flow<Int>

    @Query("SELECT * FROM cart_items WHERE componentId = :componentId")
    suspend fun getCartItemByComponent(componentId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM orders WHERE userId = :userId")
    suspend fun deleteOrdersForUser(userId: String)
}

@Dao
interface RentalDao {
    @Query("SELECT * FROM rentals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRentalsForUser(userId: String): Flow<List<RentalEntity>>

    @Query("SELECT * FROM rentals WHERE id = :id")
    suspend fun getRentalById(id: String): RentalEntity?

    @Query("SELECT * FROM rentals WHERE id = :id")
    fun observeRentalById(id: String): Flow<RentalEntity?>

    @Query("SELECT * FROM rentals WHERE status IN ('ACTIVE', 'DUE_SOON', 'OVERDUE') AND userId = :userId")
    fun getActiveRentals(userId: String): Flow<List<RentalEntity>>

    @Query("SELECT * FROM rentals ORDER BY createdAt DESC")
    fun getAllRentals(): Flow<List<RentalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRental(rental: RentalEntity)

    @Update
    suspend fun updateRental(rental: RentalEntity)

    @Query("DELETE FROM rentals WHERE userId = :userId")
    suspend fun deleteRentalsForUser(userId: String)
}

@Dao
interface FabricationJobDao {
    @Query("SELECT * FROM fabrication_jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJobsForUser(userId: String): Flow<List<FabricationJobEntity>>

    @Query("SELECT * FROM fabrication_jobs WHERE id = :id")
    suspend fun getJobById(id: String): FabricationJobEntity?

    @Query("SELECT * FROM fabrication_jobs WHERE id = :id")
    fun observeJobById(id: String): Flow<FabricationJobEntity?>

    @Query("SELECT * FROM fabrication_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<FabricationJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: FabricationJobEntity)

    @Update
    suspend fun updateJob(job: FabricationJobEntity)

    @Query("DELETE FROM fabrication_jobs WHERE userId = :userId")
    suspend fun deleteJobsForUser(userId: String)
}

@Dao
interface MarketplaceDao {
    @Query("SELECT * FROM marketplace_listings WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveListings(): Flow<List<MarketplaceListingEntity>>

    @Query("SELECT * FROM marketplace_listings WHERE id = :id")
    suspend fun getListingById(id: String): MarketplaceListingEntity?

    @Query("SELECT * FROM marketplace_listings WHERE id = :id")
    fun observeListingById(id: String): Flow<MarketplaceListingEntity?>

    @Query("SELECT * FROM marketplace_listings WHERE sellerId = :userId ORDER BY createdAt DESC")
    fun getListingsForUser(userId: String): Flow<List<MarketplaceListingEntity>>

    @Query("SELECT * FROM marketplace_listings WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchListings(query: String): Flow<List<MarketplaceListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<MarketplaceListingEntity>)

    @Update
    suspend fun updateListing(listing: MarketplaceListingEntity)

    @Delete
    suspend fun deleteListing(listing: MarketplaceListingEntity)
}

@Dao
interface AiConversationDao {
    @Query("SELECT * FROM ai_conversations WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getConversationsForUser(userId: String): Flow<List<AiConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): AiConversationEntity?

    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    fun observeConversationById(id: String): Flow<AiConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AiConversationEntity)

    @Update
    suspend fun updateConversation(conversation: AiConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: AiConversationEntity)

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM ai_conversations WHERE userId = :userId")
    suspend fun deleteConversationsForUser(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteNotificationsForUser(userId: String)
}

@Dao
interface SavedProjectDao {
    @Query("SELECT * FROM saved_projects WHERE userId = :userId ORDER BY savedAt DESC")
    fun getSavedProjects(userId: String): Flow<List<SavedProjectEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_projects WHERE userId = :userId AND projectId = :projectId)")
    fun isProjectSaved(userId: String, projectId: String): Flow<Boolean>

    @Query("SELECT p.* FROM projects p INNER JOIN saved_projects s ON p.id = s.projectId WHERE s.userId = :userId ORDER BY s.savedAt DESC")
    fun getSavedProjectsFull(userId: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProject(entity: SavedProjectEntity)

    @Query("DELETE FROM saved_projects WHERE userId = :userId AND projectId = :projectId")
    suspend fun unsaveProject(userId: String, projectId: String)

    @Query("DELETE FROM saved_projects WHERE userId = :userId")
    suspend fun deleteSavedProjectsForUser(userId: String)
}

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY itemCode ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE itemCode = :code")
    suspend fun getItemByCode(code: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE itemCode = :code")
    fun observeItemByCode(code: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE componentId = :componentId AND currentStatus = 'AVAILABLE'")
    fun getAvailableItemsForComponent(componentId: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)
}

@Dao
interface ProjectVersionDao {
    @Query("SELECT * FROM project_versions WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getVersionsForProject(projectId: String): Flow<List<ProjectVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ProjectVersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<ProjectVersionEntity>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

