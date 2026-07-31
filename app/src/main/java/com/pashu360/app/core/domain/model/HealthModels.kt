package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────
// HEALTH RECORD — General health event log
// ─────────────────────────────────────────────────────────
data class HealthRecord(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val eventDate: LocalDate,
    val eventType: HealthEventType,
    val symptoms: List<String> = emptyList(),
    val diagnosis: String? = null,
    val severity: Severity = Severity.MILD,
    val treatmentNotes: String? = null,
    val medicineName: String? = null,
    val medicineDose: String? = null,
    val vetName: String? = null,
    val cost: Double? = null,
    val isResolved: Boolean = false,
    val resolvedDate: LocalDate? = null,
    val notes: String? = null
)

enum class HealthEventType(val value: String, val displayName: String, val emoji: String) {
    CHECKUP("checkup", "Checkup", "🩺"),
    DISEASE("disease", "Disease", "🦠"),
    INJURY("injury", "Injury", "🩹"),
    VET_VISIT("vet_visit", "Vet Visit", "👨‍⚕️");

    companion object {
        fun from(value: String): HealthEventType =
            entries.find { it.value == value } ?: CHECKUP
    }
}

enum class Severity(val value: String, val displayName: String) {
    MILD("mild", "Mild"),
    MODERATE("moderate", "Moderate"),
    SEVERE("severe", "Severe");

    companion object {
        fun from(value: String): Severity = entries.find { it.value == value } ?: MILD
    }
}

/** Common Indian symptom tags — pre-populated for quick selection. */
object SymptomCatalog {
    val symptoms = listOf(
        "Fever", "Not eating", "Diarrhea", "Bloat",
        "Coughing", "Nasal discharge", "Eye discharge",
        "Limping", "Skin lesion", "Weight loss",
        "Reduced milk", "Weakness", "Vomiting"
    )
}

// ─────────────────────────────────────────────────────────
// VACCINATION
// ─────────────────────────────────────────────────────────
data class Vaccination(
    val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val farmId: String,
    val vaccineName: String,
    val diseaseTarget: String? = null,
    val administeredDate: LocalDate,
    val nextDueDate: LocalDate? = null,
    val batchNumber: String? = null,
    val administeredBy: String? = null,
    val cost: Double? = null,
    val notes: String? = null
) {
    fun isOverdue(today: LocalDate): Boolean =
        nextDueDate != null && nextDueDate < today

    fun isDueSoon(today: LocalDate, daysWindow: Int = 7): Boolean {
        val next = nextDueDate ?: return false
        if (next < today) return false
        val diff = next.toEpochDays() - today.toEpochDays()
        return diff in 0..daysWindow
    }
}

/** Default Indian cattle vaccines with typical intervals. */
data class VaccineTemplate(
    val name: String,
    val disease: String,
    val intervalDays: Int
)

object VaccineCatalog {
    val templates = listOf(
        VaccineTemplate("FMD Vaccine",         "Foot & Mouth Disease",     180),
        VaccineTemplate("BQ Vaccine",          "Black Quarter",            365),
        VaccineTemplate("HS Vaccine",          "Haemorrhagic Septicaemia", 365),
        VaccineTemplate("Brucellosis Vaccine", "Brucellosis",              365),
        VaccineTemplate("Anthrax Vaccine",     "Anthrax",                  365),
        VaccineTemplate("Theileria Vaccine",   "Theileriosis",             365),
        VaccineTemplate("PPR Vaccine",         "Peste des Petits Ruminants", 365),
        VaccineTemplate("Rabies Vaccine",      "Rabies",                   365)
    )
}

// ─────────────────────────────────────────────────────────
// VET CONTACT — farm's vet phone book
// ─────────────────────────────────────────────────────────
data class VetContact(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val name: String,
    val phone: String,
    val specialty: String? = null,
    val clinic: String? = null,
    val notes: String? = null
)
