# API / Data Layer Design
## Smart Dairy Farm Management System

---

## Data Flow Architecture

```
Compose Screen
    │  observes StateFlow<UiState>
    ▼
ViewModel
    │  calls UseCase
    ▼
UseCase (domain)
    │  calls Repository interface
    ▼
RepositoryImpl (data)
    │
    ├── READ  → Room DAO → returns Flow<List<Entity>>
    │                       mapped to Flow<List<DomainModel>>
    │
    └── WRITE → 1. Room DAO (immediate)
                2. SyncQueue enqueue
                3. Trigger WorkManager sync
```

---

## Supabase Client Setup

```kotlin
// SupabaseClient.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(GoTrue) {
            scheme = "smartdairy"
            host = "auth-callback"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }

    @Provides
    @Singleton
    fun provideGoTrue(client: SupabaseClient): GoTrue = client.auth

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): BucketApi = client.storage
}
```

---

## Remote Data Source (Generic Pattern)

```kotlin
// AnimalRemoteSource.kt
class AnimalRemoteSource @Inject constructor(
    private val postgrest: Postgrest
) {
    // Fetch all animals for a farm (used on fresh install or full re-sync)
    suspend fun fetchAnimals(farmId: String): List<AnimalRemoteDto> =
        postgrest.from("animals")
            .select {
                filter { eq("farm_id", farmId) }
                order("tag_id", ascending = true)
            }
            .decodeList<AnimalRemoteDto>()

    // Upsert a single animal (insert or update)
    suspend fun upsertAnimal(dto: AnimalRemoteDto) {
        postgrest.from("animals").upsert(dto) { onConflict = "id" }
    }

    // Soft-delete (update status, not actual delete)
    suspend fun updateAnimalStatus(id: String, status: String) {
        postgrest.from("animals")
            .update({ set("status", status) }) { filter { eq("id", id) } }
    }

    // For hard delete (transferred to another farm — remove old record)
    suspend fun deleteAnimal(id: String) {
        postgrest.from("animals").delete { filter { eq("id", id) } }
    }
}

// AnimalRemoteDto.kt — mirrors Supabase table schema exactly
@Serializable
data class AnimalRemoteDto(
    val id: String,
    @SerialName("farm_id") val farmId: String,
    @SerialName("barn_id") val barnId: String? = null,
    @SerialName("tag_id") val tagId: String,
    val name: String? = null,
    val breed: String? = null,
    val gender: String,
    val status: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    // ... all fields
)
```

---

## Repository Implementation (Complete Example)

```kotlin
// MilkRepositoryImpl.kt
class MilkRepositoryImpl @Inject constructor(
    private val milkDao: MilkRecordDao,
    private val milkRemoteSource: MilkRemoteSource,
    private val syncQueueDao: SyncQueueDao,
    private val mapper: MilkMapper
) : MilkRepository {

    // ─── READS (always from Room — offline safe) ──────────────────────

    override fun getBulkEntryData(farmId: String, date: LocalDate, session: String):
        Flow<List<BulkMilkEntry>> =
        milkDao.getBulkEntryData(farmId, date, session)
            .map { list -> list.map(mapper::toBulkEntry) }

    override fun getDailyTotal(farmId: String, date: LocalDate): Flow<Double> =
        milkDao.getDailyTotal(farmId, date).map { it ?: 0.0 }

    override fun getDailyTotals(
        farmId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DailyTotal>> =
        milkDao.getDailyTotals(farmId, startDate, endDate)
            .map { list -> list.map { DailyTotal(it.recordDate, it.total) } }

    override fun getMilkHistoryForAnimal(animalId: String): Flow<List<MilkRecord>> =
        milkDao.getMilkHistoryForAnimal(animalId)
            .map { list -> list.map(mapper::toDomain) }

    // ─── WRITES (Room first → SyncQueue → WorkManager) ──────────────

    override suspend fun saveBulkMilkEntry(
        records: List<MilkRecord>,
        context: Context
    ): Result<Unit> = runCatching {
        val entities = records.map(mapper::toEntity)

        // Batch insert to Room
        milkDao.insertMilkRecords(entities)

        // Enqueue each for sync
        entities.forEach { entity ->
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    tableName = "milk_records",
                    operation = "insert",
                    recordId = entity.id,
                    payloadJson = Json.encodeToString(entity.toRemoteDto())
                )
            )
        }

        // Trigger immediate background sync
        SyncWorker.triggerImmediate(context)
    }

    override suspend fun editMilkRecord(record: MilkRecord, context: Context): Result<Unit> =
        runCatching {
            // Validate: can only edit within 7 days
            require(record.recordDate >= LocalDate.now().minusDays(7)) {
                "Cannot edit records older than 7 days"
            }
            val entity = mapper.toEntity(record)
            milkDao.insertMilkRecords(listOf(entity))
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    tableName = "milk_records",
                    operation = "update",
                    recordId = entity.id,
                    payloadJson = Json.encodeToString(entity.toRemoteDto())
                )
            )
            SyncWorker.triggerImmediate(context)
        }
}
```

---

## Entity ↔ Domain Model Mapping

```kotlin
// AnimalMapper.kt
class AnimalMapper {

    fun toDomain(entity: AnimalEntity): Animal = Animal(
        id = entity.id,
        farmId = entity.farmId,
        barnId = entity.barnId,
        tagId = entity.tagId,
        name = entity.name,
        breed = entity.breed,
        dob = entity.dob,
        gender = Gender.from(entity.gender),
        status = AnimalStatus.from(entity.status),
        photoUrl = entity.photoUrl,
        weightKg = entity.weightKg,
        createdAt = entity.createdAt
    )

    fun toEntity(domain: Animal): AnimalEntity = AnimalEntity(
        id = domain.id,
        farmId = domain.farmId,
        barnId = domain.barnId,
        tagId = domain.tagId,
        name = domain.name,
        breed = domain.breed,
        dob = domain.dob,
        gender = domain.gender.value,
        status = domain.status.value,
        photoUrl = domain.photoUrl,
        weightKg = domain.weightKg,
        isSynced = false,
        updatedAt = LocalDateTime.now()
    )

    fun toRemoteDto(entity: AnimalEntity): AnimalRemoteDto = AnimalRemoteDto(
        id = entity.id,
        farmId = entity.farmId,
        tagId = entity.tagId,
        name = entity.name,
        gender = entity.gender,
        status = entity.status,
        updatedAt = entity.updatedAt.toString()
        // ... all fields
    )
}
```

---

## Photo Upload (Supabase Storage)

```kotlin
// PhotoUploadRepository.kt
class PhotoUploadRepository @Inject constructor(
    private val storage: BucketApi,
    private val supabase: SupabaseClient
) {
    suspend fun uploadAnimalPhoto(
        farmId: String,
        animalId: String,
        imageBytes: ByteArray,
        extension: String = "jpg"
    ): Result<String> = runCatching {
        val path = "$farmId/$animalId/photo.$extension"

        storage["animal-photos"].upload(
            path = path,
            data = imageBytes,
            options = UploadOptions(upsert = true)
        )

        // Return a signed URL (1-hour expiry)
        storage["animal-photos"].createSignedUrl(path, expiresIn = 3600)
    }

    suspend fun getSignedUrl(farmId: String, animalId: String): String =
        storage["animal-photos"].createSignedUrl(
            path = "$farmId/$animalId/photo.jpg",
            expiresIn = 3600
        )
}
```

---

## Full Sync (Initial Download)

Triggered on first login or manual "Restore from cloud" action:

```kotlin
class FullSyncUseCase @Inject constructor(
    private val animalRemoteSource: AnimalRemoteSource,
    private val milkRemoteSource: MilkRemoteSource,
    private val vaccinationRemoteSource: VaccinationRemoteSource,
    private val animalDao: AnimalDao,
    private val milkDao: MilkRecordDao,
    private val vaccinationDao: VaccinationDao,
    // ...
) {
    suspend operator fun invoke(farmId: String): Flow<FullSyncProgress> = flow {
        emit(FullSyncProgress(step = "animals", progress = 0f))
        val animals = animalRemoteSource.fetchAnimals(farmId)
        animalDao.insertAnimals(animals.map { it.toEntity() })
        emit(FullSyncProgress(step = "animals", progress = 1f))

        emit(FullSyncProgress(step = "milk", progress = 0f))
        val milkRecords = milkRemoteSource.fetchRecentRecords(farmId, days = 90)
        milkDao.insertMilkRecords(milkRecords.map { it.toEntity() })
        emit(FullSyncProgress(step = "milk", progress = 1f))

        emit(FullSyncProgress(step = "vaccinations", progress = 0f))
        val vaccinations = vaccinationRemoteSource.fetchVaccinations(farmId)
        vaccinationDao.insertVaccinations(vaccinations.map { it.toEntity() })
        emit(FullSyncProgress(step = "vaccinations", progress = 1f))

        // ... continue for all tables
        emit(FullSyncProgress(step = "complete", progress = 1f))
    }
}

data class FullSyncProgress(val step: String, val progress: Float)
```

---

## Error Handling Strategy

```kotlin
// All repository methods return Result<T>
// ViewModels handle errors:

viewModelScope.launch {
    addAnimalUseCase(animal)
        .onSuccess { id ->
            _uiState.update { it.copy(isLoading = false) }
            _events.send(AnimalEvent.AnimalSaved(id))
        }
        .onFailure { e ->
            val message = when (e) {
                is SQLiteConstraintException -> "Tag ID already exists"
                is IOException -> "Network error. Saved locally."
                else -> "Something went wrong. Please try again."
            }
            _uiState.update { it.copy(isLoading = false, error = message) }
        }
}
```
