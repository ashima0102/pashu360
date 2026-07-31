package com.pashu360.app.feature.finance.data.repository

import com.pashu360.app.core.domain.model.CategoryBreakdown
import com.pashu360.app.core.domain.model.FinanceCategory
import com.pashu360.app.core.domain.model.FinancialRecord
import com.pashu360.app.core.domain.model.MonthlyPnL
import com.pashu360.app.core.domain.model.TransactionType
import com.pashu360.app.feature.finance.data.local.FinancialRecordDao
import com.pashu360.app.feature.finance.data.local.FinancialRecordEntity
import com.pashu360.app.feature.finance.domain.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val dao: FinancialRecordDao
) : FinanceRepository {

    override fun observeAll(farmId: String): Flow<List<FinancialRecord>> =
        dao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override fun observeByType(farmId: String, type: TransactionType): Flow<List<FinancialRecord>> =
        dao.observeByType(farmId, type.value).map { list -> list.map { it.toDomain() } }

    override fun observeInRange(
        farmId: String, startDate: LocalDate, endDate: LocalDate
    ): Flow<List<FinancialRecord>> =
        dao.observeInRange(farmId, startDate.toString(), endDate.toString())
            .map { list -> list.map { it.toDomain() } }

    override fun observeMonthlyPnL(
        farmId: String, startDate: LocalDate, endDate: LocalDate
    ): Flow<List<MonthlyPnL>> =
        dao.observeMonthlyBuckets(farmId, startDate.toString(), endDate.toString())
            .map { rows ->
                rows.groupBy { it.yearStr to it.monthStr }
                    .map { (key, entries) ->
                        val (yearStr, monthStr) = key
                        val income = entries.firstOrNull { it.type == "income" }?.total ?: 0.0
                        val expense = entries.firstOrNull { it.type == "expense" }?.total ?: 0.0
                        MonthlyPnL(yearStr.toInt(), monthStr.toInt(), income, expense)
                    }
                    .sortedWith(compareBy({ it.year }, { it.month }))
            }

    override fun observeCategoryBreakdown(
        farmId: String, startDate: LocalDate, endDate: LocalDate
    ): Flow<List<CategoryBreakdown>> =
        dao.observeCategoryBreakdown(farmId, startDate.toString(), endDate.toString())
            .map { rows ->
                rows.map { r ->
                    CategoryBreakdown(
                        category = FinanceCategory.from(r.category),
                        total = r.total,
                        transactionCount = r.txCount
                    )
                }.sortedByDescending { it.total }
            }

    override suspend fun addRecord(record: FinancialRecord): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { dao.insert(FinancialRecordEntity.fromDomain(record)) }.map { }
        }

    override suspend fun deleteRecord(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { dao.deleteById(id) }.map { }
        }
}
