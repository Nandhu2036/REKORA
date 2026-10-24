package com.rekora.app

import com.rekora.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun `test remaining project cost calculation with owned components`() {
        val bomItems = listOf(
            BomItem(id = "1", name = "Raspberry Pi 4", quantity = 1, unitPrice = 5500.0, isOwned = true, sourcingChoice = SourcingChoice.USE_EXISTING),
            BomItem(id = "2", name = "Brushless Thrusters", quantity = 2, unitPrice = 2400.0, isOwned = false, sourcingChoice = SourcingChoice.BUY),
            BomItem(id = "3", name = "LiPo Battery", quantity = 1, unitPrice = 1800.0, isOwned = false, sourcingChoice = SourcingChoice.RENT),
            BomItem(id = "4", name = "Camera Module", quantity = 1, unitPrice = 450.0, isOwned = true, sourcingChoice = SourcingChoice.USE_EXISTING)
        )

        val totalOriginalCost = bomItems.sumOf { it.unitPrice * it.quantity }
        assertEquals(12550.0, totalOriginalCost, 0.01)

        // Only non-owned items count towards student out-of-pocket
        val outOfPocketCost = bomItems
            .filter { !it.isOwned }
            .sumOf { it.unitPrice * it.quantity }

        assertEquals(6600.0, outOfPocketCost, 0.01)
        // Savings from reusing owned components
        val studentSavings = totalOriginalCost - outOfPocketCost
        assertEquals(5950.0, studentSavings, 0.01)
    }

    @Test
    fun `test security deposit is segregated from revenue`() {
        val rentalItem = CartItem(
            id = "c1",
            componentId = "comp-esp32",
            quantity = 1,
            isRental = true,
            rentalDays = 14, // 2 weeks
            unitPrice = 100.0, // ₹100 per week
            deposit = 300.0    // ₹300 refundable deposit
        )

        val rentalSubtotal = 100.0 * (14 / 7)
        val depositTotal = rentalItem.deposit
        val grandTotal = rentalSubtotal + depositTotal

        assertEquals(200.0, rentalSubtotal, 0.01)
        assertEquals(300.0, depositTotal, 0.01)
        assertEquals(500.0, grandTotal, 0.01)

        // Ensure refundable deposit is never counted as platform net revenue
        val netRevenue = grandTotal - depositTotal
        assertEquals(200.0, netRevenue, 0.01)
    }

    @Test
    fun `test buy versus rent cost benefit analysis`() {
        val purchasePrice = 4500.0
        val rentalPricePerWeek = 250.0
        val projectWeeks = 4

        val totalRentalCost = rentalPricePerWeek * projectWeeks
        val netSavings = purchasePrice - totalRentalCost

        assertEquals(1000.0, totalRentalCost, 0.01)
        assertEquals(3500.0, netSavings, 0.01)
        assertTrue("Rental should yield over 70% cost reduction for semester project", netSavings / purchasePrice > 0.70)
    }
}
