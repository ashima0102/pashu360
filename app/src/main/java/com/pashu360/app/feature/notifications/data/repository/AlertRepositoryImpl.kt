package com.pashu360.app.feature.notifications.data.repository

import com.pashu360.app.core.domain.model.Alert
import com.pashu360.app.feature.notifications.data.local.AlertDao
import com.pashu360.app.feature.notifications.data.local.AlertEntity
import com.pashu360.app.feature.notifications.domain.repository.AlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertDao
) : AlertRepository {

    override fun observeAll(farmId: String): Flow<List<Alert>> =
        dao.observeAll(farmId).map { list -> list.map { it.toDomain() } }

    override fun observeUnresolved(farmId: String): Flow<List<Alert>> =
        dao.observeUnresolved(farmId).map { list -> list.map { it.toDomain() } }

    override fun countUnresolved(farmId: String): Flow<Int> = dao.countUnresolved(farmId)

    override fun observeOverdueOrToday(farmId: String, today: LocalDate): Flow<List<Alert>> =
        dao.observeOverdueOrToday(farmId, today.toString())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insertOrIgnore(alert: Alert): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Skip if we already have one for this source (e.g. same vaccination id)
                if (alert.sourceId != null) {
                    val existing = dao.getBySourceId(alert.farmId, alert.sourceId)
                    if (existing != null) return@runCatching false
                }
                // Also dedupe on animal + type + due date
                if (alert.animalId != null) {
                    val duplicate = dao.getByAnimalTypeAndDate(
                        alert.farmId, alert.animalId,
                        alert.alertType.value, alert.dueDate.toString()
                    )
                    if (duplicate != null) return@runCatching false
                }
                dao.insert(AlertEntity.fromDomain(alert))
                true
            }
        }

    override suspend fun resolve(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val nowIso = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            runCatching { dao.resolve(id, nowIso) }.map { }
        }

    override suspend fun delete(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { dao.deleteById(id) }.map { }
        }

    override suspend fun getPendingNotificationsAndMark(
        farmId: String, today: LocalDate
    ): List<Alert> = withContext(Dispatchers.IO) {
        val pending = dao.getPendingNotifications(farmId, today.toString())
        pending.forEach { dao.markNotified(it.id) }
        pending.map { it.toDomain() }
    }

    override suspend fun cleanupOldResolved(cutoff: LocalDate) {
        withContext(Dispatchers.IO) {
            dao.deleteOldResolved(cutoff.toString())
        }
    }
}
