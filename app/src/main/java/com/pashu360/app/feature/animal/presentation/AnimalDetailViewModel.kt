package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.feature.animal.domain.usecase.GetAnimalByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnimalDetailViewModel @Inject constructor(
    private val getAnimalByIdUseCase: GetAnimalByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animalId: String = checkNotNull(savedStateHandle["animalId"])

    val animal: StateFlow<Animal?> = getAnimalByIdUseCase(animalId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )
}
