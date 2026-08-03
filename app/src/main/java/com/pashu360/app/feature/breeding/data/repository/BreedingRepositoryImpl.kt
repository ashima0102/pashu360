package com.pashu360.app.feature.breeding.data.repository

import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.core.domain.model.BreedingRecord
import com.pashu360.app.core.domain.model.BreedingRecordDetail
import com.pashu360.app.core.domain.model.CalvingOutcome
import com.pashu360.app.core.domain.model.ConceptionStatus
import com.pashu360.app.core.domain.model.HeatRecord
import com.pashu360.app.core.domain.model.HeatRecordDetail
import com.pashu360.app.core.domain.model.PregnancyDetail
import com.pashu360.app.core.domain.model.PregnancyRecord
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.breeding.data.local.BreedingRecordDao
import com.pashu360.app.feature.breeding.data.local.BreedingRecordEntity
import com.pashu360.app.feature.breeding.data.local.HeatRecordDao
import com.pashu360.app.feature.breeding.data.local.HeatRecordEntity
import com.pashu360.app.feature.breeding.data.local.PregnancyRecordDao
import com.pashu360.app.feature.breeding.data.local.PregnancyRecordEntity
import com.pashu360.app.feature.breeding.domain.repository.BreedingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreedingRepositoryImpl @Inject constructor(
    private val heatDao: HeatRecordDao,
    private val breedingDao: BreedingRecordDao,
    private val pregnancyDao: PregnancyRecordDao,
    private val animalRepository: AnimalRepository
) : BreedingRepository {

    private fun animalsMap(farmId: String): Flow<Map<String, Animal>> =
        animalRepository.observeAnimals(farmId, AnimalFilter.ALL).map { list ->
            list.associateBy { it.id }
        }

    // ── HEAT ──────────────────────────────
    override fun observeHeatRecords(farmId: String): Flow<List<HeatRecordDetail>> =
        combine(heatDao.observeAll(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { r ->
                val a = animals[r.animalId]
                HeatRecordDetail(
                    heat = r.toDomain(),
                    animalName = a?.name,
                    animalTag = a?.tagId ?: "?"
                )
            }
        }

    override fun observeHeatForAnimal(animalId: String): Flow<List<HeatRecord>> =
        heatDao.observeForAnimal(animalId).map { list -> list.map { it.toDomain() } }

    override suspend fun addHeatRecord(record: HeatRecord): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { heatDao.insert(HeatRecordEntity.fromDomain(record)) }.map { }
        }

    override suspend fun deleteHeatRecord(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { heatDao.deleteById(id) }.map { }
        }

    // ── BREEDING ──────────────────────────
    override fun observeBreedingRecords(farmId: String): Flow<List<BreedingRecordDetail>> =
        combine(breedingDao.observeAll(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { r ->
                val a = animals[r.animalId]
                BreedingRecordDetail(
                    breeding = r.toDomain(),
                    animalName = a?.name,
                    animalTag = a?.tagId ?: "?"
                )
            }
        }

    override fun observeBreedingsForAnimal(animalId: String): Flow<List<BreedingRecord>> =
        breedingDao.observeForAnimal(animalId).map { list -> list.map { it.toDomain() } }

    override fun observeAwaitingPd(farmId: String): Flow<List<BreedingRecordDetail>> =
        combine(breedingDao.observeAwaitingPd(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { r ->
                val a = animals[r.animalId]
                BreedingRecordDetail(
                    breeding = r.toDomain(),
                    animalName = a?.name,
                    animalTag = a?.tagId ?: "?"
                )
            }
        }

    override fun countConfirmed(farmId: String): Flow<Int> = breedingDao.countConfirmed(farmId)
    override fun countAll(farmId: String): Flow<Int> = breedingDao.countAll(farmId)

    override suspend fun addBreedingRecord(record: BreedingRecord): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { breedingDao.insert(BreedingRecordEntity.fromDomain(record)) }.map { }
        }

    override suspend fun setConceptionStatus(id: String, status: ConceptionStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { breedingDao.setConceptionStatus(id, status.value) }.map { }
        }

    override suspend fun deleteBreedingRecord(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { breedingDao.deleteById(id) }.map { }
        }

    // ── PREGNANCY ─────────────────────────
    override fun observePregnancies(farmId: String): Flow<List<PregnancyDetail>> =
        combine(pregnancyDao.observeAll(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { toDetail(it, animals) }
        }

    override fun observePregnanciesForAnimal(animalId: String): Flow<List<PregnancyRecord>> =
        pregnancyDao.observeForAnimal(animalId).map { list -> list.map { it.toDomain() } }

    override fun observeActivePregnancies(farmId: String): Flow<List<PregnancyDetail>> =
        combine(pregnancyDao.observeActive(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { toDetail(it, animals) }
        }

    override fun observeCompletedPregnancies(farmId: String): Flow<List<PregnancyDetail>> =
        combine(pregnancyDao.observeCompleted(farmId), animalsMap(farmId)) { rows, animals ->
            rows.map { toDetail(it, animals) }
        }

    override fun countActivePregnancies(farmId: String): Flow<Int> =
        pregnancyDao.countActive(farmId)

    override fun countCalvingDueThisMonth(farmId: String, today: LocalDate): Flow<Int> {
        val cutoff = today.plus(DatePeriod(days = 30))
        return pregnancyDao.countDueSoon(farmId, today.toString(), cutoff.toString())
    }

    override suspend fun addPregnancyRecord(record: PregnancyRecord): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { pregnancyDao.insert(PregnancyRecordEntity.fromDomain(record)) }.map { }
        }

    override suspend fun recordCalving(
        pregnancyId: String,
        actualDate: LocalDate,
        difficulty: Int?,
        outcome: CalvingOutcome?,
        notes: String?,
        createCalfAnimal: Animal?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Save calf first if provided so we have its ID to link back
            val calfId = createCalfAnimal?.let { calf ->
                animalRepository.addAnimal(calf)
                calf.id
            }

            val row = requireNotNull(pregnancyDao.getById(pregnancyId)) {
                "Pregnancy $pregnancyId not found"
            }
            val updated = row.copy(
                actualCalvingDate = actualDate.toString(),
                calvingDifficulty = difficulty,
                calvingOutcome = outcome?.value,
                calvingNotes = notes,
                calfAnimalId = calfId
            )
            pregnancyDao.update(updated)
        }.map { }
    }

    override suspend fun getLatestHeatPerAnimal(farmId: String): List<HeatRecord> =
        withContext(Dispatchers.IO) {
            heatDao.getLatestPerAnimal(farmId).map { it.toDomain() }
        }

    override suspend fun getPendingBreedings(farmId: String): List<BreedingRecord> =
        withContext(Dispatchers.IO) {
            breedingDao.getPendingBreedings(farmId).map { it.toDomain() }
        }

    override suspend fun getCalvingsInWindow(
        farmId: String, today: LocalDate, cutoff: LocalDate
    ): List<PregnancyRecord> = withContext(Dispatchers.IO) {
        pregnancyDao.getCalvingsInWindow(farmId, today.toString(), cutoff.toString())
            .map { it.toDomain() }
    }

    private fun toDetail(row: PregnancyRecordEntity, animals: Map<String, Animal>): PregnancyDetail {
        val a = animals[row.animalId]
        return PregnancyDetail(
            pregnancy = row.toDomain(),
            animalName = a?.name,
            animalTag = a?.tagId ?: "?"
        )
    }
}
