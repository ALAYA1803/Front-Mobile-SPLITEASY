package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.HouseholdMemberDto
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto
import retrofit2.http.*

interface HouseholdMembersService {
    // NUEVO: endpoint explícito por household
    @GET("households/{id}/members")
    suspend fun listByHousehold(@Path("id") householdId: Long): List<HouseholdMemberDto>

    // EXISTENTE: puede que el backend ignore este query y devuelva todo
    @GET("household-members")
    suspend fun list(@Query("householdId") householdId: Long? = null): List<HouseholdMemberDto>

    @POST("household-members")
    suspend fun create(@Body body: CreateHouseholdMemberRequest): HouseholdMemberDto

    @DELETE("household-members/{id}")
    suspend fun delete(@Path("id") id: Long)

    @GET("users")
    suspend fun searchUsersByEmail(@Query("email") email: String): List<RawUserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): RawUserDto
}

data class CreateHouseholdMemberRequest(
    val userId: Long,
    val householdId: Long
)
