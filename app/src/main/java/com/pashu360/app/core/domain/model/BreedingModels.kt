package com.pashu360.app.core.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.util.UUID

// ─────────────────────────────────────────────────────────
// HEAT RECORD — logged heat detection event
// ─────────────────────────────────────────────────────────
data class HeatRecord(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val detectionDate: LocalDate,
    val symptoms: List<String> = emptyList(),
    val intensity: HeatIntensity = HeatIntensity.MEDIUM,
    val detectedBy: String? = null,
    val notes: String? = null,
    val breedingRecordId: String? = null   // linked breeding, if any
) {
    /** Standard 21-day cycle prediction. Configurable per farm later. */
    fun expectedNextHeat(cycleDays: Int = 21): LocalDate =
        detectionDate.plus(DatePeriod(days = cycleDays))
}

enum class HeatIntensity(val value: String, val displayName: String) {
    WEAK("weak", "Weak"),
    MEDIUM("medium", "Medium"),
    STRONG("strong", "Strong");

    companion object {
        fun from(v: String): HeatIntensity = entries.find { it.value == v } ?: MEDIUM
    }
}

object HeatSymptomCatalog {
    val symptoms = listOf(
        "Mounting", "Mucus discharge", "Restless", "Bellowing",
        "Reduced milk", "Off feed", "Swollen vulva", "Tail raised"
    )
}

// ─────────────────────────────────────────────────────────
// BREEDING RECORD — AI or natural mating
// ─────────────────────────────────────────────────────────
data class BreedingRecord(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val heatRecordId: String? = null,
    val breedingType: BreedingType,
    val breedingDate: LocalDate,
    val bullName: String? = null,
    val semenBatch: String? = null,
    val aiTechnician: String? = null,
    val bullAnimalId: String? = null,   // if bull is on the farm
    val conceptionStatus: ConceptionStatus = ConceptionStatus.PENDING,
    val cost: Double? = null,
    val notes: String? = null
) {
    /** Expected pregnancy check date (30 days after breeding). */
    fun expectedPdDate(): LocalDate = breedingDate.plus(DatePeriod(days = 30))

    /** Expected calving date (standard 280-day cattle gestation). */
    fun expectedCalvingDate(gestationDays: Int = 280): LocalDate =
        breedingDate.plus(DatePeriod(days = gestationDays))
}

enum class BreedingType(val value: String, val displayName: String, val emoji: String) {
    AI("ai", "AI (Artificial Insemination)", "💉"),
    NATURAL("natural", "Natural Mating", "🐂");

    companion object {
        fun from(v: String): BreedingType = entries.find { it.value == v } ?: AI
    }
}

enum class ConceptionStatus(val value: String, val displayName: String, val emoji: String) {
    PENDING("pending", "Pending", "⏳"),
    CONFIRMED("confirmed", "Confirmed", "✅"),
    FAILED("failed", "Failed", "❌");

    companion object {
        fun from(v: String): ConceptionStatus = entries.find { it.value == v } ?: PENDING
    }
}

// ─────────────────────────────────────────────────────────
// PREGNANCY RECORD
// ─────────────────────────────────────────────────────────
data class PregnancyRecord(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val breedingRecordId: String? = null,
    val confirmationDate: LocalDate,
    val pdMethod: PdMethod = PdMethod.RECTAL_PALPATION,
    val expectedCalvingDate: LocalDate,
    val dryPeriodStart: LocalDate,          // expected calving − 60 days
    val actualCalvingDate: LocalDate? = null,
    val calvingDifficulty: Int? = null,     // 1..4
    val calvingOutcome: CalvingOutcome? = null,
    val calvingNotes: String? = null,
    val calfAnimalId: String? = null,
    val notes: String? = null
) {
    val isActive: Boolean get() = actualCalvingDate == null
}

enum class PdMethod(val value: String, val displayName: String) {
    RECTAL_PALPATION("rectal_palpation", "Rectal Palpation"),
    ULTRASOUND("ultrasound", "Ultrasound"),
    BLOOD_TEST("blood_test", "Blood Test"),
    OBSERVATION("observation", "Observation");

    companion object {
        fun from(v: String): PdMethod = entries.find { it.value == v } ?: RECTAL_PALPATION
    }
}

enum class CalvingOutcome(val value: String, val displayName: String, val emoji: String) {
    LIVE_CALF("live_calf", "Live Calf", "🐄"),
    STILLBIRTH("stillbirth", "Stillbirth", "💔"),
    TWINS("twins", "Twins", "👶👶"),
    ABORTION("abortion", "Abortion", "❌");

    companion object {
        fun from(v: String): CalvingOutcome? = entries.find { it.value == v }
    }
}

// ─────────────────────────────────────────────────────────
// DENORMALIZED VIEWS (join with animal data)
// ─────────────────────────────────────────────────────────
data class HeatRecordDetail(
    val heat: HeatRecord,
    val animalName: String?,
    val animalTag: String
)

data class BreedingRecordDetail(
    val breeding: BreedingRecord,
    val animalName: String?,
    val animalTag: String
)

data class PregnancyDetail(
    val pregnancy: PregnancyRecord,
    val animalName: String?,
    val animalTag: String
)

// Farm-level breeding stats
data class BreedingStats(
    val activePregnancies: Int,
    val awaitingPd: Int,
    val heatDueThisWeek: Int,
    val calvingDueThisMonth: Int,
    val conceptionRatePercent: Double
)
