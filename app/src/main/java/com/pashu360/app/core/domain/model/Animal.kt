package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

data class Animal(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val barnId: String? = null,
    val tagId: String,
    val rfidTag: String? = null,
    val qrCodeData: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val species: String = "cow",
    val dob: LocalDate? = null,
    val gender: Gender,
    val colorMarks: String? = null,
    val weightKg: Double? = null,
    val purchaseDate: LocalDate? = null,
    val purchasePrice: Double? = null,
    val source: String? = null,
    val sireId: String? = null,
    val damId: String? = null,
    val status: AnimalStatus = AnimalStatus.ACTIVE,
    val soldDate: LocalDate? = null,
    val soldPrice: Double? = null,
    val soldTo: String? = null,
    val deceasedDate: LocalDate? = null,
    val deceasedReason: String? = null,
    val photoUrl: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: "Tag #$tagId"

    val ageString: String
        get() {
            val dob = dob ?: return "Age unknown"
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val years = today.year - dob.year
            val months = today.monthNumber - dob.monthNumber
            val totalMonths = years * 12 + months
            return when {
                totalMonths < 12 -> "$totalMonths months"
                totalMonths % 12 == 0 -> "${totalMonths / 12} yrs"
                else -> "${totalMonths / 12} yrs ${totalMonths % 12} mo"
            }
        }
}

enum class Gender(val value: String, val displayName: String) {
    FEMALE("female", "Female"),
    MALE("male", "Male");

    companion object {
        fun from(value: String): Gender = entries.find { it.value == value } ?: FEMALE
    }
}

enum class AnimalStatus(val value: String, val displayName: String) {
    ACTIVE("active", "Active"),
    PREGNANT("pregnant", "Pregnant"),
    DRY("dry", "Dry"),
    SICK("sick", "Sick"),
    SOLD("sold", "Sold"),
    DECEASED("deceased", "Deceased");

    companion object {
        fun from(value: String): AnimalStatus = entries.find { it.value == value } ?: ACTIVE
    }
}

enum class AnimalFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    PREGNANT("Pregnant"),
    SICK("Sick"),
    DRY("Dry"),
    SOLD("Sold")
}

// Common Indian dairy breeds
object BreedCatalog {
    val breeds = listOf(
        "HF (Holstein Friesian)",
        "Jersey",
        "Sahiwal",
        "Gir",
        "Red Sindhi",
        "Tharparkar",
        "Rathi",
        "Kankrej",
        "Ongole",
        "Hariana",
        "Deoni",
        "Cross Breed",
        "Other"
    )
}
