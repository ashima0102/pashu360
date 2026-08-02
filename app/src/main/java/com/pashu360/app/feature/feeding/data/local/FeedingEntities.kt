package com.pashu360.app.feature.feeding.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.FeedCategory
import com.pashu360.app.core.domain.model.FeedInventory
import com.pashu360.app.core.domain.model.FeedRecord
import com.pashu360.app.core.domain.model.FeedType
import com.pashu360.app.core.domain.model.TimeOfDay
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────
// FEED TYPE
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "feed_types",
    indices = [Index("farm_id"), Index(value = ["farm_id", "name"], unique = true)]
)
data class FeedTypeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    val name: String,
    val category: String,
    val unit: String = "kg",
    @ColumnInfo(name = "cost_per_unit") val costPerUnit: Double? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
) {
    fun toDomain(): FeedType = FeedType(
        id = id, farmId = farmId, name = name,
        category = FeedCategory.from(category),
        unit = unit, costPerUnit = costPerUnit, isActive = isActive
    )

    companion object {
        fun fromDomain(t: FeedType): FeedTypeEntity = FeedTypeEntity(
            id = t.id, farmId = t.farmId, name = t.name,
            category = t.category.value, unit = t.unit,
            costPerUnit = t.costPerUnit, isActive = t.isActive
        )
    }
}

// ─────────────────────────────────────────────────────────
// FEED RECORD
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "feed_records",
    indices = [
        Index("animal_id"),
        Index("farm_id"),
        Index("feed_type_id"),
        Index("record_date")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AnimalEntity::class,
            parentColumns = ["id"],
            childColumns = ["animal_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FeedTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["feed_type_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class FeedRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "animal_id") val animalId: String? = null,
    @ColumnInfo(name = "feed_type_id") val feedTypeId: String,
    @ColumnInfo(name = "record_date") val recordDate: String,
    @ColumnInfo(name = "time_of_day") val timeOfDay: String,
    val quantity: Double,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
) {
    fun toDomain(): FeedRecord = FeedRecord(
        id = id, farmId = farmId, animalId = animalId,
        feedTypeId = feedTypeId,
        recordDate = LocalDate.parse(recordDate),
        timeOfDay = TimeOfDay.from(timeOfDay),
        quantity = quantity, notes = notes
    )

    companion object {
        fun fromDomain(r: FeedRecord): FeedRecordEntity = FeedRecordEntity(
            id = r.id, farmId = r.farmId, animalId = r.animalId,
            feedTypeId = r.feedTypeId,
            recordDate = r.recordDate.toString(),
            timeOfDay = r.timeOfDay.value,
            quantity = r.quantity, notes = r.notes
        )
    }
}

// ─────────────────────────────────────────────────────────
// FEED INVENTORY
// ─────────────────────────────────────────────────────────
@Entity(
    tableName = "feed_inventory",
    indices = [
        Index("farm_id"),
        Index(value = ["farm_id", "feed_type_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = FeedTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["feed_type_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FeedInventoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "feed_type_id") val feedTypeId: String,
    val quantity: Double,
    @ColumnInfo(name = "low_stock_threshold") val lowStockThreshold: Double,
    @ColumnInfo(name = "last_updated") val lastUpdated: String
) {
    fun toDomain(): FeedInventory = FeedInventory(
        id = id, farmId = farmId, feedTypeId = feedTypeId,
        quantity = quantity, lowStockThreshold = lowStockThreshold,
        lastUpdated = LocalDate.parse(lastUpdated)
    )

    companion object {
        fun fromDomain(i: FeedInventory): FeedInventoryEntity = FeedInventoryEntity(
            id = i.id, farmId = i.farmId, feedTypeId = i.feedTypeId,
            quantity = i.quantity, lowStockThreshold = i.lowStockThreshold,
            lastUpdated = i.lastUpdated.toString()
        )
    }
}
