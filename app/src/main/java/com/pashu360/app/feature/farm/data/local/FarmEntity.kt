package com.pashu360.app.feature.farm.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.Farm
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_name") val ownerName: String,
    @ColumnInfo(name = "farm_name") val farmName: String,
    @ColumnInfo(name = "village") val village: String,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "expected_herd_size") val expectedHerdSize: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
) {
    fun toDomain(): Farm = Farm(
        id = id,
        ownerName = ownerName,
        farmName = farmName,
        village = village,
        state = state,
        expectedHerdSize = expectedHerdSize,
        createdAt = LocalDateTime.parse(createdAt)
    )

    companion object {
        fun fromDomain(farm: Farm): FarmEntity = FarmEntity(
            id = farm.id,
            ownerName = farm.ownerName,
            farmName = farm.farmName,
            village = farm.village,
            state = farm.state,
            expectedHerdSize = farm.expectedHerdSize,
            createdAt = farm.createdAt.toString()
        )
    }
}
