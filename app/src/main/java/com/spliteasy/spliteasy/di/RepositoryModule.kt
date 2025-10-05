package com.spliteasy.spliteasy.di

import com.spliteasy.spliteasy.data.repository.AuthRepositoryImpl
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import com.spliteasy.spliteasy.data.repository.MemberRepositoryImpl
import com.spliteasy.spliteasy.domain.repository.MemberRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: MemberRepositoryImpl): MemberRepository
}
