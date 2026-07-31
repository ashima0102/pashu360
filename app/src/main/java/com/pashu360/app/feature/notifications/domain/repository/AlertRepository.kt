package com.pashu360.app.feature.notifications.domain.repository

import com.pashu360.app.core.domain.model.Alert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface AlertRepository {
    fun observeAll(farmId: String): Flow<List<Alert>>
    fun observeUnresolved(farmId: String): Flow<List<Alert>>
    fun countUnresolved(farmId: String): Flow<Int>
    fun observeOverdueOrToday(farmId: String, today: LocalDate): Flow<List<Alert>>

    suspend fun insertOrIgnore(alert: Alert): Result<Boolean>
    suspend fun resolve(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>

    /** Pull pending notifications and mark them notified in one atomic block. */
    suspend fun getPendingNotificationsAndMark(farmId: String, today: LocalDate): List<Alert>

    /** Cleanup — delete resolved alerts older than the cutoff. */
    suspend fun cleanupOldResolved(cutoff: LocalDate)
}
