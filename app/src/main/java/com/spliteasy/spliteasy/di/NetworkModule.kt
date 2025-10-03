package com.spliteasy.spliteasy.di

import com.spliteasy.spliteasy.data.remote.AuthInterceptor
import com.spliteasy.spliteasy.data.remote.api.AuthService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(log)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://back-spliteasy.onrender.com/api/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

    @Provides @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides @Singleton
    fun provideUsersService(retrofit: Retrofit): UsersService =
        retrofit.create(UsersService::class.java)
}
