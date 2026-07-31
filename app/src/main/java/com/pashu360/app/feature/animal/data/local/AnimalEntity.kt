package com.pashu360.app.feature.animal.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalStatus
import com.pashu360.app.core.domain.model.Gender
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

@Entity(
    tableName = "animals",
    indices = [
        Index("farm_id"),
        Index("barn_id"),
        Index(value = ["farm_id", "status"]),
        Index(value = ["farm_id", "tag_id"], unique = true)
    ]
)
data class AnimalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "barn_id") val barnId: String? = null,
    @ColumnInfo(name = "tag_id") val tagId: String,
    @ColumnInfo(name = "rfid_tag") val rfidTag: String? = null,
    @ColumnInfo(name = "qr_code_data") val qrCodeData: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val species: String = "cow",
    val dob: String? = null,               // ISO date "2020-03-15"
    val gender: String,
    @ColumnInfo(name = "color_marks") val colorMarks: String? = null,
    @ColumnInfo(name = "weight_kg") val weightKg: Double? = null,
    @ColumnInfo(name = "purchase_date") val purchaseDate: String? = null,
    @ColumnInfo(name = "purchase_price") val purchasePrice: Double? = null,
    val source: String? = null,
    @ColumnInfo(name = "sire_id") val sireId: String? = null,
    @ColumnInfo(name = "dam_id") val damId: String? = null,
    val status: String = "active",
    @ColumnInfo(name = "sold_date") val soldDate: String? = null,
    @ColumnInfo(name = "sold_price") val soldPrice: Double? = null,
    @ColumnInfo(name = "sold_to") val soldTo: String? = null,
    @ColumnInfo(name = "deceased_date") val deceasedDate: String? = null,
    @ColumnInfo(name = "deceased_reason") val deceasedReason: String? = null,
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String = nowIso(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = nowIso()
) {
    fun toDomain(): Animal = Animal(
        id = id,
        farmId = farmId,
        barnId = barnId,
        tagId = tagId,
        rfidTag = rfidTag,
        qrCodeData = qrCodeData,
        name = name,
        breed = breed,
        species = species,
        dob = dob?.let { LocalDate.parse(it) },
        gender = Gender.from(gender),
        colorMarks = colorMarks,
        weightKg = weightKg,
        purchaseDate = purchaseDate?.let { LocalDate.parse(it) },
        purchasePrice = purchasePrice,
        source = source,
        sireId = sireId,
        damId = damId,
        status = AnimalStatus.from(status),
        soldDate = soldDate?.let { LocalDate.parse(it) },
        soldPrice = soldPrice,
        soldTo = soldTo,
        deceasedDate = deceasedDate?.let { LocalDate.parse(it) },
        deceasedReason = deceasedReason,
        photoUrl = photoUrl,
        notes = notes,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
    )

    companion object {
        fun fromDomain(domain: Animal): AnimalEntity = AnimalEntity(
            id = domain.id,
            farmId = domain.farmId,
            barnId = domain.barnId,
            tagId = domain.tagId,
            rfidTag = domain.rfidTag,
            qrCodeData = domain.qrCodeData,
            name = domain.name,
            breed = domain.breed,
            species = domain.species,
            dob = domain.dob?.toString(),
            gender = domain.gender.value,
            colorMarks = domain.colorMarks,
            weightKg = domain.weightKg,
            purchaseDate = domain.purchaseDate?.toString(),
            purchasePrice = domain.purchasePrice,
            source = domain.source,
            sireId = domain.sireId,
            damId = domain.damId,
            status = domain.status.value,
            soldDate = domain.soldDate?.toString(),
            soldPrice = domain.soldPrice,
            soldTo = domain.soldTo,
            deceasedDate = domain.deceasedDate?.toString(),
            deceasedReason = domain.deceasedReason,
            photoUrl = domain.photoUrl,
            notes = domain.notes,
            createdAt = domain.createdAt.toString(),
            updatedAt = domain.updatedAt.toString()
        )

        private fun nowIso(): String =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
}
