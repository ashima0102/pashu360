package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalStatus
import com.pashu360.app.core.domain.model.Gender
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Loads an existing Animal and lets the farmer edit every field.
 * Reuses AddAnimalUiState + AddAnimalEvent so the UI can share the
 * AddAnimalScreen composable when possible.
 */
@HiltViewModel
class EditAnimalViewModel @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val sessionStore: SessionStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animalId: String = checkNotNull(savedStateHandle["animalId"])
    private var originalAnimal: Animal? = null

    private val _uiState = MutableStateFlow(AddAnimalUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddAnimalEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            animalRepository.getAnimalById(animalId)?.let { a ->
                originalAnimal = a
                _uiState.value = AddAnimalUiState(
                    tagId = a.tagId,
                    name = a.name.orEmpty(),
                    breed = a.breed.orEmpty(),
                    gender = a.gender,
                    status = a.status,
                    dob = a.dob,
                    weight = a.weightKg?.toString().orEmpty(),
                    purchasePrice = a.purchasePrice?.toString().orEmpty(),
                    notes = a.notes.orEmpty()
                )
            } ?: run {
                _events.send(AddAnimalEvent.ShowError("Animal not found"))
            }
        }
    }

    fun onTagIdChanged(v: String) { _uiState.update { it.copy(tagId = v.trim()) } }
    fun onNameChanged(v: String) { _uiState.update { it.copy(name = v) } }
    fun onBreedChanged(v: String) { _uiState.update { it.copy(breed = v) } }
    fun onGenderChanged(v: Gender) { _uiState.update { it.copy(gender = v) } }
    fun onStatusChanged(v: AnimalStatus) { _uiState.update { it.copy(status = v) } }
    fun onDobChanged(v: LocalDate?) { _uiState.update { it.copy(dob = v) } }
    fun onWeightChanged(v: String) {
        _uiState.update { it.copy(weight = v.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun onPurchasePriceChanged(v: String) {
        _uiState.update { it.copy(purchasePrice = v.filter { c -> c.isDigit() }) }
    }
    fun onNotesChanged(v: String) { _uiState.update { it.copy(notes = v) } }

    fun onSubmit() {
        val s = _uiState.value
        val original = originalAnimal ?: return
        if (!s.isValid) {
            _uiState.update { it.copy(error = "Tag ID is required") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val updated = original.copy(
                tagId = s.tagId,
                name = s.name.takeIf { it.isNotBlank() },
                breed = s.breed.takeIf { it.isNotBlank() },
                gender = s.gender,
                status = s.status,
                dob = s.dob,
                weightKg = s.weight.toDoubleOrNull(),
                purchasePrice = s.purchasePrice.toDoubleOrNull(),
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            animalRepository.updateAnimal(updated)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(AddAnimalEvent.Saved)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message ?: "Could not save")
                    }
                    _events.send(AddAnimalEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }
}
