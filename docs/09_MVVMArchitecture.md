# MVVM + Clean Architecture
## Smart Dairy Farm Management System

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                          │
│                                                                │
│  ┌──────────────────┐         ┌──────────────────────────┐    │
│  │  Compose Screen  │◄────────│       ViewModel          │    │
│  │                  │         │                          │    │
│  │  Observes:       │         │  Holds: UiState (Flow)   │    │
│  │  - uiState       │ emits   │  Emits: UiEvent          │    │
│  │  - events        │◄────────│  Calls: UseCases         │    │
│  │                  │         │  Scoped: viewModelScope  │    │
│  └──────────────────┘         └──────────┬───────────────┘    │
│                                          │ calls               │
└──────────────────────────────────────────┼────────────────────┘
                                           │
┌──────────────────────────────────────────┼────────────────────┐
│                    DOMAIN LAYER          │                     │
│                                          ▼                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                    Use Cases                             │ │
│  │  - Single responsibility                                 │ │
│  │  - Pure Kotlin (no Android imports)                      │ │
│  │  - Calls Repository interface                            │ │
│  │  - Returns Flow<Resource<T>> or suspend fun              │ │
│  └──────────────────────────┬───────────────────────────────┘ │
│                             │                                  │
│  ┌──────────────────────────▼───────────────────────────────┐ │
│  │              Repository Interfaces                       │ │
│  │  - Kotlin interfaces (no implementation here)            │ │
│  │  - Define what data operations are possible              │ │
│  └──────────────────────────┬───────────────────────────────┘ │
│                             │                                  │
│  ┌──────────────────────────▼───────────────────────────────┐ │
│  │                  Domain Models                           │ │
│  │  - Pure Kotlin data classes                              │ │
│  │  - No Room/Supabase annotations                          │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                           │
┌──────────────────────────────────────────┼────────────────────┐
│                    DATA LAYER            │                     │
│                                          ▼                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Repository Implementations                  │ │
│  │  - Implements domain Repository interface                │ │
│  │  - Coordinates between local and remote sources          │ │
│  │  - Writes to Room first, queues sync                     │ │
│  │  - Reads always from Room (offline safe)                 │ │
│  └───────────────────┬──────────────────────────────────────┘ │
│                      │                                         │
│      ┌───────────────┼────────────────┐                        │
│      ▼               ▼                ▼                        │
│  ┌───────┐     ┌──────────┐     ┌──────────────┐              │
│  │ Room  │     │Supabase  │     │ WorkManager  │              │
│  │ DAOs  │     │ Client   │     │ SyncWorker   │              │
│  │(local)│     │(remote)  │     │(background)  │              │
│  └───────┘     └──────────┘     └──────────────┘              │
└────────────────────────────────────────────────────────────────┘
```

---

## Layer Responsibilities

### Presentation Layer
- **Screen (Composable):** Renders UI based on `UiState`. Sends user intent events to ViewModel via function calls.
- **ViewModel:** Holds `UiState` as `StateFlow`. Calls UseCases. Maps domain models to UI models. Never references Android context except for navigation side effects.

### Domain Layer
- **UseCase:** One public function, one responsibility. `operator fun invoke()` pattern. Returns `Flow<Resource<T>>` for streaming, `suspend fun` for one-shot operations.
- **Repository Interface:** Defines available operations. Implementation lives in Data layer.
- **Domain Model:** Plain Kotlin `data class`. No framework dependencies.

### Data Layer
- **Repository Implementation:** Reads from Room (always fast/offline). Writes to Room first, then enqueues to SyncQueue. Maps between Entity ↔ Domain Model.
- **Room DAO:** SQLite access via Kotlin Flow for reactive updates.
- **Supabase Client:** Remote CRUD via Supabase Kotlin SDK.
- **SyncWorker:** WorkManager worker that processes SyncQueue items when network is available.

---

## UiState Pattern

```kotlin
// Generic UiState wrapper
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Feature-specific UiState example — Animals
data class AnimalListUiState(
    val animals: List<Animal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: AnimalStatus = AnimalStatus.ALL,
    val searchQuery: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class AnimalStatus { ALL, ACTIVE, PREGNANT, SICK, DRY, SOLD }
enum class SyncStatus { SYNCED, SYNCING, OFFLINE, ERROR }
```

---

## ViewModel Example — Animal List

```kotlin
@HiltViewModel
class AnimalListViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val searchAnimalsUseCase: SearchAnimalsUseCase,
    private val getFarmIdUseCase: GetActiveFarmIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimalListUiState())
    val uiState: StateFlow<AnimalListUiState> = _uiState.asStateFlow()

    private val _events = Channel<AnimalListEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeAnimals()
    }

    private fun observeAnimals() {
        viewModelScope.launch {
            val farmId = getFarmIdUseCase()
            getAnimalsUseCase(farmId)
                .collect { resource ->
                    _uiState.update { state ->
                        when (resource) {
                            is Resource.Loading -> state.copy(isLoading = true)
                            is Resource.Success -> state.copy(
                                animals = resource.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                            is Resource.Error -> state.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
        }
    }

    fun onFilterChanged(filter: AnimalStatus) {
        _uiState.update { it.copy(selectedFilter = filter) }
        // trigger re-query
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onAnimalClicked(animalId: String) {
        viewModelScope.launch {
            _events.send(AnimalListEvent.NavigateToDetail(animalId))
        }
    }
}

sealed class AnimalListEvent {
    data class NavigateToDetail(val animalId: String) : AnimalListEvent()
    data class ShowError(val message: String) : AnimalListEvent()
}
```

---

## UseCase Example

```kotlin
class GetAnimalsUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    operator fun invoke(farmId: String, status: AnimalStatus = AnimalStatus.ACTIVE): Flow<Resource<List<Animal>>> =
        animalRepository.getAnimals(farmId, status)
            .map { entities ->
                Resource.Success(entities)
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
}
```

---

## Repository Example

```kotlin
class AnimalRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao,
    private val animalRemoteSource: AnimalRemoteSource,
    private val syncQueueDao: SyncQueueDao,
    private val animalMapper: AnimalMapper
) : AnimalRepository {

    // READ: Always from Room (offline-safe, reactive)
    override fun getAnimals(farmId: String, status: AnimalStatus): Flow<List<Animal>> =
        animalDao.getActiveAnimals(farmId)
            .map { entities -> entities.map(animalMapper::toDomain) }

    // WRITE: Room first → SyncQueue → background sync
    override suspend fun addAnimal(animal: Animal): Result<Unit> = runCatching {
        val entity = animalMapper.toEntity(animal)
        animalDao.insertAnimal(entity)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                tableName = "animals",
                operation = "insert",
                recordId = entity.id,
                payloadJson = Json.encodeToString(entity)
            )
        )
    }

    override suspend fun updateAnimal(animal: Animal): Result<Unit> = runCatching {
        val entity = animalMapper.toEntity(animal.copy(updatedAt = LocalDateTime.now()))
        animalDao.updateAnimal(entity)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                tableName = "animals",
                operation = "update",
                recordId = entity.id,
                payloadJson = Json.encodeToString(entity)
            )
        )
    }
}
```

---

## Resource Sealed Class

```kotlin
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
```

---

## Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAnimalRepository(
        animalDao: AnimalDao,
        animalRemoteSource: AnimalRemoteSource,
        syncQueueDao: SyncQueueDao
    ): AnimalRepository = AnimalRepositoryImpl(animalDao, animalRemoteSource, syncQueueDao, AnimalMapper())

    @Provides
    @Singleton
    fun provideMilkRepository(
        milkDao: MilkRecordDao,
        syncQueueDao: SyncQueueDao
    ): MilkRepository = MilkRepositoryImpl(milkDao, syncQueueDao)

    // ... repeat for each feature
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideAnimalDao(db: AppDatabase): AnimalDao = db.animalDao()
    @Provides fun provideMilkDao(db: AppDatabase): MilkRecordDao = db.milkRecordDao()
    // ...
}
```

---

## Screen Example (Compose)

```kotlin
@Composable
fun AnimalListScreen(
    viewModel: AnimalListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AnimalListEvent.NavigateToDetail -> onNavigateToDetail(event.animalId)
                is AnimalListEvent.ShowError -> { /* Show snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = { AnimalListTopBar(onScanQr = { /* navigate to QR scanner */ }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Animal") },
                icon = { Icon(Icons.Default.Add, null) },
                onClick = { /* navigate to add animal */ }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search by name or tag...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            // Filter chips
            AnimalFilterChips(
                selected = uiState.selectedFilter,
                onSelected = viewModel::onFilterChanged
            )
            // Content
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorState(message = uiState.error!!)
                uiState.animals.isEmpty() -> EmptyState(message = "No animals found")
                else -> LazyColumn {
                    items(uiState.animals, key = { it.id }) { animal ->
                        AnimalCard(
                            animal = animal,
                            onClick = { viewModel.onAnimalClicked(animal.id) }
                        )
                    }
                }
            }
        }
    }
}
```
