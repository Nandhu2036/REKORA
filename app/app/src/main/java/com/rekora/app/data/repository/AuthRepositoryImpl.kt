package com.rekora.app.data.repository

import com.rekora.app.BuildConfig
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.dao.*
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.User
import com.rekora.app.domain.model.UserRole
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.util.AuthorizedAccessCodes
import com.rekora.app.util.SecurityUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val rentalDao: RentalDao,
    private val fabricationJobDao: FabricationJobDao,
    private val aiConversationDao: AiConversationDao,
    private val notificationDao: NotificationDao,
    private val savedProjectDao: SavedProjectDao,
    private val rekoraPreferences: RekoraPreferences
) : AuthRepository {

    override val currentUser: Flow<User?> = rekoraPreferences.userId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(null)
        } else {
            userDao.observeUserById(userId).map { userEntity ->
                userEntity?.toDomain()
            }
        }
    }

    override val isLoggedIn: Flow<Boolean> = rekoraPreferences.isLoggedIn

    override suspend fun login(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid academic or professional email address."))
        }
        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must contain at least 6 characters."))
        }

        val existingUserEntity = userDao.getUserByEmail(trimmedEmail)
        val user = if (existingUserEntity != null) {
            // Verify salted password hash
            if (existingUserEntity.passwordHash.isNotBlank()) {
                val matches = SecurityUtils.verifyPassword(
                    password = pass,
                    salt = existingUserEntity.passwordSalt,
                    expectedHash = existingUserEntity.passwordHash
                )
                if (!matches) {
                    return Result.failure(IllegalArgumentException("Invalid password. Please check your credentials and try again."))
                }
            } else {
                // First-time upgrade: hash the provided password
                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashPassword(pass, salt)
                userDao.updateUser(existingUserEntity.copy(passwordHash = hash, passwordSalt = salt))
            }
            existingUserEntity.toDomain()
        } else {
            // Check if dev auto-provision is permitted
            val isDev = BuildConfig.DEV_MODE || rekoraPreferences.isDevMode.first()
            if (!isDev) {
                return Result.failure(IllegalArgumentException("Account not found. Please register or sign in using Authorized Tester Access."))
            }

            // Auto-provision with secure salted password hash
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPassword(pass, salt)
            val displayName = trimmedEmail.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                email = trimmedEmail,
                displayName = displayName,
                role = UserRole.STUDENT,
                college = "National Institute of Technology",
                department = "Mechatronics & Robotics",
                skills = listOf("Embedded Systems", "CAD Modelling", "Python"),
                interests = listOf("Autonomous Systems", "Smart Agriculture"),
                isEmailVerified = true,
                isProfileComplete = true,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser.toEntity(passwordHash = hash, passwordSalt = salt))
            newUser
        }

        rekoraPreferences.setLoggedIn(
            userId = user.id,
            userName = user.displayName,
            email = user.email,
            role = user.role.name
        )

        return Result.success(user)
    }

    override suspend fun register(
        email: String,
        pass: String,
        name: String,
        role: UserRole,
        college: String?
    ): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Valid email required."))
        }
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name cannot be blank."))
        }
        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(IllegalStateException("An account with this email address already exists."))
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(pass, salt)

        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            displayName = trimmedName,
            role = role,
            college = college ?: "REKORA Campus Lab",
            isEmailVerified = true,
            isProfileComplete = true,
            createdAt = System.currentTimeMillis()
        )

        userDao.insertUser(newUser.toEntity(passwordHash = hash, passwordSalt = salt))
        rekoraPreferences.setLoggedIn(
            userId = newUser.id,
            userName = newUser.displayName,
            email = newUser.email,
            role = newUser.role.name
        )

        return Result.success(newUser)
    }

    override suspend fun loginWithTesterCode(code: String, email: String?): Result<User> {
        val grant = AuthorizedAccessCodes.validateCode(code)
            ?: return Result.failure(IllegalArgumentException("Invalid authorization code. Format: PREFIX-YEAR-ID (e.g., TESTER-2026-X7)"))

        val userEmail = if (!email.isNullOrBlank() && email.contains("@")) {
            email.trim().lowercase()
        } else {
            "authorized.${grant.role.name.lowercase()}@rekora.internal"
        }

        val existingUser = userDao.getUserByEmail(userEmail)
        val user = if (existingUser != null) {
            val updated = existingUser.copy(
                role = grant.role.name,
                college = grant.organization,
                department = grant.department
            )
            userDao.updateUser(updated)
            updated.toDomain()
        } else {
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPassword("tester2026", salt)
            val newUser = User(
                id = "usr-${grant.role.name.lowercase()}-${UUID.randomUUID().toString().take(8)}",
                email = userEmail,
                displayName = grant.defaultName,
                role = grant.role,
                college = grant.organization,
                department = grant.department,
                skills = listOf("System Verification", "Hardware Testing", "Compliance"),
                interests = listOf("Robotics Validation", "Firmware QA", "BOM Auditing"),
                bio = "Authorized test identity verified via code: ${code.uppercase()}",
                isEmailVerified = true,
                isProfileComplete = true,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser.toEntity(passwordHash = hash, passwordSalt = salt))
            newUser
        }

        rekoraPreferences.setLoggedIn(
            userId = user.id,
            userName = user.displayName,
            email = user.email,
            role = user.role.name
        )

        return Result.success(user)
    }

    override suspend fun loginAsGuest(): Result<User> {
        val guestUser = User(
            id = "usr-guest-engineer",
            email = "engineer.guest@rekora.edu",
            displayName = "Guest Engineer",
            role = UserRole.STUDENT,
            college = "REKORA Campus Technology Lab",
            department = "Department of Multidisciplinary Engineering",
            skills = listOf("Rapid Prototyping", "Arduino", "3D Printing"),
            interests = listOf("Robotics", "IoT Environmental", "Power Electronics"),
            bio = "Exploring hands-on campus projects, lab inventory, and hardware fabrication.",
            isEmailVerified = true,
            isProfileComplete = true,
            createdAt = System.currentTimeMillis()
        )

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword("guestPass123", salt)
        userDao.insertUser(guestUser.toEntity(passwordHash = hash, passwordSalt = salt))

        rekoraPreferences.setLoggedIn(
            userId = guestUser.id,
            userName = guestUser.displayName,
            email = guestUser.email,
            role = guestUser.role.name
        )

        return Result.success(guestUser)
    }

    override suspend fun updateProfile(user: User): Result<User> {
        val existingEntity = userDao.getUserById(user.id)
        val hash = existingEntity?.passwordHash ?: ""
        val salt = existingEntity?.passwordSalt ?: ""
        userDao.updateUser(user.toEntity(passwordHash = hash, passwordSalt = salt))
        rekoraPreferences.setLoggedIn(
            userId = user.id,
            userName = user.displayName,
            email = user.email,
            role = user.role.name
        )
        return Result.success(user)
    }

    override suspend fun logout() {
        rekoraPreferences.setLoggedOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val currentUserId = rekoraPreferences.userId.first()
        if (currentUserId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("No active session to delete."))
        }

        try {
            // Cascaded deletion of all associated user data
            orderDao.deleteOrdersForUser(currentUserId)
            rentalDao.deleteRentalsForUser(currentUserId)
            fabricationJobDao.deleteJobsForUser(currentUserId)
            aiConversationDao.deleteConversationsForUser(currentUserId)
            notificationDao.deleteNotificationsForUser(currentUserId)
            savedProjectDao.deleteSavedProjectsForUser(currentUserId)
            userDao.deleteUserById(currentUserId)

            // Clear session preferences
            rekoraPreferences.clearAllUserData()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val user = userDao.getUserByEmail(trimmed)
        if (user == null) {
            return Result.failure(IllegalArgumentException("No account registered with $trimmed."))
        }
        return Result.success("Password reset instructions dispatched to $trimmed. For local developer mode, use Authorized Access code TESTER-2026-X7 or log in as Guest.")
    }
}
