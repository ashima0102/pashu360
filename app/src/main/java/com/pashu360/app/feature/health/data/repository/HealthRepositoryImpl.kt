package com.pashu360.app.feature.health.data.repository

import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VetContact
import com.pashu360.app.feature.health.data.local.HealthRecordDao
import com.pashu360.app.feature.health.data.local.HealthRecordEntity
import com.pashu360.app.feature.health.data.local.VaccinationDao
import com.pashu360.app.feature.health.data.local.VaccinationEntity
import com.pashu360.app.feature.health.data.local.VetContactDao
import com.pashu360.app.feature.health.data.local.VetContactEntity
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthDao: HealthRecordDao,
    private val vaccinationDao: VaccinationDao,
    private val vetContactDao: VetContactDao
) : HealthRepository {

    // ── HEALTH RECORDS ──────────────────────────────
    override fun observeHealthRecords(farmId: String): Flow<List<HealthRecord>> =
        healthDao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override fun observeHealthRecordsForAnimal(animalId: String): Flow<List<HealthRecord>> =
        healthDao.observeForAnimal(animalId).map { list -> list.map { it.toDomain() } }

    override fun countActiveHealthIssues(farmId: String): Flow<Int> =
        healthDao.countActive(farmId)

    override suspend fun saveHealthRecord(record: HealthRecord): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { healthDao.insert(HealthRecordEntity.fromDomain(record)) }.map { }
        }

    override suspend fun markHealthResolved(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            runCatching { healthDao.markResolved(id, today) }.map { }
        }

    override suspend fun deleteHealthRecord(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { healthDao.deleteById(id) }.map { }
        }

    // ── VACCINATIONS ─────────────────────────────────
    override fun observeVaccinations(farmId: String): Flow<List<Vaccination>> =
        vaccinationDao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override fun observeVaccinationsForAnimal(animalId: String): Flow<List<Vaccination>> =
        vaccinationDao.observeForAnimal(animalId).map { list -> list.map { it.toDomain() } }

    override fun countOverdueVaccines(farmId: String, today: LocalDate): Flow<Int> =
        vaccinationDao.countOverdue(farmId, today.toString())

    override fun countDueSoonVaccines(
        farmId: String, today: LocalDate, daysWindow: Int
    ): Flow<Int> {
        val cutoff = today.plus(DatePeriod(days = daysWindow))
        return vaccinationDao.countDueSoon(farmId, today.toString(), cutoff.toString())
    }

    override suspend fun saveVaccination(vaccination: Vaccination): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { vaccinationDao.insert(VaccinationEntity.fromDomain(vaccination)) }.map { }
        }

    override suspend fun deleteVaccination(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { vaccinationDao.deleteById(id) }.map { }
        }

    // ── VET CONTACTS ─────────────────────────────────
    override fun observeVetContacts(farmId: String): Flow<List<VetContact>> =
        vetContactDao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveVetContact(contact: VetContact): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { vetContactDao.insert(VetContactEntity.fromDomain(contact)) }.map { }
        }

    override suspend fun deleteVetContact(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { vetContactDao.deleteById(id) }.map { }
        }
}
