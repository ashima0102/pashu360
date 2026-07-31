package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import java.util.UUID

data class MilkRecord(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val recordDate: LocalDate,
    val session: MilkSession,
    val quantityLiters: Double,
    val fatPct: Double? = null,
    val snfPct: Double? = null,
    val clr: Double? = null,
    val ph: Double? = null,
    val notes: String? = null
) {
    /**
     * Milk quality grading based on fat% + SNF% (Indian FSSAI standards).
     *  A+ : fat >= 4.5 AND snf >= 8.5    (co-op premium)
     *  A  : fat >= 3.5 AND snf >= 8.0    (standard whole milk)
     *  B  : fat >= 3.0 AND snf >= 7.5    (lower grade)
     *  —  : below B or no quality data
     */
    val qualityGrade: MilkGrade
        get() {
            val f = fatPct ?: return MilkGrade.UNGRADED
            val s = snfPct ?: return MilkGrade.UNGRADED
            return when {
                f >= 4.5 && s >= 8.5 -> MilkGrade.A_PLUS
                f >= 3.5 && s >= 8.0 -> MilkGrade.A
                f >= 3.0 && s >= 7.5 -> MilkGrade.B
                else -> MilkGrade.BELOW_STANDARD
            }
        }
}

enum class MilkSession(val value: String, val displayName: String, val emoji: String) {
    MORNING("morning", "Morning", "🌅"),
    EVENING("evening", "Evening", "🌆");

    companion object {
        fun from(value: String): MilkSession = entries.find { it.value == value } ?: MORNING
    }
}

enum class MilkGrade(val label: String) {
    A_PLUS("A+"),
    A("A"),
    B("B"),
    BELOW_STANDARD("Below"),
    UNGRADED("—")
}

/** Represents one animal's entry state on the bulk milk entry screen. */
data class BulkMilkEntry(
    val animalId: String,
    val tagId: String,
    val animalName: String?,
    val breed: String?,
    val existingQuantity: Double? = null,
    val existingFat: Double? = null,
    val existingSnf: Double? = null
) {
    val displayName: String
        get() = animalName?.takeIf { it.isNotBlank() } ?: "Tag #$tagId"
}

/** For charts and analytics. */
data class DailyMilkTotal(
    val date: LocalDate,
    val morning: Double,
    val evening: Double
) {
    val total: Double get() = morning + evening
}
