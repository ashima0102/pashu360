package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class Alert(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val animalId: String? = null,
    val animalTag: String? = null,          // denormalized for display
    val animalName: String? = null,
    val alertType: AlertType,
    val title: String,
    val message: String? = null,
    val dueDate: LocalDate,
    val priority: AlertPriority = AlertPriority.MEDIUM,
    val isResolved: Boolean = false,
    val resolvedAt: LocalDateTime? = null,
    val sourceId: String? = null,           // link back to vaccination/breeding id
    val createdAt: LocalDateTime
) {
    fun urgency(today: LocalDate): AlertUrgency = when {
        dueDate < today -> AlertUrgency.OVERDUE
        dueDate == today -> AlertUrgency.TODAY
        else -> {
            val daysAway = dueDate.toEpochDays() - today.toEpochDays()
            if (daysAway <= 3) AlertUrgency.SOON else AlertUrgency.UPCOMING
        }
    }
}

enum class AlertType(
    val value: String,
    val displayName: String,
    val emoji: String,
    val channelId: String
) {
    VACCINATION_DUE(
        "vaccination_due", "Vaccination", "💉", "vaccination_alerts"
    ),
    HEAT_EXPECTED(
        "heat_expected", "Heat", "♨️", "heat_alerts"
    ),
    CALVING_DUE(
        "calving_due", "Calving", "🤱", "calving_alerts"
    ),
    MEDICINE_REMINDER(
        "medicine_reminder", "Medicine", "💊", "general_alerts"
    ),
    LOW_FEED_STOCK(
        "low_feed_stock", "Feed Stock", "🌿", "feeding_alerts"
    ),
    CUSTOM(
        "custom", "Reminder", "🔔", "general_alerts"
    );

    companion object {
        fun from(value: String): AlertType = entries.find { it.value == value } ?: CUSTOM
    }
}

enum class AlertPriority(val value: String, val level: Int) {
    LOW("low", 0),
    MEDIUM("medium", 1),
    HIGH("high", 2),
    URGENT("urgent", 3);

    companion object {
        fun from(value: String): AlertPriority =
            entries.find { it.value == value } ?: MEDIUM
    }
}

enum class AlertUrgency { OVERDUE, TODAY, SOON, UPCOMING }

enum class AlertFilter(val displayName: String) {
    ALL("All"),
    VACCINATION("Vaccination"),
    HEAT("Heat"),
    CALVING("Calving"),
    OTHER("Other")
}
