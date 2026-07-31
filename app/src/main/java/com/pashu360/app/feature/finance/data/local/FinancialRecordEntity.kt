package com.pashu360.app.feature.finance.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.FinanceCategory
import com.pashu360.app.core.domain.model.FinancialRecord
import com.pashu360.app.core.domain.model.TransactionType
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "financial_records",
    indices = [
        Index("farm_id"),
        Index("record_date"),
        Index("type"),
        Index("category")
    ]
)
data class FinancialRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "animal_id") val animalId: String? = null,
    @ColumnInfo(name = "record_date") val recordDate: String,   // ISO date
    val type: String,                                             // "income" | "expense"
    val category: String,
    val amount: Double,
    val quantity: Double? = null,
    @ColumnInfo(name = "unit_price") val unitPrice: Double? = null,
    val counterparty: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): FinancialRecord = FinancialRecord(
        id = id,
        farmId = farmId,
        animalId = animalId,
        recordDate = LocalDate.parse(recordDate),
        type = TransactionType.from(type),
        category = FinanceCategory.from(category),
        amount = amount,
        quantity = quantity,
        unitPrice = unitPrice,
        counterparty = counterparty,
        notes = notes
    )

    companion object {
        fun fromDomain(r: FinancialRecord): FinancialRecordEntity = FinancialRecordEntity(
            id = r.id,
            farmId = r.farmId,
            animalId = r.animalId,
            recordDate = r.recordDate.toString(),
            type = r.type.value,
            category = r.category.value,
            amount = r.amount,
            quantity = r.quantity,
            unitPrice = r.unitPrice,
            counterparty = r.counterparty,
            notes = r.notes
        )
    }
}
