package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────
// FEED TYPE — catalogue of feed items on the farm
// ─────────────────────────────────────────────────────────
data class FeedType(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val name: String,
    val category: FeedCategory,
    val unit: String = "kg",
    val costPerUnit: Double? = null,
    val isActive: Boolean = true
)

enum class FeedCategory(val value: String, val displayName: String, val emoji: String) {
    GREEN_FODDER("green_fodder", "Green Fodder", "🌿"),
    DRY_FODDER("dry_fodder", "Dry Fodder", "🌾"),
    CONCENTRATE("concentrate", "Concentrate", "🥣"),
    MINERAL_MIX("mineral_mix", "Mineral Mix", "🧂"),
    WATER("water", "Water", "💧"),
    OTHER("other", "Other", "📦");

    companion object {
        fun from(value: String): FeedCategory = entries.find { it.value == value } ?: OTHER
    }
}

/** Default feed types created on first launch. */
object FeedTypeCatalog {
    fun defaults(farmId: String): List<FeedType> = listOf(
        FeedType(farmId = farmId, name = "Green Fodder",   category = FeedCategory.GREEN_FODDER, costPerUnit = 5.0),
        FeedType(farmId = farmId, name = "Dry Fodder",     category = FeedCategory.DRY_FODDER, costPerUnit = 12.0),
        FeedType(farmId = farmId, name = "Concentrate Mix",category = FeedCategory.CONCENTRATE, costPerUnit = 32.0),
        FeedType(farmId = farmId, name = "Mineral Mix",    category = FeedCategory.MINERAL_MIX, unit = "g", costPerUnit = 0.5),
    )
}

// ─────────────────────────────────────────────────────────
// FEED RECORD — a single log entry for a feeding event
// ─────────────────────────────────────────────────────────
data class FeedRecord(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val animalId: String? = null,        // nullable — herd-level feeding
    val feedTypeId: String,
    val recordDate: LocalDate,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val quantity: Double,                 // in feed type's unit
    val notes: String? = null
) {
    val isHerdFeeding: Boolean get() = animalId == null
}

enum class TimeOfDay(val value: String, val displayName: String, val emoji: String) {
    MORNING("morning", "Morning", "🌅"),
    AFTERNOON("afternoon", "Afternoon", "☀️"),
    EVENING("evening", "Evening", "🌆");

    companion object {
        fun from(value: String): TimeOfDay = entries.find { it.value == value } ?: MORNING
    }
}

// ─────────────────────────────────────────────────────────
// FEED INVENTORY — stock levels + low-stock threshold
// ─────────────────────────────────────────────────────────
data class FeedInventory(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val feedTypeId: String,
    val quantity: Double,
    val lowStockThreshold: Double,
    val lastUpdated: LocalDate
) {
    val isLowStock: Boolean get() = quantity < lowStockThreshold
    val stockLevel: StockLevel get() = when {
        quantity <= 0 -> StockLevel.OUT
        quantity < lowStockThreshold -> StockLevel.LOW
        quantity < lowStockThreshold * 2 -> StockLevel.OK
        else -> StockLevel.HIGH
    }
}

enum class StockLevel { OUT, LOW, OK, HIGH }

// ─────────────────────────────────────────────────────────
// COMBINED VIEW — a feed record with denormalized display info
// ─────────────────────────────────────────────────────────
data class FeedRecordWithType(
    val record: FeedRecord,
    val feedTypeName: String,
    val feedTypeCategory: FeedCategory,
    val feedTypeUnit: String,
    val animalName: String? = null,
    val animalTag: String? = null,
    val estimatedCost: Double? = null
)

/** Combined view for the inventory list. */
data class InventoryWithType(
    val inventory: FeedInventory,
    val feedTypeName: String,
    val feedTypeCategory: FeedCategory,
    val feedTypeUnit: String
)
