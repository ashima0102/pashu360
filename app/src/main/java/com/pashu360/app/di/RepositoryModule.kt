package com.pashu360.app.di

import com.pashu360.app.feature.animal.data.repository.AnimalRepositoryImpl
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
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
}
