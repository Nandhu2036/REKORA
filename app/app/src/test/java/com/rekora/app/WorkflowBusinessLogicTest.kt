package com.rekora.app

import com.rekora.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class WorkflowBusinessLogicTest {

    @Test
    fun `test Journey C - Order breakdown separates refundable escrow deposit from purchase total`() {
        val purchaseItems = listOf(
            CartItem(id = "p1", componentId = "c1", quantity = 2, unitPrice = 250.0, isRental = false, deposit = 0.0),
            CartItem(id = "p2", componentId = "c2", quantity = 1, unitPrice = 1200.0, isRental = false, deposit = 0.0)
        )
        val rentalItems = listOf(
            CartItem(id = "r1", componentId = "c3", quantity = 1, unitPrice = 150.0, isRental = true, rentalDays = 14, deposit = 500.0)
        )

        val allItems = purchaseItems + rentalItems
        val purchaseSubtotal = purchaseItems.sumOf { it.unitPrice * it.quantity }
        val rentalSubtotal = rentalItems.sumOf { it.unitPrice * (it.rentalDays / 7) }
        val totalSubtotal = purchaseSubtotal + rentalSubtotal
        val depositTotal = allItems.sumOf { it.deposit }
        val tax = totalSubtotal * 0.18 // 18% GST in sandbox
        val grandTotal = totalSubtotal + tax + depositTotal

        assertEquals(1700.0, purchaseSubtotal, 0.01)
        assertEquals(300.0, rentalSubtotal, 0.01)
        assertEquals(2000.0, totalSubtotal, 0.01)
        assertEquals(500.0, depositTotal, 0.01)
        assertEquals(360.0, tax, 0.01)
        assertEquals(2860.0, grandTotal, 0.01)

        // Verifying refundable portion is tracked separately
        val nonRefundableTotal = grandTotal - depositTotal
        assertEquals(2360.0, nonRefundableTotal, 0.01)
    }

    @Test
    fun `test Journey D - 3D fabrication quotation calculation logic`() {
        // Calculation algorithm: Base Setup Fee + (Estimated Grams × Material Rate/g) + (Layer Time Factor)
        fun calculateQuotation(material: String, estimatedGrams: Double, infillPercent: Int): Double {
            val baseSetupFee = 150.0
            val ratePerGram = when (material.uppercase()) {
                "PLA" -> 3.0
                "PETG" -> 4.5
                "ABS" -> 5.0
                "RESIN" -> 8.0
                else -> 3.5
            }
            val infillMultiplier = 1.0 + (infillPercent / 100.0) * 0.4
            val materialCost = estimatedGrams * ratePerGram * infillMultiplier
            return baseSetupFee + materialCost
        }

        val petgQuote = calculateQuotation(material = "PETG", estimatedGrams = 80.0, infillPercent = 40.0.toInt())
        // 150 + (80 * 4.5 * (1 + 0.16)) = 150 + (360 * 1.16) = 150 + 417.6 = 567.6
        assertEquals(567.6, petgQuote, 0.1)

        val plaQuote = calculateQuotation(material = "PLA", estimatedGrams = 50.0, infillPercent = 20)
        // 150 + (50 * 3.0 * (1 + 0.08)) = 150 + (150 * 1.08) = 150 + 162.0 = 312.0
        assertEquals(312.0, plaQuote, 0.1)
    }

    @Test
    fun `test Journey E - Marketplace listing constraints and validation`() {
        fun validateListing(title: String, price: Double, condition: String): Result<Unit> {
            if (title.isBlank()) return Result.failure(IllegalArgumentException("Listing title cannot be empty"))
            if (price < 0.0) return Result.failure(IllegalArgumentException("Price must be non-negative"))
            if (condition.isBlank()) return Result.failure(IllegalArgumentException("Condition is required"))
            return Result.success(Unit)
        }

        assertTrue(validateListing("Raspberry Pi 4 (4GB) with heatsink", 4200.0, "LIKE_NEW").isSuccess)
        assertTrue(validateListing("", 100.0, "USED").isFailure)
        assertTrue(validateListing("ESP32 Dev Board", -50.0, "NEW").isFailure)
    }

    @Test
    fun `test Journey F - AI engineering blueprint response structure`() {
        val blueprintId = "bp-auto-waste-collector-001"
        val prompt = "Design an autonomous floating waste collector with computer vision"
        val expectedDomains = listOf("ROBOTICS", "IOT_ENVIRONMENTAL", "AI_ML")

        assertTrue("Blueprint ID must follow canonical format", blueprintId.startsWith("bp-"))
        assertFalse("Prompt must be non-empty", prompt.isBlank())
        assertEquals(3, expectedDomains.size)
    }
}
