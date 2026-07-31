package com.pashu360.app.feature.animal.data.repository

import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import javax.inject.Inject
import javax.inject.Singleton

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
            AnimalFilter.SOLD -> dao.observeByStatus(farmId, "sold")
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

    override suspend fun getAnimalById(id: String): Animal? =
        dao.getById(id)?.toDomain()

    override suspend fun getAnimalByTag(farmId: String, tagId: String): Animal? =
        dao.getByTag(farmId, tagId)?.toDomain()

    override suspend fun getAnimalByQr(farmId: String, qrData: String): Animal? =
        dao.getByQr(farmId, qrData)?.toDomain()

    override suspend fun addAnimal(animal: Animal): Result<Unit> = runCatching {
        dao.insert(AnimalEntity.fromDomain(animal))
    }

    override suspend fun updateAnimal(animal: Animal): Result<Unit> = runCatching {
        val updated = animal.copy(updatedAt = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()))
        dao.update(AnimalEntity.fromDomain(updated))
    }

    override suspend fun updateStatus(id: String, status: String): Result<Unit> = runCatching {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        dao.updateStatus(id, status, now)
    }

    override suspend fun deleteAnimal(id: String): Result<Unit> = runCatching {
        dao.delete(id)
    }
}
