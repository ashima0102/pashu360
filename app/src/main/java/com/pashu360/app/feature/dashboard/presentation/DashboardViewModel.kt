package com.pashu360.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class DashboardUiState(
    val cowCount: Int = 0,
    val vaccinesDueCount: Int = 0,
    val sickCount: Int = 0
)

@OptIn(ExperimentalTime::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    animalRepository: AnimalRepository,
    healthRepository: HealthRepository,
    sessionStore: SessionStore
) : ViewModel() {

    private val farmId = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val cowCountFlow =
        animalRepository.observeAnimals(farmId, AnimalFilter.ACTIVE).map { it.size }

    private val vaccinesDueFlow = combine(
        healthRepository.countOverdueVaccines(farmId, today),
        healthRepository.countDueSoonVaccines(farmId, today, 7)
    ) { overdue, dueSoon -> overdue + dueSoon }

    private val sickFlow = healthRepository.countActiveHealthIssues(farmId)

    val uiState: StateFlow<DashboardUiState> = combine(
        cowCountFlow,
        vaccinesDueFlow,
        sickFlow
    ) { cows, vaccines, sick ->
        DashboardUiState(cowCount = cows, vaccinesDueCount = vaccines, sickCount = sick)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )
}
