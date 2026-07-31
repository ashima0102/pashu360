package com.pashu360.app.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VetContact
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class HealthUiState(
    val records: List<HealthRecord> = emptyList(),
    val vaccinations: List<Vaccination> = emptyList(),
    val vetContacts: List<VetContact> = emptyList(),
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val activeIssueCount: Int = 0,
    val today: LocalDate,
    val isLoading: Boolean = true
)

sealed class HealthEvent {
    data class Saved(val message: String) : HealthEvent()
    data class ShowError(val message: String) : HealthEvent()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class HealthViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val farmId get() = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _events = Channel<HealthEvent>()
    val events = _events.receiveAsFlow()

    // Group A (5 flows)
    private val groupA = combine(
        repository.observeHealthRecords(farmId),
        repository.observeVaccinations(farmId),
        repository.observeVetContacts(farmId),
        repository.countOverdueVaccines(farmId, today),
        repository.countDueSoonVaccines(farmId, today, 7)
    ) { records, vaccs, contacts, overdue, dueSoon ->
        Quintuple(records, vaccs, contacts, overdue, dueSoon)
    }

    private val activeCountFlow = repository.countActiveHealthIssues(farmId)

    val uiState: StateFlow<HealthUiState> = combine(groupA, activeCountFlow) { a, active ->
        HealthUiState(
            records = a.first,
            vaccinations = a.second,
            vetContacts = a.third,
            overdueCount = a.fourth,
            dueSoonCount = a.fifth,
            activeIssueCount = active,
            today = today,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HealthUiState(today = today)
    )

    fun addVaccination(vaccination: Vaccination) {
        viewModelScope.launch {
            repository.saveVaccination(vaccination)
                .onSuccess {
                    _events.send(HealthEvent.Saved("Vaccination recorded"))
                }
                .onFailure {
                    _events.send(HealthEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    fun addHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            repository.saveHealthRecord(record)
                .onSuccess {
                    _events.send(HealthEvent.Saved("Health event recorded"))
                }
                .onFailure {
                    _events.send(HealthEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    fun addVetContact(contact: VetContact) {
        viewModelScope.launch {
            repository.saveVetContact(contact)
                .onSuccess {
                    _events.send(HealthEvent.Saved("Contact saved"))
                }
                .onFailure {
                    _events.send(HealthEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    fun resolveHealthRecord(id: String) {
        viewModelScope.launch { repository.markHealthResolved(id) }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )
}
