package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.feature.animal.domain.usecase.GetAnimalByIdUseCase
import com.pashu360.app.feature.milk.domain.repository.BulkEntryInput
import com.pashu360.app.feature.milk.domain.repository.MilkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class QuickMilkSheetState(
    val show: Boolean = false,
    val session: MilkSession = MilkSession.MORNING,
    val quantity: String = "",
    val fat: String = "",
    val snf: String = "",
    val showQualityFields: Boolean = false,
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = (quantity.toDoubleOrNull() ?: 0.0) > 0.0
}

sealed class AnimalDetailEvent {
    data class MilkSaved(val litres: Double) : AnimalDetailEvent()
    data class ShowError(val message: String) : AnimalDetailEvent()
}

@OptIn(ExperimentalTime::class)
@HiltViewModel
class AnimalDetailViewModel @Inject constructor(
    private val getAnimalByIdUseCase: GetAnimalByIdUseCase,
    private val milkRepository: MilkRepository,
    private val sessionStore: SessionStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animalId: String = checkNotNull(savedStateHandle["animalId"])

    val animal: StateFlow<Animal?> = getAnimalByIdUseCase(animalId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    private val _milkSheet = MutableStateFlow(QuickMilkSheetState())
    val milkSheet: StateFlow<QuickMilkSheetState> = _milkSheet.asStateFlow()

    private val _events = Channel<AnimalDetailEvent>()
    val events = _events.receiveAsFlow()

    // ── Milk sheet handlers ─────────────────────────────
    fun openMilkSheet() {
        _milkSheet.update { it.copy(show = true) }
    }

    fun closeMilkSheet() {
        _milkSheet.update {
            QuickMilkSheetState()   // reset all fields when closed
        }
    }

    fun onSessionChanged(session: MilkSession) {
        _milkSheet.update { it.copy(session = session) }
    }

    fun onQuantityChanged(value: String) {
        _milkSheet.update {
            it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' }.take(5))
        }
    }

    fun onFatChanged(value: String) {
        _milkSheet.update {
            it.copy(fat = value.filter { c -> c.isDigit() || c == '.' }.take(4))
        }
    }

    fun onSnfChanged(value: String) {
        _milkSheet.update {
            it.copy(snf = value.filter { c -> c.isDigit() || c == '.' }.take(4))
        }
    }

    fun onToggleQualityFields() {
        _milkSheet.update { it.copy(showQualityFields = !it.showQualityFields) }
    }

    fun onSaveMilk() {
        val state = _milkSheet.value
        val litres = state.quantity.toDoubleOrNull() ?: return
        if (litres <= 0.0) return

        _milkSheet.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val today: LocalDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            milkRepository.saveBulkEntry(
                farmId = sessionStore.getActiveFarmId(),
                date = today,
                session = state.session,
                entries = listOf(
                    BulkEntryInput(
                        animalId = animalId,
                        quantityLiters = litres,
                        fatPct = state.fat.toDoubleOrNull(),
                        snfPct = state.snf.toDoubleOrNull()
                    )
                )
            ).onSuccess {
                _events.send(AnimalDetailEvent.MilkSaved(litres))
                _milkSheet.value = QuickMilkSheetState()   // close + reset
            }.onFailure { e ->
                _milkSheet.update { it.copy(isSaving = false) }
                _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
            }
        }
    }
}
