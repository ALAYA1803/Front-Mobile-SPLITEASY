package com.spliteasy.spliteasy.di

import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import com.spliteasy.spliteasy.data.repository.RepresentativeRepositoryImpl
import com.spliteasy.spliteasy.data.repository.HouseholdRepositoryImpl
import com.spliteasy.spliteasy.domain.repository.HouseholdRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Binds @Singleton
    abstract fun bindRepresentativeRepository(
        impl: RepresentativeRepositoryImpl
    ): RepresentativeRepository
    @Binds @Singleton
    abstract fun bindHouseholdRepository(impl: HouseholdRepositoryImpl): HouseholdRepository
}