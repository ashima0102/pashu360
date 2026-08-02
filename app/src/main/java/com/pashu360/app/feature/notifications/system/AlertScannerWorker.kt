package com.pashu360.app.feature.notifications.system

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Alert
import com.pashu360.app.core.domain.model.AlertPriority
import com.pashu360.app.core.domain.model.AlertType
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.breeding.domain.repository.BreedingRepository
import com.pashu360.app.feature.feeding.domain.repository.FeedingRepository
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import com.pashu360.app.feature.notifications.domain.repository.AlertRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Scans upcoming vaccinations, breeding events, and low feed stock and
 * generates Alert entities. Fires system notifications for anything due
 * today or overdue. Runs periodically as a WorkManager job and can also
 * be triggered as a one-shot right after a record is saved.
 */
@OptIn(ExperimentalTime::class)
@HiltWorker
class AlertScannerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionStore: SessionStore,
    private val alertRepo: AlertRepository,
    private val healthRepo: HealthRepository,
    private val animalRepo: AnimalRepository,
    private val feedingRepo: FeedingRepository,
    private val breedingRepo: BreedingRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val farmId = sessionStore.getActiveFarmId()
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            scanVaccinations(farmId, today)
            scanLowFeedStock(farmId, today)
            scanExpectedHeats(farmId, today)
            scanCalvingDue(farmId, today)
            fireDueNotifications(farmId, today)
            cleanupOldResolved(today)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun scanVaccinations(farmId: String, today: LocalDate) {
        val window = today.plus(DatePeriod(days = ALERT_LEAD_DAYS))
        val vaccinations = healthRepo.observeVaccinations(farmId).first()

        vaccinations.forEach { v ->
            val next = v.nextDueDate ?: return@forEach
            if (next > window) return@forEach

            val animal = animalRepo.observeAnimals(
                farmId, com.pashu360.app.core.domain.model.AnimalFilter.ALL
            ).first().firstOrNull { it.id == v.animalId }

            val priority = when {
                next < today -> AlertPriority.URGENT
                next == today -> AlertPriority.HIGH
                else -> AlertPriority.MEDIUM
            }

            val alert = Alert(
                farmId = farmId,
                animalId = v.animalId,
                animalTag = animal?.tagId,
                animalName = animal?.name,
                alertType = AlertType.VACCINATION_DUE,
                title = "${v.vaccineName} due",
                message = v.diseaseTarget?.let { "For $it" }
                    ?: "Vaccination due on ${next}",
                dueDate = next,
                priority = priority,
                sourceId = v.id,
                createdAt = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
            alertRepo.insertOrIgnore(alert)
        }
    }

    /** Scan feed inventory for items below their low-stock threshold. */
    private suspend fun scanLowFeedStock(farmId: String, today: LocalDate) {
        val lowStock = feedingRepo.getLowStockInventory(farmId)
        lowStock.forEach { row ->
            val alert = Alert(
                farmId = farmId,
                animalId = row.inventory.feedTypeId,
                alertType = AlertType.LOW_FEED_STOCK,
                title = "Low stock: ${row.feedTypeName}",
                message = "Only %.1f ${row.feedTypeUnit} left · threshold %.0f".format(
                    row.inventory.quantity, row.inventory.lowStockThreshold
                ),
                dueDate = today,
                priority = if (row.inventory.quantity <= 0.0) AlertPriority.URGENT
                           else AlertPriority.HIGH,
                sourceId = "feed:${row.inventory.feedTypeId}:${today}",
                createdAt = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
            alertRepo.insertOrIgnore(alert)
        }
    }

    /** Predict next heat (21-day cycle), alert 1 day before. */
    private suspend fun scanExpectedHeats(farmId: String, today: LocalDate) {
        val latestHeats = breedingRepo.getLatestHeatPerAnimal(farmId)
        latestHeats.forEach { h ->
            val expected = h.expectedNextHeat()
            if (expected < today || expected > today.plus(DatePeriod(days = 1))) return@forEach

            val animal = animalRepo.observeAnimals(
                farmId, com.pashu360.app.core.domain.model.AnimalFilter.ALL
            ).first().firstOrNull { it.id == h.animalId }

            val alert = Alert(
                farmId = farmId,
                animalId = h.animalId,
                animalTag = animal?.tagId,
                animalName = animal?.name,
                alertType = AlertType.HEAT_EXPECTED,
                title = "Heat expected",
                message = "Watch for heat signs on ${animal?.name ?: "Tag #" + animal?.tagId}",
                dueDate = expected,
                priority = if (expected == today) AlertPriority.HIGH else AlertPriority.MEDIUM,
                sourceId = "heat:${h.id}",
                createdAt = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
            alertRepo.insertOrIgnore(alert)
        }
    }

    /** Fire CALVING_DUE alerts 7 days before expected calving. */
    private suspend fun scanCalvingDue(farmId: String, today: LocalDate) {
        val cutoff = today.plus(DatePeriod(days = 7))
        val calvings = breedingRepo.getCalvingsInWindow(farmId, today, cutoff)
        calvings.forEach { p ->
            val animal = animalRepo.observeAnimals(
                farmId, com.pashu360.app.core.domain.model.AnimalFilter.ALL
            ).first().firstOrNull { it.id == p.animalId }

            val alert = Alert(
                farmId = farmId,
                animalId = p.animalId,
                animalTag = animal?.tagId,
                animalName = animal?.name,
                alertType = AlertType.CALVING_DUE,
                title = "Calving due",
                message = "Expected calving on ${p.expectedCalvingDate}",
                dueDate = p.expectedCalvingDate,
                priority = if (p.expectedCalvingDate <= today.plus(DatePeriod(days = 2)))
                    AlertPriority.URGENT else AlertPriority.HIGH,
                sourceId = "calving:${p.id}",
                createdAt = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
            alertRepo.insertOrIgnore(alert)
        }
    }

    private suspend fun fireDueNotifications(farmId: String, today: LocalDate) {
        val pending = alertRepo.getPendingNotificationsAndMark(farmId, today)
        pending.forEach { notificationHelper.show(it) }
    }

    private suspend fun cleanupOldResolved(today: LocalDate) {
        alertRepo.cleanupOldResolved(today.minus(DatePeriod(days = 90)))
    }

    companion object {
        const val ALERT_LEAD_DAYS = 3
        private const val WORK_NAME_PERIODIC = "AlertScannerPeriodic"
        private const val WORK_NAME_ONESHOT = "AlertScannerOneShot"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            val request = PeriodicWorkRequestBuilder<AlertScannerWorker>(
                6, TimeUnit.HOURS
            ).setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<AlertScannerWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}