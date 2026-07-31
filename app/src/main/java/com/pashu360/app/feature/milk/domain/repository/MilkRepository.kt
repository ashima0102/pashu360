package com.pashu360.app.feature.milk.domain.repository

import com.pashu360.app.core.domain.model.BulkMilkEntry
import com.pashu360.app.core.domain.model.DailyMilkTotal
import com.pashu360.app.core.domain.model.MilkRecord
import com.pashu360.app.core.domain.model.MilkSession
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface MilkRepository {
    fun observeRecordsForDate(farmId: String, date: LocalDate): Flow<List<MilkRecord>>
    fun observeRecordsForAnimal(animalId: String, limit: Int = 60): Flow<List<MilkRecord>>
    fun observeDailyTotal(farmId: String, date: LocalDate): Flow<Double>
    fun observeWeeklyTotals(farmId: String, endDate: LocalDate): Flow<List<DailyMilkTotal>>

    /**
     * Builds the bulk entry list: every active animal, pre-filled with today's
     * existing record for the given session if one exists.
     */
    suspend fun buildBulkEntry(
        farmId: String,
        date: LocalDate,
        session: MilkSession
    ): List<BulkMilkEntry>

    suspend fun saveBulkEntry(
        farmId: String,
        date: LocalDate,
        session: MilkSession,
        entries: List<BulkEntryInput>
    ): Result<Int>

    suspend fun deleteRecord(id: String): Result<Unit>
}

data class BulkEntryInput(
    val animalId: String,
    val quantityLiters: Double?,
    val fatPct: Double? = null,
    val snfPct: Double? = null
)
