package com.pashu360.app.feature.feeding.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedTypeDao {
    @Query("SELECT * FROM feed_types WHERE farm_id = :farmId AND is_active = 1 ORDER BY name")
    fun observeAll(farmId: String): Flow<List<FeedTypeEntity>>

    @Query("SELECT * FROM feed_types WHERE id = :id LIMIT 1")
    fun getById(id: String): FeedTypeEntity?

    @Query("SELECT COUNT(*) FROM feed_types WHERE farm_id = :farmId")
    fun count(farmId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(type: FeedTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(types: List<FeedTypeEntity>): List<Long>

    @Update
    fun update(type: FeedTypeEntity): Int

    @Query("UPDATE feed_types SET is_active = 0 WHERE id = :id")
    fun softDelete(id: String): Int
}

@Dao
interface FeedRecordDao {
    @Query("""
        SELECT * FROM feed_records
        WHERE farm_id = :farmId AND record_date = :date
        ORDER BY time_of_day, id
    """)
    fun observeForDate(farmId: String, date: String): Flow<List<FeedRecordEntity>>

    @Query("""
        SELECT * FROM feed_records
        WHERE animal_id = :animalId
        ORDER BY record_date DESC, id
        LIMIT :limit
    """)
    fun observeForAnimal(animalId: String, limit: Int = 60): Flow<List<FeedRecordEntity>>

    @Query("SELECT SUM(quantity) FROM feed_records WHERE farm_id = :farmId AND record_date = :date")
    fun observeDailyTotal(farmId: String, date: String): Flow<Double?>

    @Query("""
        SELECT feed_type_id, SUM(quantity) as total
        FROM feed_records
        WHERE farm_id = :farmId
          AND record_date >= :startDate AND record_date <= :endDate
        GROUP BY feed_type_id
    """)
    fun observeConsumptionByType(
        farmId: String, startDate: String, endDate: String
    ): Flow<List<ConsumptionRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: FeedRecordEntity): Long

    @Query("DELETE FROM feed_records WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface FeedInventoryDao {
    @Query("SELECT * FROM feed_inventory WHERE farm_id = :farmId")
    fun observeAll(farmId: String): Flow<List<FeedInventoryEntity>>

    @Query("SELECT * FROM feed_inventory WHERE farm_id = :farmId AND feed_type_id = :feedTypeId LIMIT 1")
    fun getForFeedType(farmId: String, feedTypeId: String): FeedInventoryEntity?

    @Query("""
        SELECT * FROM feed_inventory
        WHERE farm_id = :farmId AND quantity < low_stock_threshold
    """)
    fun getLowStock(farmId: String): List<FeedInventoryEntity>

    @Query("SELECT COUNT(*) FROM feed_inventory WHERE farm_id = :farmId AND quantity < low_stock_threshold")
    fun countLowStock(farmId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(inventory: FeedInventoryEntity): Long

    @Query("""
        UPDATE feed_inventory
        SET quantity = quantity + :delta,
            last_updated = :date
        WHERE farm_id = :farmId AND feed_type_id = :feedTypeId
    """)
    fun adjustQuantity(farmId: String, feedTypeId: String, delta: Double, date: String): Int

    @Query("""
        UPDATE feed_inventory
        SET low_stock_threshold = :threshold
        WHERE farm_id = :farmId AND feed_type_id = :feedTypeId
    """)
    fun updateThreshold(farmId: String, feedTypeId: String, threshold: Double): Int
}

data class ConsumptionRow(
    @androidx.room.ColumnInfo(name = "feed_type_id") val feedTypeId: String,
    val total: Double
)
