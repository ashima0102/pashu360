package com.pashu360.app.feature.farm.data.repository

import com.pashu360.app.core.domain.model.Farm
import com.pashu360.app.feature.farm.data.local.FarmDao
import com.pashu360.app.feature.farm.data.local.FarmEntity
import com.pashu360.app.feature.farm.domain.repository.FarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepositoryImpl @Inject constructor(
    private val farmDao: FarmDao
) : FarmRepository {

    override fun observeFarm(farmId: String): Flow<Farm?> =
        farmDao.observeById(farmId).map { it?.toDomain() }

    override suspend fun getFarm(farmId: String): Farm? =
        withContext(Dispatchers.IO) { farmDao.getById(farmId)?.toDomain() }

    override suspend fun saveFarm(farm: Farm): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { farmDao.upsert(FarmEntity.fromDomain(farm)) }.map { }
        }

    override suspend fun updateExpectedHerdSize(farmId: String, size: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { farmDao.updateExpectedHerdSize(farmId, size) }.map { }
        }
}
