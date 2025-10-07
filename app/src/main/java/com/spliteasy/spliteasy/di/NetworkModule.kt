package com.spliteasy.spliteasy.di

import com.spliteasy.spliteasy.BuildConfig
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.api.AccountService
import com.spliteasy.spliteasy.data.remote.api.AuthService
import com.spliteasy.spliteasy.data.remote.api.BillsService
import com.spliteasy.spliteasy.data.remote.api.ContributionsService
import com.spliteasy.spliteasy.data.remote.api.ExpensesService
import com.spliteasy.spliteasy.data.remote.api.GroupsService
import com.spliteasy.spliteasy.data.remote.api.HouseholdMembersService
import com.spliteasy.spliteasy.data.remote.api.HouseholdsService
import com.spliteasy.spliteasy.data.remote.api.MemberContributionsService
import com.spliteasy.spliteasy.data.remote.api.PaymentReceiptsService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.api.WebMemberApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
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
    fun provideOkHttp(tokenStore: TokenDataStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val original = chain.request()
                val path = original.url.encodedPath
                val needsAuth = !path.contains("/authentication/", ignoreCase = true) &&
                        !path.contains("/auth/", ignoreCase = true)

                val token = runBlocking { tokenStore.readToken() }
                val req = if (needsAuth && !token.isNullOrBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(req)
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(moshi: Moshi, okHttp: OkHttpClient): Retrofit =
        Retrofit.Builder()

            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttp)
            .build()

    @Provides @Singleton
    fun provideAccountService(retrofit: Retrofit): AccountService =
        retrofit.create(AccountService::class.java)

    @Provides @Singleton
    fun provideWebMemberApi(retrofit: Retrofit): WebMemberApi =
        retrofit.create(WebMemberApi::class.java)

    @Provides @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides @Singleton
    fun provideUsersService(retrofit: Retrofit): UsersService =
        retrofit.create(UsersService::class.java)

    @Provides @Singleton
    fun provideGroupsService(retrofit: Retrofit): GroupsService =
        retrofit.create(GroupsService::class.java)

    @Provides @Singleton
    fun provideExpensesService(retrofit: Retrofit): ExpensesService =
        retrofit.create(ExpensesService::class.java)

    @Provides @Singleton
    fun provideBillsService(retrofit: Retrofit): BillsService =
        retrofit.create(BillsService::class.java)

    @Provides @Singleton
    fun provideContributionsService(retrofit: Retrofit): ContributionsService =
        retrofit.create(ContributionsService::class.java)

    @Provides @Singleton
    fun provideHouseholdsService(retrofit: Retrofit): HouseholdsService =
        retrofit.create(HouseholdsService::class.java)

    @Provides @Singleton
    fun provideHouseholdMembersService(retrofit: Retrofit): HouseholdMembersService =
        retrofit.create(HouseholdMembersService::class.java)
    @Provides @Singleton
    fun provideMemberContributionsService(retrofit: Retrofit): MemberContributionsService =
        retrofit.create(MemberContributionsService::class.java)

    @Provides @Singleton
    fun providePaymentReceiptsService(retrofit: Retrofit): PaymentReceiptsService =
        retrofit.create(PaymentReceiptsService::class.java)
}
