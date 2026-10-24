package com.rekora.app.data.local

import android.util.Log
import androidx.room.withTransaction
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.local.dao.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DatabaseSeeder initializes the local Room database with rich demonstration engineering data
 * calibrated for Indian engineering academia and student prototyping.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val rekoraDatabase: RekoraDatabase,
    private val projectDao: ProjectDao,
    private val componentDao: ComponentDao,
    private val bomItemDao: BomItemDao,
    private val inventoryItemDao: InventoryItemDao,
    private val marketplaceDao: MarketplaceDao,
    private val projectVersionDao: ProjectVersionDao,
    private val rekoraPreferences: RekoraPreferences
) {

    companion object {
        private const val TAG = "DatabaseSeeder"
    }

    /**
     * Seeds the database if either the preference indicates unseeded state
     * or the project table is empty.
     */
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val isLoaded = rekoraPreferences.isSeedDataLoaded.first()
        val hasProjects = try {
            projectDao.getAllProjects().first().isNotEmpty()
        } catch (e: Exception) {
            false
        }

        if (!isLoaded || !hasProjects) {
            Log.d(TAG, "Database not yet seeded or missing projects. Starting initial seeding...")
            resetToSeedData()
        } else {
            Log.d(TAG, "Database already seeded. Skipping initial seeding.")
        }
    }

    /**
     * Clears or updates baseline entities and repopulates the full dataset from SeedDataProvider.
     */
    suspend fun resetToSeedData() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Populating database from SeedDataProvider...")
        try {
            rekoraDatabase.withTransaction {
                // 1. Projects
                val projects = SeedDataProvider.getSeedProjects()
                projectDao.insertProjects(projects)
                Log.d(TAG, "Seeded ${projects.size} projects")

                // 2. Components
                val components = SeedDataProvider.getSeedComponents()
                componentDao.insertComponents(components)
                Log.d(TAG, "Seeded ${components.size} components")

                // 3. BOM Items
                val bomItems = SeedDataProvider.getSeedBomItems()
                bomItemDao.insertBomItems(bomItems)
                Log.d(TAG, "Seeded ${bomItems.size} BOM items")

                // 4. Inventory QR Items
                val inventoryItems = SeedDataProvider.getSeedInventoryItems()
                inventoryItemDao.insertItems(inventoryItems)
                Log.d(TAG, "Seeded ${inventoryItems.size} inventory items")

                // 5. Marketplace Listings
                val listings = SeedDataProvider.getSeedMarketplaceListings()
                listings.forEach { listing ->
                    marketplaceDao.insertListing(listing)
                }
                Log.d(TAG, "Seeded ${listings.size} marketplace listings")

                // 6. Project Versions
                val versions = SeedDataProvider.getSeedProjectVersions()
                projectVersionDao.insertVersions(versions)
                Log.d(TAG, "Seeded ${versions.size} project versions")
            }

            // Mark seed data as loaded in persistent preferences
            rekoraPreferences.setSeedDataLoaded(true)
            Log.i(TAG, "Database seeding completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database: ${e.message}", e)
            throw e
        }
    }
}
