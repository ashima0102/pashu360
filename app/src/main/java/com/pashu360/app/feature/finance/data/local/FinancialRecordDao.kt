package com.pashu360.app.feature.finance.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialRecordDao {

    @Query("""
        SELECT * FROM financial_records
        WHERE farm_id = :farmId
        ORDER BY record_date DESC, id
    """)
    fun observeAll(farmId: String): Flow<List<FinancialRecordEntity>>

    @Query("""
        SELECT * FROM financial_records
        WHERE farm_id = :farmId AND type = :type
        ORDER BY record_date DESC
    """)
    fun observeByType(farmId: String, type: String): Flow<List<FinancialRecordEntity>>

    @Query("""
        SELECT * FROM financial_records
        WHERE farm_id = :farmId
        AND record_date >= :startDate AND record_date <= :endDate
        ORDER BY record_date DESC
    """)
    fun observeInRange(
        farmId: String,
        startDate: String,
        endDate: String
    ): Flow<List<FinancialRecordEntity>>

    // Monthly aggregation for chart
    @Query("""
        SELECT
            substr(record_date, 1, 4) AS year_str,
            substr(record_date, 6, 2) AS month_str,
            type,
            SUM(amount) AS total
        FROM financial_records
        WHERE farm_id = :farmId
          AND record_date >= :startDate AND record_date <= :endDate
        GROUP BY year_str, month_str, type
        ORDER BY year_str, month_str
    """)
    fun observeMonthlyBuckets(
        farmId: String,
        startDate: String,
        endDate: String
    ): Flow<List<MonthlyBucket>>

    // Category totals for breakdown
    @Query("""
        SELECT category, type, SUM(amount) AS total, COUNT(*) AS tx_count
        FROM financial_records
        WHERE farm_id = :farmId
          AND record_date >= :startDate AND record_date <= :endDate
        GROUP BY category, type
    """)
    fun observeCategoryBreakdown(
        farmId: String,
        startDate: String,
        endDate: String
    ): Flow<List<CategoryBucket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: FinancialRecordEntity): Long

    @Query("DELETE FROM financial_records WHERE id = :id")
    fun deleteById(id: String): Int
}

data class MonthlyBucket(
    @ColumnInfo(name = "year_str") val yearStr: String,
    @ColumnInfo(name = "month_str") val monthStr: String,
    val type: String,
    val total: Double
)

data class CategoryBucket(
    val category: String,
    val type: String,
    val total: Double,
    @ColumnInfo(name = "tx_count") val txCount: Int
)
