package com.rekora.app

import com.rekora.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ProjectMatcherTest {

    private fun calculateMatch(project: Project, criteria: MatchCriteria): ProjectMatch {
        var score = 0
        val reasons = mutableListOf<String>()

        // 1. Budget fit (30% weight)
        val budgetFit = criteria.maxBudget <= 0.0 || project.estimatedCost.min <= criteria.maxBudget
        if (budgetFit) {
            score += 30
            reasons.add("Fits within max budget of ₹${criteria.maxBudget.toInt()}")
        }

        // 2. Domain alignment (25% weight)
        if (criteria.preferredDomain == null || criteria.preferredDomain == project.domain) {
            score += 25
            reasons.add("Matches preferred domain: ${project.domain.displayName}")
        }

        // 3. Timeline fit (20% weight)
        val timelineFit = project.timelineWeeks <= criteria.timelineWeeks
        if (timelineFit) {
            score += 20
            reasons.add("Feasible within your ${criteria.timelineWeeks}-week timeline")
        }

        // 4. Component overlap (15% weight)
        val ownedOverlap = criteria.ownedComponents.count { owned ->
            project.hardwareRequirements.any { req -> req.contains(owned, ignoreCase = true) }
        }
        if (ownedOverlap > 0) {
            val componentPoints = (ownedOverlap * 5).coerceAtMost(15)
            score += componentPoints
            reasons.add("Reuses $ownedOverlap of your owned components")
        }

        // 5. Difficulty alignment (10% weight)
        val skillFit = criteria.difficulty == null || criteria.difficulty == project.difficulty
        if (skillFit) {
            score += 10
            reasons.add("Suitable for ${project.difficulty.displayName} skill level")
        }

        return ProjectMatch(
            project = project,
            matchScore = score.coerceIn(0, 100),
            matchReasons = reasons,
            budgetFit = budgetFit,
            timelineFit = timelineFit,
            skillFit = skillFit,
            componentOverlap = ownedOverlap
        )
    }

    @Test
    fun `test perfect match produces high score with explainable reasons`() {
        val testProject = Project(
            id = "proj-test-1",
            title = "Autonomous Floating Waste Collector",
            domain = EngineeringDomain.ROBOTICS,
            difficulty = Difficulty.ADVANCED,
            estimatedCost = PriceRange(min = 12000.0, max = 16000.0),
            timelineWeeks = 7,
            hardwareRequirements = listOf("Raspberry Pi 4 Model B (4GB)", "Dual Underwater Thrusters", "LiPo Battery")
        )

        val studentCriteria = MatchCriteria(
            maxBudget = 15000.0,
            preferredDomain = EngineeringDomain.ROBOTICS,
            timelineWeeks = 8,
            difficulty = Difficulty.ADVANCED,
            ownedComponents = listOf("Raspberry Pi 4", "LiPo Battery")
        )

        val match = calculateMatch(testProject, studentCriteria)

        assertTrue("Score should be >= 90% for closely matching constraints", match.matchScore >= 90)
        assertTrue(match.budgetFit)
        assertTrue(match.timelineFit)
        assertTrue(match.skillFit)
        assertEquals(2, match.componentOverlap)
        assertTrue(match.matchReasons.any { it.contains("Fits within max budget") })
        assertTrue(match.matchReasons.any { it.contains("Reuses 2 of your owned components") })
    }

    @Test
    fun `test out-of-budget project receives lower score and identifies constraint violation`() {
        val expensiveProject = Project(
            id = "proj-expensive",
            title = "High Altitude LiDAR Drone",
            domain = EngineeringDomain.AEROSPACE,
            difficulty = Difficulty.EXPERT,
            estimatedCost = PriceRange(min = 45000.0, max = 60000.0),
            timelineWeeks = 12
        )

        val lowBudgetCriteria = MatchCriteria(
            maxBudget = 5000.0,
            preferredDomain = EngineeringDomain.ROBOTICS,
            timelineWeeks = 4
        )

        val match = calculateMatch(expensiveProject, lowBudgetCriteria)

        assertFalse("Project exceeds student budget constraint", match.budgetFit)
        assertFalse("Project exceeds student timeline constraint", match.timelineFit)
        assertTrue("Score should be below 30% when major constraints fail", match.matchScore < 30)
    }
}
