package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalStatus
import com.pashu360.app.core.domain.model.HealthEventType
import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.core.domain.model.Severity
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VaccineTemplate
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.animal.domain.usecase.GetAnimalByIdUseCase
import com.pashu360.app.feature.breeding.domain.repository.BreedingRepository
import com.pashu360.app.feature.feeding.domain.repository.FeedingRepository
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import com.pashu360.app.feature.milk.domain.repository.BulkEntryInput
import com.pashu360.app.feature.milk.domain.repository.MilkRepository
import com.pashu360.app.feature.notifications.system.AlertScheduler
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
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

data class QuickVaccinationState(
    val show: Boolean = false,
    val template: VaccineTemplate? = null,
    val customName: String = "",
    val administeredDate: LocalDate,
    val nextDueDate: LocalDate? = null,
    val batchNumber: String = "",
    val administeredBy: String = "",
    val cost: String = "",
    val isSaving: Boolean = false
) {
    val vaccineName: String
        get() = template?.name?.takeIf { it.isNotBlank() } ?: customName.trim()
    val isValid: Boolean get() = vaccineName.isNotBlank()
}

data class QuickHealthEventState(
    val show: Boolean = false,
    val eventType: HealthEventType = HealthEventType.CHECKUP,
    val eventDate: LocalDate,
    val selectedSymptoms: Set<String> = emptySet(),
    val severity: Severity = Severity.MILD,
    val diagnosis: String = "",
    val medicineName: String = "",
    val medicineDose: String = "",
    val vetName: String = "",
    val cost: String = "",
    val isSaving: Boolean = false
)

sealed class AnimalDetailEvent {
    data class MilkSaved(val litres: Double) : AnimalDetailEvent()
    data class VaccinationSaved(val name: String) : AnimalDetailEvent()
    data class HealthSaved(val diagnosis: String?) : AnimalDetailEvent()
    data class StatusChanged(val status: AnimalStatus) : AnimalDetailEvent()
    data class ShowError(val message: String) : AnimalDetailEvent()
}

@OptIn(ExperimentalTime::class)
@HiltViewModel
class AnimalDetailViewModel @Inject constructor(
    private val getAnimalByIdUseCase: GetAnimalByIdUseCase,
    private val animalRepository: AnimalRepository,
    private val milkRepository: MilkRepository,
    private val healthRepository: HealthRepository,
    private val feedingRepository: FeedingRepository,
    private val breedingRepository: BreedingRepository,
    private val alertScheduler: AlertScheduler,
    private val sessionStore: SessionStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animalId: String = checkNotNull(savedStateHandle["animalId"])
    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val animal: StateFlow<Animal?> = getAnimalByIdUseCase(animalId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val vaccinationHistory: StateFlow<List<Vaccination>> =
        healthRepository.observeVaccinationsForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val healthHistory: StateFlow<List<HealthRecord>> =
        healthRepository.observeHealthRecordsForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val milkHistory: StateFlow<List<com.pashu360.app.core.domain.model.MilkRecord>> =
        milkRepository.observeRecordsForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val feedHistory: StateFlow<List<com.pashu360.app.core.domain.model.FeedRecordWithType>> =
        feedingRepository.observeRecordsForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val heatHistory: StateFlow<List<com.pashu360.app.core.domain.model.HeatRecord>> =
        breedingRepository.observeHeatForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val breedingHistoryForAnimal: StateFlow<List<com.pashu360.app.core.domain.model.BreedingRecord>> =
        breedingRepository.observeBreedingsForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val pregnancyHistoryForAnimal: StateFlow<List<com.pashu360.app.core.domain.model.PregnancyRecord>> =
        breedingRepository.observePregnanciesForAnimal(animalId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    private val _milkSheet = MutableStateFlow(QuickMilkSheetState())
    val milkSheet: StateFlow<QuickMilkSheetState> = _milkSheet.asStateFlow()

    private val _vaccinationSheet = MutableStateFlow(
        QuickVaccinationState(administeredDate = today)
    )
    val vaccinationSheet: StateFlow<QuickVaccinationState> = _vaccinationSheet.asStateFlow()

    private val _healthSheet = MutableStateFlow(
        QuickHealthEventState(eventDate = today)
    )
    val healthSheet: StateFlow<QuickHealthEventState> = _healthSheet.asStateFlow()

    private val _statusPickerVisible = MutableStateFlow(false)
    val statusPickerVisible: StateFlow<Boolean> = _statusPickerVisible.asStateFlow()

    private val _pendingSold = MutableStateFlow(false)
    val pendingSold: StateFlow<Boolean> = _pendingSold.asStateFlow()

    private val _pendingDeceased = MutableStateFlow(false)
    val pendingDeceased: StateFlow<Boolean> = _pendingDeceased.asStateFlow()

    private val _events = Channel<AnimalDetailEvent>()
    val events = _events.receiveAsFlow()

    fun openStatusPicker() { _statusPickerVisible.value = true }
    fun closeStatusPicker() { _statusPickerVisible.value = false }

    fun changeStatus(status: AnimalStatus) {
        _statusPickerVisible.value = false
        when (status) {
            AnimalStatus.SOLD -> { _pendingSold.value = true }
            AnimalStatus.DECEASED -> { _pendingDeceased.value = true }
            else -> writeStatusOnly(status)
        }
    }

    private fun writeStatusOnly(status: AnimalStatus) {
        viewModelScope.launch {
            animalRepository.updateStatus(animalId, status.value)
                .onSuccess { _events.send(AnimalDetailEvent.StatusChanged(status)) }
                .onFailure { e ->
                    _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not update status"))
                }
        }
    }

    fun cancelSoldCapture() { _pendingSold.value = false }
    fun cancelDeceasedCapture() { _pendingDeceased.value = false }

    fun confirmSold(saleDate: LocalDate, salePriceRupees: Double?, buyer: String?) {
        val current = animal.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = AnimalStatus.SOLD,
                soldDate = saleDate,
                soldPrice = salePriceRupees,
                soldTo = buyer?.takeIf { it.isNotBlank() }
            )
            animalRepository.updateAnimal(updated)
                .onSuccess {
                    _pendingSold.value = false
                    _events.send(AnimalDetailEvent.StatusChanged(AnimalStatus.SOLD))
                }
                .onFailure { e ->
                    _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }

    fun confirmDeceased(deceasedDate: LocalDate, reason: String?) {
        val current = animal.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = AnimalStatus.DECEASED,
                deceasedDate = deceasedDate,
                deceasedReason = reason?.takeIf { it.isNotBlank() }
            )
            animalRepository.updateAnimal(updated)
                .onSuccess {
                    _pendingDeceased.value = false
                    _events.send(AnimalDetailEvent.StatusChanged(AnimalStatus.DECEASED))
                }
                .onFailure { e ->
                    _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }

    private val farmId get() = sessionStore.getActiveFarmId()

    // ── MILK SHEET ──────────────────────────────────────
    fun openMilkSheet() { _milkSheet.update { it.copy(show = true) } }
    fun closeMilkSheet() { _milkSheet.value = QuickMilkSheetState() }
    fun onSessionChanged(session: MilkSession) { _milkSheet.update { it.copy(session = session) } }
    fun onQuantityChanged(v: String) {
        _milkSheet.update { it.copy(quantity = v.filter { c -> c.isDigit() || c == '.' }.take(5)) }
    }
    fun onFatChanged(v: String) {
        _milkSheet.update { it.copy(fat = v.filter { c -> c.isDigit() || c == '.' }.take(4)) }
    }
    fun onSnfChanged(v: String) {
        _milkSheet.update { it.copy(snf = v.filter { c -> c.isDigit() || c == '.' }.take(4)) }
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
            milkRepository.saveBulkEntry(
                farmId = farmId,
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
                _milkSheet.value = QuickMilkSheetState()
            }.onFailure { e ->
                _milkSheet.update { it.copy(isSaving = false) }
                _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
            }
        }
    }

    // ── VACCINATION SHEET ──────────────────────────────
    fun openVaccinationSheet() {
        _vaccinationSheet.value = QuickVaccinationState(show = true, administeredDate = today)
    }
    fun closeVaccinationSheet() {
        _vaccinationSheet.value = QuickVaccinationState(administeredDate = today)
    }
    fun onVaccineTemplateChanged(t: VaccineTemplate?) {
        _vaccinationSheet.update { s ->
            val nextDue = t?.intervalDays?.let { s.administeredDate.plus(DatePeriod(days = it)) }
            s.copy(template = t, customName = "", nextDueDate = nextDue)
        }
    }
    fun onCustomVaccineNameChanged(v: String) {
        _vaccinationSheet.update { it.copy(customName = v, template = null) }
    }
    fun onVaccinationAdministeredDateChanged(d: LocalDate) {
        _vaccinationSheet.update { s ->
            val nextDue = s.template?.intervalDays?.let { d.plus(DatePeriod(days = it)) }
                ?: s.nextDueDate
            s.copy(administeredDate = d, nextDueDate = nextDue)
        }
    }
    fun onVaccinationNextDueDateChanged(d: LocalDate?) {
        _vaccinationSheet.update { it.copy(nextDueDate = d) }
    }
    fun onVaccinationBatchChanged(v: String) {
        _vaccinationSheet.update { it.copy(batchNumber = v) }
    }
    fun onVaccinationAdminByChanged(v: String) {
        _vaccinationSheet.update { it.copy(administeredBy = v) }
    }
    fun onVaccinationCostChanged(v: String) {
        _vaccinationSheet.update {
            it.copy(cost = v.filter { c -> c.isDigit() || c == '.' })
        }
    }
    fun onSaveVaccination() {
        val s = _vaccinationSheet.value
        if (!s.isValid) return
        _vaccinationSheet.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val vaccination = Vaccination(
                animalId = animalId,
                farmId = farmId,
                vaccineName = s.vaccineName,
                diseaseTarget = s.template?.disease,
                administeredDate = s.administeredDate,
                nextDueDate = s.nextDueDate,
                batchNumber = s.batchNumber.takeIf { it.isNotBlank() },
                administeredBy = s.administeredBy.takeIf { it.isNotBlank() },
                cost = s.cost.toDoubleOrNull()
            )
            healthRepository.saveVaccination(vaccination)
                .onSuccess {
                    alertScheduler.scanNow()
                    _events.send(AnimalDetailEvent.VaccinationSaved(s.vaccineName))
                    _vaccinationSheet.value = QuickVaccinationState(administeredDate = today)
                }
                .onFailure { e ->
                    _vaccinationSheet.update { it.copy(isSaving = false) }
                    _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }

    // ── HEALTH EVENT SHEET ──────────────────────────────
    fun openHealthSheet() {
        _healthSheet.value = QuickHealthEventState(show = true, eventDate = today)
    }
    fun closeHealthSheet() {
        _healthSheet.value = QuickHealthEventState(eventDate = today)
    }
    fun onHealthTypeChanged(t: HealthEventType) { _healthSheet.update { it.copy(eventType = t) } }
    fun onHealthSeverityChanged(s: Severity) { _healthSheet.update { it.copy(severity = s) } }
    fun onHealthSymptomToggled(sym: String) {
        _healthSheet.update { s ->
            val next = if (sym in s.selectedSymptoms) s.selectedSymptoms - sym
                       else s.selectedSymptoms + sym
            s.copy(selectedSymptoms = next)
        }
    }
    fun onHealthDiagnosisChanged(v: String) { _healthSheet.update { it.copy(diagnosis = v) } }
    fun onHealthMedicineNameChanged(v: String) { _healthSheet.update { it.copy(medicineName = v) } }
    fun onHealthMedicineDoseChanged(v: String) { _healthSheet.update { it.copy(medicineDose = v) } }
    fun onHealthVetNameChanged(v: String) { _healthSheet.update { it.copy(vetName = v) } }
    fun onHealthCostChanged(v: String) {
        _healthSheet.update { it.copy(cost = v.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun onSaveHealthEvent() {
        val s = _healthSheet.value
        _healthSheet.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = HealthRecord(
                animalId = animalId,
                farmId = farmId,
                eventDate = s.eventDate,
                eventType = s.eventType,
                symptoms = s.selectedSymptoms.toList(),
                diagnosis = s.diagnosis.takeIf { it.isNotBlank() },
                severity = s.severity,
                medicineName = s.medicineName.takeIf { it.isNotBlank() },
                medicineDose = s.medicineDose.takeIf { it.isNotBlank() },
                vetName = s.vetName.takeIf { it.isNotBlank() },
                cost = s.cost.toDoubleOrNull()
            )
            healthRepository.saveHealthRecord(record)
                .onSuccess {
                    _events.send(AnimalDetailEvent.HealthSaved(record.diagnosis))
                    _healthSheet.value = QuickHealthEventState(eventDate = today)
                }
                .onFailure { e ->
                    _healthSheet.update { it.copy(isSaving = false) }
                    _events.send(AnimalDetailEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }
}
