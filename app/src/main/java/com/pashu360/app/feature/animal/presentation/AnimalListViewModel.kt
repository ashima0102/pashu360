package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.domain.usecase.GetAnimalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AnimalListUiState(
    val animals: List<Animal> = emptyList(),
    val filter: AnimalFilter = AnimalFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val filteredAnimals: List<Animal>
        get() = if (searchQuery.isBlank()) animals
                else animals.filter {
                    it.tagId.contains(searchQuery, ignoreCase = true) ||
                    (it.name?.contains(searchQuery, ignoreCase = true) == true)
                }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimalListViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _filter = MutableStateFlow(AnimalFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    private val animalsFlow = _filter.flatMapLatest { filter ->
        getAnimalsUseCase(sessionStore.getActiveFarmId(), filter)
    }

    val uiState: StateFlow<AnimalListUiState> = combine(
        animalsFlow, _filter, _searchQuery
    ) { animals, filter, query ->
        AnimalListUiState(
            animals = animals,
            filter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AnimalListUiState()
    )

    fun onFilterChanged(filter: AnimalFilter) {
        _filter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
