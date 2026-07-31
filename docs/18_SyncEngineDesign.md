# Sync Engine Design
## Smart Dairy Farm Management System

---

## Overview

The sync engine enables the app to work completely offline and sync data to Supabase when connectivity is restored. It uses a **write-ahead queue** pattern: every mutation is written to Room first, queued for sync, and processed by WorkManager in the background.

---

## Architecture

```
User Action
    │
    ▼
Repository.save(entity)
    │
    ├── 1. Write to Room (instant, offline-safe)
    │         └── UI updates via Flow immediately
    │
    ├── 2. Enqueue to sync_queue table
    │         └── { table, operation, record_id, payload_json }
    │
    └── 3. Emit success to ViewModel

Background (WorkManager)
    │
    ├── SyncWorker (network required)
    │         │
    │         ├── Read sync_queue (batch of 50)
    │         │
    │         ├── For each item:
    │         │     ├── Call Supabase upsert/delete
    │         │     ├── Success → delete from sync_queue
    │         │     │             mark is_synced = true in local table
    │         │     └── Failure → increment retry_count
    │         │                   skip if retry_count >= 5
    │         │
    │         └── If queue empty → worker completes
    │
    └── Supabase Realtime
              │
              └── Remote changes pushed to device
                        └── Update Room → UI updates via Flow
```

---

## SyncEngine.kt

```kotlin
@Singleton
class SyncEngine @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val supabase: SupabaseClient,
    private val animalDao: AnimalDao,
    private val milkRecordDao: MilkRecordDao,
    private val vaccinationDao: VaccinationDao,
    // ... other DAOs
) {
    suspend fun processSyncQueue(): SyncResult {
        val pendingItems = syncQueueDao.getPendingItems()
        var successCount = 0
        var failureCount = 0

        for (item in pendingItems) {
            try {
                processSyncItem(item)
                syncQueueDao.dequeue(item.id)
                successCount++
            } catch (e: Exception) {
                syncQueueDao.incrementRetry(item.id)
                failureCount++
                if (item.retryCount >= 4) {
                    // Log to error tracking, notify user if critical
                }
            }
        }

        // Cleanup permanently failed items
        syncQueueDao.deleteFailedItems()

        return SyncResult(successCount, failureCount)
    }

    private suspend fun processSyncItem(item: SyncQueueEntity) {
        when (item.tableName) {
            "animals"        -> syncAnimal(item)
            "milk_records"   -> syncMilkRecord(item)
            "vaccinations"   -> syncVaccination(item)
            "feed_records"   -> syncFeedRecord(item)
            "health_checkups"-> syncHealthCheckup(item)
            "diseases"       -> syncDisease(item)
            "medicines"      -> syncMedicine(item)
            "heat_records"   -> syncHeatRecord(item)
            "breeding_records"-> syncBreedingRecord(item)
            "pregnancy_records"-> syncPregnancyRecord(item)
            "alerts"         -> syncAlert(item)
            "income_records" -> syncIncomeRecord(item)
            "expense_records"-> syncExpenseRecord(item)
            else -> throw IllegalArgumentException("Unknown table: ${item.tableName}")
        }
    }

    private suspend fun syncAnimal(item: SyncQueueEntity) {
        val entity = Json.decodeFromString<AnimalEntity>(item.payloadJson)
        when (item.operation) {
            "insert", "update" -> {
                supabase.from("animals").upsert(entity.toRemoteModel())
                animalDao.markSynced(entity.id)
            }
            "delete" -> supabase.from("animals").delete { filter { eq("id", item.recordId) } }
        }
    }

    // ... similar for each table
}

data class SyncResult(val successCount: Int, val failureCount: Int)
```

---

## SyncWorker.kt

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val result = syncEngine.processSyncQueue()
            if (result.failureCount == 0) Result.success()
            else Result.retry()  // Retry if some items failed
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "SmartDairySyncWork"
        const val PERIODIC_SYNC_WORK = "SmartDairyPeriodicSync"

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
```

---

## WorkManager Setup

```kotlin
// In Application.onCreate()
@HiltAndroidApp
class SmartDairyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupWorkManager()
        setupRealtimeSync()
    }

    private fun setupWorkManager() {
        // Periodic background sync every 15 minutes (when online)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            SyncWorker.buildPeriodicRequest()
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

// Trigger immediate sync after write (in Repository)
fun triggerImmediateSync(context: Context) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        SyncWorker.WORK_NAME,
        ExistingWorkPolicy.KEEP,  // Don't cancel if already running
        SyncWorker.buildOneTimeRequest()
    )
}
```

---

## Supabase Realtime Sync (Pull)

```kotlin
// Listens for remote changes and updates Room DB
class RealtimeSyncService @Inject constructor(
    private val supabase: SupabaseClient,
    private val animalDao: AnimalDao,
    private val alertDao: AlertDao,
    private val activeFarmPrefs: UserPreferencesDataStore
) {
    fun startListening(scope: CoroutineScope) {
        val farmId = activeFarmPrefs.getActiveFarmId() ?: return

        scope.launch {
            // Listen for new alerts (from Edge Function cron)
            supabase.realtime.createChannel("alerts:$farmId") {
                postgresChangeFlow<PostgresAction>(schema = "public") {
                    filter = "table=eq.alerts&farm_id=eq.$farmId"
                }
            }.collect { event ->
                when (event) {
                    is PostgresAction.Insert -> {
                        val alert = event.decodeRecord<AlertEntity>()
                        alertDao.insertAlerts(listOf(alert))
                    }
                    else -> {}
                }
            }
        }

        scope.launch {
            // Listen for remote animal updates (multi-device scenario)
            supabase.realtime.createChannel("animals:$farmId") {
                postgresChangeFlow<PostgresAction>(schema = "public") {
                    filter = "table=eq.animals&farm_id=eq.$farmId"
                }
            }.collect { event ->
                when (event) {
                    is PostgresAction.Update -> {
                        val animal = event.decodeRecord<AnimalEntity>()
                        animalDao.insertAnimal(animal.copy(isSynced = true))
                    }
                    else -> {}
                }
            }
        }
    }
}
```

---

## Conflict Resolution

```kotlin
object ConflictResolver {
    /**
     * Last-Write-Wins based on updated_at timestamp.
     * Local wins if newer than remote; remote wins otherwise.
     */
    fun resolveAnimal(local: AnimalEntity, remote: AnimalEntity): AnimalEntity {
        return if (local.updatedAt.isAfter(remote.updatedAt)) local
        else remote.copy(isSynced = true)
    }

    // For milk records — no conflict possible (unique constraint per animal/date/session)
    // For other records — same last-write-wins logic
}
```

---

## Sync Status UI

```kotlin
// Observable sync status
@Singleton
class SyncStatusRepository @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val workManager: WorkManager
) {
    val syncStatus: Flow<SyncStatus> = combine(
        syncQueueDao.getPendingCount(),
        workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME)
    ) { pendingCount, workInfos ->
        val isWorking = workInfos.any { it.state == WorkInfo.State.RUNNING }
        when {
            pendingCount == 0 -> SyncStatus.SYNCED
            isWorking -> SyncStatus.SYNCING
            else -> SyncStatus.PENDING
        }
    }
}

// SyncStatusBar.kt composable
@Composable
fun SyncStatusBar(status: SyncStatus) {
    AnimatedVisibility(visible = status != SyncStatus.SYNCED) {
        Surface(
            color = when (status) {
                SyncStatus.SYNCING -> MaterialTheme.colorScheme.primaryContainer
                SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                SyncStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                when (status) {
                    SyncStatus.SYNCING -> {
                        CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Syncing...", style = MaterialTheme.typography.labelSmall)
                    }
                    SyncStatus.PENDING -> {
                        Icon(Icons.Default.CloudOff, null, Modifier.size(12.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Changes saved locally. Sync pending.",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    SyncStatus.OFFLINE -> {
                        Icon(Icons.Default.WifiOff, null, Modifier.size(12.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Offline mode", style = MaterialTheme.typography.labelSmall)
                    }
                    else -> {}
                }
            }
        }
    }
}
```

---

## Sync Queue Table Schema (Room)

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "table_name") val tableName: String,
    val operation: String,          // "insert" | "update" | "delete"
    @ColumnInfo(name = "record_id") val recordId: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)

// Max retry = 5 → item abandoned (manual intervention required)
// Items processed in FIFO order (ORDER BY created_at ASC)
// Batch size = 50 items per sync cycle
```
