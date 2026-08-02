package com.pashu360.app.feature.breeding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.core.domain.model.BreedingRecord
import com.pashu360.app.core.domain.model.BreedingRecordDetail
import com.pashu360.app.core.domain.model.BreedingType
import com.pashu360.app.core.domain.model.CalvingOutcome
import com.pashu360.app.core.domain.model.ConceptionStatus
import com.pashu360.app.core.domain.model.Gender
import com.pashu360.app.core.domain.model.HeatIntensity
import com.pashu360.app.core.domain.model.HeatRecord
import com.pashu360.app.core.domain.model.HeatRecordDetail
import com.pashu360.app.core.domain.model.PdMethod
import com.pashu360.app.core.domain.model.PregnancyDetail
import com.pashu360.app.core.domain.model.PregnancyRecord
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.breeding.domain.repository.BreedingRepository
import com.pashu360.app.feature.notifications.system.AlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

data class BreedingUiState(
    val today: LocalDate,
    val animals: List<Animal> = emptyList(),
    val heatRecords: List<HeatRecordDetail> = emptyList(),
    val breedingRecords: List<BreedingRecordDetail> = emptyList(),
    val awaitingPd: List<BreedingRecordDetail> = emptyList(),
    val activePregnancies: List<PregnancyDetail> = emptyList(),
    val completedPregnancies: List<PregnancyDetail> = emptyList(),
    val activePregnancyCount: Int = 0,
    val calvingDueThisMonth: Int = 0,
    val isLoading: Boolean = true
)

// ── FORM STATES ──────────────────────────
data class HeatFormState(
    val show: Boolean = false,
    val animalId: String = "",
    val detectionDate: LocalDate,
    val selectedSymptoms: Set<String> = emptySet(),
    val intensity: HeatIntensity = HeatIntensity.MEDIUM,
    val detectedBy: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = animalId.isNotBlank()
}

data class BreedingFormState(
    val show: Boolean = false,
    val animalId: String = "",
    val heatRecordId: String? = null,
    val breedingType: BreedingType = BreedingType.AI,
    val breedingDate: LocalDate,
    val bullName: String = "",
    val semenBatch: String = "",
    val aiTechnician: String = "",
    val cost: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = animalId.isNotBlank()
}

data class PregnancyFormState(
    val show: Boolean = false,
    val animalId: String = "",
    val breedingRecordId: String? = null,
    val confirmationDate: LocalDate,
    val pdMethod: PdMethod = PdMethod.RECTAL_PALPATION,
    val breedingDateForCalc: LocalDate? = null,   // used to auto-calc calving
    val expectedCalvingDate: LocalDate,
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = animalId.isNotBlank()
}

data class CalvingFormState(
    val show: Boolean = false,
    val pregnancyId: String = "",
    val motherName: String = "",
    val actualDate: LocalDate,
    val difficulty: Int = 1,
    val outcome: CalvingOutcome = CalvingOutcome.LIVE_CALF,
    val calvingNotes: String = "",
    val createCalf: Boolean = true,
    val calfTagId: String = "",
    val calfName: String = "",
    val calfGender: Gender = Gender.FEMALE,
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = pregnancyId.isNotBlank() &&
                (!createCalf || calfTagId.isNotBlank())
}

sealed class BreedingEvent {
    data class Saved(val message: String) : BreedingEvent()
    data class ShowError(val message: String) : BreedingEvent()
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class BreedingViewModel @Inject constructor(
    private val repository: BreedingRepository,
    private val animalRepository: AnimalRepository,
    private val alertScheduler: AlertScheduler,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val farmId get() = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _events = Channel<BreedingEvent>()
    val events = _events.receiveAsFlow()

    private val _heatForm = MutableStateFlow(HeatFormState(detectionDate = today))
    val heatForm: StateFlow<HeatFormState> = _heatForm.asStateFlow()

    private val _breedingForm = MutableStateFlow(BreedingFormState(breedingDate = today))
    val breedingForm: StateFlow<BreedingFormState> = _breedingForm.asStateFlow()

    private val _pregnancyForm = MutableStateFlow(
        PregnancyFormState(
            confirmationDate = today,
            expectedCalvingDate = today.plus(DatePeriod(days = 280))
        )
    )
    val pregnancyForm: StateFlow<PregnancyFormState> = _pregnancyForm.asStateFlow()

    private val _calvingForm = MutableStateFlow(CalvingFormState(actualDate = today))
    val calvingForm: StateFlow<CalvingFormState> = _calvingForm.asStateFlow()

    // Compose all flows into UI state
    private val animalsFlow = animalRepository.observeAnimals(farmId, AnimalFilter.ACTIVE)

    private val groupA = combine(
        animalsFlow,
        repository.observeHeatRecords(farmId),
        repository.observeBreedingRecords(farmId),
        repository.observeAwaitingPd(farmId),
        repository.observeActivePregnancies(farmId)
    ) { animals, heat, breeding, awaitingPd, pregnancies ->
        Quintuple(animals, heat, breeding, awaitingPd, pregnancies)
    }

    private val groupB = combine(
        repository.observeCompletedPregnancies(farmId),
        repository.countActivePregnancies(farmId),
        repository.countCalvingDueThisMonth(farmId, today)
    ) { completed, activeCount, dueThisMonth ->
        Triple(completed, activeCount, dueThisMonth)
    }

    val uiState: StateFlow<BreedingUiState> = combine(groupA, groupB) { a, b ->
        BreedingUiState(
            today = today,
            animals = a.first,
            heatRecords = a.second,
            breedingRecords = a.third,
            awaitingPd = a.fourth,
            activePregnancies = a.fifth,
            completedPregnancies = b.first,
            activePregnancyCount = b.second,
            calvingDueThisMonth = b.third,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BreedingUiState(today = today)
    )

    // ── HEAT FORM ──────────────────────────
    fun openHeatForm() { _heatForm.value = HeatFormState(show = true, detectionDate = today) }
    fun closeHeatForm() { _heatForm.value = HeatFormState(detectionDate = today) }
    fun onHeatAnimalChanged(id: String) { _heatForm.update { it.copy(animalId = id) } }
    fun onHeatSymptomToggled(s: String) {
        _heatForm.update { st ->
            val next = if (s in st.selectedSymptoms) st.selectedSymptoms - s
                       else st.selectedSymptoms + s
            st.copy(selectedSymptoms = next)
        }
    }
    fun onHeatIntensityChanged(i: HeatIntensity) { _heatForm.update { it.copy(intensity = i) } }
    fun onHeatDateChanged(d: LocalDate) { _heatForm.update { it.copy(detectionDate = d) } }
    fun onHeatDetectedByChanged(v: String) { _heatForm.update { it.copy(detectedBy = v) } }
    fun onHeatNotesChanged(v: String) { _heatForm.update { it.copy(notes = v) } }

    fun saveHeat() {
        val s = _heatForm.value
        if (!s.isValid) return
        _heatForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = HeatRecord(
                animalId = s.animalId, farmId = farmId,
                detectionDate = s.detectionDate,
                symptoms = s.selectedSymptoms.toList(),
                intensity = s.intensity,
                detectedBy = s.detectedBy.takeIf { it.isNotBlank() },
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.addHeatRecord(record)
                .onSuccess {
                    alertScheduler.scanNow()
                    _events.send(BreedingEvent.Saved("Heat recorded"))
                    closeHeatForm()
                }
                .onFailure {
                    _heatForm.update { it.copy(isSaving = false) }
                    _events.send(BreedingEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    // ── BREEDING FORM ──────────────────────
    fun openBreedingForm(preAnimalId: String = "", heatId: String? = null) {
        _breedingForm.value = BreedingFormState(
            show = true,
            animalId = preAnimalId,
            heatRecordId = heatId,
            breedingDate = today
        )
    }
    fun closeBreedingForm() { _breedingForm.value = BreedingFormState(breedingDate = today) }
    fun onBreedingAnimalChanged(id: String) { _breedingForm.update { it.copy(animalId = id) } }
    fun onBreedingTypeChanged(t: BreedingType) { _breedingForm.update { it.copy(breedingType = t) } }
    fun onBreedingDateChanged(d: LocalDate) { _breedingForm.update { it.copy(breedingDate = d) } }
    fun onBullNameChanged(v: String) { _breedingForm.update { it.copy(bullName = v) } }
    fun onSemenBatchChanged(v: String) { _breedingForm.update { it.copy(semenBatch = v) } }
    fun onAiTechnicianChanged(v: String) { _breedingForm.update { it.copy(aiTechnician = v) } }
    fun onBreedingCostChanged(v: String) {
        _breedingForm.update { it.copy(cost = v.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun onBreedingNotesChanged(v: String) { _breedingForm.update { it.copy(notes = v) } }

    fun saveBreeding() {
        val s = _breedingForm.value
        if (!s.isValid) return
        _breedingForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = BreedingRecord(
                animalId = s.animalId, farmId = farmId,
                heatRecordId = s.heatRecordId,
                breedingType = s.breedingType,
                breedingDate = s.breedingDate,
                bullName = s.bullName.takeIf { it.isNotBlank() },
                semenBatch = s.semenBatch.takeIf { it.isNotBlank() },
                aiTechnician = s.aiTechnician.takeIf { it.isNotBlank() },
                cost = s.cost.toDoubleOrNull(),
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.addBreedingRecord(record)
                .onSuccess {
                    alertScheduler.scanNow()
                    _events.send(BreedingEvent.Saved("Breeding recorded"))
                    closeBreedingForm()
                }
                .onFailure {
                    _breedingForm.update { it.copy(isSaving = false) }
                    _events.send(BreedingEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    fun markConceptionStatus(breedingId: String, status: ConceptionStatus) {
        viewModelScope.launch {
            repository.setConceptionStatus(breedingId, status)
                .onSuccess {
                    if (status == ConceptionStatus.CONFIRMED) {
                        _events.send(BreedingEvent.Saved("✅ Conception confirmed — remember to add pregnancy record"))
                    }
                }
        }
    }

    // ── PREGNANCY FORM ─────────────────────
    fun openPregnancyForm(preAnimalId: String = "", breedingId: String? = null, breedingDate: LocalDate? = null) {
        val expected = breedingDate?.plus(DatePeriod(days = 280)) ?: today.plus(DatePeriod(days = 280))
        _pregnancyForm.value = PregnancyFormState(
            show = true,
            animalId = preAnimalId,
            breedingRecordId = breedingId,
            confirmationDate = today,
            breedingDateForCalc = breedingDate,
            expectedCalvingDate = expected
        )
    }
    fun closePregnancyForm() {
        _pregnancyForm.value = PregnancyFormState(
            confirmationDate = today,
            expectedCalvingDate = today.plus(DatePeriod(days = 280))
        )
    }
    fun onPregnancyAnimalChanged(id: String) { _pregnancyForm.update { it.copy(animalId = id) } }
    fun onPdMethodChanged(m: PdMethod) { _pregnancyForm.update { it.copy(pdMethod = m) } }
    fun onExpectedCalvingChanged(d: LocalDate) { _pregnancyForm.update { it.copy(expectedCalvingDate = d) } }
    fun onPregnancyNotesChanged(v: String) { _pregnancyForm.update { it.copy(notes = v) } }

    fun savePregnancy() {
        val s = _pregnancyForm.value
        if (!s.isValid) return
        _pregnancyForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = PregnancyRecord(
                animalId = s.animalId, farmId = farmId,
                breedingRecordId = s.breedingRecordId,
                confirmationDate = s.confirmationDate,
                pdMethod = s.pdMethod,
                expectedCalvingDate = s.expectedCalvingDate,
                dryPeriodStart = s.expectedCalvingDate.plus(DatePeriod(days = -60)),
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.addPregnancyRecord(record)
                .onSuccess {
                    alertScheduler.scanNow()
                    _events.send(BreedingEvent.Saved("Pregnancy confirmed"))
                    closePregnancyForm()
                }
                .onFailure {
                    _pregnancyForm.update { it.copy(isSaving = false) }
                    _events.send(BreedingEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    // ── CALVING FORM ───────────────────────
    fun openCalvingForm(pregnancyId: String, motherName: String) {
        _calvingForm.value = CalvingFormState(
            show = true,
            pregnancyId = pregnancyId,
            motherName = motherName,
            actualDate = today
        )
    }
    fun closeCalvingForm() { _calvingForm.value = CalvingFormState(actualDate = today) }
    fun onCalvingDifficultyChanged(v: Int) { _calvingForm.update { it.copy(difficulty = v) } }
    fun onCalvingOutcomeChanged(o: CalvingOutcome) { _calvingForm.update { it.copy(outcome = o) } }
    fun onCalvingNotesChanged(v: String) { _calvingForm.update { it.copy(calvingNotes = v) } }
    fun onCreateCalfToggled(v: Boolean) { _calvingForm.update { it.copy(createCalf = v) } }
    fun onCalfTagChanged(v: String) { _calvingForm.update { it.copy(calfTagId = v) } }
    fun onCalfNameChanged(v: String) { _calvingForm.update { it.copy(calfName = v) } }
    fun onCalfGenderChanged(g: Gender) { _calvingForm.update { it.copy(calfGender = g) } }

    fun saveCalving() {
        val s = _calvingForm.value
        if (!s.isValid) return
        _calvingForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            // Build calf animal if requested and outcome allows it
            val calfAnimal = if (s.createCalf && s.outcome == CalvingOutcome.LIVE_CALF) {
                Animal(
                    farmId = farmId,
                    tagId = s.calfTagId,
                    name = s.calfName.takeIf { it.isNotBlank() },
                    gender = s.calfGender,
                    dob = s.actualDate,
                    source = "Born on farm"
                )
            } else null

            repository.recordCalving(
                pregnancyId = s.pregnancyId,
                actualDate = s.actualDate,
                difficulty = s.difficulty,
                outcome = s.outcome,
                notes = s.calvingNotes.takeIf { it.isNotBlank() },
                createCalfAnimal = calfAnimal
            ).onSuccess {
                _events.send(BreedingEvent.Saved(
                    if (calfAnimal != null) "🐄 Calving recorded • calf added"
                    else "Calving recorded"
                ))
                closeCalvingForm()
            }.onFailure {
                _calvingForm.update { it.copy(isSaving = false) }
                _events.send(BreedingEvent.ShowError(it.message ?: "Failed"))
            }
        }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )
}
