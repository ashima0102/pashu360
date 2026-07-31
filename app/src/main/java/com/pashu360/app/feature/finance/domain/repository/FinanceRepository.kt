package com.pashu360.app.feature.finance.domain.repository

import com.pashu360.app.core.domain.model.CategoryBreakdown
import com.pashu360.app.core.domain.model.FinancialRecord
import com.pashu360.app.core.domain.model.MonthlyPnL
import com.pashu360.app.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface FinanceRepository {
    fun observeAll(farmId: String): Flow<List<FinancialRecord>>
    fun observeByType(farmId: String, type: TransactionType): Flow<List<FinancialRecord>>
    fun observeInRange(
        farmId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<FinancialRecord>>

    fun observeMonthlyPnL(
        farmId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MonthlyPnL>>

    fun observeCategoryBreakdown(
        farmId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CategoryBreakdown>>

    suspend fun addRecord(record: FinancialRecord): Result<Unit>
    suspend fun deleteRecord(id: String): Result<Unit>
}
