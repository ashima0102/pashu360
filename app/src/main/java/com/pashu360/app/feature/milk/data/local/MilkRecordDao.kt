package com.pashu360.app.feature.milk.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Non-suspend DAO methods to work around a KSP2 codegen bug — see
 * MilkRepositoryImpl where every call is wrapped in withContext(Dispatchers.IO).
 */
@Dao
interface MilkRecordDao {

    @Query("""
        SELECT * FROM milk_records
        WHERE farm_id = :farmId AND record_date = :date
        ORDER BY session, animal_id
    """)
    fun observeRecordsForDate(farmId: String, date: String): Flow<List<MilkRecordEntity>>

    @Query("""
        SELECT * FROM milk_records
        WHERE animal_id = :animalId
        ORDER BY record_date DESC, session DESC
        LIMIT :limit
    """)
    fun observeRecordsForAnimal(animalId: String, limit: Int = 60): Flow<List<MilkRecordEntity>>

    @Query("""
        SELECT SUM(quantity_liters) FROM milk_records
        WHERE farm_id = :farmId AND record_date = :date
    """)
    fun observeDailyTotal(farmId: String, date: String): Flow<Double?>

    @Query("""
        SELECT record_date, session, SUM(quantity_liters) as total
        FROM milk_records
        WHERE farm_id = :farmId
          AND record_date >= :startDate AND record_date <= :endDate
        GROUP BY record_date, session
        ORDER BY record_date ASC
    """)
    fun observeDailyTotalsByDate(
        farmId: String,
        startDate: String,
        endDate: String
    ): Flow<List<DailySessionRow>>

    @Query("""
        SELECT * FROM milk_records
        WHERE farm_id = :farmId AND record_date = :date AND session = :session
    """)
    fun getSessionRecords(farmId: String, date: String, session: String): List<MilkRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: MilkRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(records: List<MilkRecordEntity>): List<Long>

    @Query("DELETE FROM milk_records WHERE id = :id")
    fun deleteById(id: String): Int
}

data class DailySessionRow(
    @androidx.room.ColumnInfo(name = "record_date") val recordDate: String,
    val session: String,
    val total: Double
)
