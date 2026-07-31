# Offline-First Architecture
## Smart Dairy Farm Management System

---

## Core Principle

> "The app must work identically whether the device is online or offline. Internet connectivity is a background concern, not a prerequisite."

---

## Why Offline-First for Dairy Farms

- Rural farms in India frequently have 2G, intermittent 4G, or no connectivity
- Milk logging happens twice daily in the field — farmer cannot wait for network
- Vaccination alerts must still be visible even when offline
- Data entered offline must never be lost, even if app is killed

---

## Architecture Decision: Room as Primary Data Source

```
❌ Wrong approach (network-first):
    User taps Save
    → Call Supabase API
    → If success, save to Room
    → If failure, show error

✅ Correct approach (Room-first):
    User taps Save
    → Write to Room (guaranteed to work offline)
    → Emit success to UI immediately
    → Queue sync to Supabase in background
    → Supabase syncs when connectivity available
```

---

## Layer Responsibilities for Offline Support

```
Presentation (Compose)
  ↕ only reads/writes UiState
ViewModel
  ↕ calls UseCases
UseCase (domain)
  ↕ calls Repository
Repository (implements offline-first contract)
  ├── READ always from Room → Flow (reactive, always fresh)
  └── WRITE → Room first → SyncQueue → WorkManager

Room DB
  ├── All entities have `is_synced: Boolean = false`
  ├── SyncQueue table tracks pending Supabase writes
  └── All entities have `updated_at` for conflict resolution

WorkManager
  └── SyncWorker runs when NetworkType.CONNECTED
      Processes SyncQueue → Supabase upsert/delete
```

---

## Offline Capability by Feature

| Feature | Offline Write | Offline Read | Notes |
|---|---|---|---|
| Login | ❌ | ✅ (cached session) | Requires internet for OTP |
| Dashboard | ✅ | ✅ | Stats from Room |
| Animal List | ✅ | ✅ | From Room |
| Add Animal | ✅ | - | Saved to Room; synced later |
| Milk Entry | ✅ | ✅ | Critical — must always work |
| View History | - | ✅ | From Room cache |
| Add Vaccination | ✅ | ✅ | |
| Vaccination Calendar | - | ✅ | |
| Log Health | ✅ | ✅ | |
| Log Feed | ✅ | ✅ | |
| Alerts/Notifications | - | ✅ | Cached alerts visible offline |
| Photo Upload | ❌ | ✅ | Queued; uploads when online |
| PDF Export | ✅ | ✅ | Generated from local Room data |
| Sync Status | - | ✅ | Shows pending count |

---

## Read Strategy

```kotlin
// All reads come from Room — never directly from Supabase in runtime
// Room is always up-to-date via:
//   1. Local writes (immediate)
//   2. Supabase Realtime (updates Room when connected)
//   3. SyncWorker (pulls remote changes during sync)

// Example: getAnimals always reads Room
override fun getAnimals(farmId: String): Flow<List<Animal>> =
    animalDao.getActiveAnimals(farmId)    // Room DAO
        .map { entities -> entities.map(mapper::toDomain) }
// Room's Flow automatically emits when data changes
// UI always shows latest local state
```

---

## Write Strategy

```kotlin
// Write-ahead to Room; queue for Supabase sync
suspend fun addAnimal(animal: Animal, context: Context): Result<Unit> = runCatching {
    // 1. Write to Room immediately (works offline)
    val entity = mapper.toEntity(animal.copy(isSynced = false))
    animalDao.insertAnimal(entity)

    // 2. Enqueue for Supabase sync
    syncQueueDao.enqueue(
        SyncQueueEntity(
            tableName = "animals",
            operation = "insert",
            recordId = entity.id,
            payloadJson = Json.encodeToString(entity.toRemoteDto())
        )
    )

    // 3. Trigger WorkManager sync (no-op if offline)
    SyncWorker.triggerImmediate(context)
}
// UI gets success response from Room — never waits for Supabase
```

---

## SyncQueue Processing

```kotlin
// WorkManager processes queue when online
class SyncWorker(context: Context, params: WorkerParameters,
    private val syncEngine: SyncEngine) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val items = syncQueueDao.getPendingItems()  // 50 at a time
        var allSucceeded = true

        for (item in items) {
            try {
                when (item.operation) {
                    "insert", "update" -> {
                        supabase.from(item.tableName).upsert(
                            Json.parseToJsonElement(item.payloadJson)
                        )
                        syncQueueDao.dequeue(item.id)
                        // Mark entity as synced in its table
                        markEntitySynced(item.tableName, item.recordId)
                    }
                    "delete" -> {
                        supabase.from(item.tableName).delete {
                            filter { eq("id", item.recordId) }
                        }
                        syncQueueDao.dequeue(item.id)
                    }
                }
            } catch (e: Exception) {
                syncQueueDao.incrementRetry(item.id)
                allSucceeded = false
                // Exponential backoff handled by WorkManager
            }
        }

        // Delete permanently failed items (retry_count >= 5)
        syncQueueDao.deleteFailedItems()

        return if (allSucceeded) Result.success() else Result.retry()
    }
}
```

---

## Conflict Resolution

```kotlin
// When the same record is modified on two devices offline:
// Device A edits Gouri's weight at 2 PM
// Device B edits Gouri's weight at 3 PM
// Both sync when online

// Resolution: Last-Write-Wins based on updated_at timestamp
object ConflictResolver {
    fun <T : HasUpdatedAt> resolve(local: T, remote: T): T =
        if (local.updatedAt.isAfter(remote.updatedAt)) local else remote
}

// In SyncWorker, when upsert gets a conflict:
// Use `ON CONFLICT (id) DO UPDATE SET ... WHERE excluded.updated_at > {table}.updated_at`
// This is handled at PostgreSQL level via upsert semantics
```

---

## Photo Upload Queue

```kotlin
// Photos cannot be uploaded offline (binary data)
// Strategy: save photo locally; upload when online

data class PendingPhotoUpload(
    val localUri: Uri,
    val farmId: String,
    val animalId: String,
    val type: String  // "animal_photo", "health_photo"
)

// PhotoSyncWorker runs separately from data SyncWorker
class PhotoSyncWorker(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val pending = photoUploadQueueDao.getPending()
        pending.forEach { upload ->
            val bytes = context.contentResolver
                .openInputStream(upload.localUri)?.readBytes() ?: return@forEach
            photoUploadRepo.uploadAnimalPhoto(upload.farmId, upload.animalId, bytes)
            photoUploadQueueDao.markUploaded(upload.id)
        }
        return Result.success()
    }
}
```

---

## Realtime Pull (Online Updates)

```kotlin
// When app is online, Supabase Realtime pushes changes to Room
// This handles multi-device scenarios

class RealtimeSyncService @Inject constructor(
    private val supabase: SupabaseClient,
    private val animalDao: AnimalDao,
    private val alertDao: AlertDao
) {
    fun startListening(farmId: String, scope: CoroutineScope) {
        scope.launch {
            supabase.realtime.createChannel("farm:$farmId")
                .postgresChangeFlow<PostgresAction.Update>("public") {
                    table = "animals"
                    filter = PostgresChangeFilter.eq("farm_id", farmId)
                }
                .collect { event ->
                    val remoteAnimal = event.decodeRecord<AnimalEntity>()
                    val localAnimal = animalDao.getAnimalById(remoteAnimal.id)

                    // Only update if remote is newer
                    if (localAnimal == null || remoteAnimal.updatedAt.isAfter(localAnimal.updatedAt)) {
                        animalDao.insertAnimal(remoteAnimal.copy(isSynced = true))
                    }
                }
        }

        // Also listen for new alerts (generated by Edge Function)
        scope.launch {
            supabase.realtime.createChannel("alerts:$farmId")
                .postgresChangeFlow<PostgresAction.Insert>("public") {
                    table = "alerts"
                    filter = PostgresChangeFilter.eq("farm_id", farmId)
                }
                .collect { event ->
                    val alert = event.decodeRecord<AlertEntity>()
                    alertDao.insertAlerts(listOf(alert))
                }
        }
    }
}
```

---

## Offline Indicator

```kotlin
// Connectivity monitoring
@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Initial state
        val isConnected = connectivityManager.activeNetwork != null
        trySend(isConnected)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}

// When connectivity restores → trigger sync immediately
LaunchedEffect(Unit) {
    connectivityMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
            SyncWorker.triggerImmediate(context)
        }
    }
}
```

---

## Data Freshness Guarantee

| Scenario | Data State | UI Impact |
|---|---|---|
| Online, synced | Room = Supabase | Realtime — latest |
| Online, pending writes | Room ahead of Supabase | Shows correctly; sync in background |
| Offline, first use | Empty Room | Onboarding — add animals |
| Offline, returning | Cached Room data | All data visible and editable |
| Back online | WorkManager syncs queue | Background — no disruption |
| Fresh install | No Room data | Full sync download from Supabase |
