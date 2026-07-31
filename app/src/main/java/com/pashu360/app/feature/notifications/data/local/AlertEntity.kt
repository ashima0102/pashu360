package com.pashu360.app.feature.notifications.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pashu360.app.core.domain.model.Alert
import com.pashu360.app.core.domain.model.AlertPriority
import com.pashu360.app.core.domain.model.AlertType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(
    tableName = "alerts",
    indices = [
        Index("farm_id"),
        Index("is_resolved"),
        Index("due_date"),
        Index("source_id")
    ]
)
data class AlertEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "animal_id") val animalId: String? = null,
    @ColumnInfo(name = "animal_tag") val animalTag: String? = null,
    @ColumnInfo(name = "animal_name") val animalName: String? = null,
    @ColumnInfo(name = "alert_type") val alertType: String,
    val title: String,
    val message: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: String,             // ISO date
    val priority: String = "medium",
    @ColumnInfo(name = "is_resolved") val isResolved: Boolean = false,
    @ColumnInfo(name = "resolved_at") val resolvedAt: String? = null,
    @ColumnInfo(name = "source_id") val sourceId: String? = null,
    @ColumnInfo(name = "notification_sent") val notificationSent: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String
) {
    fun toDomain(): Alert = Alert(
        id = id,
        farmId = farmId,
        animalId = animalId,
        animalTag = animalTag,
        animalName = animalName,
        alertType = AlertType.from(alertType),
        title = title,
        message = message,
        dueDate = LocalDate.parse(dueDate),
        priority = AlertPriority.from(priority),
        isResolved = isResolved,
        resolvedAt = resolvedAt?.let { LocalDateTime.parse(it) },
        sourceId = sourceId,
        createdAt = LocalDateTime.parse(createdAt)
    )

    companion object {
        @OptIn(ExperimentalTime::class)
        fun fromDomain(a: Alert): AlertEntity = AlertEntity(
            id = a.id,
            farmId = a.farmId,
            animalId = a.animalId,
            animalTag = a.animalTag,
            animalName = a.animalName,
            alertType = a.alertType.value,
            title = a.title,
            message = a.message,
            dueDate = a.dueDate.toString(),
            priority = a.priority.value,
            isResolved = a.isResolved,
            resolvedAt = a.resolvedAt?.toString(),
            sourceId = a.sourceId,
            createdAt = a.createdAt.toString()
        )

        @OptIn(ExperimentalTime::class)
        fun nowIso(): String =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
}
