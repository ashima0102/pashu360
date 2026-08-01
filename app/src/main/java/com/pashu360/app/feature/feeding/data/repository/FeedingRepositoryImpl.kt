package com.pashu360.app.feature.feeding.data.repository

import com.pashu360.app.core.domain.model.FeedCategory
import com.pashu360.app.core.domain.model.FeedInventory
import com.pashu360.app.core.domain.model.FeedRecord
import com.pashu360.app.core.domain.model.FeedRecordWithType
import com.pashu360.app.core.domain.model.FeedType
import com.pashu360.app.core.domain.model.FeedTypeCatalog
import com.pashu360.app.core.domain.model.InventoryWithType
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.feeding.data.local.FeedInventoryDao
import com.pashu360.app.feature.feeding.data.local.FeedInventoryEntity
import com.pashu360.app.feature.feeding.data.local.FeedRecordDao
import com.pashu360.app.feature.feeding.data.local.FeedRecordEntity
import com.pashu360.app.feature.feeding.data.local.FeedTypeDao
import com.pashu360.app.feature.feeding.data.local.FeedTypeEntity
import com.pashu360.app.feature.feeding.domain.repository.FeedingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Singleton
class FeedingRepositoryImpl @Inject constructor(
    private val feedTypeDao: FeedTypeDao,
    private val feedRecordDao: FeedRecordDao,
    private val feedInventoryDao: FeedInventoryDao,
    private val animalDao: AnimalDao
) : FeedingRepository {

    // ── FEED TYPES ────────────────────────────────
    override fun observeFeedTypes(farmId: String): Flow<List<FeedType>> =
        feedTypeDao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override suspend fun ensureDefaultFeedTypes(farmId: String) {
        withContext(Dispatchers.IO) {
            if (feedTypeDao.count(farmId) == 0) {
                val defaults = FeedTypeCatalog.defaults(farmId)
                feedTypeDao.insertAll(defaults.map(FeedTypeEntity::fromDomain))
                // Also create inventory rows at zero with sensible default thresholds.
                val today = today()
                defaults.forEach { ft ->
                    feedInventoryDao.insert(
                        FeedInventoryEntity(
                            farmId = farmId,
                            feedTypeId = ft.id,
                            quantity = 0.0,
                            lowStockThreshold = if (ft.category == FeedCategory.MINERAL_MIX) 500.0 else 50.0,
                            lastUpdated = today
                        )
                    )
                }
            }
        }
    }

    override suspend fun addFeedType(feedType: FeedType): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { feedTypeDao.insert(FeedTypeEntity.fromDomain(feedType)) }.map { }
        }

    override suspend fun updateFeedType(feedType: FeedType): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { feedTypeDao.update(FeedTypeEntity.fromDomain(feedType)) }.map { }
        }

    override suspend fun softDeleteFeedType(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { feedTypeDao.softDelete(id) }.map { }
        }

    // ── FEED RECORDS ─────────────────────────────
    override fun observeRecordsForDate(
        farmId: String, date: LocalDate
    ): Flow<List<FeedRecordWithType>> {
        val recordsFlow = feedRecordDao.observeForDate(farmId, date.toString())
        val typesFlow = feedTypeDao.observeAll(farmId)
        return combine(recordsFlow, typesFlow) { records, types ->
            val byId = types.associateBy { it.id }
            records.mapNotNull { r ->
                val ft = byId[r.feedTypeId] ?: return@mapNotNull null
                FeedRecordWithType(
                    record = r.toDomain(),
                    feedTypeName = ft.name,
                    feedTypeCategory = FeedCategory.from(ft.category),
                    feedTypeUnit = ft.unit,
                    estimatedCost = ft.costPerUnit?.let { it * r.quantity }
                )
            }
        }
    }

    override fun observeRecordsForAnimal(
        animalId: String, limit: Int
    ): Flow<List<FeedRecordWithType>> {
        // Not batched by animal; feed types are farm-scoped so just resolve inline.
        return feedRecordDao.observeForAnimal(animalId, limit).map { records ->
            withContext(Dispatchers.IO) {
                records.mapNotNull { r ->
                    val ft = feedTypeDao.getById(r.feedTypeId) ?: return@mapNotNull null
                    FeedRecordWithType(
                        record = r.toDomain(),
                        feedTypeName = ft.name,
                        feedTypeCategory = FeedCategory.from(ft.category),
                        feedTypeUnit = ft.unit,
                        estimatedCost = ft.costPerUnit?.let { it * r.quantity }
                    )
                }
            }
        }
    }

    override fun observeDailyTotal(farmId: String, date: LocalDate): Flow<Double> =
        feedRecordDao.observeDailyTotal(farmId, date.toString()).map { it ?: 0.0 }

    override suspend fun logFeed(record: FeedRecord, deductFromInventory: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                feedRecordDao.insert(FeedRecordEntity.fromDomain(record))
                if (deductFromInventory) {
                    // Deduct if inventory exists for this type
                    val inv = feedInventoryDao.getForFeedType(record.farmId, record.feedTypeId)
                    if (inv != null) {
                        feedInventoryDao.adjustQuantity(
                            record.farmId, record.feedTypeId,
                            delta = -record.quantity,
                            date = today()
                        )
                    }
                }
            }
        }

    override suspend fun deleteRecord(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { feedRecordDao.deleteById(id) }.map { }
        }

    // ── INVENTORY ─────────────────────────────────
    override fun observeInventory(farmId: String): Flow<List<InventoryWithType>> {
        val invFlow = feedInventoryDao.observeAll(farmId)
        val typesFlow = feedTypeDao.observeAll(farmId)
        return combine(invFlow, typesFlow) { inv, types ->
            val byId = types.associateBy { it.id }
            inv.mapNotNull { i ->
                val ft = byId[i.feedTypeId] ?: return@mapNotNull null
                InventoryWithType(
                    inventory = i.toDomain(),
                    feedTypeName = ft.name,
                    feedTypeCategory = FeedCategory.from(ft.category),
                    feedTypeUnit = ft.unit
                )
            }.sortedBy { it.feedTypeName }
        }
    }

    override fun countLowStockItems(farmId: String): Flow<Int> =
        feedInventoryDao.countLowStock(farmId)

    override suspend fun addStock(farmId: String, feedTypeId: String, quantity: Double): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val existing = feedInventoryDao.getForFeedType(farmId, feedTypeId)
                if (existing == null) {
                    feedInventoryDao.insert(
                        FeedInventoryEntity(
                            farmId = farmId,
                            feedTypeId = feedTypeId,
                            quantity = quantity,
                            lowStockThreshold = 50.0,
                            lastUpdated = today()
                        )
                    )
                } else {
                    feedInventoryDao.adjustQuantity(farmId, feedTypeId, quantity, today())
                }
            }.map { }
        }

    override suspend fun setThreshold(
        farmId: String, feedTypeId: String, threshold: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { feedInventoryDao.updateThreshold(farmId, feedTypeId, threshold) }.map { }
    }

    override suspend fun getLowStockInventory(farmId: String): List<InventoryWithType> =
        withContext(Dispatchers.IO) {
            val lowStock = feedInventoryDao.getLowStock(farmId)
            lowStock.mapNotNull { inv ->
                val ft = feedTypeDao.getById(inv.feedTypeId) ?: return@mapNotNull null
                InventoryWithType(
                    inventory = inv.toDomain(),
                    feedTypeName = ft.name,
                    feedTypeCategory = FeedCategory.from(ft.category),
                    feedTypeUnit = ft.unit
                )
            }
        }

    private fun today(): String =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}
