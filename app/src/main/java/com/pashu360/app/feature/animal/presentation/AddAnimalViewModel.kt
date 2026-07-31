package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.Gender
import com.pashu360.app.feature.animal.domain.usecase.AddAnimalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class AddAnimalUiState(
    val tagId: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: Gender = Gender.FEMALE,
    val dob: LocalDate? = null,
    val weight: String = "",
    val purchasePrice: String = "",
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = tagId.isNotBlank()
}

sealed class AddAnimalEvent {
    data object Saved : AddAnimalEvent()
    data class ShowError(val message: String) : AddAnimalEvent()
}

@HiltViewModel
class AddAnimalViewModel @Inject constructor(
    private val addAnimalUseCase: AddAnimalUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddAnimalUiState(tagId = generateSuggestedTag())
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddAnimalEvent>()
    val events = _events.receiveAsFlow()

    fun onTagIdChanged(value: String) { _uiState.update { it.copy(tagId = value.trim()) } }
    fun onNameChanged(value: String) { _uiState.update { it.copy(name = value) } }
    fun onBreedChanged(value: String) { _uiState.update { it.copy(breed = value) } }
    fun onGenderChanged(value: Gender) { _uiState.update { it.copy(gender = value) } }
    fun onDobChanged(value: LocalDate?) { _uiState.update { it.copy(dob = value) } }
    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weight = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun onPurchasePriceChanged(value: String) {
        _uiState.update { it.copy(purchasePrice = value.filter { c -> c.isDigit() }) }
    }
    fun onNotesChanged(value: String) { _uiState.update { it.copy(notes = value) } }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(error = "Tag ID is required") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val animal = Animal(
                farmId = sessionStore.getActiveFarmId(),
                tagId = state.tagId,
                name = state.name.takeIf { it.isNotBlank() },
                breed = state.breed.takeIf { it.isNotBlank() },
                gender = state.gender,
                dob = state.dob,
                weightKg = state.weight.toDoubleOrNull(),
                purchasePrice = state.purchasePrice.toDoubleOrNull(),
                notes = state.notes.takeIf { it.isNotBlank() },
                qrCodeData = state.tagId  // QR content = tag id for now
            )

            addAnimalUseCase(animal)
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

    private fun generateSuggestedTag(): String {
        // Simple suggestion: T + last 4 digits of current time
        val nowMillis = System.currentTimeMillis()
        return "T" + nowMillis.toString().takeLast(4)
    }
}
