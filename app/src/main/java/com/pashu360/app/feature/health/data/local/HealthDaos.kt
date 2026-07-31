package com.pashu360.app.feature.health.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Query("""
        SELECT * FROM health_records
        WHERE farm_id = :farmId
        ORDER BY event_date DESC, id
    """)
    fun observeAll(farmId: String): Flow<List<HealthRecordEntity>>

    @Query("""
        SELECT * FROM health_records
        WHERE animal_id = :animalId
        ORDER BY event_date DESC, id
    """)
    fun observeForAnimal(animalId: String): Flow<List<HealthRecordEntity>>

    @Query("""
        SELECT COUNT(*) FROM health_records
        WHERE farm_id = :farmId AND is_resolved = 0
    """)
    fun countActive(farmId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: HealthRecordEntity): Long

    @Update
    fun update(record: HealthRecordEntity): Int

    @Query("UPDATE health_records SET is_resolved = 1, resolved_date = :date WHERE id = :id")
    fun markResolved(id: String, date: String): Int

    @Query("DELETE FROM health_records WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface VaccinationDao {

    @Query("""
        SELECT * FROM vaccinations
        WHERE farm_id = :farmId
        ORDER BY next_due_date IS NULL, next_due_date ASC, administered_date DESC
    """)
    fun observeAll(farmId: String): Flow<List<VaccinationEntity>>

    @Query("""
        SELECT * FROM vaccinations
        WHERE animal_id = :animalId
        ORDER BY administered_date DESC
    """)
    fun observeForAnimal(animalId: String): Flow<List<VaccinationEntity>>

    @Query("""
        SELECT COUNT(*) FROM vaccinations
        WHERE farm_id = :farmId AND next_due_date IS NOT NULL AND next_due_date < :today
    """)
    fun countOverdue(farmId: String, today: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM vaccinations
        WHERE farm_id = :farmId
        AND next_due_date IS NOT NULL
        AND next_due_date >= :today AND next_due_date <= :cutoff
    """)
    fun countDueSoon(farmId: String, today: String, cutoff: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: VaccinationEntity): Long

    @Query("DELETE FROM vaccinations WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface VetContactDao {

    @Query("SELECT * FROM vet_contacts WHERE farm_id = :farmId ORDER BY name")
    fun observeAll(farmId: String): Flow<List<VetContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: VetContactEntity): Long

    @Query("DELETE FROM vet_contacts WHERE id = :id")
    fun deleteById(id: String): Int
}
