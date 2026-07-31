package com.pashu360.app.feature.health.domain.repository

import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VetContact
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HealthRepository {
    // Health events
    fun observeHealthRecords(farmId: String): Flow<List<HealthRecord>>
    fun observeHealthRecordsForAnimal(animalId: String): Flow<List<HealthRecord>>
    fun countActiveHealthIssues(farmId: String): Flow<Int>
    suspend fun saveHealthRecord(record: HealthRecord): Result<Unit>
    suspend fun markHealthResolved(id: String): Result<Unit>
    suspend fun deleteHealthRecord(id: String): Result<Unit>

    // Vaccinations
    fun observeVaccinations(farmId: String): Flow<List<Vaccination>>
    fun observeVaccinationsForAnimal(animalId: String): Flow<List<Vaccination>>
    fun countOverdueVaccines(farmId: String, today: LocalDate): Flow<Int>
    fun countDueSoonVaccines(farmId: String, today: LocalDate, daysWindow: Int = 7): Flow<Int>
    suspend fun saveVaccination(vaccination: Vaccination): Result<Unit>
    suspend fun deleteVaccination(id: String): Result<Unit>

    // Vet contacts
    fun observeVetContacts(farmId: String): Flow<List<VetContact>>
    suspend fun saveVetContact(contact: VetContact): Result<Unit>
    suspend fun deleteVetContact(id: String): Result<Unit>
}
