package com.spliteasy.spliteasy.di

import com.spliteasy.spliteasy.BuildConfig
import com.spliteasy.spliteasy.data.remote.api.AuthService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.api.GroupsService
import com.spliteasy.spliteasy.data.remote.api.ExpensesService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(moshi: Moshi, okHttp: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // Asegúrate del import: com.spliteasy.spliteasy.BuildConfig
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttp)
            .build()

    @Provides @Singleton fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides @Singleton fun provideUsersService(retrofit: Retrofit): UsersService =
        retrofit.create(UsersService::class.java)

    @Provides @Singleton fun provideGroupsService(retrofit: Retrofit): GroupsService =
        retrofit.create(GroupsService::class.java)

    @Provides @Singleton fun provideExpensesService(retrofit: Retrofit): ExpensesService =
        retrofit.create(ExpensesService::class.java)
}
