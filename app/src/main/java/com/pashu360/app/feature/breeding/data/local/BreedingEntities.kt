package com.pashu360.app.feature.breeding.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.BreedingRecord
import com.pashu360.app.core.domain.model.BreedingType
import com.pashu360.app.core.domain.model.CalvingOutcome
import com.pashu360.app.core.domain.model.ConceptionStatus
import com.pashu360.app.core.domain.model.HeatIntensity
import com.pashu360.app.core.domain.model.HeatRecord
import com.pashu360.app.core.domain.model.PdMethod
import com.pashu360.app.core.domain.model.PregnancyRecord
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────
// HEAT
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "heat_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("detection_date")
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
data class HeatRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "detection_date") val detectionDate: String,
    val symptoms: String = "",                 // pipe-delimited
    val intensity: String = "medium",
    @ColumnInfo(name = "detected_by") val detectedBy: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "breeding_record_id") val breedingRecordId: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): HeatRecord = HeatRecord(
        id = id, animalId = animalId, farmId = farmId,
        detectionDate = LocalDate.parse(detectionDate),
        symptoms = symptoms.split("|").filter { it.isNotBlank() },
        intensity = HeatIntensity.from(intensity),
        detectedBy = detectedBy, notes = notes,
        breedingRecordId = breedingRecordId
    )

    companion object {
        fun fromDomain(h: HeatRecord): HeatRecordEntity = HeatRecordEntity(
            id = h.id, animalId = h.animalId, farmId = h.farmId,
            detectionDate = h.detectionDate.toString(),
            symptoms = h.symptoms.joinToString("|"),
            intensity = h.intensity.value,
            detectedBy = h.detectedBy, notes = h.notes,
            breedingRecordId = h.breedingRecordId
        )
    }
}

// ─────────────────────────────────────────────────────────
// BREEDING
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "breeding_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("breeding_date"),
        Index("conception_status")
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
data class BreedingRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "heat_record_id") val heatRecordId: String? = null,
    @ColumnInfo(name = "breeding_type") val breedingType: String,
    @ColumnInfo(name = "breeding_date") val breedingDate: String,
    @ColumnInfo(name = "bull_name") val bullName: String? = null,
    @ColumnInfo(name = "semen_batch") val semenBatch: String? = null,
    @ColumnInfo(name = "ai_technician") val aiTechnician: String? = null,
    @ColumnInfo(name = "bull_animal_id") val bullAnimalId: String? = null,
    @ColumnInfo(name = "conception_status") val conceptionStatus: String = "pending",
    val cost: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): BreedingRecord = BreedingRecord(
        id = id, animalId = animalId, farmId = farmId,
        heatRecordId = heatRecordId,
        breedingType = BreedingType.from(breedingType),
        breedingDate = LocalDate.parse(breedingDate),
        bullName = bullName, semenBatch = semenBatch,
        aiTechnician = aiTechnician, bullAnimalId = bullAnimalId,
        conceptionStatus = ConceptionStatus.from(conceptionStatus),
        cost = cost, notes = notes
    )

    companion object {
        fun fromDomain(b: BreedingRecord): BreedingRecordEntity = BreedingRecordEntity(
            id = b.id, animalId = b.animalId, farmId = b.farmId,
            heatRecordId = b.heatRecordId,
            breedingType = b.breedingType.value,
            breedingDate = b.breedingDate.toString(),
            bullName = b.bullName, semenBatch = b.semenBatch,
            aiTechnician = b.aiTechnician, bullAnimalId = b.bullAnimalId,
            conceptionStatus = b.conceptionStatus.value,
            cost = b.cost, notes = b.notes
        )
    }
}

// ─────────────────────────────────────────────────────────
// PREGNANCY
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "pregnancy_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("expected_calving_date"),
        Index("actual_calving_date")
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
data class PregnancyRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "breeding_record_id") val breedingRecordId: String? = null,
    @ColumnInfo(name = "confirmation_date") val confirmationDate: String,
    @ColumnInfo(name = "pd_method") val pdMethod: String = "rectal_palpation",
    @ColumnInfo(name = "expected_calving_date") val expectedCalvingDate: String,
    @ColumnInfo(name = "dry_period_start") val dryPeriodStart: String,
    @ColumnInfo(name = "actual_calving_date") val actualCalvingDate: String? = null,
    @ColumnInfo(name = "calving_difficulty") val calvingDifficulty: Int? = null,
    @ColumnInfo(name = "calving_outcome") val calvingOutcome: String? = null,
    @ColumnInfo(name = "calving_notes") val calvingNotes: String? = null,
    @ColumnInfo(name = "calf_animal_id") val calfAnimalId: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): PregnancyRecord = PregnancyRecord(
        id = id, animalId = animalId, farmId = farmId,
        breedingRecordId = breedingRecordId,
        confirmationDate = LocalDate.parse(confirmationDate),
        pdMethod = PdMethod.from(pdMethod),
        expectedCalvingDate = LocalDate.parse(expectedCalvingDate),
        dryPeriodStart = LocalDate.parse(dryPeriodStart),
        actualCalvingDate = actualCalvingDate?.let { LocalDate.parse(it) },
        calvingDifficulty = calvingDifficulty,
        calvingOutcome = calvingOutcome?.let { CalvingOutcome.from(it) },
        calvingNotes = calvingNotes,
        calfAnimalId = calfAnimalId,
        notes = notes
    )

    companion object {
        fun fromDomain(p: PregnancyRecord): PregnancyRecordEntity = PregnancyRecordEntity(
            id = p.id, animalId = p.animalId, farmId = p.farmId,
            breedingRecordId = p.breedingRecordId,
            confirmationDate = p.confirmationDate.toString(),
            pdMethod = p.pdMethod.value,
            expectedCalvingDate = p.expectedCalvingDate.toString(),
            dryPeriodStart = p.dryPeriodStart.toString(),
            actualCalvingDate = p.actualCalvingDate?.toString(),
            calvingDifficulty = p.calvingDifficulty,
            calvingOutcome = p.calvingOutcome?.value,
            calvingNotes = p.calvingNotes,
            calfAnimalId = p.calfAnimalId,
            notes = p.notes
        )
    }
}
