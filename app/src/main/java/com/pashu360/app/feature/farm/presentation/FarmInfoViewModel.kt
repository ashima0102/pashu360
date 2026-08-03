package com.pashu360.app.feature.farm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Farm
import com.pashu360.app.feature.farm.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class FarmInfoUiState(
    val ownerName: String = "",
    val farmName: String = "",
    val village: String = "",
    val state: String = "",
    val expectedHerdSize: String = "",
    val isSaving: Boolean = false,
    val loaded: Boolean = false
) {
    val isValid: Boolean
        get() = ownerName.isNotBlank() && farmName.isNotBlank() && village.isNotBlank()
}

sealed class FarmInfoEvent {
    object Saved : FarmInfoEvent()
    data class ShowError(val message: String) : FarmInfoEvent()
}

@OptIn(ExperimentalTime::class)
@HiltViewModel
class FarmInfoViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmInfoUiState())
    val uiState: StateFlow<FarmInfoUiState> = _uiState.asStateFlow()

    private val _events = Channel<FarmInfoEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            farmRepository.getFarm(sessionStore.getActiveFarmId())?.let { f ->
                _uiState.value = FarmInfoUiState(
                    ownerName = f.ownerName,
                    farmName = f.farmName,
                    village = f.village,
                    state = f.state,
                    expectedHerdSize = if (f.expectedHerdSize > 0)
                        f.expectedHerdSize.toString() else "",
                    loaded = true
                )
            } ?: run {
                _uiState.update { it.copy(loaded = true) }
            }
        }
    }

    fun onOwnerNameChanged(v: String) { _uiState.update { it.copy(ownerName = v) } }
    fun onFarmNameChanged(v: String) { _uiState.update { it.copy(farmName = v) } }
    fun onVillageChanged(v: String) { _uiState.update { it.copy(village = v) } }
    fun onStateChanged(v: String) { _uiState.update { it.copy(state = v) } }
    fun onHerdSizeChanged(v: String) {
        _uiState.update { it.copy(expectedHerdSize = v.filter { c -> c.isDigit() }.take(4)) }
    }

    fun save() {
        val s = _uiState.value
        if (!s.isValid) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val farm = Farm(
                id = sessionStore.getActiveFarmId(),
                ownerName = s.ownerName.trim(),
                farmName = s.farmName.trim(),
                village = s.village.trim(),
                state = s.state.trim(),
                expectedHerdSize = s.expectedHerdSize.toIntOrNull() ?: 0,
                createdAt = now
            )
            farmRepository.saveFarm(farm)
                .onSuccess { _events.send(FarmInfoEvent.Saved) }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(FarmInfoEvent.ShowError(it.message ?: "Failed to save"))
                }
        }
    }
}
