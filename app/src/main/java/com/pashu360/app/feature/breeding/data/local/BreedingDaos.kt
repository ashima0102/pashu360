package com.pashu360.app.feature.breeding.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HeatRecordDao {
    @Query("SELECT * FROM heat_records WHERE farm_id = :farmId ORDER BY detection_date DESC")
    fun observeAll(farmId: String): Flow<List<HeatRecordEntity>>

    @Query("""
        SELECT * FROM heat_records
        WHERE animal_id = :animalId
        ORDER BY detection_date DESC
        LIMIT :limit
    """)
    fun observeForAnimal(animalId: String, limit: Int = 20): Flow<List<HeatRecordEntity>>

    /** For heat-alert scanner: latest heat per animal in one query. */
    @Query("""
        SELECT h.* FROM heat_records h
        WHERE h.farm_id = :farmId
        AND h.detection_date = (
            SELECT MAX(detection_date) FROM heat_records
            WHERE animal_id = h.animal_id
        )
    """)
    fun getLatestPerAnimal(farmId: String): List<HeatRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: HeatRecordEntity): Long

    @Query("DELETE FROM heat_records WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface BreedingRecordDao {
    @Query("SELECT * FROM breeding_records WHERE farm_id = :farmId ORDER BY breeding_date DESC")
    fun observeAll(farmId: String): Flow<List<BreedingRecordEntity>>

    @Query("""
        SELECT * FROM breeding_records
        WHERE animal_id = :animalId
        ORDER BY breeding_date DESC
    """)
    fun observeForAnimal(animalId: String): Flow<List<BreedingRecordEntity>>

    @Query("""
        SELECT * FROM breeding_records
        WHERE farm_id = :farmId AND conception_status = 'pending'
        ORDER BY breeding_date DESC
    """)
    fun observeAwaitingPd(farmId: String): Flow<List<BreedingRecordEntity>>

    /** For scanner: pending breedings so PD reminders can fire. */
    @Query("""
        SELECT * FROM breeding_records
        WHERE farm_id = :farmId AND conception_status = 'pending'
    """)
    fun getPendingBreedings(farmId: String): List<BreedingRecordEntity>

    @Query("SELECT COUNT(*) FROM breeding_records WHERE farm_id = :farmId AND conception_status = 'confirmed'")
    fun countConfirmed(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM breeding_records WHERE farm_id = :farmId")
    fun countAll(farmId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: BreedingRecordEntity): Long

    @Update
    fun update(record: BreedingRecordEntity): Int

    @Query("""
        UPDATE breeding_records SET conception_status = :status
        WHERE id = :id
    """)
    fun setConceptionStatus(id: String, status: String): Int

    @Query("DELETE FROM breeding_records WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface PregnancyRecordDao {
    @Query("SELECT * FROM pregnancy_records WHERE farm_id = :farmId ORDER BY expected_calving_date")
    fun observeAll(farmId: String): Flow<List<PregnancyRecordEntity>>

    @Query("""
        SELECT * FROM pregnancy_records
        WHERE farm_id = :farmId AND actual_calving_date IS NULL
        ORDER BY expected_calving_date ASC
    """)
    fun observeActive(farmId: String): Flow<List<PregnancyRecordEntity>>

    @Query("""
        SELECT * FROM pregnancy_records
        WHERE farm_id = :farmId AND actual_calving_date IS NOT NULL
        ORDER BY actual_calving_date DESC
    """)
    fun observeCompleted(farmId: String): Flow<List<PregnancyRecordEntity>>

    /** For scanner: pregnancies with expected calving within the alert window. */
    @Query("""
        SELECT * FROM pregnancy_records
        WHERE farm_id = :farmId
        AND actual_calving_date IS NULL
        AND expected_calving_date >= :today
        AND expected_calving_date <= :cutoff
    """)
    fun getCalvingsInWindow(farmId: String, today: String, cutoff: String): List<PregnancyRecordEntity>

    @Query("SELECT * FROM pregnancy_records WHERE animal_id = :animalId AND actual_calving_date IS NULL LIMIT 1")
    fun getActiveForAnimal(animalId: String): PregnancyRecordEntity?

    @Query("SELECT * FROM pregnancy_records WHERE id = :id LIMIT 1")
    fun getById(id: String): PregnancyRecordEntity?

    @Query("SELECT COUNT(*) FROM pregnancy_records WHERE farm_id = :farmId AND actual_calving_date IS NULL")
    fun countActive(farmId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM pregnancy_records
        WHERE farm_id = :farmId AND actual_calving_date IS NULL
        AND expected_calving_date >= :today AND expected_calving_date <= :cutoff
    """)
    fun countDueSoon(farmId: String, today: String, cutoff: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: PregnancyRecordEntity): Long

    @Update
    fun update(record: PregnancyRecordEntity): Int

    @Query("DELETE FROM pregnancy_records WHERE id = :id")
    fun deleteById(id: String): Int
}
