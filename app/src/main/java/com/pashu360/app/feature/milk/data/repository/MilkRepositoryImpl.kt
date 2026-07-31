package com.pashu360.app.feature.milk.data.repository

import com.pashu360.app.core.domain.model.BulkMilkEntry
import com.pashu360.app.core.domain.model.DailyMilkTotal
import com.pashu360.app.core.domain.model.MilkRecord
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.milk.data.local.MilkRecordDao
import com.pashu360.app.feature.milk.data.local.MilkRecordEntity
import com.pashu360.app.feature.milk.domain.repository.BulkEntryInput
import com.pashu360.app.feature.milk.domain.repository.MilkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilkRepositoryImpl @Inject constructor(
    private val milkDao: MilkRecordDao,
    private val animalDao: AnimalDao
) : MilkRepository {

    override fun observeRecordsForDate(farmId: String, date: LocalDate): Flow<List<MilkRecord>> =
        milkDao.observeRecordsForDate(farmId, date.toString())
            .map { list -> list.map { it.toDomain() } }

    override fun observeRecordsForAnimal(animalId: String, limit: Int): Flow<List<MilkRecord>> =
        milkDao.observeRecordsForAnimal(animalId, limit)
            .map { list -> list.map { it.toDomain() } }

    override fun observeDailyTotal(farmId: String, date: LocalDate): Flow<Double> =
        milkDao.observeDailyTotal(farmId, date.toString()).map { it ?: 0.0 }

    override fun observeWeeklyTotals(
        farmId: String,
        endDate: LocalDate
    ): Flow<List<DailyMilkTotal>> {
        val startDate = endDate.minus(DatePeriod(days = 6))
        return milkDao.observeDailyTotalsByDate(farmId, startDate.toString(), endDate.toString())
            .map { rows ->
                // Bucket by date, fill missing days with zeros so charts have 7 bars.
                val byDate = rows.groupBy { LocalDate.parse(it.recordDate) }
                (0..6).map { offset ->
                    val d = startDate.plus(DatePeriod(days = offset))
                    val rowsForDay = byDate[d].orEmpty()
                    DailyMilkTotal(
                        date = d,
                        morning = rowsForDay.firstOrNull { it.session == "morning" }?.total ?: 0.0,
                        evening = rowsForDay.firstOrNull { it.session == "evening" }?.total ?: 0.0
                    )
                }
            }
    }

    override fun observeBulkEntry(
        farmId: String,
        date: LocalDate,
        session: MilkSession
    ): Flow<List<BulkMilkEntry>> {
        // Reactively combine active animals + this session's records; re-emit
        // whenever either changes so newly added animals show up automatically.
        val animalsFlow = animalDao.observeActiveAnimals(farmId)
        val recordsFlow = milkDao.observeRecordsForDate(farmId, date.toString())

        return combine(animalsFlow, recordsFlow) { animals, records ->
            val existingForSession = records
                .filter { it.session == session.value }
                .associateBy { it.animalId }

            animals.map { a ->
                val existingRec = existingForSession[a.id]
                BulkMilkEntry(
                    animalId = a.id,
                    tagId = a.tagId,
                    animalName = a.name,
                    breed = a.breed,
                    existingQuantity = existingRec?.quantityLiters,
                    existingFat = existingRec?.fatPct,
                    existingSnf = existingRec?.snfPct
                )
            }
        }
    }

    override suspend fun saveBulkEntry(
        farmId: String,
        date: LocalDate,
        session: MilkSession,
        entries: List<BulkEntryInput>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val toInsert = entries
                .filter { (it.quantityLiters ?: 0.0) > 0.0 }
                .map { input ->
                    MilkRecordEntity(
                        animalId = input.animalId,
                        farmId = farmId,
                        recordDate = date.toString(),
                        session = session.value,
                        quantityLiters = input.quantityLiters!!,
                        fatPct = input.fatPct,
                        snfPct = input.snfPct
                    )
                }
            if (toInsert.isNotEmpty()) milkDao.insertAll(toInsert)
            toInsert.size
        }
    }

    override suspend fun deleteRecord(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { milkDao.deleteById(id) }.map { }
    }
}
