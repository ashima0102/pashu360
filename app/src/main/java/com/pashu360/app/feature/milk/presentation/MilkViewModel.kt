package com.pashu360.app.feature.milk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.BulkMilkEntry
import com.pashu360.app.core.domain.model.DailyMilkTotal
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.feature.milk.domain.repository.BulkEntryInput
import com.pashu360.app.feature.milk.domain.repository.MilkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class MilkUiState(
    val selectedDate: LocalDate,
    val selectedSession: MilkSession = MilkSession.MORNING,
    val entries: List<BulkMilkEntry> = emptyList(),
    val quantityInputs: Map<String, String> = emptyMap(),        // animalId -> user typing
    val fatInputs: Map<String, String> = emptyMap(),
    val snfInputs: Map<String, String> = emptyMap(),
    val showQualityFields: Boolean = false,
    val dailyTotalLiters: Double = 0.0,
    val weeklyTotals: List<DailyMilkTotal> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
) {
    val enteredTotal: Double
        get() = quantityInputs.values.mapNotNull { it.toDoubleOrNull() }.sum()
}

sealed class MilkEvent {
    data class Saved(val recordsSaved: Int) : MilkEvent()
    data class ShowError(val message: String) : MilkEvent()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class MilkViewModel @Inject constructor(
    private val repository: MilkRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _selectedDate = MutableStateFlow(today)
    private val _selectedSession = MutableStateFlow(MilkSession.MORNING)
    private val _quantityInputs = MutableStateFlow(emptyMap<String, String>())
    private val _fatInputs = MutableStateFlow(emptyMap<String, String>())
    private val _snfInputs = MutableStateFlow(emptyMap<String, String>())
    private val _showQualityFields = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)

    // Track which animals the user has manually typed in — so their edits aren't
    // overwritten by the reactive flow re-emitting the same data.
    private val touchedAnimals = mutableSetOf<String>()

    private val _events = Channel<MilkEvent>()
    val events = _events.receiveAsFlow()

    private val farmId get() = sessionStore.getActiveFarmId()

    /** Reactive list of active animals + their existing session records. */
    private val entriesFlow = combine(_selectedDate, _selectedSession) { d, s -> d to s }
        .flatMapLatest { (date, session) ->
            repository.observeBulkEntry(farmId, date, session)
        }

    /** Reactive daily total for the selected date. */
    private val dailyTotalFlow = _selectedDate.flatMapLatest { date ->
        repository.observeDailyTotal(farmId, date)
    }

    /** Reactive 7-day chart data. */
    private val weeklyFlow = _selectedDate.flatMapLatest { date ->
        repository.observeWeeklyTotals(farmId, date)
    }

    init {
        // Prefill (or re-fill) inputs when animals/records change, without stomping
        // on values the user is currently typing.
        viewModelScope.launch {
            entriesFlow.collect { list ->
                _quantityInputs.value = list.associate { entry ->
                    val current = _quantityInputs.value[entry.animalId]
                    val next = if (entry.animalId in touchedAnimals) current.orEmpty()
                               else entry.existingQuantity?.toString().orEmpty()
                    entry.animalId to next
                }
                _fatInputs.value = list.associate { entry ->
                    entry.animalId to (entry.existingFat?.toString().orEmpty())
                }
                _snfInputs.value = list.associate { entry ->
                    entry.animalId to (entry.existingSnf?.toString().orEmpty())
                }
            }
        }
    }

    // Group the many state sources — Kotlin combine tops out at 5.
    private val groupA = combine(
        _selectedDate, _selectedSession, entriesFlow, _showQualityFields, _isSaving
    ) { date, session, entries, showQual, saving -> Quintuple(date, session, entries, showQual, saving) }

    private val groupB = combine(
        _quantityInputs, _fatInputs, _snfInputs, dailyTotalFlow, weeklyFlow
    ) { qty, fat, snf, daily, weekly -> Quintuple(qty, fat, snf, daily, weekly) }

    val uiState: StateFlow<MilkUiState> = combine(groupA, groupB) { a, b ->
        MilkUiState(
            selectedDate = a.first,
            selectedSession = a.second,
            entries = a.third,
            showQualityFields = a.fourth,
            isSaving = a.fifth,
            quantityInputs = b.first,
            fatInputs = b.second,
            snfInputs = b.third,
            dailyTotalLiters = b.fourth,
            weeklyTotals = b.fifth,
            isLoading = a.third.isEmpty()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MilkUiState(selectedDate = today)
    )

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )

    fun onSessionChanged(session: MilkSession) {
        if (session == _selectedSession.value) return
        _selectedSession.value = session
        // Reset touched set — user's edits belonged to the previous session.
        touchedAnimals.clear()
    }

    fun onQuantityChanged(animalId: String, value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }.take(5)
        touchedAnimals += animalId
        _quantityInputs.value = _quantityInputs.value + (animalId to sanitized)
    }

    fun onFatChanged(animalId: String, value: String) {
        _fatInputs.value = _fatInputs.value +
            (animalId to value.filter { it.isDigit() || it == '.' }.take(4))
    }

    fun onSnfChanged(animalId: String, value: String) {
        _snfInputs.value = _snfInputs.value +
            (animalId to value.filter { it.isDigit() || it == '.' }.take(4))
    }

    fun onToggleQualityFields() {
        _showQualityFields.value = !_showQualityFields.value
    }

    fun onSaveAll() {
        if (_isSaving.value) return
        _isSaving.value = true

        viewModelScope.launch {
            val entries = uiState.value.entries
            val inputs = entries.map { entry ->
                BulkEntryInput(
                    animalId = entry.animalId,
                    quantityLiters = _quantityInputs.value[entry.animalId]?.toDoubleOrNull(),
                    fatPct = _fatInputs.value[entry.animalId]?.toDoubleOrNull(),
                    snfPct = _snfInputs.value[entry.animalId]?.toDoubleOrNull()
                )
            }
            repository.saveBulkEntry(
                farmId, _selectedDate.value, _selectedSession.value, inputs
            ).onSuccess { count ->
                _isSaving.value = false
                touchedAnimals.clear()   // saved values are now canonical
                _events.send(MilkEvent.Saved(count))
            }.onFailure { e ->
                _isSaving.value = false
                _events.send(MilkEvent.ShowError(e.message ?: "Could not save"))
            }
        }
    }
}
