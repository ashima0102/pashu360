package com.pashu360.app.feature.animal.data.repository

import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All DAO calls that were previously `suspend fun` are now synchronous
 * (a workaround for a KSP2 codegen bug). This repository wraps them
 * in `withContext(Dispatchers.IO)` so callers can still use `suspend fun`
 * and never accidentally hit the DB on the main thread.
 */
@OptIn(ExperimentalTime::class)
@Singleton
class AnimalRepositoryImpl @Inject constructor(
    private val dao: AnimalDao
) : AnimalRepository {

    override fun observeAnimals(farmId: String, filter: AnimalFilter): Flow<List<Animal>> {
        val flow = when (filter) {
            AnimalFilter.ALL -> dao.observeAll(farmId)
            AnimalFilter.ACTIVE -> dao.observeActiveAnimals(farmId)
            AnimalFilter.PREGNANT -> dao.observeByStatus(farmId, "pregnant")
            AnimalFilter.SICK -> dao.observeByStatus(farmId, "sick")
            AnimalFilter.DRY -> dao.observeByStatus(farmId, "dry")
            AnimalFilter.INACTIVE -> dao.observeInactive(farmId)
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun observeAnimalById(id: String): Flow<Animal?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun searchAnimals(farmId: String, query: String): Flow<List<Animal>> =
        dao.search(farmId, query).map { list -> list.map { it.toDomain() } }

    override fun countActive(farmId: String): Flow<Int> = dao.countActive(farmId)
    override fun countSick(farmId: String): Flow<Int> = dao.countSick(farmId)
    override fun countPregnant(farmId: String): Flow<Int> = dao.countPregnant(farmId)

    override suspend fun getAnimalById(id: String): Animal? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun getAnimalByTag(farmId: String, tagId: String): Animal? =
        withContext(Dispatchers.IO) {
            dao.getByTag(farmId, tagId)?.toDomain()
        }

    override suspend fun getAnimalByQr(farmId: String, qrData: String): Animal? =
        withContext(Dispatchers.IO) {
            dao.getByQr(farmId, qrData)?.toDomain()
        }

    override suspend fun addAnimal(animal: Animal): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { dao.insert(AnimalEntity.fromDomain(animal)) }.map { }
    }

    override suspend fun updateAnimal(animal: Animal): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val updated = animal.copy(updatedAt = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()))
            dao.update(AnimalEntity.fromDomain(updated))
        }.map { }
    }

    override suspend fun updateStatus(id: String, status: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val now = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                dao.updateStatus(id, status, now)
            }.map { }
        }

    override suspend fun deleteAnimal(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { dao.deleteById(id) }.map { }
    }
}
