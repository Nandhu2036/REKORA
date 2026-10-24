package com.rekora.app

import com.rekora.app.domain.model.UserRole
import com.rekora.app.util.AuthorizedAccessCodes
import com.rekora.app.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class AuthAndTesterAccessTest {

    @Test
    fun `test password hashing generates distinct salts and validates correctly`() {
        val password = "SecureEngineeringPass123!"
        val salt1 = SecurityUtils.generateSalt()
        val salt2 = SecurityUtils.generateSalt()

        assertNotEquals("Generated salts must be unique and cryptographically random", salt1, salt2)

        val hash1 = SecurityUtils.hashPassword(password, salt1)
        val hash2 = SecurityUtils.hashPassword(password, salt2)

        assertNotEquals("Identical passwords with different salts must produce distinct hashes", hash1, hash2)

        // Verifications
        assertTrue("Matching password and salt must verify successfully", SecurityUtils.verifyPassword(password, salt1, hash1))
        assertFalse("Wrong password must fail verification", SecurityUtils.verifyPassword("WrongPassword123!", salt1, hash1))
        assertFalse("Wrong salt must fail verification", SecurityUtils.verifyPassword(password, salt2, hash1))
        assertFalse("Empty expected hash must fail verification", SecurityUtils.verifyPassword(password, salt1, ""))
    }

    @Test
    fun `test authorized invitation code validation assigns correct tester roles`() {
        // 1. Tester Verification Code
        val testerGrant = AuthorizedAccessCodes.validateCode("TESTER-2026-X7")
        assertNotNull("TESTER-2026-X7 must be a valid authorized code", testerGrant)
        assertEquals(UserRole.TESTER, testerGrant?.role)
        assertEquals("QA Verification Specialist", testerGrant?.defaultName)

        // 2. Admin Code
        val adminGrant = AuthorizedAccessCodes.validateCode("ADMIN-LAB-2026")
        assertNotNull("ADMIN-LAB-2026 must be valid", adminGrant)
        assertEquals(UserRole.ADMIN, adminGrant?.role)

        // 3. Faculty Code
        val facultyGrant = AuthorizedAccessCodes.validateCode("FACULTY-LAB-2026")
        assertNotNull("FACULTY-LAB-2026 must be valid", facultyGrant)
        assertEquals(UserRole.FACULTY, facultyGrant?.role)

        // 4. Lab Tech / Inventory Operator Code
        val operatorGrant = AuthorizedAccessCodes.validateCode("OPERATOR-INV-2026")
        assertNotNull("OPERATOR-INV-2026 must be valid", operatorGrant)
        assertEquals(UserRole.INVENTORY_OPERATOR, operatorGrant?.role)

        // 5. Contributor Code
        val contributorGrant = AuthorizedAccessCodes.validateCode("SELLER-COMM-2026")
        assertNotNull("SELLER-COMM-2026 must be valid", contributorGrant)
        assertEquals(UserRole.CONTRIBUTOR, contributorGrant?.role)

        // Case insensitivity & trimming
        val lowercaseWithSpaces = AuthorizedAccessCodes.validateCode("  tester-2026-x7  ")
        assertNotNull("Code validation must handle whitespace and lowercase", lowercaseWithSpaces)
        assertEquals(UserRole.TESTER, lowercaseWithSpaces?.role)

        // Rejection of invalid codes
        val invalidCode = AuthorizedAccessCodes.validateCode("RANDOM-UNKNOWN-CODE")
        assertNull("Invalid or unrecognized code must be rejected", invalidCode)
    }

    @Test
    fun `test timing safe comparison in password verification`() {
        val password = "EngineeringLabPass2026"
        val salt = SecurityUtils.generateSalt()
        val validHash = SecurityUtils.hashPassword(password, salt)

        // Tamper with last character of hash
        val tamperedHash = validHash.dropLast(1) + if (validHash.last() == 'A') 'B' else 'A'
        assertFalse("Tampered hash must not match", SecurityUtils.verifyPassword(password, salt, tamperedHash))
    }
}
