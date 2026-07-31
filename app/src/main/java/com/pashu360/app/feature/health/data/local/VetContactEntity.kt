package com.pashu360.app.feature.health.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.VetContact
import java.util.UUID

@Entity(
    tableName = "vet_contacts",
    indices = [Index("farm_id")]
)
data class VetContactEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    val name: String,
    val phone: String,
    val specialty: String? = null,
    val clinic: String? = null,
    val notes: String? = null
) {
    fun toDomain(): VetContact = VetContact(
        id = id, farmId = farmId, name = name,
        phone = phone, specialty = specialty,
        clinic = clinic, notes = notes
    )

    companion object {
        fun fromDomain(v: VetContact): VetContactEntity = VetContactEntity(
            id = v.id, farmId = v.farmId, name = v.name,
            phone = v.phone, specialty = v.specialty,
            clinic = v.clinic, notes = v.notes
        )
    }
}
