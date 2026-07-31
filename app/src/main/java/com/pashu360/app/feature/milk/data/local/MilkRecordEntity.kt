package com.pashu360.app.feature.milk.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.MilkRecord
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "milk_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("record_date"),
        Index(value = ["animal_id", "record_date", "session"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = AnimalEntity::class,
            parentColumns = ["id"],
            childColumns = ["animal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MilkRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "record_date") val recordDate: String,   // ISO date
    val session: String,                                          // "morning" | "evening"
    @ColumnInfo(name = "quantity_liters") val quantityLiters: Double,
    @ColumnInfo(name = "fat_pct") val fatPct: Double? = null,
    @ColumnInfo(name = "snf_pct") val snfPct: Double? = null,
    val clr: Double? = null,
    val ph: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): MilkRecord = MilkRecord(
        id = id,
        animalId = animalId,
        farmId = farmId,
        recordDate = LocalDate.parse(recordDate),
        session = MilkSession.from(session),
        quantityLiters = quantityLiters,
        fatPct = fatPct,
        snfPct = snfPct,
        clr = clr,
        ph = ph,
        notes = notes
    )

    companion object {
        fun fromDomain(m: MilkRecord): MilkRecordEntity = MilkRecordEntity(
            id = m.id,
            animalId = m.animalId,
            farmId = m.farmId,
            recordDate = m.recordDate.toString(),
            session = m.session.value,
            quantityLiters = m.quantityLiters,
            fatPct = m.fatPct,
            snfPct = m.snfPct,
            clr = m.clr,
            ph = m.ph,
            notes = m.notes
        )
    }
}
