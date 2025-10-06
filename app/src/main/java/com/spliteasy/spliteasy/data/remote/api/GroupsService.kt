package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupsService {
    @GET("households")
    suspend fun listHouseholds(): List<HouseholdDto>

    @GET("households/{id}")
    suspend fun getHousehold(@Path("id") id: Long): HouseholdDto

    @GET("households/{id}/members")
    suspend fun listHouseholdMembers(@Path("id") id: Long): List<Any>
}
