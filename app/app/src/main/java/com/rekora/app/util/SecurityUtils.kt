package com.rekora.app.util

import com.rekora.app.domain.model.UserRole
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityUtils {
    private val secureRandom = SecureRandom()

    fun generateSalt(length: Int = 16): String {
        val salt = ByteArray(length)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val computedHash = hashPassword(password, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }
}

object AuthorizedAccessCodes {
    data class CodeGrant(
        val role: UserRole,
        val defaultName: String,
        val organization: String,
        val department: String
    )

    val VALID_CODES: Map<String, CodeGrant> = mapOf(
        "TESTER-2026-X7" to CodeGrant(
            role = UserRole.TESTER,
            defaultName = "QA Verification Specialist",
            organization = "REKORA Quality Assurance",
            department = "Test Systems & Automation"
        ),
        "FACULTY-LAB-2026" to CodeGrant(
            role = UserRole.FACULTY,
            defaultName = "Dr. S. Ramanathan",
            organization = "Department of Mechatronics",
            department = "School of Electrical & Mechanical Sciences"
        ),
        "ADMIN-LAB-2026" to CodeGrant(
            role = UserRole.ADMIN,
            defaultName = "System Administrator",
            organization = "REKORA Central Operations",
            department = "Engineering Platform Infrastructure"
        ),
        "OPERATOR-INV-2026" to CodeGrant(
            role = UserRole.INVENTORY_OPERATOR,
            defaultName = "Inventory Controller",
            organization = "Campus Maker Space & Fabrication Lab",
            department = "Hardware Inventory & Logistics"
        ),
        "SELLER-COMM-2026" to CodeGrant(
            role = UserRole.CONTRIBUTOR,
            defaultName = "Hardware Contributor",
            organization = "Robotics & Embedded Systems Club",
            department = "Hardware Innovations Division"
        )
    )

    fun validateCode(code: String): CodeGrant? {
        val normalized = code.trim().uppercase()
        return VALID_CODES[normalized]
    }
}
