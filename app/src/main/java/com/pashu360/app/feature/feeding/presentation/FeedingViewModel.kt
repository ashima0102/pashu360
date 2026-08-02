package com.pashu360.app.feature.feeding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.core.domain.model.FeedRecord
import com.pashu360.app.core.domain.model.FeedRecordWithType
import com.pashu360.app.core.domain.model.FeedType
import com.pashu360.app.core.domain.model.InventoryWithType
import com.pashu360.app.core.domain.model.TimeOfDay
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.feeding.domain.repository.FeedingRepository
import com.pashu360.app.feature.notifications.system.AlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

data class FeedingUiState(
    val today: LocalDate,
    val feedTypes: List<FeedType> = emptyList(),
    val animals: List<Animal> = emptyList(),
    val todayRecords: List<FeedRecordWithType> = emptyList(),
    val todayTotalKg: Double = 0.0,
    val inventory: List<InventoryWithType> = emptyList(),
    val lowStockCount: Int = 0,
    val isLoading: Boolean = true
) {
    val totalFedAnimals: Int get() = todayRecords.map { it.record.animalId }
        .filterNotNull().toSet().size
}

data class LogFeedFormState(
    val show: Boolean = false,
    val feedTypeId: String = "",
    val animalId: String? = null,     // null = herd feeding
    val isHerdFeeding: Boolean = false,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val quantity: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = feedTypeId.isNotBlank() && (quantity.toDoubleOrNull() ?: 0.0) > 0.0
}

data class InventoryAdjustFormState(
    val show: Boolean = false,
    val feedTypeId: String = "",
    val feedTypeName: String = "",
    val unit: String = "kg",
    val addQuantity: String = "",
    val newThreshold: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = feedTypeId.isNotBlank() &&
                ((addQuantity.toDoubleOrNull() ?: 0.0) != 0.0 ||
                 (newThreshold.toDoubleOrNull() ?: 0.0) > 0.0)
}

data class FeedTypeFormState(
    val show: Boolean = false,
    val name: String = "",
    val category: com.pashu360.app.core.domain.model.FeedCategory =
        com.pashu360.app.core.domain.model.FeedCategory.GREEN_FODDER,
    val unit: String = "kg",
    val costPerUnit: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = name.isNotBlank()
}

sealed class FeedingEvent {
    data class Saved(val message: String) : FeedingEvent()
    data class ShowError(val message: String) : FeedingEvent()
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class FeedingViewModel @Inject constructor(
    private val repository: FeedingRepository,
    private val animalRepository: AnimalRepository,
    private val alertScheduler: AlertScheduler,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val farmId get() = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _events = Channel<FeedingEvent>()
    val events = _events.receiveAsFlow()

    private val _logForm = MutableStateFlow(LogFeedFormState())
    val logForm: StateFlow<LogFeedFormState> = _logForm.asStateFlow()

    private val _inventoryForm = MutableStateFlow(InventoryAdjustFormState())
    val inventoryForm: StateFlow<InventoryAdjustFormState> = _inventoryForm.asStateFlow()

    private val _feedTypeForm = MutableStateFlow(FeedTypeFormState())
    val feedTypeForm: StateFlow<FeedTypeFormState> = _feedTypeForm.asStateFlow()

    init {
        // Seed defaults once
        viewModelScope.launch {
            repository.ensureDefaultFeedTypes(farmId)
        }
    }

    // Reactive data streams
    private val feedTypesFlow = repository.observeFeedTypes(farmId)
    private val animalsFlow = animalRepository.observeAnimals(farmId, AnimalFilter.ACTIVE)
    private val todayRecordsFlow = repository.observeRecordsForDate(farmId, today)
    private val dailyTotalFlow = repository.observeDailyTotal(farmId, today)
    private val inventoryFlow = repository.observeInventory(farmId)
    private val lowStockCountFlow = repository.countLowStockItems(farmId)

    // Kotlin combine tops out at 5 → split into two groups
    private val groupA = combine(
        feedTypesFlow, animalsFlow, todayRecordsFlow, dailyTotalFlow, inventoryFlow
    ) { types, animals, records, total, inv ->
        Quintuple(types, animals, records, total, inv)
    }

    val uiState: StateFlow<FeedingUiState> = combine(groupA, lowStockCountFlow) { a, low ->
        FeedingUiState(
            today = today,
            feedTypes = a.first,
            animals = a.second,
            todayRecords = a.third,
            todayTotalKg = a.fourth,
            inventory = a.fifth,
            lowStockCount = low,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FeedingUiState(today = today)
    )

    // ── LOG FEED FORM ────────────────────────
    fun openLogFeedForm(preSelectFeedTypeId: String = "") {
        _logForm.value = LogFeedFormState(
            show = true,
            feedTypeId = preSelectFeedTypeId
        )
    }
    fun closeLogFeedForm() { _logForm.value = LogFeedFormState() }

    fun onLogFeedTypeChanged(id: String) { _logForm.update { it.copy(feedTypeId = id) } }
    fun onLogAnimalChanged(id: String?) { _logForm.update { it.copy(animalId = id) } }
    fun onLogHerdToggled(herd: Boolean) {
        _logForm.update {
            it.copy(isHerdFeeding = herd, animalId = if (herd) null else it.animalId)
        }
    }
    fun onLogTimeChanged(t: TimeOfDay) { _logForm.update { it.copy(timeOfDay = t) } }
    fun onLogQuantityChanged(v: String) {
        _logForm.update {
            it.copy(quantity = v.filter { c -> c.isDigit() || c == '.' }.take(6))
        }
    }
    fun onLogNotesChanged(v: String) { _logForm.update { it.copy(notes = v) } }

    fun saveFeedLog() {
        val s = _logForm.value
        val qty = s.quantity.toDoubleOrNull() ?: return
        if (qty <= 0.0) return
        _logForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = FeedRecord(
                farmId = farmId,
                animalId = if (s.isHerdFeeding) null else s.animalId,
                feedTypeId = s.feedTypeId,
                recordDate = today,
                timeOfDay = s.timeOfDay,
                quantity = qty,
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.logFeed(record, deductFromInventory = true)
                .onSuccess {
                    alertScheduler.scanNow()   // maybe stock crossed threshold
                    _events.send(FeedingEvent.Saved("Feed logged"))
                    _logForm.value = LogFeedFormState()
                }
                .onFailure {
                    _logForm.update { it.copy(isSaving = false) }
                    _events.send(FeedingEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    // ── INVENTORY ADJUST FORM ─────────────────
    fun openInventoryForm(feedTypeId: String, name: String, unit: String) {
        _inventoryForm.value = InventoryAdjustFormState(
            show = true,
            feedTypeId = feedTypeId,
            feedTypeName = name,
            unit = unit
        )
    }
    fun closeInventoryForm() { _inventoryForm.value = InventoryAdjustFormState() }

    fun onInventoryAddChanged(v: String) {
        _inventoryForm.update {
            it.copy(addQuantity = v.filter { c -> c.isDigit() || c == '.' || c == '-' }.take(8))
        }
    }
    fun onInventoryThresholdChanged(v: String) {
        _inventoryForm.update {
            it.copy(newThreshold = v.filter { c -> c.isDigit() || c == '.' }.take(6))
        }
    }

    fun saveInventoryAdjustment() {
        val s = _inventoryForm.value
        _inventoryForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val delta = s.addQuantity.toDoubleOrNull() ?: 0.0
            val threshold = s.newThreshold.toDoubleOrNull()

            if (delta != 0.0) {
                repository.addStock(farmId, s.feedTypeId, delta)
                    .onFailure {
                        _inventoryForm.update { it.copy(isSaving = false) }
                        _events.send(FeedingEvent.ShowError(it.message ?: "Failed"))
                        return@launch
                    }
            }
            if (threshold != null && threshold > 0) {
                repository.setThreshold(farmId, s.feedTypeId, threshold)
            }
            alertScheduler.scanNow()
            _events.send(FeedingEvent.Saved("Inventory updated"))
            _inventoryForm.value = InventoryAdjustFormState()
        }
    }

    // ── FEED TYPE FORM ────────────────────────
    fun openFeedTypeForm() { _feedTypeForm.value = FeedTypeFormState(show = true) }
    fun closeFeedTypeForm() { _feedTypeForm.value = FeedTypeFormState() }

    fun onFeedTypeNameChanged(v: String) { _feedTypeForm.update { it.copy(name = v) } }
    fun onFeedTypeCategoryChanged(c: com.pashu360.app.core.domain.model.FeedCategory) {
        _feedTypeForm.update { it.copy(category = c) }
    }
    fun onFeedTypeUnitChanged(v: String) { _feedTypeForm.update { it.copy(unit = v.take(6)) } }
    fun onFeedTypeCostChanged(v: String) {
        _feedTypeForm.update {
            it.copy(costPerUnit = v.filter { c -> c.isDigit() || c == '.' }.take(8))
        }
    }

    fun saveFeedType() {
        val s = _feedTypeForm.value
        if (!s.isValid) return
        _feedTypeForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val ft = FeedType(
                farmId = farmId,
                name = s.name.trim(),
                category = s.category,
                unit = s.unit.ifBlank { "kg" },
                costPerUnit = s.costPerUnit.toDoubleOrNull()
            )
            repository.addFeedType(ft)
                .onSuccess {
                    _events.send(FeedingEvent.Saved("${s.name} added"))
                    _feedTypeForm.value = FeedTypeFormState()
                }
                .onFailure {
                    _feedTypeForm.update { it.copy(isSaving = false) }
                    _events.send(FeedingEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )
}
