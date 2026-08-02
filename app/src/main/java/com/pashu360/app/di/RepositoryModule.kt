package com.pashu360.app.di

import com.pashu360.app.feature.animal.data.repository.AnimalRepositoryImpl
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.feeding.data.repository.FeedingRepositoryImpl
import com.pashu360.app.feature.feeding.domain.repository.FeedingRepository
import com.pashu360.app.feature.finance.data.repository.FinanceRepositoryImpl
import com.pashu360.app.feature.finance.domain.repository.FinanceRepository
import com.pashu360.app.feature.health.data.repository.HealthRepositoryImpl
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import com.pashu360.app.feature.milk.data.repository.MilkRepositoryImpl
import com.pashu360.app.feature.milk.domain.repository.MilkRepository
import com.pashu360.app.feature.notifications.data.repository.AlertRepositoryImpl
import com.pashu360.app.feature.notifications.domain.repository.AlertRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnimalRepository(impl: AnimalRepositoryImpl): AnimalRepository

    @Binds
    @Singleton
    abstract fun bindMilkRepository(impl: MilkRepositoryImpl): MilkRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindFinanceRepository(impl: FinanceRepositoryImpl): FinanceRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    @Singleton
    abstract fun bindFeedingRepository(impl: FeedingRepositoryImpl): FeedingRepository
}
