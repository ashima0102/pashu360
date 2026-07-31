# State Management
## Smart Dairy Farm Management System

---

## State Architecture Overview

```
                    ┌──────────────────────┐
                    │   Compose Screen     │
                    │                      │
                    │  collectAsState()    │
                    └──────────┬───────────┘
                               │ observes
                    ┌──────────▼───────────┐
                    │     ViewModel        │
                    │                      │
                    │  MutableStateFlow    │
                    │  Channel<UiEvent>    │
                    │  viewModelScope      │
                    └──────────┬───────────┘
                               │ calls
                    ┌──────────▼───────────┐
                    │     UseCase          │
                    │                      │
                    │  Returns:            │
                    │  Flow<Resource<T>>   │ (streaming)
                    │  suspend fun         │ (one-shot)
                    └──────────────────────┘
```

---

## Global App State (SessionStore)

```kotlin
// Single source of truth for auth session and active farm
@Singleton
class SessionStore @Inject constructor(
    private val supabase: SupabaseClient,
    private val userPrefs: UserPreferencesDataStore
) {
    // Auth state
    val currentUser: Flow<User?> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.toUser()
            else -> null
        }
    }

    // Active farm — persisted between sessions
    val activeFarmId: Flow<String?> = flow {
        emit(userPrefs.getActiveFarmId())
    }

    fun setActiveFarm(farmId: String) {
        userPrefs.setActiveFarmId(farmId)
    }
}
```

---

## UiState + UiEvent Pattern

```kotlin
// Pattern: Each screen has its own UiState + UiEvent
// UiState — what to render (held in StateFlow)
// UiEvent — one-time side effects (held in Channel)

// Animal List Example
data class AnimalListUiState(
    val animals: List<Animal> = emptyList(),
    val filteredAnimals: List<Animal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: AnimalFilter = AnimalFilter.ALL,
    val searchQuery: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val unreadAlertCount: Int = 0
)

sealed class AnimalListUiEvent {
    data class NavigateToDetail(val animalId: String) : AnimalListUiEvent()
    data class NavigateToEdit(val animalId: String) : AnimalListUiEvent()
    object NavigateToAddAnimal : AnimalListUiEvent()
    object NavigateToQrScanner : AnimalListUiEvent()
    data class ShowSnackbar(val message: String) : AnimalListUiEvent()
    data class AnimalStatusChanged(val animal: Animal, val newStatus: AnimalStatus) : AnimalListUiEvent()
}
```

---

## ViewModel State Management

```kotlin
@HiltViewModel
class AnimalListViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val searchAnimalsUseCase: SearchAnimalsUseCase,
    private val updateAnimalStatusUseCase: UpdateAnimalStatusUseCase,
    private val syncStatusRepository: SyncStatusRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimalListUiState())
    val uiState: StateFlow<AnimalListUiState> = _uiState.asStateFlow()

    private val _events = Channel<AnimalListUiEvent>(Channel.BUFFERED)
    val events: Flow<AnimalListUiEvent> = _events.receiveAsFlow()

    private var allAnimals: List<Animal> = emptyList()

    init {
        observeAnimals()
        observeSyncStatus()
    }

    private fun observeAnimals() {
        viewModelScope.launch {
            sessionStore.activeFarmId.filterNotNull().collect { farmId ->
                getAnimalsUseCase(farmId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Resource.Success -> {
                            allAnimals = resource.data ?: emptyList()
                            _uiState.update { state ->
                                state.copy(
                                    animals = allAnimals,
                                    filteredAnimals = applyFilters(allAnimals, state),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> _uiState.update {
                            it.copy(isLoading = false, error = resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncStatusRepository.syncStatus.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    // ─── User Actions ─────────────────────────────────────────────────

    fun onFilterChanged(filter: AnimalFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredAnimals = applyFilters(allAnimals, state.copy(selectedFilter = filter))
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredAnimals = applyFilters(allAnimals, state.copy(searchQuery = query))
            )
        }
    }

    fun onAnimalClicked(animalId: String) {
        viewModelScope.launch { _events.send(AnimalListUiEvent.NavigateToDetail(animalId)) }
    }

    fun onAddAnimalClicked() {
        viewModelScope.launch { _events.send(AnimalListUiEvent.NavigateToAddAnimal) }
    }

    fun onMarkAnimalSold(animal: Animal, price: Double, buyer: String) {
        viewModelScope.launch {
            updateAnimalStatusUseCase(animal.id, AnimalStatus.SOLD, price, buyer)
                .onSuccess {
                    _events.send(AnimalListUiEvent.ShowSnackbar("${animal.displayName} marked as sold"))
                }
                .onFailure { e ->
                    _events.send(AnimalListUiEvent.ShowSnackbar("Failed: ${e.message}"))
                }
        }
    }

    // ─── Filter Logic ─────────────────────────────────────────────────

    private fun applyFilters(animals: List<Animal>, state: AnimalListUiState): List<Animal> {
        return animals
            .filter { animal ->
                when (state.selectedFilter) {
                    AnimalFilter.ALL -> true
                    AnimalFilter.ACTIVE -> animal.status == AnimalStatus.ACTIVE
                    AnimalFilter.PREGNANT -> animal.status == AnimalStatus.PREGNANT
                    AnimalFilter.SICK -> animal.status == AnimalStatus.SICK
                    AnimalFilter.DRY -> animal.status == AnimalStatus.DRY
                }
            }
            .filter { animal ->
                if (state.searchQuery.isBlank()) true
                else animal.displayName.contains(state.searchQuery, ignoreCase = true) ||
                     animal.tagId.contains(state.searchQuery, ignoreCase = true)
            }
    }
}
```

---

## Collecting State in Compose

```kotlin
@Composable
fun AnimalListScreen(
    viewModel: AnimalListViewModel = hiltViewModel(),
    navController: NavController
) {
    // Collect state safely (respects lifecycle — pauses when app is backgrounded)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AnimalListUiEvent.NavigateToDetail ->
                    navController.navigate(Screen.AnimalDetail.createRoute(event.animalId))
                is AnimalListUiEvent.NavigateToAddAnimal ->
                    navController.navigate(Screen.AddAnimal.route)
                is AnimalListUiEvent.NavigateToQrScanner ->
                    navController.navigate(Screen.QrScanner.route)
                is AnimalListUiEvent.ShowSnackbar -> {
                    // Show snackbar via scaffoldState
                }
                else -> {}
            }
        }
    }

    // UI renders based on uiState only
    // No logic in composables — all state transitions in ViewModel
}
```

---

## Form State Management

```kotlin
// Add Animal Form State
data class AddAnimalFormState(
    val tagId: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: Gender = Gender.FEMALE,
    val dob: LocalDate? = null,
    val weightKg: String = "",
    val barnId: String? = null,
    val photoUri: Uri? = null,
    val purchaseDate: LocalDate? = null,
    val purchasePrice: String = "",

    // Validation errors (null = no error)
    val tagIdError: String? = null,
    val genderError: String? = null,

    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)

// ViewModel handles validation before submit
fun onSubmit() {
    val state = _formState.value

    // Validate
    val tagIdError = when {
        state.tagId.isBlank() -> "Tag ID is required"
        state.tagId.length > 20 -> "Too long (max 20 characters)"
        !state.tagId.matches(Regex("[a-zA-Z0-9-]+")) -> "Only letters, numbers, hyphen allowed"
        else -> null
    }

    if (tagIdError != null) {
        _formState.update { it.copy(tagIdError = tagIdError) }
        return
    }

    // Submit
    viewModelScope.launch {
        _formState.update { it.copy(isSubmitting = true) }
        addAnimalUseCase(state.toAnimal())
            .onSuccess { _formState.update { it.copy(isSubmitting = false, isSuccess = true) } }
            .onFailure { e ->
                _formState.update { it.copy(isSubmitting = false,
                    tagIdError = if (e is SQLiteConstraintException) "Tag ID already exists" else null)
                }
            }
    }
}
```

---

## Shared State (Multiple Screens)

```kotlin
// Shared ViewModel scoped to the Animal Detail navigation backstack
// All tabs (Overview, Milk, Health, etc.) share the same animal data

@HiltViewModel
class AnimalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnimalByIdUseCase: GetAnimalByIdUseCase,
    private val getMilkHistoryUseCase: GetMilkHistoryUseCase,
    private val getVaccinationHistoryUseCase: GetVaccinationHistoryUseCase,
    private val getHealthHistoryUseCase: GetHealthHistoryUseCase
) : ViewModel() {

    private val animalId: String = checkNotNull(savedStateHandle["animalId"])

    // Animal data loaded once — shared across all tabs
    private val _animal = MutableStateFlow<Animal?>(null)
    val animal: StateFlow<Animal?> = _animal.asStateFlow()

    // Tab-specific data loaded lazily
    val milkHistory: Flow<List<MilkRecord>> = getMilkHistoryUseCase(animalId)
    val vaccinationHistory: Flow<List<VaccinationRecord>> = getVaccinationHistoryUseCase(animalId)
    val healthHistory: Flow<List<HealthEvent>> = getHealthHistoryUseCase(animalId)

    init {
        viewModelScope.launch {
            getAnimalByIdUseCase(animalId).collect { animal ->
                _animal.value = animal
            }
        }
    }
}
```

---

## StateFlow vs SharedFlow

| Use Case | Type | Why |
|---|---|---|
| UI state (screen contents) | `StateFlow` | Holds last value; new collectors get current state immediately |
| One-time events (navigation, snackbar) | `Channel` / `SharedFlow` | Doesn't replay on recomposition; consumed once |
| Data streams (animal list) | `Flow` from Room | Cold flow; only active when screen is visible |
| Realtime sync updates | `SharedFlow` | Broadcast to multiple collectors |

```kotlin
// DO: Use Channel for one-time events
private val _events = Channel<UiEvent>(Channel.BUFFERED)
val events = _events.receiveAsFlow()

// DON'T: Use StateFlow for navigation events
// (collector would re-navigate on every recomposition)
```
