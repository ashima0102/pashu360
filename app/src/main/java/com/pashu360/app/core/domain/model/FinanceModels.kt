package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import java.util.UUID

data class FinancialRecord(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val animalId: String? = null,     // optional — link to specific animal
    val recordDate: LocalDate,
    val type: TransactionType,
    val category: FinanceCategory,
    val amount: Double,
    val quantity: Double? = null,      // e.g. litres of milk sold
    val unitPrice: Double? = null,     // e.g. ₹40 per litre
    val counterparty: String? = null,  // buyer / seller / vendor
    val notes: String? = null
) {
    val isIncome: Boolean get() = type == TransactionType.INCOME
    val signedAmount: Double get() = if (isIncome) amount else -amount
}

enum class TransactionType(val value: String, val displayName: String) {
    INCOME("income", "Income"),
    EXPENSE("expense", "Expense");

    companion object {
        fun from(value: String): TransactionType =
            entries.find { it.value == value } ?: EXPENSE
    }
}

enum class FinanceCategory(
    val value: String,
    val displayName: String,
    val emoji: String,
    val type: TransactionType
) {
    // Income
    MILK_SALE("milk_sale", "Milk Sale", "🥛", TransactionType.INCOME),
    ANIMAL_SALE("animal_sale", "Animal Sale", "🐄", TransactionType.INCOME),
    OTHER_INCOME("other_income", "Other", "💰", TransactionType.INCOME),

    // Expense
    FEED("feed", "Feed", "🌿", TransactionType.EXPENSE),
    MEDICINE("medicine", "Medicine", "💊", TransactionType.EXPENSE),
    LABOUR("labour", "Labour", "👷", TransactionType.EXPENSE),
    VET_FEES("vet_fees", "Vet Fees", "👨‍⚕️", TransactionType.EXPENSE),
    EQUIPMENT("equipment", "Equipment", "🔧", TransactionType.EXPENSE),
    VACCINATION("vaccination", "Vaccination", "💉", TransactionType.EXPENSE),
    ANIMAL_PURCHASE("animal_purchase", "Buy Animal", "🐮", TransactionType.EXPENSE),
    OTHER_EXPENSE("other_expense", "Other", "📦", TransactionType.EXPENSE);

    companion object {
        fun from(value: String): FinanceCategory =
            entries.find { it.value == value } ?: OTHER_EXPENSE
        fun incomeCategories() = entries.filter { it.type == TransactionType.INCOME }
        fun expenseCategories() = entries.filter { it.type == TransactionType.EXPENSE }
    }
}

/** Monthly income vs expense bucket for chart data. */
data class MonthlyPnL(
    val year: Int,
    val month: Int,            // 1..12
    val income: Double,
    val expense: Double
) {
    val net: Double get() = income - expense
    val label: String get() = MONTH_ABBREVIATIONS[month - 1]

    companion object {
        val MONTH_ABBREVIATIONS = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }
}

/** Category breakdown for pie / list chart. */
data class CategoryBreakdown(
    val category: FinanceCategory,
    val total: Double,
    val transactionCount: Int
)
