package com.pashu360.app.feature.feeding.domain.repository

import com.pashu360.app.core.domain.model.FeedInventory
import com.pashu360.app.core.domain.model.FeedRecord
import com.pashu360.app.core.domain.model.FeedRecordWithType
import com.pashu360.app.core.domain.model.FeedType
import com.pashu360.app.core.domain.model.InventoryWithType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface FeedingRepository {
    // Feed types
    fun observeFeedTypes(farmId: String): Flow<List<FeedType>>
    suspend fun ensureDefaultFeedTypes(farmId: String)
    suspend fun addFeedType(feedType: FeedType): Result<Unit>
    suspend fun updateFeedType(feedType: FeedType): Result<Unit>
    suspend fun softDeleteFeedType(id: String): Result<Unit>

    // Feed records
    fun observeRecordsForDate(farmId: String, date: LocalDate): Flow<List<FeedRecordWithType>>
    fun observeRecordsForAnimal(animalId: String, limit: Int = 60): Flow<List<FeedRecordWithType>>
    fun observeDailyTotal(farmId: String, date: LocalDate): Flow<Double>
    suspend fun logFeed(record: FeedRecord, deductFromInventory: Boolean = true): Result<Unit>
    suspend fun deleteRecord(id: String): Result<Unit>

    // Inventory
    fun observeInventory(farmId: String): Flow<List<InventoryWithType>>
    fun countLowStockItems(farmId: String): Flow<Int>
    suspend fun addStock(farmId: String, feedTypeId: String, quantity: Double): Result<Unit>
    suspend fun setThreshold(farmId: String, feedTypeId: String, threshold: Double): Result<Unit>
    suspend fun getLowStockInventory(farmId: String): List<InventoryWithType>
}
