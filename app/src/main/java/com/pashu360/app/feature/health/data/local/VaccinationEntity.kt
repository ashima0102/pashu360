package com.pashu360.app.feature.health.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "vaccinations",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("next_due_date")
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
data class VaccinationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "vaccine_name") val vaccineName: String,
    @ColumnInfo(name = "disease_target") val diseaseTarget: String? = null,
    @ColumnInfo(name = "administered_date") val administeredDate: String,
    @ColumnInfo(name = "next_due_date") val nextDueDate: String? = null,
    @ColumnInfo(name = "batch_number") val batchNumber: String? = null,
    @ColumnInfo(name = "administered_by") val administeredBy: String? = null,
    val cost: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): Vaccination = Vaccination(
        id = id,
        animalId = animalId,
        farmId = farmId,
        vaccineName = vaccineName,
        diseaseTarget = diseaseTarget,
        administeredDate = LocalDate.parse(administeredDate),
        nextDueDate = nextDueDate?.let { LocalDate.parse(it) },
        batchNumber = batchNumber,
        administeredBy = administeredBy,
        cost = cost,
        notes = notes
    )

    companion object {
        fun fromDomain(v: Vaccination): VaccinationEntity = VaccinationEntity(
            id = v.id,
            animalId = v.animalId,
            farmId = v.farmId,
            vaccineName = v.vaccineName,
            diseaseTarget = v.diseaseTarget,
            administeredDate = v.administeredDate.toString(),
            nextDueDate = v.nextDueDate?.toString(),
            batchNumber = v.batchNumber,
            administeredBy = v.administeredBy,
            cost = v.cost,
            notes = v.notes
        )
    }
}
