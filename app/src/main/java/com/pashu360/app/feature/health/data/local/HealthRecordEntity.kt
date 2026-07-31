package com.pashu360.app.feature.health.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.HealthEventType
import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Severity
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "health_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("event_date"),
        Index("is_resolved")
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
data class HealthRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "event_date") val eventDate: String,          // ISO
    @ColumnInfo(name = "event_type") val eventType: String,
    val symptoms: String = "",                                        // pipe-delimited
    val diagnosis: String? = null,
    val severity: String = "mild",
    @ColumnInfo(name = "treatment_notes") val treatmentNotes: String? = null,
    @ColumnInfo(name = "medicine_name") val medicineName: String? = null,
    @ColumnInfo(name = "medicine_dose") val medicineDose: String? = null,
    @ColumnInfo(name = "vet_name") val vetName: String? = null,
    val cost: Double? = null,
    @ColumnInfo(name = "is_resolved") val isResolved: Boolean = false,
    @ColumnInfo(name = "resolved_date") val resolvedDate: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): HealthRecord = HealthRecord(
        id = id,
        animalId = animalId,
        farmId = farmId,
        eventDate = LocalDate.parse(eventDate),
        eventType = HealthEventType.from(eventType),
        symptoms = symptoms.split("|").filter { it.isNotBlank() },
        diagnosis = diagnosis,
        severity = Severity.from(severity),
        treatmentNotes = treatmentNotes,
        medicineName = medicineName,
        medicineDose = medicineDose,
        vetName = vetName,
        cost = cost,
        isResolved = isResolved,
        resolvedDate = resolvedDate?.let { LocalDate.parse(it) },
        notes = notes
    )

    companion object {
        fun fromDomain(r: HealthRecord): HealthRecordEntity = HealthRecordEntity(
            id = r.id,
            animalId = r.animalId,
            farmId = r.farmId,
            eventDate = r.eventDate.toString(),
            eventType = r.eventType.value,
            symptoms = r.symptoms.joinToString("|"),
            diagnosis = r.diagnosis,
            severity = r.severity.value,
            treatmentNotes = r.treatmentNotes,
            medicineName = r.medicineName,
            medicineDose = r.medicineDose,
            vetName = r.vetName,
            cost = r.cost,
            isResolved = r.isResolved,
            resolvedDate = r.resolvedDate?.toString(),
            notes = r.notes
        )
    }
}
