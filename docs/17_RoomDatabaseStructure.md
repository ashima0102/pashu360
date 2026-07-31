# Room Database Structure
## Smart Dairy Farm Management System

---

## AppDatabase.kt

```kotlin
@Database(
    entities = [
        FarmEntity::class,
        BarnEntity::class,
        AnimalEntity::class,
        MilkRecordEntity::class,
        LactationRecordEntity::class,
        FeedTypeEntity::class,
        FeedRecordEntity::class,
        FeedInventoryEntity::class,
        HealthCheckupEntity::class,
        WeightRecordEntity::class,
        DiseaseEntity::class,
        MedicineEntity::class,
        VetVisitEntity::class,
        VaccineCatalogueEntity::class,
        VaccinationEntity::class,
        HeatRecordEntity::class,
        BreedingRecordEntity::class,
        PregnancyRecordEntity::class,
        IncomeRecordEntity::class,
        ExpenseRecordEntity::class,
        AlertEntity::class,
        SyncQueueEntity::class,
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun barnDao(): BarnDao
    abstract fun animalDao(): AnimalDao
    abstract fun milkRecordDao(): MilkRecordDao
    abstract fun lactationRecordDao(): LactationRecordDao
    abstract fun feedTypeDao(): FeedTypeDao
    abstract fun feedRecordDao(): FeedRecordDao
    abstract fun feedInventoryDao(): FeedInventoryDao
    abstract fun healthCheckupDao(): HealthCheckupDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun diseaseDao(): DiseaseDao
    abstract fun medicineDao(): MedicineDao
    abstract fun vetVisitDao(): VetVisitDao
    abstract fun vaccineCatalogueDao(): VaccineCatalogueDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun heatRecordDao(): HeatRecordDao
    abstract fun breedingRecordDao(): BreedingRecordDao
    abstract fun pregnancyRecordDao(): PregnancyRecordDao
    abstract fun incomeRecordDao(): IncomeRecordDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
    abstract fun alertDao(): AlertDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        const val DATABASE_NAME = "smart_dairy.db"
    }
}
```

---

## Type Converters

```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(dt: LocalDateTime?): String? = dt?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }
}
```

---

## Key Entity Classes

### AnimalEntity.kt
```kotlin
@Entity(
    tableName = "animals",
    indices = [
        Index("farm_id"),
        Index("barn_id"),
        Index(value = ["farm_id", "status"]),
        Index(value = ["farm_id", "tag_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(entity = FarmEntity::class, parentColumns = ["id"],
            childColumns = ["farm_id"], onDelete = CASCADE),
        ForeignKey(entity = BarnEntity::class, parentColumns = ["id"],
            childColumns = ["barn_id"], onDelete = SET_NULL)
    ]
)
data class AnimalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "barn_id") val barnId: String? = null,
    @ColumnInfo(name = "tag_id") val tagId: String,
    @ColumnInfo(name = "rfid_tag") val rfidTag: String? = null,
    @ColumnInfo(name = "qr_code_data") val qrCodeData: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val species: String = "cow",
    val dob: LocalDate? = null,
    val gender: String,
    @ColumnInfo(name = "color_marks") val colorMarks: String? = null,
    @ColumnInfo(name = "weight_kg") val weightKg: Double? = null,
    @ColumnInfo(name = "purchase_date") val purchaseDate: LocalDate? = null,
    @ColumnInfo(name = "purchase_price") val purchasePrice: Double? = null,
    val source: String? = null,
    @ColumnInfo(name = "sire_id") val sireId: String? = null,
    @ColumnInfo(name = "dam_id") val damId: String? = null,
    val status: String = "active",
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### MilkRecordEntity.kt
```kotlin
@Entity(
    tableName = "milk_records",
    indices = [Index("animal_id"), Index("farm_id"), Index("record_date")],
    foreignKeys = [
        ForeignKey(entity = AnimalEntity::class, parentColumns = ["id"],
            childColumns = ["animal_id"], onDelete = CASCADE)
    ]
)
data class MilkRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "record_date") val recordDate: LocalDate,
    val session: String,                    // "morning" | "evening"
    @ColumnInfo(name = "quantity_liters") val quantityLiters: Double,
    @ColumnInfo(name = "fat_pct") val fatPct: Double? = null,
    @ColumnInfo(name = "snf_pct") val snfPct: Double? = null,
    val clr: Double? = null,
    val ph: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### VaccinationEntity.kt
```kotlin
@Entity(
    tableName = "vaccinations",
    indices = [Index("animal_id"), Index("next_due_date"), Index("farm_id")],
    foreignKeys = [
        ForeignKey(entity = AnimalEntity::class, parentColumns = ["id"],
            childColumns = ["animal_id"], onDelete = CASCADE)
    ]
)
data class VaccinationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "animal_id") val animalId: String,
    @ColumnInfo(name = "vaccine_id") val vaccineId: String,
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "administered_date") val administeredDate: LocalDate,
    @ColumnInfo(name = "next_due_date") val nextDueDate: LocalDate? = null,
    @ColumnInfo(name = "batch_number") val batchNumber: String? = null,
    @ColumnInfo(name = "administered_by") val administeredBy: String? = null,
    val cost: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### SyncQueueEntity.kt
```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "table_name") val tableName: String,
    val operation: String,                  // "insert" | "update" | "delete"
    @ColumnInfo(name = "record_id") val recordId: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### AlertEntity.kt
```kotlin
@Entity(
    tableName = "alerts",
    indices = [Index("farm_id"), Index("is_resolved"), Index("due_date")]
)
data class AlertEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "farm_id") val farmId: String,
    @ColumnInfo(name = "animal_id") val animalId: String? = null,
    @ColumnInfo(name = "alert_type") val alertType: String,
    val title: String,
    val message: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate? = null,
    @ColumnInfo(name = "is_resolved") val isResolved: Boolean = false,
    @ColumnInfo(name = "resolved_at") val resolvedAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)
```

---

## Key DAO Classes

### AnimalDao.kt
```kotlin
@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND status != 'sold' AND status != 'deceased' ORDER BY tag_id ASC")
    fun getActiveAnimals(farmId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND status = :status ORDER BY tag_id")
    fun getAnimalsByStatus(farmId: String, status: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND tag_id = :tagId LIMIT 1")
    suspend fun getAnimalByTag(farmId: String, tagId: String): AnimalEntity?

    @Query("""
        SELECT * FROM animals 
        WHERE farm_id = :farmId 
        AND (name LIKE '%' || :query || '%' OR tag_id LIKE '%' || :query || '%')
        ORDER BY tag_id
    """)
    fun searchAnimals(farmId: String, query: String): Flow<List<AnimalEntity>>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'active'")
    fun getActiveAnimalCount(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'sick'")
    fun getSickAnimalCount(farmId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimals(animals: List<AnimalEntity>)

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Query("UPDATE animals SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: LocalDateTime = LocalDateTime.now())

    @Query("SELECT * FROM animals WHERE is_synced = 0")
    suspend fun getUnsyncedAnimals(): List<AnimalEntity>

    @Query("UPDATE animals SET is_synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
```

### MilkRecordDao.kt
```kotlin
@Dao
interface MilkRecordDao {
    @Query("""
        SELECT a.id, a.name, a.tag_id, a.photo_url,
               m.id as milk_id, m.quantity_liters, m.session
        FROM animals a
        LEFT JOIN milk_records m ON m.animal_id = a.id 
            AND m.record_date = :date AND m.session = :session
        WHERE a.farm_id = :farmId AND a.status = 'active'
        ORDER BY a.tag_id
    """)
    fun getBulkEntryData(farmId: String, date: LocalDate, session: String): Flow<List<BulkMilkEntryItem>>

    @Query("SELECT SUM(quantity_liters) FROM milk_records WHERE farm_id = :farmId AND record_date = :date")
    fun getDailyTotal(farmId: String, date: LocalDate): Flow<Double?>

    @Query("""
        SELECT record_date, SUM(quantity_liters) as total
        FROM milk_records 
        WHERE farm_id = :farmId AND record_date BETWEEN :startDate AND :endDate
        GROUP BY record_date
        ORDER BY record_date
    """)
    fun getDailyTotals(farmId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyMilkTotal>>

    @Query("SELECT * FROM milk_records WHERE animal_id = :animalId ORDER BY record_date DESC, session")
    fun getMilkHistoryForAnimal(animalId: String): Flow<List<MilkRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkRecords(records: List<MilkRecordEntity>)

    @Query("SELECT * FROM milk_records WHERE is_synced = 0")
    suspend fun getUnsyncedRecords(): List<MilkRecordEntity>

    @Query("UPDATE milk_records SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}

data class DailyMilkTotal(val recordDate: LocalDate, val total: Double)
```

### VaccinationDao.kt
```kotlin
@Dao
interface VaccinationDao {
    @Query("""
        SELECT v.*, vc.name as vaccine_name, vc.disease_target, a.name as animal_name, a.tag_id
        FROM vaccinations v
        JOIN vaccine_catalogue vc ON v.vaccine_id = vc.id
        JOIN animals a ON v.animal_id = a.id
        WHERE v.farm_id = :farmId
        AND v.next_due_date BETWEEN :startDate AND :endDate
        ORDER BY v.next_due_date ASC
    """)
    fun getUpcomingVaccinations(farmId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<VaccinationWithDetails>>

    @Query("""
        SELECT * FROM vaccinations 
        WHERE animal_id = :animalId 
        ORDER BY administered_date DESC
    """)
    fun getVaccinationHistoryForAnimal(animalId: String): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinations(vaccinations: List<VaccinationEntity>)

    @Query("SELECT * FROM vaccinations WHERE is_synced = 0")
    suspend fun getUnsyncedVaccinations(): List<VaccinationEntity>
}
```

### AlertDao.kt
```kotlin
@Dao
interface AlertDao {
    @Query("""
        SELECT * FROM alerts 
        WHERE farm_id = :farmId AND is_resolved = 0 
        ORDER BY due_date ASC
    """)
    fun getUnresolvedAlerts(farmId: String): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE farm_id = :farmId AND is_resolved = 0")
    fun getUnresolvedCount(farmId: String): Flow<Int>

    @Query("UPDATE alerts SET is_resolved = 1, resolved_at = :resolvedAt WHERE id = :id")
    suspend fun resolveAlert(id: String, resolvedAt: LocalDateTime = LocalDateTime.now())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Query("DELETE FROM alerts WHERE due_date < :cutoffDate AND is_resolved = 1")
    suspend fun deleteOldResolvedAlerts(cutoffDate: LocalDate)
}
```

### SyncQueueDao.kt
```kotlin
@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC LIMIT 50")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun dequeue(id: String)

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Query("DELETE FROM sync_queue WHERE retry_count >= 5")
    suspend fun deleteFailedItems()

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingCount(): Flow<Int>
}
```
