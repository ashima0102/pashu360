package com.pashu360.app.feature.breeding.domain.repository

import com.pashu360.app.core.domain.model.BreedingRecord
import com.pashu360.app.core.domain.model.BreedingRecordDetail
import com.pashu360.app.core.domain.model.ConceptionStatus
import com.pashu360.app.core.domain.model.HeatRecord
import com.pashu360.app.core.domain.model.HeatRecordDetail
import com.pashu360.app.core.domain.model.PregnancyDetail
import com.pashu360.app.core.domain.model.PregnancyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface BreedingRepository {
    // Heat
    fun observeHeatRecords(farmId: String): Flow<List<HeatRecordDetail>>
    fun observeHeatForAnimal(animalId: String): Flow<List<HeatRecord>>
    suspend fun addHeatRecord(record: HeatRecord): Result<Unit>
    suspend fun deleteHeatRecord(id: String): Result<Unit>

    // Breeding
    fun observeBreedingRecords(farmId: String): Flow<List<BreedingRecordDetail>>
    fun observeAwaitingPd(farmId: String): Flow<List<BreedingRecordDetail>>
    fun countConfirmed(farmId: String): Flow<Int>
    fun countAll(farmId: String): Flow<Int>
    suspend fun addBreedingRecord(record: BreedingRecord): Result<Unit>
    suspend fun setConceptionStatus(id: String, status: ConceptionStatus): Result<Unit>
    suspend fun deleteBreedingRecord(id: String): Result<Unit>

    // Pregnancy
    fun observePregnancies(farmId: String): Flow<List<PregnancyDetail>>
    fun observeActivePregnancies(farmId: String): Flow<List<PregnancyDetail>>
    fun observeCompletedPregnancies(farmId: String): Flow<List<PregnancyDetail>>
    fun countActivePregnancies(farmId: String): Flow<Int>
    fun countCalvingDueThisMonth(farmId: String, today: LocalDate): Flow<Int>
    suspend fun addPregnancyRecord(record: PregnancyRecord): Result<Unit>

    /**
     * Record calving completion. If [createCalfAnimal] is provided, a new Animal
     * is written and linked back via the calfAnimalId column.
     */
    suspend fun recordCalving(
        pregnancyId: String,
        actualDate: LocalDate,
        difficulty: Int?,
        outcome: com.pashu360.app.core.domain.model.CalvingOutcome?,
        notes: String?,
        createCalfAnimal: com.pashu360.app.core.domain.model.Animal? = null
    ): Result<Unit>

    // Scanner helpers
    suspend fun getLatestHeatPerAnimal(farmId: String): List<HeatRecord>
    suspend fun getPendingBreedings(farmId: String): List<BreedingRecord>
    suspend fun getCalvingsInWindow(
        farmId: String, today: LocalDate, cutoff: LocalDate
    ): List<PregnancyRecord>
}
