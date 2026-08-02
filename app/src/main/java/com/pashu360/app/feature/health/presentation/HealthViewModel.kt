package com.pashu360.app.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.core.domain.model.HealthEventType
import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Severity
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VaccineTemplate
import com.pashu360.app.core.domain.model.VetContact
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import com.pashu360.app.feature.notifications.system.AlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

data class HealthUiState(
    val records: List<HealthRecord> = emptyList(),
    val vaccinations: List<Vaccination> = emptyList(),
    val vetContacts: List<VetContact> = emptyList(),
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val activeIssueCount: Int = 0,
    val today: LocalDate,
    val animals: List<Animal> = emptyList(),
    val isLoading: Boolean = true
)

/** State for the Add Vaccination bottom sheet. */
data class VaccinationFormState(
    val show: Boolean = false,
    val animalId: String = "",
    val template: VaccineTemplate? = null,
    val customVaccineName: String = "",
    val administeredDate: LocalDate,
    val nextDueDate: LocalDate? = null,
    val batchNumber: String = "",
    val administeredBy: String = "",
    val cost: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val vaccineName: String
        get() = template?.name?.takeIf { it.isNotBlank() } ?: customVaccineName.trim()

    val isValid: Boolean
        get() = animalId.isNotBlank() && vaccineName.isNotBlank()
}

/** State for the Add Health Event bottom sheet. */
data class HealthEventFormState(
    val show: Boolean = false,
    val animalId: String = "",
    val eventType: HealthEventType = HealthEventType.CHECKUP,
    val eventDate: LocalDate,
    val selectedSymptoms: Set<String> = emptySet(),
    val severity: Severity = Severity.MILD,
    val diagnosis: String = "",
    val medicineName: String = "",
    val medicineDose: String = "",
    val vetName: String = "",
    val cost: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = animalId.isNotBlank()
}

/** State for the Add Vet Contact bottom sheet. */
data class VetContactFormState(
    val show: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val specialty: String = "",
    val clinic: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && phone.length >= 10
}

sealed class HealthEvent {
    data class Saved(val message: String) : HealthEvent()
    data class ShowError(val message: String) : HealthEvent()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class HealthViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val animalRepository: AnimalRepository,
    private val alertScheduler: AlertScheduler,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val farmId get() = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _events = Channel<HealthEvent>()
    val events = _events.receiveAsFlow()

    private val _vaccinationForm = MutableStateFlow(
        VaccinationFormState(administeredDate = today, nextDueDate = null)
    )
    val vaccinationForm: StateFlow<VaccinationFormState> = _vaccinationForm.asStateFlow()

    private val _healthEventForm = MutableStateFlow(
        HealthEventFormState(eventDate = today)
    )
    val healthEventForm: StateFlow<HealthEventFormState> = _healthEventForm.asStateFlow()

    private val _vetContactForm = MutableStateFlow(VetContactFormState())
    val vetContactForm: StateFlow<VetContactFormState> = _vetContactForm.asStateFlow()

    // ── Group A (5 flows) — same as before ──────────────
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

    // Animals for the picker
    private val animalsFlow = animalRepository.observeAnimals(farmId, AnimalFilter.ACTIVE)

    val uiState: StateFlow<HealthUiState> = combine(
        groupA, activeCountFlow, animalsFlow
    ) { a, active, animals ->
        HealthUiState(
            records = a.first,
            vaccinations = a.second,
            vetContacts = a.third,
            overdueCount = a.fourth,
            dueSoonCount = a.fifth,
            activeIssueCount = active,
            animals = animals,
            today = today,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HealthUiState(today = today)
    )

    // ─────────────────────────────────────────────────────
    // VACCINATION FORM
    // ─────────────────────────────────────────────────────
    fun openVaccinationForm(preSelectAnimalId: String = "") {
        _vaccinationForm.value = VaccinationFormState(
            show = true,
            animalId = preSelectAnimalId,
            administeredDate = today,
            nextDueDate = null
        )
    }

    fun closeVaccinationForm() {
        _vaccinationForm.value = VaccinationFormState(administeredDate = today)
    }

    fun onVaccinationAnimalChanged(id: String) {
        _vaccinationForm.update { it.copy(animalId = id) }
    }

    fun onVaccinationTemplateChanged(template: VaccineTemplate?) {
        _vaccinationForm.update { s ->
            val next = template?.intervalDays?.let { s.administeredDate.plus(DatePeriod(days = it)) }
            s.copy(
                template = template,
                customVaccineName = "",
                nextDueDate = next
            )
        }
    }

    fun onCustomVaccineNameChanged(v: String) {
        _vaccinationForm.update { it.copy(customVaccineName = v, template = null) }
    }

    fun onVaccinationAdministeredDateChanged(d: LocalDate) {
        _vaccinationForm.update { s ->
            // Recompute next due if a template is set
            val nextDue = s.template?.intervalDays?.let { d.plus(DatePeriod(days = it)) }
                ?: s.nextDueDate
            s.copy(administeredDate = d, nextDueDate = nextDue)
        }
    }

    fun onVaccinationNextDueDateChanged(d: LocalDate?) {
        _vaccinationForm.update { it.copy(nextDueDate = d) }
    }

    fun onVaccinationFieldChanged(field: VaccinationField, value: String) {
        _vaccinationForm.update {
            when (field) {
                VaccinationField.BATCH -> it.copy(batchNumber = value)
                VaccinationField.ADMIN_BY -> it.copy(administeredBy = value)
                VaccinationField.COST -> it.copy(cost = value.filter { c -> c.isDigit() || c == '.' })
                VaccinationField.NOTES -> it.copy(notes = value)
            }
        }
    }

    fun saveVaccination() {
        val s = _vaccinationForm.value
        if (!s.isValid) return
        _vaccinationForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val vaccination = Vaccination(
                animalId = s.animalId,
                farmId = farmId,
                vaccineName = s.vaccineName,
                diseaseTarget = s.template?.disease,
                administeredDate = s.administeredDate,
                nextDueDate = s.nextDueDate,
                batchNumber = s.batchNumber.takeIf { it.isNotBlank() },
                administeredBy = s.administeredBy.takeIf { it.isNotBlank() },
                cost = s.cost.toDoubleOrNull(),
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.saveVaccination(vaccination)
                .onSuccess {
                    alertScheduler.scanNow()
                    _events.send(HealthEvent.Saved("Vaccination recorded"))
                    closeVaccinationForm()
                }
                .onFailure {
                    _vaccinationForm.update { it.copy(isSaving = false) }
                    _events.send(HealthEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    // ─────────────────────────────────────────────────────
    // HEALTH EVENT FORM
    // ─────────────────────────────────────────────────────
    fun openHealthEventForm(preSelectAnimalId: String = "") {
        _healthEventForm.value = HealthEventFormState(
            show = true,
            animalId = preSelectAnimalId,
            eventDate = today
        )
    }

    fun closeHealthEventForm() {
        _healthEventForm.value = HealthEventFormState(eventDate = today)
    }

    fun onHealthAnimalChanged(id: String) {
        _healthEventForm.update { it.copy(animalId = id) }
    }

    fun onEventTypeChanged(type: HealthEventType) {
        _healthEventForm.update { it.copy(eventType = type) }
    }

    fun onSeverityChanged(severity: Severity) {
        _healthEventForm.update { it.copy(severity = severity) }
    }

    fun onSymptomToggled(symptom: String) {
        _healthEventForm.update { s ->
            val next = if (symptom in s.selectedSymptoms) s.selectedSymptoms - symptom
                       else s.selectedSymptoms + symptom
            s.copy(selectedSymptoms = next)
        }
    }

    fun onHealthFieldChanged(field: HealthField, value: String) {
        _healthEventForm.update {
            when (field) {
                HealthField.DIAGNOSIS -> it.copy(diagnosis = value)
                HealthField.MEDICINE_NAME -> it.copy(medicineName = value)
                HealthField.MEDICINE_DOSE -> it.copy(medicineDose = value)
                HealthField.VET_NAME -> it.copy(vetName = value)
                HealthField.COST -> it.copy(cost = value.filter { c -> c.isDigit() || c == '.' })
                HealthField.NOTES -> it.copy(notes = value)
            }
        }
    }

    fun saveHealthEvent() {
        val s = _healthEventForm.value
        if (!s.isValid) return
        _healthEventForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = HealthRecord(
                animalId = s.animalId,
                farmId = farmId,
                eventDate = s.eventDate,
                eventType = s.eventType,
                symptoms = s.selectedSymptoms.toList(),
                diagnosis = s.diagnosis.takeIf { it.isNotBlank() },
                severity = s.severity,
                medicineName = s.medicineName.takeIf { it.isNotBlank() },
                medicineDose = s.medicineDose.takeIf { it.isNotBlank() },
                vetName = s.vetName.takeIf { it.isNotBlank() },
                cost = s.cost.toDoubleOrNull(),
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.saveHealthRecord(record)
                .onSuccess {
                    _events.send(HealthEvent.Saved("Health event recorded"))
                    closeHealthEventForm()
                }
                .onFailure {
                    _healthEventForm.update { it.copy(isSaving = false) }
                    _events.send(HealthEvent.ShowError(it.message ?: "Failed"))
                }
        }
    }

    // ─────────────────────────────────────────────────────
    // VET CONTACT FORM
    // ─────────────────────────────────────────────────────
    fun openVetContactForm() {
        _vetContactForm.value = VetContactFormState(show = true)
    }

    fun closeVetContactForm() {
        _vetContactForm.value = VetContactFormState()
    }

    fun onVetContactFieldChanged(field: VetField, value: String) {
        _vetContactForm.update {
            when (field) {
                VetField.NAME -> it.copy(name = value)
                VetField.PHONE -> it.copy(phone = value.filter { c -> c.isDigit() }.take(15))
                VetField.SPECIALTY -> it.copy(specialty = value)
                VetField.CLINIC -> it.copy(clinic = value)
                VetField.NOTES -> it.copy(notes = value)
            }
        }
    }

    fun saveVetContact() {
        val s = _vetContactForm.value
        if (!s.isValid) return
        _vetContactForm.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val contact = VetContact(
                farmId = farmId,
                name = s.name.trim(),
                phone = s.phone,
                specialty = s.specialty.takeIf { it.isNotBlank() },
                clinic = s.clinic.takeIf { it.isNotBlank() },
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            repository.saveVetContact(contact)
                .onSuccess {
                    _events.send(HealthEvent.Saved("Vet contact saved"))
                    closeVetContactForm()
                }
                .onFailure {
                    _vetContactForm.update { it.copy(isSaving = false) }
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

enum class VaccinationField { BATCH, ADMIN_BY, COST, NOTES }
enum class HealthField { DIAGNOSIS, MEDICINE_NAME, MEDICINE_DOSE, VET_NAME, COST, NOTES }
enum class VetField { NAME, PHONE, SPECIALTY, CLINIC, NOTES }
