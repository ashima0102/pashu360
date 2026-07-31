package com.pashu360.app.feature.notifications.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId
        ORDER BY is_resolved ASC, due_date ASC
    """)
    fun observeAll(farmId: String): Flow<List<AlertEntity>>

    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId AND is_resolved = 0
        ORDER BY due_date ASC
    """)
    fun observeUnresolved(farmId: String): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE farm_id = :farmId AND is_resolved = 0")
    fun countUnresolved(farmId: String): Flow<Int>

    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId AND is_resolved = 0 AND due_date <= :today
        ORDER BY due_date ASC
    """)
    fun observeOverdueOrToday(farmId: String, today: String): Flow<List<AlertEntity>>

    /** For duplicate detection when the scanner regenerates alerts. */
    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId AND source_id = :sourceId
        LIMIT 1
    """)
    fun getBySourceId(farmId: String, sourceId: String): AlertEntity?

    /** For duplicate detection based on (animal + type + date) — for cases with no source id. */
    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId
          AND animal_id = :animalId
          AND alert_type = :alertType
          AND due_date = :dueDate
        LIMIT 1
    """)
    fun getByAnimalTypeAndDate(
        farmId: String,
        animalId: String,
        alertType: String,
        dueDate: String
    ): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alert: AlertEntity): Long

    @Query("UPDATE alerts SET is_resolved = 1, resolved_at = :resolvedAt WHERE id = :id")
    fun resolve(id: String, resolvedAt: String): Int

    @Query("UPDATE alerts SET notification_sent = 1 WHERE id = :id")
    fun markNotified(id: String): Int

    @Query("""
        SELECT * FROM alerts
        WHERE farm_id = :farmId
          AND is_resolved = 0
          AND notification_sent = 0
          AND due_date <= :today
    """)
    fun getPendingNotifications(farmId: String, today: String): List<AlertEntity>

    @Query("DELETE FROM alerts WHERE id = :id")
    fun deleteById(id: String): Int

    /** Cleanup: delete resolved alerts older than 90 days. */
    @Query("DELETE FROM alerts WHERE is_resolved = 1 AND resolved_at < :cutoff")
    fun deleteOldResolved(cutoff: String): Int
}
