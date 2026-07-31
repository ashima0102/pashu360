package com.pashu360.app.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Alert
import com.pashu360.app.core.domain.model.AlertFilter
import com.pashu360.app.core.domain.model.AlertType
import com.pashu360.app.feature.notifications.domain.repository.AlertRepository
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

data class AlertsUiState(
    val alerts: List<Alert> = emptyList(),
    val filter: AlertFilter = AlertFilter.ALL,
    val today: LocalDate,
    val isLoading: Boolean = true
) {
    val filteredAlerts: List<Alert>
        get() = when (filter) {
            AlertFilter.ALL -> alerts
            AlertFilter.VACCINATION -> alerts.filter { it.alertType == AlertType.VACCINATION_DUE }
            AlertFilter.HEAT -> alerts.filter { it.alertType == AlertType.HEAT_EXPECTED }
            AlertFilter.CALVING -> alerts.filter { it.alertType == AlertType.CALVING_DUE }
            AlertFilter.OTHER -> alerts.filter {
                it.alertType !in setOf(
                    AlertType.VACCINATION_DUE,
                    AlertType.HEAT_EXPECTED,
                    AlertType.CALVING_DUE
                )
            }
        }
}

sealed class AlertsEvent {
    data class ShowMessage(val text: String) : AlertsEvent()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: AlertRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _filter = MutableStateFlow(AlertFilter.ALL)

    private val _events = Channel<AlertsEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<AlertsUiState> = combine(
        repository.observeAll(sessionStore.getActiveFarmId()),
        _filter
    ) { alerts, filter ->
        AlertsUiState(
            alerts = alerts,
            filter = filter,
            today = today,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AlertsUiState(today = today)
    )

    fun onFilterChanged(filter: AlertFilter) {
        _filter.value = filter
    }

    fun onMarkResolved(id: String) {
        viewModelScope.launch {
            repository.resolve(id)
                .onSuccess { _events.send(AlertsEvent.ShowMessage("Resolved")) }
                .onFailure { _events.send(AlertsEvent.ShowMessage(it.message ?: "Failed")) }
        }
    }

    fun onDelete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}
