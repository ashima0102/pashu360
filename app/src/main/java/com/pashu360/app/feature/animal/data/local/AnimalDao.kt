package com.pashu360.app.feature.animal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND status NOT IN ('sold', 'deceased')
        ORDER BY tag_id DESC
    """)
    fun observeActiveAnimals(farmId: String): Flow<List<AnimalEntity>>

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND status = :status
        ORDER BY tag_id DESC
    """)
    fun observeByStatus(farmId: String, status: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE farm_id = :farmId ORDER BY tag_id DESC")
    fun observeAll(farmId: String): Flow<List<AnimalEntity>>

    /** Alias for the "Inactive" filter chip — deceased + sold animals. */
    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND status IN ('sold', 'deceased')
        ORDER BY tag_id DESC
    """)
    fun observeInactive(farmId: String): Flow<List<AnimalEntity>>

    // Non-suspend @Query methods work around a KSP2 bug where suspend @Query
    // methods aren't emitted into the generated DAO_Impl. Callers dispatch
    // to Dispatchers.IO in the repository.
    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    fun getById(id: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AnimalEntity?>

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND tag_id = :tagId LIMIT 1")
    fun getByTag(farmId: String, tagId: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND qr_code_data = :qrData LIMIT 1")
    fun getByQr(farmId: String, qrData: String): AnimalEntity?

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND (name LIKE '%' || :query || '%' OR tag_id LIKE '%' || :query || '%')
        ORDER BY tag_id
    """)
    fun search(farmId: String, query: String): Flow<List<AnimalEntity>>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'active'")
    fun countActive(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'sick'")
    fun countSick(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'pregnant'")
    fun countPregnant(farmId: String): Flow<Int>

    // All mutations are non-suspend (see note above). Explicit Long/Int
    // returns instead of Unit are also required — KSP2 additionally has
    // "unexpected jvm signature V" on suspend fun returning Unit
    // (see https://github.com/google/ksp/issues/2957).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(animal: AnimalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(animals: List<AnimalEntity>): List<Long>

    @Update
    fun update(animal: AnimalEntity): Int

    @Query("UPDATE animals SET status = :status, updated_at = :updatedAt WHERE id = :id")
    fun updateStatus(id: String, status: String, updatedAt: String): Int

    @Query("DELETE FROM animals WHERE id = :id")
    fun deleteById(id: String): Int
}
